package com.techhf.capdf.cert;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
 * 证书生成器
 * 用于生成自签名测试证书
 */
public class CertificateGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateGenerator.class);
    
    static {
        // 注册BouncyCastle Provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成自签名证书并保存为PKCS12格式
     * 
     * @param subject 证书主题（例如：CN=Test Organization,O=My Company,C=CN）
     * @param outputPath 输出路径（.p12文件）
     * @param password 密码（可以为null或空字符串表示无密码）
     * @param validYears 有效期（年）
     * @return 生成的证书
     */
    public static X509Certificate generateSelfSignedCert(
            String subject, 
            String outputPath, 
            String password,
            int validYears) throws Exception {
        
        logger.info("开始生成自签名证书...");
        logger.info("主题: {}", subject);
        logger.info("输出路径: {}", outputPath);
        logger.info("有效期: {} 年", validYears);
        
        // 1. 生成RSA密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        logger.info("RSA密钥对生成完成");
        
        // 2. 设置证书信息
        X500Name issuer = new X500Name(subject);  // 自签名，颁发者和主题相同
        X500Name subjectName = new X500Name(subject);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        
        Date notBefore = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(notBefore);
        calendar.add(Calendar.YEAR, validYears);
        Date notAfter = calendar.getTime();
        
        // 3. 构建证书
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subjectName,
                keyPair.getPublic()
        );
        
        // 4. 添加扩展
        // 基本约束：标记为CA证书
        certBuilder.addExtension(
                Extension.basicConstraints, 
                true, 
                new BasicConstraints(true)
        );
        
        // 密钥用途：数字签名
        certBuilder.addExtension(
                Extension.keyUsage,
                true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign)
        );
        
        // 扩展密钥用途：仅支持时间戳签名（TSA证书要求）
        certBuilder.addExtension(
                Extension.extendedKeyUsage,
                true,  // 设置为 critical
                new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping)  // 仅时间戳
        );
        
        // 5. 签名证书
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());
        
        X509CertificateHolder certHolder = certBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
        
        logger.info("证书生成完成");
        logger.info("序列号: {}", certificate.getSerialNumber().toString(16).toUpperCase());
        logger.info("有效期: {} 至 {}", certificate.getNotBefore(), certificate.getNotAfter());
        
        // 6. 保存为PKCS12格式
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(null, null);
        
        // 处理空密码情况
        char[] passwordChars = (password == null || password.isEmpty()) ? new char[0] : password.toCharArray();
        
        keyStore.setKeyEntry(
                "signing-key",
                keyPair.getPrivate(),
                passwordChars,
                new X509Certificate[]{certificate}
        );
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            keyStore.store(fos, passwordChars);
        }
        
        logger.info("证书已保存到: {}", outputPath);
        return certificate;
    }

    /**
     * 快速生成测试证书（使用默认参数）
     */
    public static X509Certificate generateTestCert(String outputPath) throws Exception {
        return generateSelfSignedCert(
                "CN=Test Organization,OU=Digital Signature Dept,O=Example Company,L=Beijing,ST=Beijing,C=CN",
                outputPath,
                "123456",
                5
        );
    }

    /**
     * 从Base64编码的证书数据加载证书
     * 兼容第三方API返回的证书数据格式
     * 
     * @param p12Base64 Base64编码的PKCS12证书数据
     * @param password 证书密码（可以为null或空字符串）
     * @param outputPath 可选的输出路径，如果提供则保存到文件
     * @return 加载的X509证书
     */
    public static X509Certificate loadCertFromBase64(String p12Base64, String password, String outputPath) throws Exception {
        logger.info("开始从Base64数据加载证书...");
        
        // 1. 解码Base64
        byte[] p12Data = Base64.getDecoder().decode(p12Base64);
        logger.info("Base64解码完成，数据大小: {} 字节", p12Data.length);
        
        // 2. 如果提供了输出路径，先保存文件
        if (outputPath != null && !outputPath.isEmpty()) {
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(p12Data);
            }
            logger.info("证书已保存到: {}", outputPath);
        }
        
        // 3. 加载PKCS12证书
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        char[] passwordChars = (password == null || password.isEmpty()) ? new char[0] : password.toCharArray();
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(p12Data)) {
            keyStore.load(bais, passwordChars);
        }
        
        // 4. 获取第一个证书别名
        String alias = keyStore.aliases().nextElement();
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        
        logger.info("证书加载成功");
        logger.info("主题: {}", certificate.getSubjectDN());
        logger.info("序列号: {}", certificate.getSerialNumber().toString(16).toUpperCase());
        logger.info("有效期: {} 至 {}", certificate.getNotBefore(), certificate.getNotAfter());
        
        return certificate;
    }

    /**
     * 从Base64编码的证书数据加载证书（不保存到文件）
     * 
     * @param p12Base64 Base64编码的PKCS12证书数据
     * @param password 证书密码
     * @return 加载的X509证书
     */
    public static X509Certificate loadCertFromBase64(String p12Base64, String password) throws Exception {
        return loadCertFromBase64(p12Base64, password, null);
    }

    /**
     * 主方法 - 用于命令行生成证书
     */
    public static void main(String[] args) {
        try {
            String outputPath = "test-cert.p12";
            if (args.length > 0) {
                outputPath = args[0];
            }
            
            X509Certificate cert = generateTestCert(outputPath);
            System.out.println("\n====== 证书生成成功 ======");
            System.out.println("文件路径: " + outputPath);
            System.out.println("密码: 123456");
            System.out.println("主题: " + cert.getSubjectDN());
            System.out.println("序列号: " + cert.getSerialNumber().toString(16).toUpperCase());
            System.out.println("有效期: " + cert.getNotBefore() + " 至 " + cert.getNotAfter());
            System.out.println("========================");
            
        } catch (Exception e) {
            logger.error("证书生成失败", e);
            e.printStackTrace();
        }
    }
}
