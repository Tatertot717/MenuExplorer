package teneo.MenuExplorer.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import teneo.MenuExplorer.server.MenuSmartSearch.MenuItem;
import teneo.MenuExplorer.shared.IAllergen;
import teneo.MenuExplorer.shared.IMenu;

public class MenuAllergensLogic implements IAllergen {

	public class MenuData {
		@SerializedName("sub_items")
		Map<String, SubItem> subItems;

		@SerializedName("products")
		List<Product> products;
	}

	public class SubItem {
		Map<String, Boolean> allergens;
		String ingredients;
	}

	public class Product {
		String name;
		Map<String, Boolean> allergens;
		@SerializedName("sub_items")
		List<String> subItems;
	}

	private static final boolean LOAD_MSS = true;

	private final MenuData menuData;
	private final List<MenuItem> allergenMenu = new ArrayList<>();


	public MenuAllergensLogic(String allergensFile) throws FileNotFoundException {
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
				MenuSmartSearch.addMenuItem(allergenMenu, product.name, -1);
			}
		}
	}
	
	@Override
	public String searchAllergens(String query) {
		if (MenuSmartSearch.searchEnabled()) {
			return getAllergens(MenuSmartSearch.match(query, allergenMenu).name).toString();
		}
		
		return null;
	}

	//print allergens for all products, debug
	@Override
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

	@Override
	public void printAllergens(String name) {
		boolean found = false;

		// Try to find product
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

		// Try to find sub-item
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

	@Override
	public Map<String, Boolean> getAllergens(String name) {
		// Check for product allergens
		if (menuData != null && menuData.products != null) {
			for (Product product : menuData.products) {
				if (product.name.equalsIgnoreCase(name)) {
					return product.allergens;
				}
			}
		}

		// Check for sub-item allergens
		if (menuData != null && menuData.subItems != null) {
			SubItem subItem = menuData.subItems.get(name);
			if (subItem != null) {
				return subItem.allergens;
			}
		}

		return null; // Not found
	}

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

	//testing
	public static void main(String[] args) {
		try {
			if (LOAD_MSS) {
				System.out.println("Loading model...");
				Instant start = Instant.now();
				MenuSmartSearch.loadModel("./glove.2024.wikigiga.100d.zip"); // about 1gb in size, uncompressed
				Instant end = Instant.now();

				long secondsElapsed = Duration.between(start, end).getSeconds();
				System.out.println("Finished loading model in: " + secondsElapsed + " seconds");
					}
			
			IMenu explorer = new MenuExplorerLogic("./maxmenu.json");
			explorer.setNewMenuAllergens("./allergens.json");
			IAllergen allergens = explorer.getMenuAllergens();
			//MenuAllergens allergens = new MenuAllergens("./allergens.json");

			// allergens.printAllAllergens();
			//allergens.printAllergens("Brioche bread");
			System.out.println(allergens.getAllergens("Brioche bread").toString());
			System.out.println();
			System.out.println(allergens.getIngredients("Brioche bread"));
			System.out.println();
			ArrayList<Integer> fakeOrder = new ArrayList<Integer>();
			fakeOrder.add(14580);
			System.out.println(explorer.getMenuAllergens().searchAllergens(explorer.getOrderTitle(fakeOrder)));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
	}
}
