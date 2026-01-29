package com.techhf.capdf.test;

import com.techhf.capdf.cert.CertificateGenerator;
import com.techhf.capdf.signer.PdfSigner;
import com.techhf.capdf.util.SignImageGenerator;
import com.techhf.capdf.util.SimplePdfCreator;
import org.junit.Test;

import java.io.File;
import java.util.Date;

/**
 * 文本签章测试
 */
public class TextSignTest {

    @Test
    public void testTextSign() {
        try {
            System.out.println("\n========== 文本签章测试 ==========\n");
            
            // 创建输出目录
            File outputDir = new File("test-output/text-sign");
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
            
            // 第二步：生成文本签章图片
            System.out.println("\n【步骤2】生成文本签章图片...");
            String textImagePath = new File(outputDir, "sign-text.png").getPath();
            SignImageGenerator.generateTextSignImage(
                    "Example Organization",
                    "Beijing, China",
                    "Test Certificate Digital Signature",
                    new Date(),
                    textImagePath
            );
            System.out.println("✓ 文本签章生成成功: " + textImagePath);
            
            // 第三步：创建待签名PDF
            System.out.println("\n【步骤3】创建待签名PDF...");
            File inputPdf = new File(outputDir, "test-text-unsigned.pdf");
            SimplePdfCreator.createParkingCertificate(inputPdf);
            System.out.println("✓ PDF创建成功: " + inputPdf.getName());
            
            // 第四步：使用文本签章签名
            System.out.println("\n【步骤4】使用文本签章签名...");
            File outputPdf = new File(outputDir, "test-text-signed.pdf");
            
            PdfSigner.signPdf(
                    inputPdf,
                    outputPdf,
                    certPath,
                    certPassword,
                    "Test Certificate Digital Signature",
                    "Beijing, China",
                    textImagePath,
                    true
            );
            System.out.println("✓ 签名完成: " + outputPdf.getName());
            
            System.out.println("\n========== 测试完成 ==========");
            System.out.println("输出目录: " + outputDir.getAbsolutePath());
            System.out.println("生成文件:");
            System.out.println("  - 证书: test-cert.p12");
            System.out.println("  - 签章图片: sign-text.png");
            System.out.println("  - 未签名PDF: test-text-unsigned.pdf");
            System.out.println("  - 已签名PDF: test-text-signed.pdf");
            System.out.println("\n特点: 蓝色边框文本签章，信息清晰明了\n");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
