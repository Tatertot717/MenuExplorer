package teneo.MenuExplorer.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.support.SpringMvcContract;

import com.google.gson.JsonObject;

import feign.Client;
import feign.Feign;
import feign.gson.GsonEncoder;

/**
 * Client wrapper for interacting with the remote menu API using Feign.
 * <p>
 * Provides methods for order creation, cart management, product lookup, and
 * pricing via a backend service.
 */
public class MenuExplorer {
	private final FMenu explorer;
	private final List<List<Integer>> cart = new ArrayList<>();

	/**
	 * Constructs a new MenuExplorer instance that connects to the given server URL.
	 *
	 * @param baseUrl the base URL of the menu server API
	 * @param apiKey  the API key used for authenticating requests
	 */
	public MenuExplorer(String baseUrl, String apiKey) {
		try {
			Client unsafeClient = UnsafeSslClient.create(); // REMOVE UNSAFE CLIENT IN PROD, NEEDED FOR SELFSIGN!!!
															// //TODO
			explorer = Feign.builder().client(unsafeClient).contract(new SpringMvcContract()).encoder(new GsonEncoder())
					.decoder(new StringDecoder()).requestInterceptor(new ApiKeyInterceptor(apiKey))
					.requestInterceptor(new JsonRequestInterceptor()).target(FMenu.class, baseUrl);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create unsafe SSL client", e);
		}
	}

	/**
	 * Generates a human-readable title for a given order.
	 *
	 * @param order the list of item IDs representing the order
	 * @return the title of the order
	 */
	public String getOrderTitle(List<Integer> order) {
		return explorer.getOrderTitle(order);
	}

	/**
	 * Performs a search across a order's options for matching product names or
	 * keywords.
	 *
	 * @param query the search string
	 * @param order the order to search in
	 * @return the search results as a formatted string
	 */
	public String searchOrder(String query, List<Integer> order) {
		return explorer.searchOrder(query, order);
	}

	/**
	 * Returns the top 10 search results based on relevance or popularity.
	 *
	 * @param query the search string
	 * @return the top 10 results as a formatted string
	 */
	public String searchTop10(String query) {
		return explorer.searchTop10(query);
	}

	/**
	 * Adds multiple items to the order by calling {@code addToOrder} for each item
	 * ID.
	 *
	 * @param ids   the list of item IDs to add
	 * @param order the current order to modify
	 * @return the updated order
	 */
	public List<Integer> addMultipleToOrder(List<Integer> ids, List<Integer> order) {
		return explorer.addMultipleToOrder(ids, order);
	}

	/**
	 * Adds a single item to the given order list based on configuration rules.
	 *
	 * @param id    the ID of the item to add
	 * @param order the current order to modify
	 * @return the updated order
	 */
	public List<Integer> addToOrder(int id, List<Integer> order) {
		return explorer.addToOrder(id, order);
	}

	/**
	 * Gets all nested item IDs under a specified product, including all
	 * descendants.
	 *
	 * @param rootId the product ID to search under
	 * @return a set of all descendant item IDs
	 */
	public Set<Integer> getAllSubIds(int rootId) {
		return explorer.getAllSubIds(rootId);
	}

	/**
	 * Returns the current cart containing all active orders.
	 *
	 * @return the cart as a list of orders
	 */
	public List<List<Integer>> getCart() {
		return cart;
	}

	/**
	 * Calculates the total price of all orders in the cart.
	 *
	 * @return the total cart price
	 */
	public int getCartTotalPrice() {
		return explorer.getCartTotalPrice(cart);
	}

	/**
	 * Retrieves the description of a product based on its ID.
	 *
	 * @param id the product ID
	 * @return the product description, or an error message if not found
	 */
	public String getDescriptionFromId(int id) {
		return explorer.getDescriptionFromId(id);
	}

	/**
	 * Calculates the total price of a single order.
	 *
	 * @param order the order to calculate price for
	 * @return the total price of the order
	 */
	public int getOrderPrice(List<Integer> order) {
		return explorer.getOrderPrice(order);
	}

	/**
	 * Finds and returns the product JSON object with the given ID.
	 *
	 * @param id the product ID
	 * @return the JSON object for the product, or {@code null} if not found
	 */
	public JsonObject getProductById(int id) {
		return explorer.getProductById(id);
	}

	/**
	 * Finds and returns the ref JSON object with the given ID.
	 *
	 * @param id the ref ID
	 * @return the JSON object for the ref, or {@code null} if not found
	 */
	public JsonObject getRefById(int id) {
		return explorer.getRefById(id);
	}

	/**
	 * Gets all nested item IDs under a specified cutoff node within a base product.
	 *
	 * @param baseId   the base product ID
	 * @param cutoffId the node ID under which to collect sub-IDs
	 * @return a set of descendant item IDs under the cutoff node
	 */
	public Set<Integer> getSubIdsUnder(int baseId, int cutoffId) {
		return explorer.getSubIdsUnder(baseId, cutoffId);
	}

	/**
	 * Retrieves the title of a product based on its ID.
	 *
	 * @param id the product ID
	 * @return the product title, or {@code "Unknown"} if not found
	 */
	public String getTitleForId(int id) {
		return explorer.getTitleForId(id);
	}

	/**
	 * Prints the current contents of the cart in a human-readable format.
	 *
	 * @return a formatted string showing each order and the cart total
	 */
	public String printCart() {
		return explorer.printCart(cart);
	}

	/**
	 * Prints a single order in a readable format, including selected options and
	 * price.
	 *
	 * @param order the order to print
	 * @return a formatted string representing the order
	 */
	public String printOrder(List<Integer> order) {
		return explorer.printOrder(order);
	}

	/**
	 * Prints the full menu structure for a given order, including all configurable
	 * options.
	 *
	 * @param order the order to display options for
	 * @return a formatted string showing all selectable options
	 */
	public String printOrderOptions(List<Integer> order) {
		return explorer.printOrderOptions(order);
	}

	/**
	 * Removes an item (or its subtree) from the given order based on its structure.
	 *
	 * @param id    the ID of the item to remove
	 * @param order the current order to modify
	 * @return the updated order
	 */
	public List<Integer> removeFromOrder(int id, List<Integer> order) {
		return explorer.removeFromOrder(id, order);
	}

	/**
	 * Removes multiple items from the given order.
	 *
	 * @param ids   the list of item IDs to remove
	 * @param order the current order to modify
	 * @return the updated order
	 */
	public List<Integer> removeMultipleFromOrder(List<Integer> ids, List<Integer> order) {
		return explorer.removeMultipleFromOrder(ids, order);
	}

	/**
	 * Removes the given order from the cart.
	 *
	 * @param order the order to remove
	 * @return the updated cart
	 */
	public List<List<Integer>> removeOrder(List<Integer> order) {
		cart.remove(order);
		return cart;
	}

	/**
	 * Starts a new order with the given root product ID and applies any default
	 * selections.
	 *
	 * @param rootId the root product ID to start the order with
	 * @return a list of selected item IDs representing the new order
	 */
	public List<Integer> startOrder(int rootId) {
		List<Integer> order = explorer.startOrder(rootId);
		cart.add(order);
		return order;
	}
}