package com.techhf.capdf.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单PDF创建工具
 * 用于创建测试用的PDF文件
 */
public class SimplePdfCreator {

    /**
     * 创建示例证书PDF（通用模板）
     */
    public static void createParkingCertificate(File outputFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 加载中文字体（使用系统字体）
                File fontFile = new File("C:/Windows/Fonts/simhei.ttf");  // 黑体
                if (!fontFile.exists()) {
                    fontFile = new File("C:/Windows/Fonts/simsun.ttc");  // 宋体
                }
                
                PDType0Font font;
                if (fontFile.exists()) {
                    font = PDType0Font.load(document, fontFile);
                } else {
                    // 如果系统字体不存在，使用PDFBox内置字体（不支持中文）
                    throw new IOException("未找到中文字体，请确保系统存在黑体或宋体");
                }
                
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float leading = 25;
                
                // 标题
                contentStream.beginText();
                contentStream.setFont(font, 20);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("测试证书样本");
                contentStream.endText();
                
                yPosition -= leading * 2;
                
                // 内容
                contentStream.setFont(font, 12);
                String[] lines = {
                        "证书编号：CERT-2024-001234",
                        "",
                        "持有人：张三",
                        "证件号码：370102199001011234",
                        "项目编号：A-101",
                        "项目位置：示例位置A区101号",
                        "面积：15.5平方米",
                        "有效期限：2024年1月1日 至 2074年1月1日",
                        "",
                        "颁发单位：示例机构",
                        "颁发日期：" + new SimpleDateFormat("yyyy年MM月dd日").format(new Date()),
                        "",
                        "本证书经数字签名，具有法律效力。",
                        "请使用Adobe Reader验证签名有效性。",
                        "",
                        "",
                        "",
                        "（签名区域）"
                };
                
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= leading;
                }
            }
            
            document.save(outputFile);
        }
    }

    /**
     * 创建简单的测试PDF
     */
    public static void createSimplePdf(File outputFile, String content) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 尝试加载中文字体
                File fontFile = new File("C:/Windows/Fonts/simhei.ttf");
                if (!fontFile.exists()) {
                    fontFile = new File("C:/Windows/Fonts/simsun.ttc");
                }
                
                if (fontFile.exists()) {
                    PDType0Font font = PDType0Font.load(document, fontFile);
                    contentStream.setFont(font, 12);
                } else {
                    contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12);
                }
                
                contentStream.beginText();
                contentStream.newLineAtOffset(50, page.getMediaBox().getHeight() - 50);
                contentStream.showText(content);
                contentStream.endText();
            }
            
            document.save(outputFile);
        }
    }

    public static void main(String[] args) {
        try {
            File outputFile = new File("test-certificate.pdf");
            createParkingCertificate(outputFile);
            System.out.println("PDF创建成功: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
