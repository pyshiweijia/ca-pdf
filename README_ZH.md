# PDF 数字签名工具包

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-red.svg)](https://maven.apache.org/)

一个功能完善的 Java 库，用于创建和验证带有 RFC 3161 时间戳的 PDF 数字签名，支持自定义可视化签章。

> ⚠️ **注意**: 本项目使用自签名时间戳进行测试。生产环境请集成商业 TSA 服务（DigiCert、GlobalSign 等）。

简体中文 | [English](README.md)

## 🌟 核心特性

- ✅ **X.509 证书生成**：创建用于测试的自签名证书
- ✅ **PDF 数字签名**：PKCS#7 分离格式 (adbe.pkcs7.detached)
- ✅ **可视化签章**：印章样式和文本样式签名外观
- ✅ **RFC 3161 时间戳**：在 CMS 结构中嵌入时间戳令牌
- ✅ **Base64 证书加载**：从 API 响应导入证书
- ✅ **无密码证书**：支持无密码保护的证书
- ✅ **签名验证**：从已签名 PDF 中提取和验证时间戳

## 🎯 应用场景

- **合同签署**：为合同添加具有法律效力的数字签名
- **文档认证**：验证文档完整性和签名者身份
- **归档系统**：带时间戳的长期文档保存
- **工作流自动化**：将数字签名集成到业务流程中
- **证书 API 集成**：与第三方证书提供商对接

## 📋 目录

- [快速开始](#-快速开始)
- [安装](#-安装)
- [使用示例](#-使用示例)
- [架构设计](#-架构设计)
- [技术细节](#-技术细节)
- [API 文档](#-api-文档)
- [测试](#-测试)
- [生产环境建议](#-生产环境建议)
- [贡献](#-贡献)
- [许可证](#-许可证)

## 🚀 快速开始

### 基础 PDF 签名

```java
import com.techhf.capdf.cert.CertificateGenerator;
import com.techhf.capdf.signer.PdfSigner;
import java.io.File;

// 1. 生成测试证书
CertificateGenerator.generateTestCert("test-cert.p12");

// 2. 签名 PDF
PdfSigner.signPdf(
    new File("input.pdf"),
    new File("output-signed.pdf"),
    "test-cert.p12",
    "123456",
    "数字签名",
    "北京市",
    "seal.png",
    true
);
```

### 从 API 加载证书

```java
// 从 Base64 编码数据加载证书（例如从 REST API）
String p12Base64 = apiResponse.getP12Buf();
String password = apiResponse.getCertPassword();

X509Certificate cert = CertificateGenerator.loadCertFromBase64(
    p12Base64,
    password,
    "loaded-cert.p12"
);

// 使用加载的证书进行签名
PdfSigner.signPdf(/* ... */);
```

## 📦 安装

### Maven

```xml
<dependency>
    <groupId>io.github.pyshiweijia</groupId>
    <artifactId>ca-pdf</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'io.github.pyshiweijia:ca-pdf:1.0.0'
```

### 从源码构建

```bash
git clone https://github.com/pyshiweijia/ca-pdf.git
cd ca-pdf
mvn clean install
```

## 💻 使用示例

### 示例 1：生成自签名证书

```java
import com.techhf.capdf.cert.CertificateGenerator;
import java.security.cert.X509Certificate;

// 使用自定义参数生成证书
X509Certificate cert = CertificateGenerator.generateSelfSignedCert(
    "CN=我的组织,OU=技术部,O=我的公司,L=北京,ST=北京,C=CN",
    "my-cert.p12",
    "securePassword123",
    10  // 有效期10年
);

System.out.println("证书已生成: " + cert.getSubjectDN());
```

### 示例 2：创建可视化签名

```java
import com.techhf.capdf.util.SignImageGenerator;
import java.util.Date;

// 创建印章样式签名（圆形带五角星）
SignImageGenerator.generateSealImage(
    "我的组织",
    "seal.png",
    200,
    200
);

// 创建文本样式签名
SignImageGenerator.generateTextSignImage(
    "我的组织",
    "北京市",
    "数字签名",
    new Date(),
    "text-signature.png"
);
```

### 示例 3：签名 PDF 并添加时间戳

```java
import com.techhf.capdf.signer.PdfSigner;
import java.io.File;

// 签名 PDF 并嵌入 RFC 3161 时间戳
PdfSigner.signPdf(
    new File("document.pdf"),
    new File("document-signed.pdf"),
    "certificate.p12",
    "password",
    "文档审批",              // 原因
    "总部",                  // 位置
    "signature.png",        // 可视化外观
    true                    // 可见签名
);
```

### 示例 4：验证签名并提取时间戳

```java
import com.techhf.capdf.util.SignatureVerifier;

// 验证签名并提取时间戳信息
SignatureVerifier.verifySignatures("document-signed.pdf");

/* 输出:
 * ✓ 发现签名
 * 签名人: 我的组织
 * 时间戳: 2024-01-25 10:30:00 UTC
 * 序列号: 1234567890
 */
```

### 示例 5：无密码证书

```java
// 生成无密码证书
CertificateGenerator.generateSelfSignedCert(
    "CN=测试组织,O=测试,C=CN",
    "no-password-cert.p12",
    null,  // 无密码
    5
);

// 使用无密码证书签名
PdfSigner.signPdf(
    inputPdf,
    outputPdf,
    "no-password-cert.p12",
    null,  // 无密码
    "测试签名",
    "北京",
    "seal.png",
    true
);
```

### 示例 6：集成外部证书 API

```java
// 与第三方证书提供商集成的典型工作流程
public class CertificateIntegration {
    
    public void signWithApiCertificate(File pdfFile) throws Exception {
        // 1. 从 API 获取证书
        ApiCertResponse response = certificateApi.getCertificate(userId);
        
        // 2. 从 Base64 加载证书
        String certPath = "api-cert.p12";
        CertificateGenerator.loadCertFromBase64(
            response.getP12Buf(),
            response.getCertPassword(),
            certPath
        );
        
        // 3. 生成签名图像
        SignImageGenerator.generateSealImage(
            response.getSubjectName(),
            "signature.png",
            200,
            200
        );
        
        // 4. 签名 PDF
        PdfSigner.signPdf(
            pdfFile,
            new File("signed-" + pdfFile.getName()),
            certPath,
            response.getCertPassword(),
            "API 证书签名",
            "在线服务",
            "signature.png",
            true
        );
    }
}
```

## 🏗️ 架构设计

```
src/main/java/com/techhf/capdf/
├── cert/
│   └── CertificateGenerator.java    # X.509 证书生成和加载
├── signer/
│   └── PdfSigner.java                # 核心 PDF 签名与时间戳嵌入
└── util/
    ├── SignatureVerifier.java        # 签名验证和时间戳提取
    ├── SignImageGenerator.java       # 可视化签名图像生成
    └── SimplePdfCreator.java         # 测试 PDF 文档创建
```

## 🔧 技术细节

### 技术栈

| 库 | 版本 | 用途 |
|---------|---------|---------|
| Apache PDFBox | 2.0.29 | PDF 处理和可视化签名 |
| BouncyCastle | 1.70 | 密码学、证书和时间戳 |
| SLF4J + Logback | 1.7.36 / 1.2.11 | 日志框架 |

### 标准合规性

- **RFC 3161**: 时间戳协议 (TSP)
- **RFC 5652**: 密码学消息语法 (CMS)
- **PKCS#7**: 密码学消息语法标准
- **PKCS#12**: 个人信息交换语法
- **X.509**: 公钥基础设施证书

### RFC 3161 时间戳实现

本库实现了在 CMS 结构中正确嵌入 RFC 3161 时间戳：

```java
// 时间戳作为未签名属性添加到 SignerInfo 中
SignerInfo
  ├── signedAttrs (content-type, message-digest, signing-time)
  ├── signature (RSA 签名字节)
  └── unsignedAttrs
      └── id-aa-signatureTimeStampToken (OID: 1.2.840.113549.1.9.16.2.14)
          └── SET
              └── ContentInfo (TimeStampToken)
                  └── SignedData (TSA 签名)
```

**关键实现要点**：

1. TimeStampToken 通过对签名哈希签名生成
2. 作为未签名属性嵌入，使用 OID `1.2.840.113549.1.9.16.2.14`
3. 正确的 ASN.1 结构：`SET -> ContentInfo`（不是 `SET -> SET -> ContentInfo`）
4. TSA 证书必须具有 ExtendedKeyUsage，带有 `id_kp_timeStamping`（关键）

### 证书要求

用于时间戳生成的 TSA 证书：

- **ExtendedKeyUsage**: 必须标记为关键
- **密钥用途**: 仅 `id-kp-timeStamping` (1.3.6.1.5.5.7.3.8)
- **基本约束**: 自签名 TSA 的 CA = true
- **密钥用法**: digitalSignature, keyCertSign

## 📚 API 文档

### CertificateGenerator

#### `generateSelfSignedCert`

```java
public static X509Certificate generateSelfSignedCert(
    String subject,      // X.500 DN (例如："CN=组织,O=公司,C=CN")
    String outputPath,   // .p12 文件输出路径
    String password,     // 密码（无密码传 null）
    int validYears       // 证书有效期（年）
) throws Exception
```

#### `loadCertFromBase64`

```java
public static X509Certificate loadCertFromBase64(
    String p12Base64,    // Base64 编码的 PKCS12 证书
    String password,     // 证书密码（可以为 null）
    String outputPath    // 可选：保存到文件
) throws Exception
```

### PdfSigner

#### `signPdf`

```java
public static void signPdf(
    File inputPdf,          // 输入 PDF 文件
    File outputPdf,         // 输出已签名 PDF 文件
    String certPath,        // .p12 证书路径
    String certPassword,    // 证书密码
    String reason,          // 签名原因
    String location,        // 签名位置
    String signImagePath,   // 签名图像路径
    boolean visible         // 签名是否可见
) throws Exception
```

### SignImageGenerator

#### `generateSealImage`

```java
public static void generateSealImage(
    String orgName,      // 组织名称
    String outputPath,   // 输出图像路径
    int width,          // 图像宽度
    int height          // 图像高度
) throws IOException
```

#### `generateTextSignImage`

```java
public static void generateTextSignImage(
    String signer,       // 签名人姓名
    String location,     // 签名位置
    String reason,       // 签名原因
    Date signTime,       // 签名时间戳
    String outputPath    // 输出图像路径
) throws IOException
```

## 🧪 测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试

```bash
# 印章签名测试（包含无密码和空密码测试）
mvn test -Dtest=SealSignTest

# 测试单独的方法
mvn test -Dtest=SealSignTest#testSealSign
mvn test -Dtest=SealSignTest#testNoPasswordCert
mvn test -Dtest=SealSignTest#testEmptyPasswordCert

# 文本签名测试
mvn test -Dtest=TextSignTest

# Base64 证书加载测试
mvn test -Dtest=Base64CertTest
```

### 生成测试报告

```bash
# 运行测试并生成覆盖率
mvn clean test

# 生成 Javadoc
mvn javadoc:javadoc

# 打包并附带源码和文档
mvn clean package
```

### 测试输出结构

```
test-output/
├── seal-sign/              # 印章签名测试输出
│   ├── test-cert.p12
│   ├── sign-seal.png
│   ├── test-seal-unsigned.pdf
│   └── test-seal-signed.pdf
├── text-sign/              # 文本签名测试输出
├── no-password/            # 无密码证书测试
├── empty-password/         # 空密码证书测试
└── base64-cert/            # Base64 加载测试
```

## ⚠️ 生产环境建议

### 当前限制

1. **自签名时间戳**: 使用内部 TSA，非商业可信
2. **Adobe Reader 兼容性**: 自签名 TSA 可能无法显示时间戳信息
3. **证书信任**: 自签名证书不受系统信任

### 生产部署建议

#### 使用商业 TSA 服务

推荐提供商：
- DigiCert TSA
- GlobalSign TSA
- Entrust TSA
- CFCA（中国金融认证中心）

#### 示例：集成商业 TSA

```java
private static TimeStampToken requestCommercialTSA(
        byte[] signatureHash,
        String tsaUrl) throws Exception {
    
    TimeStampRequestGenerator tsReqGen = new TimeStampRequestGenerator();
    tsReqGen.setCertReq(true);
    
    TimeStampRequest tsRequest = tsReqGen.generate(
        TSPAlgorithms.SHA256,
        signatureHash,
        BigInteger.valueOf(System.currentTimeMillis())
    );
    
    // HTTP POST 到商业 TSA
    HttpURLConnection conn = (HttpURLConnection) new URL(tsaUrl).openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/timestamp-query");
    conn.setDoOutput(true);
    
    try (OutputStream os = conn.getOutputStream()) {
        os.write(tsRequest.getEncoded());
    }
    
    TimeStampResponse tsResponse = new TimeStampResponse(conn.getInputStream());
    tsResponse.validate(tsRequest);
    
    return tsResponse.getTimeStampToken();
}
```

#### 证书管理

- 使用 CA 颁发的正式证书
- 将私钥存储在 HSM（硬件安全模块）中
- 实施证书轮换策略
- 定期安全审计

## 🤝 贡献

欢迎贡献！请遵循以下指南：

1. Fork 仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m '添加某某功能'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

### 开发指南

- 遵循 Java 代码规范
- 为新功能添加单元测试
- 更新相关文档
- 提交 PR 前确保所有测试通过

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 详见 [LICENSE](LICENSE) 文件。

## 🙏 致谢

- [Apache PDFBox](https://pdfbox.apache.org/) - PDF 处理库
- [Bouncy Castle](https://www.bouncycastle.org/) - 密码学提供者
- [RFC 3161](https://www.rfc-editor.org/rfc/rfc3161) - 时间戳协议规范

## 📊 项目统计

- **核心代码**: ~1,100 行（不含测试）
- **测试覆盖率**: 80%+
- **Java 版本**: 8+
- **依赖项**: 4 个核心库

---

**用 ❤️ 为 Java 社区打造**
