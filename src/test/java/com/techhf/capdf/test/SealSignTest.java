package com.techhf.capdf.test;

import com.techhf.capdf.cert.CertificateGenerator;
import com.techhf.capdf.signer.PdfSigner;
import com.techhf.capdf.util.SignImageGenerator;
import com.techhf.capdf.util.SimplePdfCreator;
import org.junit.Test;

import java.io.File;

/**
 * 印章签章测试
 * 包含标准密码、无密码和空密码场景
 */
public class SealSignTest {

    @Test
    public void testSealSign() {
        try {
            System.out.println("\n========== 印章签章测试 ==========\n");
            
            // 创建输出目录
            File outputDir = new File("test-output/seal-sign");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 第一步：生成测试证书
            System.out.println("【步骤1】生成测试证书...");
            String certPath = new File(outputDir, "test-cert.p12").getPath();
            String certPassword = "123456";
            CertificateGenerator.generateSelfSignedCert(
                    "CN=Example Organization,OU=Digital Signature Dept,O=Example Company,L=Beijing,ST=Beijing,C=CN",
                    certPath,
                    certPassword,
                    10
            );
            System.out.println("✓ 证书生成成功");
            
            // 第二步：生成印章签章图片
            System.out.println("\n【步骤2】生成印章签章图片...");
            String sealImagePath = new File(outputDir, "sign-seal.png").getPath();
            SignImageGenerator.generateSealImage(
                    "Example Org",
                    sealImagePath,
                    200,
                    200
            );
            System.out.println("✓ 印章签章生成成功: " + sealImagePath);
            
            // 第三步：创建待签名PDF
            System.out.println("\n【步骤3】创建待签名PDF...");
            File inputPdf = new File(outputDir, "test-seal-unsigned.pdf");
            SimplePdfCreator.createParkingCertificate(inputPdf);
            System.out.println("✓ PDF创建成功: " + inputPdf.getName());
            
            // 第四步：使用印章签名
            System.out.println("\n【步骤4】使用印章签章签名...");
            File outputPdf = new File(outputDir, "test-seal-signed.pdf");
            
            PdfSigner.signPdf(
                    inputPdf,
                    outputPdf,
                    certPath,
                    certPassword,
                    "Test Certificate Digital Signature",
                    "Beijing, China",
                    sealImagePath,
                    true
            );
            System.out.println("✓ 签名完成: " + outputPdf.getName());
            
            System.out.println("\n========== 测试完成 ==========");
            System.out.println("输出目录: " + outputDir.getAbsolutePath());
            System.out.println("生成文件:");
            System.out.println("  - 证书: test-cert.p12");
            System.out.println("  - 签章图片: sign-seal.png");
            System.out.println("  - 未签名PDF: test-seal-unsigned.pdf");
            System.out.println("  - 已签名PDF: test-seal-signed.pdf");
            System.out.println("\n特点: 红色圆形印章，五角星中心，传统公章样式\n");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testNoPasswordCert() {
        try {
            System.out.println("\n========== 无密码证书测试 ==========\n");
            
            File outputDir = new File("test-output/no-password");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            System.out.println("【步骤1】生成无密码测试证书...");
            String certPath = new File(outputDir, "no-password-cert.p12").getPath();
            CertificateGenerator.generateSelfSignedCert(
                    "CN=Test Organization (No Password),OU=Testing Dept,O=Example Company,L=Beijing,ST=Beijing,C=CN",
                    certPath,
                    null,  // 无密码
                    10
            );
            System.out.println("✓ 无密码证书生成成功");
            
            System.out.println("\n【步骤2】生成签章图片...");
            String sealImagePath = new File(outputDir, "sign-seal.png").getPath();
            SignImageGenerator.generateSealImage("Test Org", sealImagePath, 200, 200);
            System.out.println("✓ 签章图片生成成功");
            
            System.out.println("\n【步骤3】创建待签名PDF...");
            File inputPdf = new File(outputDir, "test-unsigned.pdf");
            SimplePdfCreator.createParkingCertificate(inputPdf);
            System.out.println("✓ PDF创建成功");
            
            System.out.println("\n【步骤4】使用无密码证书签名...");
            File outputPdf = new File(outputDir, "test-signed.pdf");
            PdfSigner.signPdf(
                    inputPdf, outputPdf, certPath, null,
                    "No Password Certificate Signature Test",
                    "Beijing", sealImagePath, true
            );
            System.out.println("✓ 签名完成");
            
            System.out.println("\n========== 测试完成 ==========");
            System.out.println("特点: 证书无密码保护，适用于自动化场景\n");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testEmptyPasswordCert() {
        try {
            System.out.println("\n========== 空密码证书测试 ==========\n");
            
            File outputDir = new File("test-output/empty-password");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            System.out.println("【步骤1】生成空密码测试证书...");
            String certPath = new File(outputDir, "empty-password-cert.p12").getPath();
            CertificateGenerator.generateSelfSignedCert(
                    "CN=Test Organization (Empty Password),OU=Testing Dept,O=Example Company,L=Shanghai,ST=Shanghai,C=CN",
                    certPath,
                    "",  // 空字符串密码
                    10
            );
            System.out.println("✓ 空密码证书生成成功");
            
            System.out.println("\n【步骤2】生成签章图片...");
            String sealImagePath = new File(outputDir, "sign-seal.png").getPath();
            SignImageGenerator.generateSealImage("Test Org", sealImagePath, 200, 200);
            
            System.out.println("\n【步骤3】创建待签名PDF...");
            File inputPdf = new File(outputDir, "test-unsigned.pdf");
            SimplePdfCreator.createParkingCertificate(inputPdf);
            
            System.out.println("\n【步骤4】使用空密码证书签名...");
            File outputPdf = new File(outputDir, "test-signed.pdf");
            PdfSigner.signPdf(
                    inputPdf, outputPdf, certPath, "",
                    "Empty Password Certificate Signature Test",
                    "Shanghai", sealImagePath, true
            );
            System.out.println("✓ 签名完成");
            
            System.out.println("\n========== 测试完成 ==========");
            System.out.println("特点: 空字符串密码与null密码效果相同\n");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
