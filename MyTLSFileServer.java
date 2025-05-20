//William Malone
//ID: 1604564

// Java provides SSLSocket and SSLServerSocket classes, which are roughly 
// equivalent to Socket and ServerSocket:
//       SSLServerSocket listens on a port for incoming connections, like ServerSocket
//       SSLSocket connects to an SSLServerSocket, like Socket, and represents an individual 
//       connection accepted from an SSLServerSocket.
// To create a SSLSocket or SSLServerSocket, we must use "factories"

// Socket factories are a convenient way to set TLS parameters that will 
// apply to Sockets created from the factory, e.g:
//       Which TLS versions to support
//       Which Ciphers and Hashes to use
//       Which Keys to use and which Certificates to trust
// As you might guess by the names
//       SSLServerSocketFactory creates SSLServerSocket objects
//       SSLSocketFactory creates SSLSocket objects

// Java uses KeyStore objects to store Keys and Certificates
// A KeyStore object is used when encrypting and authenticating
// The files that contain Keys and Certificates are password protected

// THE CODE BELOW IS INCOMPLETE AND HAS PROBLEMS
// FOR EXAMPLE, IT IS MISSING THE NECESSARY EXCEPTION HANDLING

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.Arrays;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class MyTLSFileServer {

   // Variables
   private static int portNum = 42050;

   private static ServerSocketFactory getSSF() {
      try {
         // Get
         // an SSL Context that speaks some version of TLS,
         // a KeyManager that can hold certs in X.509 format,
         // and a JavaKeyStore (JKS) instance
         SSLContext ctx = SSLContext.getInstance("TLS");
         KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
         KeyStore ks = KeyStore.getInstance("JKS");

         // Store the passphrase to unlock the JKS file.
         char[] passwd = System.console().readPassword("Please gimme a password");

         // char[] passphrase = "freaks".toCharArray();

         // Load the keystore file. The passphrase is
         // an optional parameter to allow for integrity
         // checking of the keystore. Could be null
         ks.load(new FileInputStream("server.jks"), passwd);

         // Init the KeyManagerFactory with a source
         // of key material. The passphrase is necessary
         // to unlock the private key contained.
         kmf.init(ks, passwd);

         // Get rid of password for security
         Arrays.fill(passwd, '\0');

         // initialise the SSL context with the keys.
         ctx.init(kmf.getKeyManagers(), null, null);

         // Get the factory we will use to create
         // our SSLServerSocket
         SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
         return ssf;

      } catch (Exception e) {
         System.out.println("An error occured " + e);
         return null;
      }
   }

   public static void main(String args[]) {
      try {
         // use the getSSF method to get a SSLServerSocketFactory and
         // create our SSLServerSocket, bound to specified port
         ServerSocketFactory ssf = getSSF();
         SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(portNum);
         System.out.println(ss.getLocalPort() + " " + ss.getInetAddress());
         String EnabledProtocols[] = { "TLSv1.2", "TLSv1.3" };
         ss.setEnabledProtocols(EnabledProtocols);
         System.out.println("Server Running... ");
         SSLSocket s = (SSLSocket) ss.accept();

         // reader realine
         BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
         read.readLine();
      } catch (Exception e) {
         System.out.println("An error occured " + e);
      }
   }
}
