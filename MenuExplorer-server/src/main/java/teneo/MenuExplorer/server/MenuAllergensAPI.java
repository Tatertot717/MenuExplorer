package teneo.MenuExplorer.server;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that provides endpoints for retrieving allergen and
 * ingredient information for menu items.
 * <p>
 * This API allows clients to search for allergens, retrieve allergen mappings
 * for specific items, and get ingredient lists. It delegates all functionality
 * to the shared {@link IAllergen} service, and implemented with
 * {@link MenuAllergensLogic}.
 * <p>
 * All endpoints are prefixed with {@code /api/getMenuAllergens}.
 */
@RestController
@RequestMapping("/api/allergen") // <-- Updated to match client
public class MenuAllergensAPI {

	private final IAllergen allergenService;

	@Autowired
	public MenuAllergensAPI(IAllergen allergenService) {
		this.allergenService = allergenService;
	}

	@GetMapping("/searchAllergens")
	public String searchAllergens(@RequestParam String query) {
		return allergenService.searchAllergens(query);
	}

	@GetMapping("/getAllergens")
	public Map<String, Boolean> getAllergens(@RequestParam String name) {
		return allergenService.getAllergens(name);
	}

	@GetMapping("/getIngredients")
	public String getIngredients(@RequestParam String name) {
		return allergenService.getIngredients(name);
	}

	@GetMapping("/searchIngredients")
	public String searchIngredients(@RequestParam String query) {
		return allergenService.searchIngredients(query);
	}
}