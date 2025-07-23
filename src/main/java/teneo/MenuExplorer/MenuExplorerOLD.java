package teneo.MenuExplorer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class MenuExplorerOLD {

	private static class CartEntry {
		List<JsonObject> selectedObjects;
		int totalPrice;

		CartEntry(List<JsonObject> selectedObjects, int totalPrice) {
			this.selectedObjects = selectedObjects;
			this.totalPrice = totalPrice;
		}
	}

	private final Deque<JsonObject> pendingConfigs = new ArrayDeque<>();
	private List<JsonObject> currentSelection = new ArrayList<>();
	public boolean orderingInProgress = false;
	private final Map<String, JsonObject> refs = new HashMap<>();
	private final List<JsonObject> products = new ArrayList<>();
	private final List<CartEntry> cart = new ArrayList<>();
	private final Scanner scanner;

	public MenuExplorerOLD() {
		this.scanner = new Scanner(System.in);
	}

	public void loadJsonData(String filePath) throws FileNotFoundException {
		Gson gson = new Gson();
		JsonObject root = gson.fromJson(new FileReader(filePath), JsonObject.class);

		Type mapType = new TypeToken<Map<String, JsonObject>>() {}.getType();
		Type listType = new TypeToken<List<JsonObject>>() {}.getType();

		refs.clear();
		refs.putAll(gson.fromJson(root.getAsJsonObject("Refs"), mapType));

		products.clear();
		products.addAll(gson.fromJson(root.getAsJsonArray("Products"), listType));
	}

	public JsonObject getProductById(int id) {
		for (JsonObject product : products) {
			if (product.has("Id") && product.get("Id").getAsInt() == id) {
				return product;
			}
		}
		return null;
	}

	public void startDebugOrdering() {
		while (true) {
			System.out.print("Enter a Product or Ref ID to order (or 'exit'): ");
			String input = scanner.nextLine().trim();

			if (input.equalsIgnoreCase("exit")) break;

			if (!input.matches("\\d+")) {
				System.out.println("Please enter a numeric ID.");
				continue;
			}

			int refId = Integer.parseInt(input);
			JsonObject product = getProductById(refId);

			if (product == null) {
				System.out.println("Product not found.");
				continue;
			}

			System.out.println("\nStarting order for Product ID: " + refId);
			List<JsonObject> selectedObjects = processProduct(product);
			int totalPrice = calculateTotalPrice(selectedObjects);

			displaySelectedItems(selectedObjects);
			cart.add(new CartEntry(selectedObjects, totalPrice));
			displayCartSummary();
		}
	}

	public List<CartEntry> getCart() {
		return cart;
	}

	private void displaySelectedItems(List<JsonObject> selectedObjects) {
		System.out.println("\nYour selected items:");
		for (JsonObject obj : selectedObjects) {
			int id = obj.get("Id").getAsInt();
			String title = obj.has("Title") ? obj.get("Title").getAsString() : "No title";
			int price = obj.has("Price") ? obj.get("Price").getAsInt() : 0;
			System.out.printf("- %s (ID: %d, Price: %d)%n", title, id, price);
		}
	}

	private void displayCartSummary() {
		if (cart.isEmpty()) return;

		System.out.println("\n======= CART SUMMARY =======");
		int grandTotal = 0;
		int count = 1;

		for (CartEntry entry : cart) {
			System.out.printf("\nItem #%d:\n", count++);
			for (JsonObject obj : entry.selectedObjects) {
				int id = obj.get("Id").getAsInt();
				String title = obj.has("Title") ? obj.get("Title").getAsString() : "No title";
				int price = obj.has("Price") ? obj.get("Price").getAsInt() : 0;
				System.out.printf("- %s (ID: %d, Price: %d)%n", title, id, price);
			}
			System.out.println("Subtotal: " + entry.totalPrice);
			grandTotal += entry.totalPrice;
		}

		System.out.println("\nTotal Price for all items: " + grandTotal);
		System.out.println("============================");
	}

	private List<JsonObject> processProduct(JsonObject product) {
		List<JsonObject> selectedObjects = new ArrayList<>();

		String productIdStr = String.valueOf(product.get("Id").getAsInt());
		if (refs.containsKey(productIdStr)) {
			selectedObjects.add(refs.get(productIdStr));
		}

		if (product.has("Items")) {
			for (JsonElement item : product.getAsJsonArray("Items")) {
				selectedObjects.addAll(handleConfigurable(item.getAsJsonObject()));
			}
		}

		if (product.has("Configurables")) {
			for (JsonElement config : product.getAsJsonArray("Configurables")) {
				selectedObjects.addAll(handleConfigurable(config.getAsJsonObject()));
			}
		}

		return selectedObjects;
	}

	private List<JsonObject> handleConfigurable(JsonObject config) {
		List<JsonObject> selectedObjects = new ArrayList<>();

		if (config.has("Choices")) {
			String title = config.has("Title") ? config.get("Title").getAsString() : "Unnamed Configurable";
			System.out.println("\n--- Configuring option: " + title + " ---");
			selectedObjects.addAll(selectFromChoices(config.getAsJsonArray("Choices")));
		}

		if (config.has("Items")) {
			for (JsonElement item : config.getAsJsonArray("Items")) {
				selectedObjects.addAll(handleConfigurable(item.getAsJsonObject()));
			}
		}

		if (config.has("Configurables")) {
			for (JsonElement sub : config.getAsJsonArray("Configurables")) {
				selectedObjects.addAll(handleConfigurable(sub.getAsJsonObject()));
			}
		}

		return selectedObjects;
	}

	private List<JsonObject> selectFromChoices(JsonArray choices) {
		List<JsonObject> selectedObjects = new ArrayList<>();

		for (int i = 0; i < choices.size(); i++) {
			JsonObject choice = choices.get(i).getAsJsonObject();
			String refId = choice.get("Id").getAsString();
			JsonObject ref = refs.get(refId);

			String title = ref != null && ref.has("Title") ? ref.get("Title").getAsString() : "No title";

			int price = choice.has("Price")
					? choice.get("Price").getAsInt()
							: (ref != null && ref.has("Price") ? ref.get("Price").getAsInt() : 0);

			System.out.printf("%d. %s (ID: %s, Price: %d)%n", i + 1, title, refId, price);
		}

		while (true) {
			System.out.print("Select an option by number (or 'skip'): ");
			String input = scanner.nextLine().trim().toLowerCase();

			if (input.equals("skip")) return Collections.emptyList();
			if (!input.matches("\\d+")) {
				System.out.println("Invalid input. Please enter a number.");
				continue;
			}

			int index = Integer.parseInt(input) - 1;
			if (index < 0 || index >= choices.size()) {
				System.out.println("Invalid selection. Try again.");
				continue;
			}

			JsonObject choice = choices.get(index).getAsJsonObject();
			String refId = choice.get("Id").getAsString();
			JsonObject ref = refs.get(refId);

			JsonObject merged = new JsonObject();
			if (ref != null) {
				for (Map.Entry<String, JsonElement> entry : ref.entrySet()) {
					merged.add(entry.getKey(), entry.getValue());
				}
			}
			if (choice.has("Price")) {
				merged.add("Price", choice.get("Price"));
			}
			if (!merged.has("Title")) {
				merged.addProperty("Title", "No title");
			}

			selectedObjects.add(merged);
			selectedObjects.addAll(handleConfigurable(choice));
			return selectedObjects;
		}
	}

	private int calculateTotalPrice(List<JsonObject> selectedObjects) {
		int total = 0;
		for (JsonObject obj : selectedObjects) {
			if (obj.has("Price")) {
				total += obj.get("Price").getAsInt();
			}
		}
		return total;
	}

	public CartEntry getCartItem(int index) {
		if (index < 0 || index >= cart.size()) {
			throw new IndexOutOfBoundsException("Cart index out of bounds.");
		}
		return cart.get(index);
	}

	public String idToTitle(String id) {
		JsonObject ref = refs.get(id);
		if (ref != null && ref.has("Title")) {
			return ref.get("Title").getAsString();
		}
		return "No Title";
	}

	public String idToTitle(int id) {
		return idToTitle(String.valueOf(id));
	}
	
	public String idToDesc(String id) {
		JsonObject ref = refs.get(id);
		if (ref != null && ref.has("Description")) {
			return ref.get("Description").getAsString();
		}
		return "No description available.";
	}

	public String idToDesc(int id) {
		return idToDesc(String.valueOf(id));
	}

	public int getCartItemPrice(int index) {
		return getCartItem(index).totalPrice;
	}

	public int getCartTotalPrice() {
		int total = 0;
		for (CartEntry entry : cart) {
			total += entry.totalPrice;
		}
		return total;
	}

	public List<String> startOrder(int productId) {
		if (orderingInProgress) throw new IllegalStateException("Finish the current order first.");

		JsonObject product = getProductById(productId);
		if (product == null) throw new IllegalArgumentException("Product not found.");

		currentSelection.clear();
		pendingConfigs.clear();
		orderingInProgress = true;

		String productIdStr = String.valueOf(product.get("Id").getAsInt());
		if (refs.containsKey(productIdStr)) {
			currentSelection.add(refs.get(productIdStr));
		}

		if (product.has("Configurables")) {
			for (JsonElement elem : product.getAsJsonArray("Configurables")) {
				pendingConfigs.addLast(elem.getAsJsonObject());
			}
		}
		if (product.has("Items")) {
			for (JsonElement elem : product.getAsJsonArray("Items")) {
				pendingConfigs.addLast(elem.getAsJsonObject());
			}
		}

		return getNextChoiceIds();
	}


	public List<String> submitChoice(int choiceId) { 
		return submitChoice(String.valueOf(choiceId));
	}
	public List<String> submitChoice(String choiceId) {
		if (!orderingInProgress) throw new IllegalStateException("No active order.");

		JsonObject currentConfig = pendingConfigs.pollFirst();
		if (currentConfig == null || !currentConfig.has("Choices")) {
			throw new IllegalStateException("No choices expected at this point.");
		}

		// Validate choice
		JsonArray choices = currentConfig.getAsJsonArray("Choices");
		JsonObject selectedChoice = null;

		for (JsonElement elem : choices) {
			JsonObject choice = elem.getAsJsonObject();
			if (choiceId.equals(choice.get("Id").getAsString())) {
				selectedChoice = choice;
				break;
			}
		}

		if (selectedChoice == null) {
			throw new IllegalArgumentException("Invalid choice ID: " + choiceId);
		}

		JsonObject ref = refs.get(choiceId);
		JsonObject merged = new JsonObject();
		if (ref != null) {
			for (Map.Entry<String, JsonElement> entry : ref.entrySet()) {
				merged.add(entry.getKey(), entry.getValue());
			}
		}
		if (selectedChoice.has("Price")) {
			merged.add("Price", selectedChoice.get("Price"));
		}
		if (!merged.has("Title")) {
			merged.addProperty("Title", "No title");
		}
		currentSelection.add(merged);

		if (selectedChoice.has("Configurables")) {
			for (JsonElement elem : selectedChoice.getAsJsonArray("Configurables")) {
				pendingConfigs.addFirst(elem.getAsJsonObject());
			}
		}
		if (selectedChoice.has("Items")) {
			for (JsonElement elem : selectedChoice.getAsJsonArray("Items")) {
				pendingConfigs.addFirst(elem.getAsJsonObject());
			}
		}

		if (pendingConfigs.isEmpty()) {
			int total = calculateTotalPrice(currentSelection);
			cart.add(new CartEntry(new ArrayList<>(currentSelection), total));
			orderingInProgress = false;
			currentSelection.clear();
			return Collections.emptyList();
		}

		return getNextChoiceIds();
	}

	private List<String> getNextChoiceIds() {
		while (!pendingConfigs.isEmpty()) {
			JsonObject config = pendingConfigs.peekFirst();
			if (config.has("Choices")) {
				List<String> ids = new ArrayList<>();
				for (JsonElement choice : config.getAsJsonArray("Choices")) {
					JsonObject obj = choice.getAsJsonObject();
					if (obj.has("Id")) {
						ids.add(obj.get("Id").getAsString());
					}
				}
				return ids;
			} else {
				pendingConfigs.pollFirst();
			}
		}
		return Collections.emptyList();
	}
	
	public void displayProductTree(int productId) {
		JsonObject product = getProductById(productId);
		if (product == null) {
			System.out.println("Product not found.");
			return;
		}
		System.out.println(idToTitle(productId) + " (ID: " + productId + ")");
		displayConfigTree(product, 1);
	}

	private void displayConfigTree(JsonObject node, int indent) {
		// Check if this node has a Ref ID to resolve
		if (node.has("Id")) {
			String idStr = node.get("Id").getAsString();
			String title = idToTitle(idStr);
			printIndented(indent, "- " + title + " (ID: " + idStr + ")");
		}

		// If this is a configurable with Choices
		if (node.has("Choices")) {
			JsonArray choices = node.getAsJsonArray("Choices");
			for (int i = 0; i < choices.size(); i++) {
				JsonObject choice = choices.get(i).getAsJsonObject();
				String choiceId = choice.get("Id").getAsString();
				String title = idToTitle(choiceId);
				boolean isDefault = i == 0; // first item = default
				String marker = isDefault ? " *default*" : "";
				printIndented(indent + 1, "-- " + title + " (ID: " + choiceId + ")" + marker);
				displayConfigTree(choice, indent + 2);
			}
		}

		// If this is a configurable with Items
		if (node.has("Items")) {
			JsonArray items = node.getAsJsonArray("Items");
			for (JsonElement elem : items) {
				JsonObject item = elem.getAsJsonObject();
				displayConfigTree(item, indent + 1);
			}
		}

		// Configurables (recursively nested options)
		if (node.has("Configurables")) {
			JsonArray configurables = node.getAsJsonArray("Configurables");
			for (JsonElement elem : configurables) {
				JsonObject config = elem.getAsJsonObject();
				displayConfigTree(config, indent + 1);
			}
		}
	}

	private void printIndented(int indent, String text) {
		for (int i = 0; i < indent; i++) System.out.print("  ");
		System.out.println(text);
	}



}
