package com.techhf.capdf.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 签章图片生成器
 */
public class SignImageGenerator {

    /**
     * 生成印章样式的签章图片
     * 
     * @param organizationName 组织名称
     * @param outputPath 输出路径
     * @param width 图片宽度
     * @param height 图片高度
     */
    public static void generateSealImage(String organizationName, String outputPath, int width, int height) throws IOException {
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 透明背景
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.Src);
        
        // 红色印章
        Color sealColor = new Color(220, 20, 60); // 红色
        g2d.setColor(sealColor);
        g2d.setStroke(new BasicStroke(3));
        
        // 绘制圆形边框
        int margin = 10;
        int diameter = Math.min(width, height) - 2 * margin;
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2;
        g2d.drawOval(x, y, diameter, diameter);
        
        // 绘制内圆
        int innerMargin = margin + 15;
        int innerDiameter = Math.min(width, height) - 2 * innerMargin;
        int innerX = (width - innerDiameter) / 2;
        int innerY = (height - innerDiameter) / 2;
        g2d.drawOval(innerX, innerY, innerDiameter, innerDiameter);
        
        // 绘制五角星
        drawStar(g2d, width / 2, height / 2, 15, sealColor);
        
        // 绘制组织名称（圆形排列）
        g2d.setFont(new Font("黑体", Font.BOLD, 16));
        drawCircleText(g2d, organizationName, width / 2, height / 2, diameter / 2 - 20, sealColor);
        
        g2d.dispose();
        
        // 保存图片
        ImageIO.write(image, "PNG", new File(outputPath));
    }

    /**
     * 生成文本签章图片
     */
    public static void generateTextSignImage(
            String signerName,
            String location,
            String reason,
            Date signTime,
            String outputPath) throws IOException {
        
        int width = 300;
        int height = 150;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 白色背景带边框
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, width, height, 10, 10);
        
        g2d.setColor(new Color(0, 102, 204)); // 蓝色边框
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(1, 1, width - 2, height - 2, 10, 10);
        
        // 标题
        g2d.setColor(new Color(0, 102, 204));
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 18));
        drawCenteredString(g2d, "数字签名", width / 2, 25);
        
        // 分隔线
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawLine(20, 40, width - 20, 40);
        
        // 详细信息
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        int yPos = 60;
        int lineHeight = 22;
        
        g2d.drawString("签名人: " + signerName, 20, yPos);
        yPos += lineHeight;
        
        if (location != null && !location.isEmpty()) {
            g2d.drawString("位置: " + location, 20, yPos);
            yPos += lineHeight;
        }
        
        if (reason != null && !reason.isEmpty()) {
            String shortReason = reason.length() > 20 ? reason.substring(0, 20) + "..." : reason;
            g2d.drawString("原因: " + shortReason, 20, yPos);
            yPos += lineHeight;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        g2d.drawString("时间: " + sdf.format(signTime), 20, yPos);
        
        g2d.dispose();
        
        // 保存
        ImageIO.write(image, "PNG", new File(outputPath));
    }

    /**
     * 绘制五角星
     */
    private static void drawStar(Graphics2D g2d, int centerX, int centerY, int radius, Color color) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        double angle = Math.PI / 2; // 起始角度
        double step = Math.PI / 5;  // 步进角度
        
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? radius : radius / 2.5;
            xPoints[i] = centerX + (int) (r * Math.cos(angle));
            yPoints[i] = centerY - (int) (r * Math.sin(angle));
            angle += step;
        }
        
        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 10);
    }

    /**
     * 绘制圆形排列的文字
     */
    private static void drawCircleText(Graphics2D g2d, String text, int centerX, int centerY, int radius, Color color) {
        g2d.setColor(color);
        
        int len = text.length();
        double angleStep = 2 * Math.PI / len;
        double startAngle = Math.PI / 2 + Math.PI; // 从顶部开始
        
        for (int i = 0; i < len; i++) {
            double angle = startAngle + i * angleStep;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY - (int) (radius * Math.sin(angle));
            
            // 旋转文字
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y);
            g2dCopy.rotate(-angle + Math.PI / 2);
            
            String ch = String.valueOf(text.charAt(i));
            FontMetrics fm = g2dCopy.getFontMetrics();
            int charWidth = fm.stringWidth(ch);
            int charHeight = fm.getHeight();
            
            g2dCopy.drawString(ch, -charWidth / 2, charHeight / 4);
            g2dCopy.dispose();
        }
    }

    /**
     * 居中绘制字符串
     */
    private static void drawCenteredString(Graphics2D g2d, String text, int x, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, x - textWidth / 2, y);
    }

    /**
     * 测试生成签章图片
     */
    public static void main(String[] args) {
        try {
            // 生成印章样式
            generateSealImage("Example Organization", "seal-signature.png", 200, 200);
            System.out.println("印章签章生成成功: seal-signature.png");
            
            // 生成文本签章
            generateTextSignImage(
                    "Example Organization",
                    "Beijing, China",
                    "Digital Signature for Test Document",
                    new Date(),
                    "text-signature.png"
            );
            System.out.println("文本签章生成成功: text-signature.png");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
