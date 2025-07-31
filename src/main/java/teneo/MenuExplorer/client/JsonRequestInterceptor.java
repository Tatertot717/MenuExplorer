package teneo.MenuExplorer.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class JsonRequestInterceptor implements RequestInterceptor {
	@Override
	public void apply(RequestTemplate template) {
		// Set Content-Type only for POST/PUT with body
		if (template.method().equalsIgnoreCase("POST") || template.method().equalsIgnoreCase("PUT")) {
			if (!template.headers().containsKey("Content-Type")) {
				template.header("Content-Type", "application/json");
			}
		}
	}
}
