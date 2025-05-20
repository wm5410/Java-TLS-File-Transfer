# Java TLS File Transfer - Secure File Request & Delivery

## Overview

This project implements a secure file transfer system in Java using Transport Layer Security (TLS). It includes:

1. **Certificate Authority setup** (Step 1): Create your own CA key and certificate.
2. **Server certificate setup** (Step 2): Generate and sign a server keypair with your CA.
3. **TLS server implementation** (Step 3): A Java `MyTLSFileServer` that uses `SSLServerSocket` to authenticate and serve files.
4. **TLS client implementation** (Step 4): A Java `MyTLSFileClient` that establishes a secure connection, validates the server certificate, and handles hostname verification.
5. **File transfer** (Step 5): Client requests a filename; server sends file contents over the TLS channel.

## Requirements

- Java Development Kit (JDK)
- OpenSSL (for certificate and key management)
- Linux or equivalent command-line environment
- Terminal tools: `openssl`, `keytool`, `javac`, `java`

## Files Produced

- **Certificates & Keystores**  
  - `ca-private.pem`      : CA’s private key (passphrase protected)  
  - `ca-cert.pem`         : CA’s self‑signed public certificate  
  - `ca-cert.jks`         : Java KeyStore containing the CA certificate  
  - `server.jks`          : Java KeyStore containing server private key and signed cert  
  - `server.csr`          : Certificate Signing Request for the server  
  - `server-cert.pem`     : Server’s signed public certificate  

- **Java Source**  
  - `MyTLSFileServer.java`: TLS-enabled server  
  - `MyTLSFileClient.java`: TLS-enabled client  

- **Configuration & Notes**  
  - `notes.txt`           : Stores passphrases, keystore passwords, and recorded exceptions  

## Compilation

```bash
javac MyTLSFileServer.java
javac MyTLSFileClient.java
```

## Usage

### 1. Prepare CA and Server Keystores

Follow steps 1 & 2 in the assignment spec to generate:

```bash
# Step 1: Create CA
openssl req -new -x509 -keyout ca-private.pem -out ca-cert.pem -days 3650
keytool -import -trustcacerts -alias root -file ca-cert.pem -keystore ca-cert.jks

# Step 2: Generate server keypair and CSR
keytool -genkeypair -alias <server-hostname> -keyalg RSA -keystore server.jks
keytool -certreq -alias <server-hostname> -file server.csr -keystore server.jks

# Sign CSR with CA
openssl x509 -req -in server.csr -CA ca-cert.pem -CAkey ca-private.pem -CAcreateserial -out server-cert.pem -days 90

# Import CA and server cert into server keystore
keytool -import -trustcacerts -alias root -file ca-cert.pem -keystore server.jks
keytool -import -alias <server-hostname> -file server-cert.pem -keystore server.jks
```

Record all passphrases, keystore passwords, and certificate fingerprints in `notes.txt`.

### 2. Start the TLS Server

```bash
java MyTLSFileServer <port> <server.jks> <keystore-password>
```

Example:
```bash
java MyTLSFileServer 40202 server.jks freaks
Listening on port 40202 (TLS enabled)
```

### 3. Run the TLS Client

```bash
java -Djavax.net.ssl.trustStore=ca-cert.jks      -Djavax.net.ssl.trustStorePassword=<ca-keystore-password>      MyTLSFileClient <hostname> <port> <filename>
```

Examples:
```bash
# Without hostname verification (initial test)
java -Djavax.net.ssl.trustStore=ca-cert.jks -Djavax.net.ssl.trustStorePassword=yesyes      MyTLSFileClient localhost 40202 cat.png

# With hostname verification enabled
java -Djavax.net.ssl.trustStore=ca-cert.jks -Djavax.net.ssl.trustStorePassword=yesyes      MyTLSFileClient lab-rg06-01.cms.waikato.ac.nz 40202 cat.png
```

The client will save the retrieved file as `_cat.png` in the current directory.

## Notes

- TLS handshake exceptions (e.g. `SSLHandshakeException`) should be recorded in `notes.txt`.
- Hostname verification errors (e.g. `No name matching ... found`) should be recorded in `notes.txt`.
- Ensure your CA and server certificates share the same SHA‑256 fingerprint.
- Use `openssl s_client -connect <host>:<port> -CAfile ca-cert.pem` to debug TLS handshakes.
