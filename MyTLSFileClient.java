//William Malone
//ID: 1604564

// The client is usually much more straight forward
// Defaults will load Java’s set of Trusted Certificates
// Java will validate there is a path to a trusted CA
// By default, Java will NOT do hostname validation,
// but the more secure thing to do is to check!

// THE CODE BELOW IS INCOMPLETE AND HAS PROBLEMS
// FOR EXAMPLE, IT IS MISSING THE NECESSARY EXCEPTION HANDLING

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class MyTLSFileClient {

  // the hostname of the system to connect to,
  // the port it is listening on,
  // and the file to retrieve.
  public static void main(String args[]) {
    String serverHost = "lab-rg06-19";
    String serverHost2 = "lab-rg06-19.cms.waikato.ac.nz";
    String serverHost3 = "localhost";

    int serverPort = 42050;
    String filePath = "cat.jpg";

    try {
      System.out.println("1");
      SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket socket = (SSLSocket) factory.createSocket(serverHost, serverPort);

      // String hostname = args[0];
      // String port = args[1];
      // String file = args[2];

      // set HTTPS-style checking of HostName _before_
      // the handshake
      SSLParameters params = new SSLParameters();
      params.setEndpointIdentificationAlgorithm("HTTPS");
      socket.setSSLParameters(params);
      System.out.println("2");

      socket.startHandshake(); // explicitly starting the TLS handshake

      System.out.println("3");

      // at this point, can use getInputStream and
      // getOutputStream methods as you would in a regular Socket
      OutputStream outputStream = socket.getOutputStream();

      // Send file to server
      sendFile(filePath, outputStream);

      // get the X509Certificate for this session
      SSLSession session = socket.getSession();
      X509Certificate cert = (X509Certificate) session.getPeerCertificates()[0];

      // extract the CommonName, and then compare
      // getCommonName(cert);
    } catch (Exception e) {
      // TODO: handle exception
      System.out.println("An error occured " + e);
    }

  }

  public static String getCommonName(X509Certificate cert) {
    try {
      String name = cert.getSubjectX500Principal().getName();
      LdapName ln = new LdapName(name);
      String cn = null;

      // Rdn: Relative Distinguished Name
      for (Rdn rdn : ln.getRdns())
        if ("CN".equalsIgnoreCase(rdn.getType()))
          cn = rdn.getValue().toString();
      return cn;
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      return null;
    }

  }

  private static void sendFile(String filePath, OutputStream outputStream) throws IOException {
    File file = new File(filePath);
    byte[] buffer = new byte[8192];
    int bytesRead;

    try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file))) {
      while ((bytesRead = fileInputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }
  }
}
