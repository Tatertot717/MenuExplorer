package teneo.MenuExplorer.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contract which states how the allergens api is to be used by the client. See {@link Allergens} for function reference.
 */
@FeignClient(name = "allergenapi", url = "${menu.api.url}")
public interface FAllergens {

	@GetMapping("/api/allergen/searchAllergens")
	String searchAllergens(@RequestParam(value = "query") String query);

	@GetMapping("/api/allergen/getAllergens")
	Map<String, Boolean> getAllergens(@RequestParam(value = "name") String name);

	@GetMapping("/api/allergen/getIngredients")
	String getIngredients(@RequestParam(value = "name") String name);

	@GetMapping("/api/allergen/searchIngredients")
	String searchIngredients(@RequestParam(value = "query") String query);
}
