package teneo.MenuExplorer.server;

import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

/**
 * Shared interface for menu-related operations including order creation, cart
 * management, product lookup, and pricing.
 * <p>
 * Intended to be used across both server and client layers as a guide to
 * implementing the api.
 */
public interface IMenu {

	/**
	 * Generates a human-readable title for a given order.
	 *
	 * @param order the list of item IDs representing the order
	 * @return the title of the order
	 */
	String getOrderTitle(List<Integer> order);

	/**
	 * Performs a search across a order's options for matching product names or
	 * keywords.
	 *
	 * @param query the search string
	 * @param order the order to search in
	 * @return the search results as a formatted string
	 */
	String searchOrder(String query, List<Integer> order);

	/**
	 * Returns the top 10 search results based on similarity.
	 *
	 * @param query the search string
	 * @return the top 10 results as a formatted string
	 */
	String searchTop10(String query);

	/**
	 * Adds multiple items to the order by calling {@code addToOrder} for each item
	 * ID.
	 *
	 * @param ids   the list of item IDs to add
	 * @param order the current order to modify
	 * @return the updated order list with added items
	 */
	List<Integer> addMultipleToOrder(List<Integer> ids, List<Integer> order);

	/**
	 * Adds a single item to the given order list based on configuration rules.
	 *
	 * @param id    the ID of the item to add
	 * @param order the current order to modify
	 * @return the updated order list with the added item
	 */
	List<Integer> addToOrder(int id, List<Integer> order);

	/**
	 * Gets all nested item IDs under a specified product, including all
	 * descendants.
	 *
	 * @param rootId the product ID to search under
	 * @return a set of all descendant item IDs
	 */
	Set<Integer> getAllSubIds(int rootId);

	/**
	 * Returns the current cart (list of all orders).
	 *
	 * @return the cart containing all current orders
	 */
	List<List<Integer>> getCart();

	/**
	 * Calculates the total price of all orders in the cart.
	 *
	 * @param cart the list of all orders
	 * @return the total price of all orders combined
	 */
	int getCartTotalPrice(List<List<Integer>> cart);

	/**
	 * Retrieves the description of a product based on its ID.
	 *
	 * @param id the product ID
	 * @return the product description, or an error message if not found
	 */
	String getDescriptionFromId(int id);

	/**
	 * Calculates the total price of a single order.
	 *
	 * @param order the order to calculate price for
	 * @return the total price of the order
	 */
	int getOrderPrice(List<Integer> order);

	/**
	 * Finds and returns the product JSON object with the given ID.
	 *
	 * @param id the product ID
	 * @return the JSON object for the product, or {@code null} if not found
	 */
	JsonObject getProductById(int id);

	/**
	 * Finds and returns the ref JSON object with the given ID.
	 *
	 * @param id the ref ID
	 * @return the JSON object for the ref, or {@code null} if not found
	 */
	JsonObject getRefById(int id);

	/**
	 * Gets all nested item IDs under a specified cutoff node within a base product.
	 *
	 * @param baseId   the base product ID
	 * @param cutoffId the node ID under which to collect sub-IDs
	 * @return a set of descendant item IDs under the cutoff node
	 */
	Set<Integer> getSubIdsUnder(int baseId, int cutoffId);

	/**
	 * Retrieves the title of a product based on its ID.
	 *
	 * @param id the product ID
	 * @return the product title, or {@code "Unknown"} if not found
	 */
	String getTitleForId(int id);

	/**
	 * Prints all orders in the cart in a human-readable format.
	 *
	 * @param cart the list of all orders in the cart
	 * @return a formatted string showing each order and the cart total
	 */
	String printCart(List<List<Integer>> cart);

	/**
	 * Prints a single order in a readable format, including selected options and
	 * price.
	 *
	 * @param order the order to print
	 * @return a formatted string representing the order
	 */
	String printOrder(List<Integer> order);

	/**
	 * Prints the full menu structure for a given order, including all configurable
	 * options.
	 *
	 * @param order the order to display options for
	 * @return a formatted string showing all selectable options
	 */
	String printOrderOptions(List<Integer> order);

	/**
	 * Removes an item (or its subtree) from the given order based on its structure.
	 *
	 * @param id    the ID of the item to remove
	 * @param order the current order to modify
	 * @return the updated order list after removal
	 */
	List<Integer> removeFromOrder(int id, List<Integer> order);

	/**
	 * Removes multiple items from the given order.
	 *
	 * @param ids   the list of item IDs to remove
	 * @param order the current order to modify
	 * @return the updated order list after removals
	 */
	List<Integer> removeMultipleFromOrder(List<Integer> ids, List<Integer> order);

	/**
	 * Removes the given order from the cart.
	 *
	 * @param order the order to remove from the cart
	 */
	void removeOrder(List<Integer> order);

	/**
	 * Starts a new order with the given root product ID, applying any default
	 * selections.
	 *
	 * @param rootId the root product ID to start the order with
	 * @return a list of selected item IDs representing the new order
	 */
	List<Integer> startOrder(int rootId);
}