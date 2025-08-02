package teneo.MenuExplorer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Interceptor that adds an API key header to outgoing HTTP requests.
 */
@Component
public class ApiKeyInterceptor implements RequestInterceptor {

	/**
	 * The API key to be added to the request headers.
	 */
	private final String apiKey;

	/**
	 * Constructs an ApiKeyInterceptor with the specified API key.
	 *
	 * @param apiKey the API key injected from application properties; must not be
	 *               null or empty
	 * @throws IllegalArgumentException if the provided API key is null or empty
	 */
	public ApiKeyInterceptor(@Value("${apiKey}") String apiKey) {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			throw new IllegalArgumentException("API key must not be null or empty");
		}
		this.apiKey = apiKey.trim();
	}

	/**
	 * Applies the API key header to the outgoing request.
	 *
	 * @param template the request template to which the API key header will be
	 *                 added
	 */
	@Override
	public void apply(RequestTemplate template) {
		template.header("x-api-key", apiKey);
	}
}