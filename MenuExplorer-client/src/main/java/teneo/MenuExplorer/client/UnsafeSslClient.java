package teneo.MenuExplorer.client;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import feign.Client;

/**
 * WARNING: DISABLE BEFORE PROD!!! This client disables SSL certificate
 * validation and hostname verification, effectively trusting all certificates
 * and hosts.
 * 
 * This is highly insecure and should ONLY be used for testing or development
 * purposes.
 * 
 * **DO NOT USE THIS IN PRODUCTION** as it exposes your application to
 * man-in-the-middle attacks and other security vulnerabilities.
 * 
 * Remove this implementation before deploying to any production environment.
 */
public class UnsafeSslClient {

	public static Client create() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

		HostnameVerifier allowAllHosts = (hostname, session) -> true;

		return new Client.Default(sslContext.getSocketFactory(), allowAllHosts);
	}
}
