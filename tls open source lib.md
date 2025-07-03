
##  Tls Open Source

### 1.OpenSLL ,全名：Open Secure Sockets Layer。C语言

[OpenSLL官网](https://www.openssl.org/) ，[Github仓库](https://github.com/openssl/openssl) 。开源代码采用C语言实现。

OpenSSL整个软件包大概可以分成三个主要的功能部分：SSL协议库、应用程序以及密码算法库。OpenSSL的目录结构自然也是围绕这三个功能部分进行规划的。OpenSSL是用的最广泛的开源项目，已经形成了事实上的加密TLS的行业标准。


###  2.OpenJSSE (Only TLS1.3 ，Java 8)

OpenJSSE: A JSSE provider that supports TLS 1.3 on Java SE 8

Java执行环境供应商Azul Systems在OSCON 2019大会宣布释出OpenJSSE项目，这是一个为Java SE 8开发的TLS 1.3开源实作，开发者可以在现有的Java 8应用程序中，增加对TLS 1.3的支持，这提供了一种可程式化的方法，以弥补Java SE 8 API缺乏的TLS 1.3与RSASSA-PSS功能。
[Azul官网(OpenJSSE开源说明)]( https://www.azul.com/newsroom/azul-systems-brings-updated-transport-layer-security-to-java-se-8/)， [ Github仓库](https://github.com/openjsse/openjsse)

### 3. Bouncy Castle，Java and C#


[Bouncy Castle官网](https://www.bouncycastle.org/)，[Github仓库](https://github.com/bcgit)

Bouncy Castle 就是Java(Android)开发中经常遇到大名鼎鼎的BC库。

BC库是提供TLS中相关的一系列了的API，包括跟个版本SSL/TLS/DTLS，X.509 CA证书

```
The Bouncy Castle APIs currently consist of the following:
A lightweight cryptography API for Java and C#.
A provider for the Java Cryptography Extension (JCE) and the Java Cryptography Architecture (JCA).
A provider for the Java Secure Socket Extension (JSSE).
A clean room implementation of the JCE 1.2.1.

```

### 4.CyaSSL /  WolfSSL  嵌入式SSL / TLS库 C python

CyaSSL

wolfSSL是一款开源的商用级嵌入式安全软件(已经支持tls1.3)

wolfSSL is a lightweight C-language-based SSL/TLS library targeted for embedded, RTOS, or resource-constrained environments primarily because of its small size, speed, and portability. wolfSSL supports industry standards up to the current TLS 1.3 and DTLS 1.2 levels, is up to 20 times smaller than OpenSSL, offers a simple API, an OpenSSL compatibility layer, OCSP and CRL support, and offers several progressive ciphers.


[WolfSSL官网]( https://www.wolfssl.com),[Github仓库](https://github.com/wolfSSL)


### 5.CyaSSL嵌入式

CyaSSL是一个双协议SSL，适用于嵌入式设备，产品支持兼容OpenSSL的多个API方法。


[Github仓库]( https://github.com/cyassl/cyassl)
https://www.wolfssl.com