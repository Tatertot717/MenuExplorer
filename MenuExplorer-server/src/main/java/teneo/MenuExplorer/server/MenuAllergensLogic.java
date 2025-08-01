package teneo.MenuExplorer.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import teneo.MenuExplorer.server.MenuSmartSearch.MenuItem;

/**
 * Service class responsible for loading, parsing, and querying allergen and
 * ingredient information from a structured JSON file. Supports smart search
 * capabilities via MenuSmartSearch.
 */
@Service
public class MenuAllergensLogic implements IAllergen {

	/**
	 * Root structure for parsed allergen data.
	 */
	public class MenuData {
		@SerializedName("sub_items")
		Map<String, SubItem> subItems;

		@SerializedName("products")
		List<Product> products;
	}

	/**
	 * Represents a sub-item with allergens and ingredients.
	 */
	public class SubItem {
		Map<String, Boolean> allergens;
		String ingredients;
	}

	/**
	 * Represents a product with allergens and associated sub-items.
	 */
	public class Product {
		String name;
		Map<String, Boolean> allergens;

		@SerializedName("sub_items")
		List<String> subItems;
	}

	private static final boolean LOAD_MSS = true;

	private final MenuData menuData;
	private final List<MenuItem> allergenMenu = new ArrayList<>();

	/**
	 * Loads allergen data from a JSON file and populates smart search index if
	 * enabled.
	 *
	 * @param allergensFile Path to the allergen JSON file (injected via @Value).
	 * @throws FileNotFoundException If the file path is invalid.
	 */
	public MenuAllergensLogic(@Value("${allergenPath}") String allergensFile) throws FileNotFoundException {
		FileReader reader = new FileReader(allergensFile);
		Gson gson = new Gson();
		Type type = new TypeToken<MenuData>() {
		}.getType();
		menuData = gson.fromJson(reader, type);

		if (MenuSmartSearch.searchEnabled()) {
			for (String product : menuData.subItems.keySet()) {
				MenuSmartSearch.addMenuItem(allergenMenu, product, -1);
			}
			for (Product product : menuData.products) {
				String name = product.name.toLowerCase().replaceAll("[^\\p{ASCII}]", " ");
				MenuSmartSearch.addMenuItem(allergenMenu, name, -1);
			}
		}
	}

	/**
	 * Searches for allergens using the smart search query.
	 *
	 * @param query The product or sub-item name.
	 * @return A string representation of allergen data.
	 */
	@Override
	public String searchAllergens(String query) {
		if (MenuSmartSearch.searchEnabled()) {
			MenuItem match = MenuSmartSearch.match(query, allergenMenu);
			if (match != null)
				return getAllergens(match.name).toString();
		}
		return null;
	}

	/**
	 * Searches for ingredients using the smart search query.
	 *
	 * @param query The sub-item name.
	 * @return A string representation of the ingredients.
	 */
	@Override
	public String searchIngredients(String query) {
		if (MenuSmartSearch.searchEnabled()) {
			MenuItem match = MenuSmartSearch.match(query, allergenMenu);
			if (match != null)
				return getIngredients(match.name);
		}
		return null;
	}

	/**
	 * Prints all allergen data for all products to the console. Used for debugging.
	 */
	public void printAllAllergens() {
		if (menuData != null && menuData.products != null) {
			for (Product product : menuData.products) {
				System.out.println("Product: " + product.name);
				System.out.println("Allergens:");
				product.allergens.forEach((allergen, present) -> System.out.println("  " + allergen + ": " + present));
				System.out.println();
			}
		}
	}

	/**
	 * Prints allergen and ingredient details for a given product or sub-item name.
	 *
	 * @param name Name of the product or sub-item.
	 */
	public void printAllergens(String name) {
		boolean found = false;

		// Search for product
		if (menuData != null && menuData.products != null) {
			for (Product product : menuData.products) {
				if (product.name.equalsIgnoreCase(name)) {
					found = true;
					System.out.println("Product: " + product.name);
					System.out.println("Main Allergens:");
					Map<String, Boolean> allergens = getAllergens(name);
					if (allergens != null) {
						allergens.forEach((a, v) -> System.out.println("  " + a + ": " + v));
					}

					if (product.subItems != null && menuData.subItems != null) {
						System.out.println("\nSub-item Allergens:");
						for (String subItemName : product.subItems) {
							SubItem subItem = menuData.subItems.get(subItemName);
							if (subItem != null) {
								System.out.println("  Sub-item: " + subItemName);
								subItem.allergens.forEach((a, v) -> System.out.println("    " + a + ": " + v));
							} else {
								System.out.println("  Sub-item: " + subItemName + " (not found)");
							}
						}
					}
					break;
				}
			}
		}

		// Search for sub-item
		if (!found && menuData != null && menuData.subItems != null) {
			SubItem subItem = menuData.subItems.get(name);
			if (subItem != null) {
				found = true;
				System.out.println("Sub-item: " + name);
				String ingredients = getIngredients(name);
				if (ingredients != null) {
					System.out.println("Ingredients: " + ingredients);
				}

				System.out.println("Allergens:");
				Map<String, Boolean> allergens = getAllergens(name);
				if (allergens != null) {
					allergens.forEach((a, v) -> System.out.println("  " + a + ": " + v));
				}
			}
		}

		if (!found) {
			System.out.println("'" + name + "' not found as a product or sub-item.");
		}
	}

	/**
	 * Retrieves the allergen information for a given product or sub-item.
	 *
	 * @param name Name of the product or sub-item.
	 * @return Map of allergen names to boolean values indicating presence.
	 */
	@Override
	public Map<String, Boolean> getAllergens(String name) {
		if (menuData != null && menuData.products != null) {
			for (Product product : menuData.products) {
				if (product.name.equalsIgnoreCase(name)) {
					return product.allergens;
				}
			}
		}

		if (menuData != null && menuData.subItems != null) {
			SubItem subItem = menuData.subItems.get(name);
			if (subItem != null) {
				return subItem.allergens;
			}
		}

		return null;
	}

	/**
	 * Retrieves the ingredient list for a given sub-item.
	 *
	 * @param name Name of the sub-item.
	 * @return A string of ingredients, or null if not found.
	 */
	@Override
	public String getIngredients(String name) {
		if (menuData != null && menuData.subItems != null) {
			SubItem subItem = menuData.subItems.get(name);
			if (subItem != null) {
				return subItem.ingredients;
			}
		}
		return null;
	}

	/**
	 * Main method for standalone testing of the allergen logic and smart search.
	 *
	 * @param args Command-line arguments (not used).
	 */
	public static void main(String[] args) {
		try {
			if (LOAD_MSS) {
				System.out.println("Loading model...");
				Instant start = Instant.now();
				MenuSmartSearch.loadModel("./glove.2024.wikigiga.100d.zip");
				Instant end = Instant.now();
				long secondsElapsed = Duration.between(start, end).getSeconds();
				System.out.println("Finished loading model in: " + secondsElapsed + " seconds");
			}

			IMenu explorer = new MenuExplorerLogic("./maxmenu.json");
			IAllergen allergens = new MenuAllergensLogic("./allergens.json");

			System.out.println(allergens.getAllergens("Brioche bread").toString());
			System.out.println();
			System.out.println(allergens.getIngredients("Brioche bread"));
			System.out.println();

			ArrayList<Integer> fakeOrder = new ArrayList<>();
			fakeOrder.add(14580);
			System.out.println(allergens.searchAllergens(explorer.getOrderTitle(fakeOrder)));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}