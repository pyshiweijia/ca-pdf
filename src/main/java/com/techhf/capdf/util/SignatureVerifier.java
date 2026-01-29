package com.techhf.capdf.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * PDF签名验证工具
 * 用于验证和显示PDF签名中的时间戳信息
 */
public class SignatureVerifier {

    /**
     * 验证并显示PDF签名信息
     */
    public static void verifySignature(File pdfFile) throws Exception {
        System.out.println("\n========== PDF签名验证 ==========");
        System.out.println("文件: " + pdfFile.getName());
        System.out.println();
        
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            List<PDSignature> signatures = doc.getSignatureDictionaries();
            
            if (signatures.isEmpty()) {
                System.out.println("❌ 该PDF没有签名");
                return;
            }
            
            System.out.println("✓ 找到 " + signatures.size() + " 个签名");
            System.out.println();
            
            for (int i = 0; i < signatures.size(); i++) {
                PDSignature signature = signatures.get(i);
                System.out.println("【签名 #" + (i + 1) + "】");
                System.out.println("  签名人: " + signature.getName());
                System.out.println("  原因: " + signature.getReason());
                System.out.println("  位置: " + signature.getLocation());
                System.out.println("  签名时间: " + signature.getSignDate().getTime());
                System.out.println("  子过滤器: " + signature.getSubFilter());
                
                // 获取签名内容
                byte[] signatureContent = signature.getContents();
                
                if (signatureContent != null) {
                    System.out.println("  签名数据大小: " + signatureContent.length + " bytes");
                    
                    // 解析CMS签名数据
                    CMSSignedData cmsSignedData = new CMSSignedData(signatureContent);
                    
                    // 获取签名者信息
                    SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
                    Collection<SignerInformation> signers = signerInfos.getSigners();
                    
                    System.out.println("  签名者数量: " + signers.size());
                    
                    for (SignerInformation signerInfo : signers) {
                        System.out.println("\n  --- 签名者详情 ---");
                        System.out.println("  签名算法: " + signerInfo.getDigestAlgOID());
                        System.out.println("  签名值大小: " + signerInfo.getSignature().length + " bytes");
                        
                        // 检查 unsigned attributes（时间戳应该在这里）
                        AttributeTable unsignedAttrs = signerInfo.getUnsignedAttributes();
                        
                        if (unsignedAttrs == null) {
                            System.out.println("  ❌ 没有找到 unsigned attributes");
                        } else {
                            System.out.println("  ✓ 找到 unsigned attributes");
                            System.out.println("  Unsigned attributes 数量: " + unsignedAttrs.size());
                            
                            // 查找时间戳属性
                            ASN1ObjectIdentifier tsAttrType = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken;
                            Attribute tsAttr = unsignedAttrs.get(tsAttrType);
                            
                            if (tsAttr == null) {
                                System.out.println("  ❌ 未找到时间戳属性 (OID: " + tsAttrType + ")");
                                
                                // 列出所有 unsigned attributes
                                System.out.println("\n  所有 Unsigned Attributes:");
                                for (Object oid : unsignedAttrs.toHashtable().keySet()) {
                                    System.out.println("    - " + oid);
                                }
                            } else {
                                System.out.println("  ✓✓✓ 找到时间戳属性！");
                                System.out.println("  时间戳属性值数量: " + tsAttr.getAttrValues().size());
                                
                                try {
                                    // 解析时间戳令牌
                                    // RFC 3161: 时间戳作为 ContentInfo 结构存储在 Attribute 的 SET 中
                                    org.bouncycastle.asn1.ASN1Set tsValueSet = tsAttr.getAttrValues();
                                    
                                    // 从 SET 中提取第一个元素
                                    org.bouncycastle.asn1.ASN1Encodable tsValue = tsValueSet.getObjectAt(0);
                                    
                                    // 编码并重新解析为 ContentInfo（避免类型转换问题）
                                    byte[] tsBytes = tsValue.toASN1Primitive().getEncoded();
                                    
                                    // 直接从字节创建 ContentInfo
                                    org.bouncycastle.asn1.cms.ContentInfo tsContentInfo = 
                                        org.bouncycastle.asn1.cms.ContentInfo.getInstance(
                                            org.bouncycastle.asn1.ASN1Primitive.fromByteArray(tsBytes)
                                        );
                                    
                                    // 从 ContentInfo 创建 CMSSignedData，再创建 TimeStampToken
                                    CMSSignedData tsSignedData = new CMSSignedData(tsContentInfo);
                                    TimeStampToken tsToken = new TimeStampToken(tsSignedData);
                                    
                                    System.out.println("\n  ✓✓✓ 时间戳解析成功！");
                                    System.out.println("  ═══════════════════════════════════════");
                                    System.out.println("  时间戳时间: " + tsToken.getTimeStampInfo().getGenTime());
                                    System.out.println("  时间戳序列号: " + tsToken.getTimeStampInfo().getSerialNumber());
                                    if (tsToken.getTimeStampInfo().getTsa() != null) {
                                        System.out.println("  TSA名称: " + tsToken.getTimeStampInfo().getTsa());
                                    }
                                    
                                    // 获取TSA证书
                                    Store certStore = tsToken.getCertificates();
                                    Collection<X509CertificateHolder> certs = certStore.getMatches(null);
                                    System.out.println("  TSA证书数量: " + certs.size());
                                    
                                    for (X509CertificateHolder cert : certs) {
                                        System.out.println("  TSA证书主题: " + cert.getSubject());
                                    }
                                    System.out.println("  ═══════════════════════════════════════");
                                } catch (Exception e) {
                                    System.out.println("  ❌ 时间戳解析失败: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }
                        
                        // 检查 signed attributes
                        AttributeTable signedAttrs = signerInfo.getSignedAttributes();
                        if (signedAttrs != null) {
                            System.out.println("\n  Signed attributes 数量: " + signedAttrs.size());
                        }
                    }
                } else {
                    System.out.println("  ❌ 无法读取签名内容");
                }
                
                System.out.println();
            }
        }
        
        System.out.println("========== 验证完成 ==========\n");
    }

    /**
     * 测试验证
     */
    public static void main(String[] args) {
        try {
            // 验证印章签名
            File sealPdf = new File("test-output/seal-sign/test-seal-signed.pdf");
            if (sealPdf.exists()) {
                verifySignature(sealPdf);
            }
            
            // 验证文本签名
            File textPdf = new File("test-output/text-sign/test-text-signed.pdf");
            if (textPdf.exists()) {
                verifySignature(textPdf);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
