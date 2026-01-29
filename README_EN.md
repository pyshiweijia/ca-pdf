# PDF Digital Signature Toolkit

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-red.svg)](https://maven.apache.org/)

A comprehensive Java library for creating and verifying PDF digital signatures with RFC 3161 timestamp support and customizable visual signatures.

> ⚠️ **Note**: This project uses self-signed timestamps for testing. For production use, integrate with commercial TSA services (DigiCert, GlobalSign, etc.).

[简体中文](README_ZH.md) | English

## 🌟 Features

- ✅ **X.509 Certificate Generation**: Create self-signed certificates for testing
- ✅ **PDF Digital Signature**: PKCS#7 detached format (adbe.pkcs7.detached)
- ✅ **Visual Signatures**: Seal-style and text-based signature appearances
- ✅ **RFC 3161 Timestamp**: Embedded timestamp tokens in CMS structure
- ✅ **Base64 Certificate Loading**: Import certificates from API responses
- ✅ **Passwordless Certificates**: Support for certificates without password protection
- ✅ **Signature Verification**: Extract and validate timestamps from signed PDFs

## 🎯 Use Cases

- **Contract Signing**: Add legally binding digital signatures to contracts
- **Document Authentication**: Verify document integrity and signer identity
- **Archival Systems**: Long-term document preservation with timestamps
- **Workflow Automation**: Integrate digital signatures into business processes
- **Certificate API Integration**: Work with third-party certificate providers

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Installation](#-installation)
- [Usage Examples](#-usage-examples)
- [Architecture](#-architecture)
- [Technical Details](#-technical-details)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Production Considerations](#-production-considerations)
- [Contributing](#-contributing)
- [License](#-license)

## 🚀 Quick Start

### Basic PDF Signing

```java
import com.techhf.capdf.cert.CertificateGenerator;
import com.techhf.capdf.signer.PdfSigner;
import java.io.File;

// 1. Generate a test certificate
CertificateGenerator.generateTestCert("test-cert.p12");

// 2. Sign a PDF
PdfSigner.signPdf(
    new File("input.pdf"),
    new File("output-signed.pdf"),
    "test-cert.p12",
    "123456",
    "Digital Signature",
    "Beijing, China",
    "seal.png",
    true
);
```

### Loading Certificate from API

```java
// Load certificate from Base64-encoded data (e.g., from REST API)
String p12Base64 = apiResponse.getP12Buf();
String password = apiResponse.getCertPassword();

X509Certificate cert = CertificateGenerator.loadCertFromBase64(
    p12Base64,
    password,
    "loaded-cert.p12"
);

// Use the loaded certificate for signing
PdfSigner.signPdf(/* ... */);
```

## 📦 Installation

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

### Build from Source

```bash
git clone https://github.com/pyshiweijia/ca-pdf.git
cd ca-pdf
mvn clean install
```

## 💻 Usage Examples

### Example 1: Generate Self-Signed Certificate

```java
import com.techhf.capdf.cert.CertificateGenerator;
import java.security.cert.X509Certificate;

// Generate certificate with custom parameters
X509Certificate cert = CertificateGenerator.generateSelfSignedCert(
    "CN=My Organization,OU=IT Dept,O=My Company,L=Beijing,ST=Beijing,C=CN",
    "my-cert.p12",
    "securePassword123",
    10  // Valid for 10 years
);

System.out.println("Certificate generated: " + cert.getSubjectDN());
```

### Example 2: Create Visual Signature

```java
import com.techhf.capdf.util.SignImageGenerator;
import java.util.Date;

// Create seal-style signature (circular with star)
SignImageGenerator.generateSealImage(
    "My Organization",
    "seal.png",
    200,
    200
);

// Create text-based signature
SignImageGenerator.generateTextSignImage(
    "My Organization",
    "Beijing, China",
    "Digital Signature",
    new Date(),
    "text-signature.png"
);
```

### Example 3: Sign PDF with Timestamp

```java
import com.techhf.capdf.signer.PdfSigner;
import java.io.File;

// Sign PDF with embedded RFC 3161 timestamp
PdfSigner.signPdf(
    new File("document.pdf"),
    new File("document-signed.pdf"),
    "certificate.p12",
    "password",
    "Document Approval",        // Reason
    "Head Office",              // Location
    "signature.png",            // Visual appearance
    true                        // Visible signature
);
```

### Example 4: Verify Signature and Extract Timestamp

```java
import com.techhf.capdf.util.SignatureVerifier;

// Verify signature and extract timestamp information
SignatureVerifier.verifySignatures("document-signed.pdf");

/* Output:
 * ✓ Signature found
 * Signer: My Organization
 * Timestamp: 2024-01-25 10:30:00 UTC
 * Serial Number: 1234567890
 */
```

### Example 5: Passwordless Certificate

```java
// Generate certificate without password
CertificateGenerator.generateSelfSignedCert(
    "CN=Test Org,O=Test,C=CN",
    "no-password-cert.p12",
    null,  // No password
    5
);

// Sign with passwordless certificate
PdfSigner.signPdf(
    inputPdf,
    outputPdf,
    "no-password-cert.p12",
    null,  // No password
    "Test Signature",
    "Location",
    "seal.png",
    true
);
```

### Example 6: Integration with External Certificate API

```java
// Typical workflow with third-party certificate provider
public class CertificateIntegration {
    
    public void signWithApiCertificate(File pdfFile) throws Exception {
        // 1. Get certificate from API
        ApiCertResponse response = certificateApi.getCertificate(userId);
        
        // 2. Load certificate from Base64
        String certPath = "api-cert.p12";
        CertificateGenerator.loadCertFromBase64(
            response.getP12Buf(),
            response.getCertPassword(),
            certPath
        );
        
        // 3. Generate signature image
        SignImageGenerator.generateSealImage(
            response.getSubjectName(),
            "signature.png",
            200,
            200
        );
        
        // 4. Sign PDF
        PdfSigner.signPdf(
            pdfFile,
            new File("signed-" + pdfFile.getName()),
            certPath,
            response.getCertPassword(),
            "API Certificate Signature",
            "Online Service",
            "signature.png",
            true
        );
    }
}
```

## 🏗️ Architecture

```
src/main/java/com/techhf/capdf/
├── cert/
│   └── CertificateGenerator.java    # X.509 certificate generation and loading
├── signer/
│   └── PdfSigner.java                # Core PDF signing with timestamp embedding
└── util/
    ├── SignatureVerifier.java        # Signature verification and timestamp extraction
    ├── SignImageGenerator.java       # Visual signature image generation
    └── SimplePdfCreator.java         # Test PDF document creation
```

## 🔧 Technical Details

### Technology Stack

| Library | Version | Purpose |
|---------|---------|---------|
| Apache PDFBox | 2.0.29 | PDF processing and visual signatures |
| BouncyCastle | 1.70 | Cryptography, certificates, and timestamps |
| SLF4J + Logback | 1.7.36 / 1.2.11 | Logging framework |

### Standards Compliance

- **RFC 3161**: Time-Stamp Protocol (TSP)
- **RFC 5652**: Cryptographic Message Syntax (CMS)
- **PKCS#7**: Cryptographic Message Syntax Standard
- **PKCS#12**: Personal Information Exchange Syntax
- **X.509**: Public Key Infrastructure Certificate

### RFC 3161 Timestamp Implementation

The library implements proper RFC 3161 timestamp embedding in CMS structures:

```java
// Timestamp is added as unsigned attribute in SignerInfo
SignerInfo
  ├── signedAttrs (content-type, message-digest, signing-time)
  ├── signature (RSA signature bytes)
  └── unsignedAttrs
      └── id-aa-signatureTimeStampToken (OID: 1.2.840.113549.1.9.16.2.14)
          └── SET
              └── ContentInfo (TimeStampToken)
                  └── SignedData (TSA signature)
```

**Key Implementation Points**:

1. TimeStampToken is generated by signing the signature hash
2. Embedded as unsigned attribute using OID `1.2.840.113549.1.9.16.2.14`
3. Proper ASN.1 structure: `SET -> ContentInfo` (not `SET -> SET -> ContentInfo`)
4. TSA certificate must have ExtendedKeyUsage with `id_kp_timeStamping` (critical)

### Certificate Requirements

For TSA certificates used in timestamp generation:

- **ExtendedKeyUsage**: Must be marked as critical
- **Key Purpose**: Only `id-kp-timeStamping` (1.3.6.1.5.5.7.3.8)
- **Basic Constraints**: CA = true for self-signed TSA
- **Key Usage**: digitalSignature, keyCertSign

## 📚 API Documentation

### CertificateGenerator

#### `generateSelfSignedCert`

```java
public static X509Certificate generateSelfSignedCert(
    String subject,      // X.500 DN (e.g., "CN=Org,O=Company,C=CN")
    String outputPath,   // Output path for .p12 file
    String password,     // Password (null for passwordless)
    int validYears       // Certificate validity in years
) throws Exception
```

#### `loadCertFromBase64`

```java
public static X509Certificate loadCertFromBase64(
    String p12Base64,    // Base64-encoded PKCS12 certificate
    String password,     // Certificate password (can be null)
    String outputPath    // Optional: save to file
) throws Exception
```

### PdfSigner

#### `signPdf`

```java
public static void signPdf(
    File inputPdf,          // Input PDF file
    File outputPdf,         // Output signed PDF file
    String certPath,        // Path to .p12 certificate
    String certPassword,    // Certificate password
    String reason,          // Signature reason
    String location,        // Signature location
    String signImagePath,   // Path to signature image
    boolean visible         // Whether signature is visible
) throws Exception
```

### SignImageGenerator

#### `generateSealImage`

```java
public static void generateSealImage(
    String orgName,      // Organization name
    String outputPath,   // Output image path
    int width,          // Image width
    int height          // Image height
) throws IOException
```

#### `generateTextSignImage`

```java
public static void generateTextSignImage(
    String signer,       // Signer name
    String location,     // Signature location
    String reason,       // Signature reason
    Date signTime,       // Signature timestamp
    String outputPath    // Output image path
) throws IOException
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Tests

```bash
# Seal signature test (includes no-password and empty-password tests)
mvn test -Dtest=SealSignTest

# Test individual methods
mvn test -Dtest=SealSignTest#testSealSign
mvn test -Dtest=SealSignTest#testNoPasswordCert
mvn test -Dtest=SealSignTest#testEmptyPasswordCert

# Text signature test
mvn test -Dtest=TextSignTest

# Base64 certificate loading test
mvn test -Dtest=Base64CertTest
```

### Generate Test Reports

```bash
# Run tests with coverage
mvn clean test

# Generate Javadoc
mvn javadoc:javadoc

# Package with sources and docs
mvn clean package
```

### Test Output Structure

```
test-output/
├── seal-sign/              # Seal signature test outputs
│   ├── test-cert.p12
│   ├── sign-seal.png
│   ├── test-seal-unsigned.pdf
│   └── test-seal-signed.pdf
├── text-sign/              # Text signature test outputs
├── no-password/            # No password certificate tests
├── empty-password/         # Empty password certificate tests
└── base64-cert/            # Base64 loading tests
```

All test classes in `src/test/java` serve as comprehensive examples.

## ⚠️ Production Considerations

### Current Limitations

1. **Self-Signed Timestamps**: Uses internal TSA, not commercially trusted
2. **Adobe Reader Compatibility**: May not display timestamp info with self-signed TSA
3. **Certificate Trust**: Self-signed certificates are not system-trusted

### Production Deployment Recommendations

#### Use Commercial TSA Services

Recommended providers:
- DigiCert TSA
- GlobalSign TSA
- Entrust TSA
- CFCA (China Financial Certification Authority)

#### Example: Integrate Commercial TSA

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
    
    // HTTP POST to commercial TSA
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

#### Certificate Management

- Use CA-issued certificates for production
- Store private keys in HSM (Hardware Security Module)
- Implement certificate rotation policy
- Regular security audits

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java code conventions
- Add unit tests for new features
- Update documentation
- Ensure all tests pass before submitting PR

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Apache PDFBox](https://pdfbox.apache.org/) - PDF manipulation library
- [Bouncy Castle](https://www.bouncycastle.org/) - Cryptography provider
- [RFC 3161](https://www.rfc-editor.org/rfc/rfc3161) - Timestamp Protocol specification

## 📊 Project Statistics

- **Core Code**: ~1,100 lines (excluding tests)
- **Test Coverage**: 80%+
- **Java Version**: 8+
- **Dependencies**: 4 core libraries

---

**Made with ❤️ for the Java community**
