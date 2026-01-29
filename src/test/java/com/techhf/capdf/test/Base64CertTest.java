package com.techhf.capdf.test;

import com.techhf.capdf.cert.CertificateGenerator;
import com.techhf.capdf.signer.PdfSigner;
import com.techhf.capdf.util.SignImageGenerator;
import com.techhf.capdf.util.SimplePdfCreator;
import org.junit.Test;

import java.io.File;

/**
 * Base64证书加载测试
 * 测试从第三方API获取Base64编码证书后的加载和使用
 */
public class Base64CertTest {
    
    @Test
    public void testLoadCertFromBase64() {
        try {
            System.out.println("\n========== 从Base64加载证书测试（兼容第三方API） ==========\n");
            
            // 创建输出目录
            File outputDir = new File("test-output/base64-cert");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 第一步：先生成一个测试证书并转换为Base64（模拟API返回）
            System.out.println("【步骤1】模拟API返回的证书数据...");
            String tempCertPath = new File(outputDir, "temp-cert.p12").getPath();
            CertificateGenerator.generateSelfSignedCert(
                    "CN=Example Organization,O=Example Company,OU=IT Dept,C=CN",
                    tempCertPath,
                    null,  // 模拟无密码
                    10
            );
            
            // 读取证书文件并转换为Base64（模拟API返回的p12Buf字段）
            java.nio.file.Path path = java.nio.file.Paths.get(tempCertPath);
            byte[] certBytes = java.nio.file.Files.readAllBytes(path);
            String p12Base64 = java.util.Base64.getEncoder().encodeToString(certBytes);
            System.out.println("✓ 证书Base64编码完成（模拟API返回）");
            System.out.println("  Base64长度: " + p12Base64.length() + " 字符");
            
            // 第二步：从Base64加载证书（模拟从API响应对象获取）
            System.out.println("\n【步骤2】从Base64数据加载证书...");
            // 模拟从API获取的数据：
            // ApiCertResult.p12Buf = p12Base64
            // ApiCertResult.certPassword = null
            String outputCertPath = new File(outputDir, "loaded-cert.p12").getPath();
            CertificateGenerator.loadCertFromBase64(
                    p12Base64,           // 从API返回值的 p12Buf 字段获取
                    null,                // 从API返回值的 certPassword 字段获取
                    outputCertPath       // 可选：保存到本地文件
            );
            System.out.println("✓ 证书加载成功并保存到本地");
            
            // 第三步：使用加载的证书进行PDF签名
            System.out.println("\n【步骤3】使用加载的证书签名PDF...");
            String sealImagePath = new File(outputDir, "sign-seal.png").getPath();
            SignImageGenerator.generateSealImage(
                    "Example Org",
                    sealImagePath,
                    200,
                    200
            );
            
            File inputPdf = new File(outputDir, "test-unsigned.pdf");
            SimplePdfCreator.createParkingCertificate(inputPdf);
            
            File outputPdf = new File(outputDir, "test-signed.pdf");
            PdfSigner.signPdf(
                    inputPdf,
                    outputPdf,
                    outputCertPath,  // 使用从Base64加载的证书
                    null,
                    "API Certificate Signature Test",
                    "Beijing",
                    sealImagePath,
                    true
            );
            System.out.println("✓ PDF签名完成");
            
            System.out.println("\n========== 测试完成 ==========");
            System.out.println("输出目录: " + outputDir.getAbsolutePath());
            System.out.println("\n使用说明：");
            System.out.println("1. 从第三方API获取证书响应对象");
            System.out.println("2. 提取 p12Buf 和 certPassword 字段");
            System.out.println("3. 调用 loadCertFromBase64(p12Buf, certPassword, outputPath)");
            System.out.println("4. 使用生成的证书文件进行PDF签名\n");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
