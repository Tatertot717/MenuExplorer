package teneo.MenuExplorer.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import feign.Response;
import feign.codec.Decoder;
import feign.gson.GsonDecoder;

/**
 * If the response type is {@code String}, the raw response body is read and
 * returned as a string. For all other types, it delegates decoding to a
 * {@link GsonDecoder}. This is necessary because GSON cannot deserialize raw
 * string values returned by the server.
 */
public class StringDecoder implements Decoder {
	private final GsonDecoder gsonDecoder = new GsonDecoder();

	@Override
	public Object decode(Response response, java.lang.reflect.Type type) throws IOException {
		if (type.equals(String.class)) {
			// Read raw body as string
			return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
		}
		// For all other types, fallback to GsonDecoder
		return gsonDecoder.decode(response, type);
	}
}
