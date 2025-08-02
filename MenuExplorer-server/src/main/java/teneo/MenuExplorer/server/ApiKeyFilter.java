package teneo.MenuExplorer.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * API key filter that validates incoming requests using a list of keys defined
 * in a file named 'apikeys.txt' located in the same directory as the
 * application jar.
 *
 * <p>
 * This filter intercepts all HTTP requests and checks the presence and validity
 * of the 'x-api-key' header. If the API key is missing or invalid, the request
 * is rejected with a 401 Unauthorized status.
 * </p>
 *
 * <p>
 * It is automatically registered and applied to all endpoints via the embedded
 * {@link FilterRegistrationBean} bean.
 * </p>
 *
 * <p>
 * Example 'apikeys.txt' file format:
 * </p>
 * 
 * <pre>
 * abc123
 * key456
 * my-secret-key
 * </pre>
 */
@Configuration
public class ApiKeyFilter extends OncePerRequestFilter {

	private final Set<String> validApiKeys = new HashSet<>();

	/**
	 * Constructs the API key filter and loads valid API keys from 'apikeys.txt'.
	 *
	 * <p>
	 * If the file does not exist or cannot be read, no keys will be loaded, and all
	 * requests will be rejected unless the file is later fixed.
	 * </p>
	 *
	 * @param env the Spring environment to get configuration properties, like the
	 *            API keys file path
	 */
	public ApiKeyFilter(Environment env) {
		String keyFilePath = env.getProperty("apiKeys", "./apikeys.txt"); // fallback default
		File file = Paths.get(keyFilePath).toFile();
		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String trimmed = line.trim();
					if (!trimmed.isEmpty())
						validApiKeys.add(trimmed);
				}
				System.out.println("Loaded " + validApiKeys.size() + " API keys from " + keyFilePath);
			} catch (IOException e) {
				System.out.println("Error reading API keys from " + keyFilePath + ": " + e.getMessage());
			}
		} else {
			System.out.println("API key file not found at: " + keyFilePath);
		}
	}

	/**
	 * Validates each HTTP request by checking the 'x-api-key' header.
	 *
	 * @param request     the incoming HTTP request
	 * @param response    the HTTP response
	 * @param filterChain the filter chain to continue processing if authorized
	 * @throws ServletException in case of a servlet error
	 * @throws IOException      in case of I/O errors
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String apiKey = request.getHeader("x-api-key");
		if (apiKey == null || !validApiKeys.contains(apiKey)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Missing or invalid API key");
			return;
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Registers this filter with the Spring Boot filter chain and applies it to all
	 * routes.
	 *
	 * @return a configured {@link FilterRegistrationBean} for this filter
	 */
	@Bean
	public FilterRegistrationBean<ApiKeyFilter> registerFilter() {
		FilterRegistrationBean<ApiKeyFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(this);
		registrationBean.addUrlPatterns("/*"); // apply to all endpoints
		registrationBean.setOrder(1); // priority order
		return registrationBean;
	}
}