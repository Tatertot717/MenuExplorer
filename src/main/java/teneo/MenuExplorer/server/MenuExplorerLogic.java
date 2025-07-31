package teneo.MenuExplorer.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import teneo.MenuExplorer.server.MenuSmartSearch.MenuItem;
import teneo.MenuExplorer.shared.IAllergen;
import teneo.MenuExplorer.shared.IMenu;

public class MenuExplorerLogic implements IMenu {

	private final Map<String, JsonObject> refs = new HashMap<>();
	private final List<JsonObject> products = new ArrayList<>();
	private final List<List<Integer>> cart = new ArrayList<>();
	private final List<MenuItem> searchMenu = new ArrayList<>();
	private final Map<Integer, Set<Integer>> parentMap = new HashMap<>();
	private IAllergen allergens;

	/**
	 * Constructs a new MenuExplorer by loading menu data from the specified JSON
	 * file.
	 *
	 * @param filePath the path to the JSON file containing menu data
	 * @throws FileNotFoundException if the specified file does not exist
	 */
	public MenuExplorerLogic(String filePath) throws FileNotFoundException {
		loadJsonData(filePath);
		buildParentMap();
		if (MenuSmartSearch.searchEnabled() == true) {
			for (JsonObject product : refs.values()) {
				String title = product.get("Title").getAsString();
				int id = product.get("Id").getAsInt();
				MenuSmartSearch.addMenuItem(searchMenu, title, id);
			}
		}
	}
	
	@Override
	public String getOrderTitle(List<Integer> order) {
		return getRefById(order.get(0)).get("Title").getAsString();
	}
	
	@Override
	public void setNewMenuAllergens(String allergensFile) throws FileNotFoundException {
		allergens = new MenuAllergensLogic(allergensFile);
	}
	
	@Override
	public IAllergen getMenuAllergens() {
		return allergens;
	}
	
	@Override
	public String search(String query) {
		if (MenuSmartSearch.searchEnabled()) {
			return MenuSmartSearch.match(query, searchMenu).name;
		}
		return null;
	}
	
	@Override
	public String searchTop10(String query) {
		if (MenuSmartSearch.searchEnabled()) {
	        List<MenuItem> topMatches = MenuSmartSearch.matchTop10(query, searchMenu);
	        StringBuilder result = new StringBuilder();
	        for (MenuItem item : topMatches) {
	            result.append(item.name)
	                  .append(" - ")
	                  .append(item.id)
	                  .append("\n");
	        }
	        return result.toString().trim();
		}
		return null;
	}


	/**
	 * Adds multiple items to the order by calling {@code addToOrder} for each item
	 * ID.
	 *
	 * @param ids   the list of item IDs to add
	 * @param order the current order to modify
	 */
	@Override
	public void addMultipleToOrder(List<Integer> ids, List<Integer> order) {
		for (int id : ids) {
			addToOrder(id, order);
		}
	}

	/**
	 * Adds a single item to the given order list based on configuration rules.
	 *
	 * @param id    the ID of the item to add
	 * @param order the current order to modify
	 */
	@Override
	public void addToOrder(int id, List<Integer> order) {
		if (order == null || order.isEmpty()) {
			return;
		}

		// Step 1: Path to root from the new id
		List<Integer> path = pathToRoot(id, order);
		if (path.isEmpty()) {
			return;
		}

		// Step 2: Deepest shared ancestor with current order
		List<Integer> matchingChain = findDeepestMatchingChain(path, order);
		if (matchingChain.isEmpty()) {
			return;
		}

		int splitId = matchingChain.get(matchingChain.size() - 1);

		// special case
		if (path.equals(matchingChain)) {
			// Re-adding same item (e.g., ketchup x2), allowed only if parent supports it

			if (path.size() < 2) {
				return; // Sanity check, must have parent + self
			}

			int parentId = path.get(path.size() - 2); // Get parent of the current id
			JsonObject parentNode = findNodeById(getProductById(order.get(0)), parentId);
			JsonObject parentRef = getRefFor(parentNode);
			List<String> tags = getTags(parentRef);

			boolean isMultiselect = tags.contains("configurationtype:multiselect");

			if (isMultiselect) {
				// Check Max selections allowed on parent
				JsonObject selections = parentRef.has("Selections") ? parentRef.getAsJsonObject("Selections") : null;
				Integer max = (selections != null && selections.has("Max") && !selections.get("Max").isJsonNull())
						? selections.get("Max").getAsInt()
						: null;

				Set<Integer> subIds = getSubIdsUnder(order.get(0), parentId);
				long selectedCount = order.stream().filter(subIds::contains).count();

				if (max == null || selectedCount < max) {
					// 9 limit for any individual item
					long count = order.stream().filter(e -> e == id).count();
					if (count >= 9) {
						return; // too many of this item already
					}

					order.add(id); // only add the final leaf again, not full path
				}
			}

			return; // Always return since we’re handling this branch specially
		}

		boolean shouldPrune = true;

		// Step 2.5: Multiselect + Max check
		JsonObject splitNode = findNodeById(getProductById(order.get(0)), splitId);
		JsonObject splitRef = getRefFor(splitNode);

		List<String> tags = getTags(splitRef);
		boolean isMultiselect = tags.contains("configurationtype:multiselect");

		if (isMultiselect) {
			// Check Max selections allowed
			JsonObject split = splitRef;
			JsonObject selections = split.has("Selections") ? split.getAsJsonObject("Selections") : null;
			Integer max = (selections != null && selections.has("Max") && !selections.get("Max").isJsonNull())
					? selections.get("Max").getAsInt()
					: null;

			if (max == null) {
				shouldPrune = false; // Unlimited selections
			} else {
				// Count how many under this splitId are already selected
				Set<Integer> subIds = getSubIdsUnder(order.get(0), splitId);
				long selectedCount = order.stream().filter(subIds::contains).count();
				if (selectedCount < max) {
					shouldPrune = false;
				}
			}
		}

		// Step 3: Prune only if needed
		List<Integer> prunedOrder = shouldPrune ? removeSubtreeFromOrder(splitId, order) : new ArrayList<>(order);
		order.clear();
		order.addAll(prunedOrder);

		// Step 4: Add path from splitId → id
		int splitIndex = path.indexOf(splitId);
		List<Integer> missingPath = path.subList(splitIndex + 1, path.size());
		order.addAll(missingPath);

		// Step 5: Apply defaults
		JsonObject node = findNodeById(getProductById(order.get(0)), id);
		if (node != null) {
			applyDefaults(node, order);
		}
	}

	private void applyDefaults(JsonObject node, List<Integer> order) {
		// Ensure this node is in the order
		if (node.has("Id")) {
			int nodeId = node.get("Id").getAsInt();
			if (!order.contains(nodeId)) {
				order.add(nodeId);
			}
		}

		// Process Items
		if (node.has("Items")) {
			for (JsonElement itemEl : node.getAsJsonArray("Items")) {
				JsonObject item = itemEl.getAsJsonObject();
				if (isDefault(item)) {
					applyDefaults(item, order);
				}
			}
		}

		// Process Choices
		if (node.has("Choices")) {
			JsonArray choices = node.getAsJsonArray("Choices");
			List<JsonObject> choiceList = new ArrayList<>();
			for (JsonElement choiceEl : choices) {
				choiceList.add(choiceEl.getAsJsonObject());
			}

			JsonObject ref = getRefFor(node);
			List<String> tags = getTags(ref);
			boolean isMultiselect = tags.contains("configurationtype:multiselect");
			boolean isMandatory = tags.contains("configurationtype:mandatory");

			boolean defaultSelected = false;

			// Apply explicitly default selections
			for (JsonObject choice : choiceList) {
				if (isDefault(choice)) {
					applyDefaults(choice, order);
					defaultSelected = true;
					if (!isMultiselect) {
						break;
					}
				}
			}

			// Fallback if mandatory single-select and no default was found
			if (!defaultSelected && isMandatory && !isMultiselect && !choiceList.isEmpty()) {
				JsonObject fallback = choiceList.get(0);
				applyDefaults(fallback, order);
			} else {
				// Recurse into other choices only if not already added
				for (JsonObject choice : choiceList) {
					int id = choice.get("Id").getAsInt();
					if (!order.contains(id)) {
						applyDefaults(choice, order);
					}
				}
			}
		}

		// Process Configurables
		if (node.has("Configurables")) {
			for (JsonElement configEl : node.getAsJsonArray("Configurables")) {
				JsonObject config = configEl.getAsJsonObject();
				applyDefaults(config, order);
			}
		}
	}

	private void buildParentMap() {
		parentMap.clear();
		for (JsonObject product : products) {
			if (product.has("Id")) {
				int rootId = product.get("Id").getAsInt();
				buildParentMapRecursive(product, rootId);
			}
		}
	}

	private void buildParentMapRecursive(JsonObject node, int parentId) {
		for (String key : List.of("Items", "Choices", "Configurables")) {
			if (node.has(key)) {
				for (JsonElement el : node.getAsJsonArray(key)) {
					JsonObject child = el.getAsJsonObject();
					if (child.has("Id")) {
						int childId = child.get("Id").getAsInt();
						parentMap.computeIfAbsent(childId, throwaway -> new HashSet<>()).add(parentId);
						//remove throwaway in future
						buildParentMapRecursive(child, childId);
					}
				}
			}
		}
	}

	private void collectSubIdsRecursive(JsonObject node, Set<Integer> subIds) {
		if (node.has("Id")) {
			int nodeId = node.get("Id").getAsInt();
			if (!subIds.add(nodeId)) {
				return; // Avoid infinite loops if already visited
			}
		}

		// Traverse Items, Choices, Configurables
		for (String key : List.of("Items", "Choices", "Configurables")) {
			if (node.has(key)) {
				for (JsonElement el : node.getAsJsonArray(key)) {
					JsonObject child = el.getAsJsonObject();
					collectSubIdsRecursive(child, subIds);
				}
			}
		}
	}

	private List<Integer> findDeepestMatchingChain(List<Integer> pathToRoot, List<Integer> order) {
		Set<Integer> orderSet = new HashSet<>(order); // For fast lookup
		List<Integer> result = new ArrayList<>();

		for (int id : pathToRoot) {
			if (orderSet.contains(id)) {
				result.add(id);
			} else {
				break; // Stop at first non-match
			}
		}

		return result;
	}

	private JsonObject findNodeById(JsonObject node, int targetId) {
		if (node.has("Id") && node.get("Id").getAsInt() == targetId) {
			return node;
		}

		for (String key : List.of("Items", "Choices", "Configurables")) {
			if (node.has(key)) {
				for (JsonElement el : node.getAsJsonArray(key)) {
					JsonObject child = el.getAsJsonObject();
					JsonObject found = findNodeById(child, targetId);
					if (found != null) {
						return found;
					}
				}
			}
		}
		return null;
	}

	private String formatSelectionSuffix(Long count) {
		if (count == null || count == 0) {
			return "";
		}
		return count == 1 ? " *selected*" : " *selected " + count + "x*";
	}

	/**
	 * Gets all nested item IDs under a specified product, including all
	 * descendants.
	 *
	 * @param rootId the product ID to search under
	 * @return a set of all descendant item IDs
	 */
	@Override
	public Set<Integer> getAllSubIds(int rootId) {
		Set<Integer> subIds = new HashSet<>();
		JsonObject root = getProductById(rootId);
		if (root != null) {
			collectSubIdsRecursive(root, subIds);
		}
		return subIds;
	}

	/**
	 * Returns the current cart (list of all orders).
	 *
	 * @return the cart containing all current orders
	 */
	@Override
	public List<List<Integer>> getCart() {
		return cart;
	}

	/**
	 * Calculates the total price of all orders in the cart.
	 *
	 * @return the total cart price
	 */
	@Override
	public int getCartTotalPrice() {
		int totalPrice = 0;
		for (List<Integer> order : cart) {
			totalPrice += getOrderPrice(order);
		}
		return totalPrice;
	}

	/**
	 * Retrieves the description of a product based on its ID.
	 *
	 * @param id the product ID
	 * @return the product description, or an error message if not found
	 */
	@Override
	public String getDescriptionFromId(int id) {
		JsonObject product = refs.get(String.valueOf(id));
		if (product != null) {
			if (product.has("Description")) {
				return product.get("Description").toString();
			} else {
				return "No Description";
			}
		}
		return "Invalid Item";
	}

	/**
	 * Calculates the total price of a single order.
	 *
	 * @param order the order to calculate price for
	 * @return the total price of the order
	 */
	@Override
	public int getOrderPrice(List<Integer> order) {
		if (order == null || order.isEmpty()) {
			return 0;
		}

		Map<Integer, Long> counts = order.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));

		int totalPrice = 0;
		for (Map.Entry<Integer, Long> entry : counts.entrySet()) {
			int id = entry.getKey();
			long quantity = entry.getValue();

			JsonObject ref = refs.get(String.valueOf(id));
			if (ref != null && ref.has("Price")) {
				int price = ref.get("Price").getAsInt();
				totalPrice += price * quantity;
			}
		}
		return totalPrice;
	}

	/**
	 * Finds and returns the product JSON object with the given ID.
	 *
	 * @param id the product ID
	 * @return the JSON object for the product, or null if not found
	 */
	@Override
	public JsonObject getProductById(int id) {
		for (JsonObject product : products) {
			if (product.has("Id") && product.get("Id").getAsInt() == id) {
				return product;
			}
		}
		return null;
	}
	
	/**
	 * Finds and returns the ref JSON object with the given ID.
	 *
	 * @param id the product ID
	 * @return the JSON object for the ref, or null if not found
	 */
	@Override
	public JsonObject getRefById(int id) {
		for (JsonObject product : refs.values()) {
			if (product.has("Id") && product.get("Id").getAsInt() == id) {
				return product;
			}
		}
		return null;
	}


	private JsonObject getRefFor(JsonObject obj) {
		if (!obj.has("Id")) {
			return null;
		}
		String id = obj.get("Id").getAsString();
		return refs.get(id);
	}

	/**
	 * Gets all nested item IDs under a specified cutoff node within a base product.
	 *
	 * @param baseId   the base product ID
	 * @param cutoffId the node ID under which to collect sub-IDs
	 * @return a set of descendant item IDs under the cutoff node
	 */
	@Override
	public Set<Integer> getSubIdsUnder(int baseId, int cutoffId) {
		JsonObject base = getProductById(baseId);
		if (base == null) {
			return Collections.emptySet();
		}

		JsonObject target = findNodeById(base, cutoffId);
		if (target == null) {
			return Collections.emptySet();
		}

		Set<Integer> result = new HashSet<>();
		collectSubIdsRecursive(target, result);
		return result;
	}

	private List<String> getTags(JsonObject ref) {
		if (ref == null || !ref.has("Tags")) {
			return Collections.emptyList();
		}
		return StreamSupport.stream(ref.getAsJsonArray("Tags").spliterator(), false).map(JsonElement::getAsString)
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the title of a product based on its ID.
	 *
	 * @param id the product ID
	 * @return the product title, or "Unknown" if not found
	 */
	@Override
	public String getTitleForId(int id) {
		JsonObject ref = refs.get(String.valueOf(id));
		if (ref != null && ref.has("Title")) {
			return ref.get("Title").getAsString();
		}
		return "Unknown";
	}

	private boolean isDefault(JsonObject obj) {
		return obj.has("IsDefault") && obj.get("IsDefault").getAsBoolean() && obj.has("Checked")
				&& obj.get("Checked").getAsBoolean();
	}

	private void loadJsonData(String filePath) throws FileNotFoundException {
		Gson gson = new Gson();
		JsonObject root = gson.fromJson(new FileReader(filePath), JsonObject.class);

		Type mapType = new TypeToken<Map<String, JsonObject>>() {
		}.getType();
		Type listType = new TypeToken<List<JsonObject>>() {
		}.getType();

		refs.clear();
		refs.putAll(gson.fromJson(root.getAsJsonObject("Refs"), mapType));

		products.clear();
		products.addAll(gson.fromJson(root.getAsJsonArray("Products"), listType));
	}

	private List<Integer> pathToRoot(int targetId, List<Integer> order) {
		if (order == null || order.isEmpty()) {
			return Collections.emptyList();
		}

		int rootId = order.get(0);
		Set<Integer> validSubIds = getAllSubIds(rootId);
		Set<Integer> visited = new HashSet<>();

		LinkedList<Integer> path = new LinkedList<>();
		boolean found = walkToRoot(targetId, validSubIds, order, visited, path);
		return found ? path : Collections.emptyList();
	}

	/**
	 * Prints all orders in the cart in a human-readable format.
	 *
	 * @return a formatted string showing each order and the cart total
	 */
	@Override
	public String printCart() {
		if (cart.isEmpty()) {
			return "Cart is empty.";
		}

		StringBuilder sb = new StringBuilder();
		int index = 1;
		for (List<Integer> order : cart) {
			sb.append("Order ").append(index).append(":\n");
			sb.append(printOrder(order)).append("\n\n");
			index++;
		}

		sb.append("Cart Total Price: ").append(getCartTotalPrice()).append("\n");
		return sb.toString();
	}

	/**
	 * Prints a single order in a readable format, including selected options and
	 * price.
	 *
	 * @param order the order to print
	 * @return a formatted string representing the order
	 */
	@Override
	public String printOrder(List<Integer> order) {
		if (order == null || order.isEmpty()) {
			return "No order, start one";
		}

		int rootId = order.get(0);
		JsonObject root = getProductById(rootId);
		if (root == null) {
			return "Product not found: " + rootId;
		}

		StringBuilder sb = new StringBuilder();
		Map<Integer, Long> counts = order.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));

		sb.append("-- ").append(getTitleForId(rootId)).append(" (id ").append(rootId).append(")")
				.append(formatSelectionSuffix(counts.get(rootId))).append("\n");

		printOrderRecursive(root, counts, 1, sb);
		sb.append("Order Price: " + getOrderPrice(order));
		return sb.toString();
	}

	/**
	 * Prints the full menu structure for a given order, including all configurable
	 * options.
	 *
	 * @param order the order to display options for
	 * @return a formatted string showing all selectable options
	 */
	@Override
	public String printOrderOptions(List<Integer> order) {
		int rootId = order.get(0);
		JsonObject root = getProductById(rootId);
		if (root == null) {
			return "Product not found: " + rootId;
		}

		StringBuilder sb = new StringBuilder();
		Map<Integer, Long> counts = order.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));

		sb.append("-- ").append(getTitleForId(rootId)).append(" (id ").append(rootId).append(")")
				.append(formatSelectionSuffix(counts.get(rootId))).append("\n");

		printTreeRecursive(root, counts, 1, sb);
		return sb.toString();
	}

	private void printOrderRecursive(JsonObject node, Map<Integer, Long> counts, int indent, StringBuilder sb) {
		String indentStr = "  ".repeat(indent);

		for (String key : List.of("Configurables", "Items", "Choices")) {
			if (node.has(key)) {
				for (JsonElement el : node.getAsJsonArray(key)) {
					JsonObject child = el.getAsJsonObject();
					int id = child.get("Id").getAsInt();
					Long count = counts.get(id);

					if (count != null && count > 0) {
						sb.append(indentStr).append("-- ").append(getTitleForId(id)).append(" (id ").append(id)
								.append(")").append(formatSelectionSuffix(count)).append("\n");

						printOrderRecursive(child, counts, indent + 1, sb);
					}
				}
			}
		}
	}

	private void printTreeRecursive(JsonObject node, Map<Integer, Long> counts, int indent, StringBuilder sb) {
		String indentStr = "  ".repeat(indent);
		for (String key : List.of("Configurables", "Items", "Choices")) {
			if (node.has(key)) {
				for (JsonElement el : node.getAsJsonArray(key)) {
					JsonObject child = el.getAsJsonObject();
					int id = child.get("Id").getAsInt();
					sb.append(indentStr).append("-- ").append(getTitleForId(id)).append(" (id ").append(id).append(")")
							.append(formatSelectionSuffix(counts.get(id))).append("\n");

					printTreeRecursive(child, counts, indent + 1, sb);
				}
			}
		}
	}

	/**
	 * Removes an item (or its subtree) from the given order based on its structure.
	 *
	 * @param id    the ID of the item to remove
	 * @param order the current order to modify
	 */
	@Override
	public void removeFromOrder(int id, List<Integer> order) {
		if (order == null || order.isEmpty()) {
			return;
		}

		// Step 1: Path to root from the id to remove
		List<Integer> path = pathToRoot(id, order);
		if (path.isEmpty()) {
			return;
		}

		// Step 2: Deepest matching chain with current order
		List<Integer> matchingChain = findDeepestMatchingChain(path, order);
		// If path and matching chain do not fully match, id not in order tree → do
		// nothing
		if (matchingChain.isEmpty() || !path.equals(matchingChain)) {
			return;
		}

		// Count how many instances of id are currently in order
		long count = order.stream().filter(i -> i == id).count();

		if (count > 1) {
			// Just remove one instance (avoid removing entire subtree)
			order.remove(Integer.valueOf(id));
			return;
		}

		// Only one instance → perform full removal of subtree starting at this id

		// Step 3: Find parent id for this node (if any)
		int parentId = -1;
		if (path.size() > 1) {
			parentId = path.get(path.size() - 2);
		}

		// Step 4: Check if the node is a leaf (subtree includes only itself)
		Set<Integer> subtreeIds = getSubIdsUnder(order.get(0), id); // rootId assumed order.get(0)
		boolean isLeafNode = (subtreeIds.size() == 1);

		List<Integer> prunedOrder;

		if (isLeafNode) {
			// For leaf nodes, do NOT remove the node itself (no subtree to prune)
			// So keep order as-is; just remove the single instance safely
			prunedOrder = new ArrayList<>(order);
			prunedOrder.remove(Integer.valueOf(id));
		} else {
			// For non-leaf nodes, remove the entire subtree (including this id)
			prunedOrder = removeSubtreeFromOrder(id, order);
		}

		// Update order list with pruned order
		order.clear();
		order.addAll(prunedOrder);

		// Step 5: If parent node is mandatory, apply defaults
		if (parentId != -1) {
			JsonObject root = getProductById(order.get(0));
			JsonObject parentNode = findNodeById(root, parentId);
			if (parentNode != null) {
				JsonObject parentRef = getRefFor(parentNode);
				List<String> tags = getTags(parentRef);
				boolean isMandatory = tags.contains("configurationtype:mandatory");

				if (isMandatory) {
					applyDefaults(parentNode, order);
				}
			}
		}
	}

	/**
	 * Removes multiple items from the given order.
	 *
	 * @param ids   the list of item IDs to remove
	 * @param order the current order to modify
	 */
	@Override
	public void removeMultipleFromOrder(List<Integer> ids, List<Integer> order) {
		for (int id : ids) {
			removeFromOrder(id, order);
		}
	}

	/**
	 * Removes the given order from the cart.
	 *
	 * @param order the order to remove
	 */
	@Override
	public void removeOrder(List<Integer> order) {
		if (order == null || order.isEmpty() || cart.isEmpty()) {
			return;
		}
		cart.remove(order);
	}

	private List<Integer> removeSubtreeFromOrder(int cutoffId, List<Integer> order) {
		if (order == null || order.isEmpty()) {
			return order;
		}
		int rootId = order.get(0);
		Set<Integer> subIds = getSubIdsUnder(rootId, cutoffId);
		subIds.remove(cutoffId); // Exclude cutoffId itself
		return order.stream().filter(id -> !subIds.contains(id)).collect(Collectors.toList());
	}

	/**
	 * Starts a new order with the given root product ID, applying any default
	 * selections.
	 *
	 * @param rootId the root product ID to start the order with
	 * @return a list of selected item IDs representing the new order
	 */
	@Override
	public List<Integer> startOrder(int rootId) {
		List<Integer> order = new ArrayList<>();
		order.add(rootId); // Start with the main product

		JsonObject product = getProductById(rootId);
		if (product != null) {
			applyDefaults(product, order);
		}

		cart.add(order);

		return order;
	}

	private boolean walkToRoot(int currentId, Set<Integer> validSubIds, List<Integer> order, Set<Integer> visited,
			LinkedList<Integer> path) {
		if (!validSubIds.contains(currentId) || !visited.add(currentId)) {
			return false;
		}

		path.addFirst(currentId);

		if (order.get(0) == currentId) {
			return true; // Reached the root
		}

		Set<Integer> parents = parentMap.getOrDefault(currentId, Collections.emptySet());

		// Sort parents by appearance in `order`, prioritize those earlier in the list
		List<Integer> sortedParents = new ArrayList<>(parents);
		sortedParents.sort(Comparator.comparingInt(id -> {
			int idx = order.indexOf(id);
			return idx >= 0 ? idx : Integer.MAX_VALUE;
		}));

		for (int parentId : sortedParents) {
			if (walkToRoot(parentId, validSubIds, order, visited, path)) {
				return true;
			}
		}

		path.removeFirst(); // backtrack
		return false;
	}

}