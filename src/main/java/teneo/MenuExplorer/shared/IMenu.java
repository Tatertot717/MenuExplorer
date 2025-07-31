package teneo.MenuExplorer.shared;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

public interface IMenu {

	String getOrderTitle(List<Integer> order);

	void setNewMenuAllergens(String allergensFile) throws FileNotFoundException;

	IAllergen getMenuAllergens();

	String search(String query);

	String searchTop10(String query);

	/**
	 * Adds multiple items to the order by calling {@code addToOrder} for each item
	 * ID.
	 *
	 * @param ids   the list of item IDs to add
	 * @param order the current order to modify
	 */
	void addMultipleToOrder(List<Integer> ids, List<Integer> order);

	/**
	 * Adds a single item to the given order list based on configuration rules.
	 *
	 * @param id    the ID of the item to add
	 * @param order the current order to modify
	 */
	void addToOrder(int id, List<Integer> order);

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
	 * @return the total cart price
	 */
	int getCartTotalPrice();

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
	 * @return the JSON object for the product, or null if not found
	 */
	JsonObject getProductById(int id);

	/**
	 * Finds and returns the ref JSON object with the given ID.
	 *
	 * @param id the product ID
	 * @return the JSON object for the ref, or null if not found
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
	 * @return the product title, or "Unknown" if not found
	 */
	String getTitleForId(int id);

	/**
	 * Prints all orders in the cart in a human-readable format.
	 *
	 * @return a formatted string showing each order and the cart total
	 */
	String printCart();

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
	 */
	void removeFromOrder(int id, List<Integer> order);

	/**
	 * Removes multiple items from the given order.
	 *
	 * @param ids   the list of item IDs to remove
	 * @param order the current order to modify
	 */
	void removeMultipleFromOrder(List<Integer> ids, List<Integer> order);

	/**
	 * Removes the given order from the cart.
	 *
	 * @param order the order to remove
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