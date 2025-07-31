package teneo.MenuExplorer.client;
import feign.codec.Decoder;
import feign.gson.GsonDecoder;
import feign.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
