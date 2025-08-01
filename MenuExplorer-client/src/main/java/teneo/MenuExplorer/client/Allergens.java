package teneo.MenuExplorer.client;

import java.util.Map;

import org.springframework.cloud.openfeign.support.SpringMvcContract;

import feign.Feign;
import feign.gson.GsonEncoder;

/**
 * Client wrapper class for communicating with the allergen service using Feign.
 * Provides methods to query allergen and ingredient data remotely.
 */
public class Allergens {

	private final FAllergens allergens;

	/**
	 * Constructs a new Allergens client that connects to the given base URL. Sets
	 * up Feign with custom encoders, decoders, and interceptors.
	 *
	 * @param baseUrl The base URL of the remote allergen service.
	 */
	public Allergens(String baseUrl) {
		allergens = Feign.builder().contract(new SpringMvcContract()).encoder(new GsonEncoder())
				.decoder(new StringDecoder()).requestInterceptor(new JsonRequestInterceptor())
				.target(FAllergens.class, baseUrl);
	}

	/**
	 * Searches for allergens using a free-text query.
	 *
	 * @param query A product or sub-item name.
	 * @return A string representation of matched allergen data.
	 */
	public String searchAllergens(String query) {
		return allergens.searchAllergens(query);
	}

	/**
	 * Retrieves the allergen map for a given product or sub-item name.
	 *
	 * @param name Name of the product or sub-item.
	 * @return Map of allergens (name to boolean value).
	 */
	public Map<String, Boolean> getAllergens(String name) {
		return allergens.getAllergens(name);
	}

	/**
	 * Retrieves the list of ingredients for a given sub-item name.
	 *
	 * @param name Name of the sub-item.
	 * @return String representation of ingredients.
	 */
	public String getIngredients(String name) {
		return allergens.getIngredients(name);
	}

	/**
	 * Searches for ingredients using a free-text query.
	 * 
	 * @param query A sub-item name.
	 * @return A string representation of matched ingredient data.
	 * 
	 */
	public String searchIngredients(String query) {
		return allergens.searchIngredients(query);
	}
}
