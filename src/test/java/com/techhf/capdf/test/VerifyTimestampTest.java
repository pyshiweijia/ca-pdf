package com.techhf.capdf.test;

import com.techhf.capdf.util.SignatureVerifier;
import org.junit.Test;

import java.io.File;

/**
 * 验证时间戳测试
 */
public class VerifyTimestampTest {

    @Test
    public void testVerifyTimestamp() {
        try {
            // 验证印章签名
            File sealPdf = new File("test-output/seal-sign/test-seal-signed.pdf");
            if (sealPdf.exists()) {
                SignatureVerifier.verifySignature(sealPdf);
            } else {
                System.out.println("文件不存在: " + sealPdf.getPath());
            }
            
            // 验证文本签名
            File textPdf = new File("test-output/text-sign/test-text-signed.pdf");
            if (textPdf.exists()) {
                SignatureVerifier.verifySignature(textPdf);
            } else {
                System.out.println("文件不存在: " + textPdf.getPath());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
