package teneo.MenuExplorer.server;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

/**
 * REST controller that exposes endpoints for interacting with the menu and cart
 * system.
 * <p>
 * This API allows clients to search for products, manage orders and carts,
 * retrieve item details, and calculate pricing. It delegates all operations to
 * the shared {@link IMenu} service, implemented with {@link MenuExplorerLogic}.
 * <p>
 * All endpoints are prefixed with {@code /api}.
 */
@RestController
@RequestMapping("/api")
public class MenuExplorerAPI {

	private final IMenu menu;

	@Autowired
	public MenuExplorerAPI(IMenu menu) {
		this.menu = menu;
	}

	@PostMapping("/getOrderTitle")
	public String getOrderTitle(@RequestBody List<Integer> order) {
		return menu.getOrderTitle(order);
	}

	@PostMapping("/searchOrder")
	public String searchOrder(@RequestParam("query") String query, @RequestBody List<Integer> order) {
		return menu.searchOrder(query, order);
	}

	@GetMapping("/searchTop10")
	public String searchTop10(@RequestParam String query) {
		return menu.searchTop10(query);
	}

	@PostMapping("/addMultipleToOrder")
	public List<Integer> addMultipleToOrder(@RequestParam List<Integer> ids, @RequestBody List<Integer> order) {
		return menu.addMultipleToOrder(ids, order);
	}

	@PostMapping("/addToOrder")
	public List<Integer> addToOrder(@RequestParam int id, @RequestBody List<Integer> order) {
		return menu.addToOrder(id, order);
	}

	@GetMapping("/getAllSubIds")
	public Set<Integer> getAllSubIds(@RequestParam int rootId) {
		return menu.getAllSubIds(rootId);
	}

	/*
	 * Handled client now, route not needed.
	 * 
	 * @GetMapping("/getCart") public List<List<Integer>> getCart() { return
	 * menu.getCart(); }
	 */

	@PostMapping("/getCartTotalPrice")
	public int getCartTotalPrice(@RequestBody List<List<Integer>> cart) {
		return menu.getCartTotalPrice(cart);
	}

	@GetMapping("/getDescriptionFromId")
	public String getDescriptionFromId(@RequestParam int id) {
		return menu.getDescriptionFromId(id);
	}

	@PostMapping("/getOrderPrice")
	public int getOrderPrice(@RequestBody List<Integer> order) {
		return menu.getOrderPrice(order);
	}

	@GetMapping("/getProductById")
	public JsonObject getProductById(@RequestParam int id) {
		return menu.getProductById(id);
	}

	@GetMapping("/getRefById")
	public JsonObject getRefById(@RequestParam int id) {
		return menu.getRefById(id);
	}

	@GetMapping("/getSubIdsUnder")
	public Set<Integer> getSubIdsUnder(@RequestParam int baseId, @RequestParam int cutoffId) {
		return menu.getSubIdsUnder(baseId, cutoffId);
	}

	@GetMapping("/getTitleForId")
	public String getTitleForId(@RequestParam int id) {
		return menu.getTitleForId(id);
	}

	@PostMapping("/printCart")
	public String printCart(@RequestBody List<List<Integer>> cart) {
		return menu.printCart(cart);
	}

	@PostMapping("/printOrder")
	public String printOrder(@RequestBody List<Integer> order) {
		return menu.printOrder(order);
	}

	@PostMapping("/printOrderOptions")
	public String printOrderOptions(@RequestBody List<Integer> order) {
		return menu.printOrderOptions(order);
	}

	@PostMapping("/removeFromOrder")
	public List<Integer> removeFromOrder(@RequestParam int id, @RequestBody List<Integer> order) {
		return menu.removeFromOrder(id, order);
	}

	@PostMapping("/removeMultipleFromOrder")
	public List<Integer> removeMultipleFromOrder(@RequestParam List<Integer> ids, @RequestBody List<Integer> order) {
		return menu.removeMultipleFromOrder(ids, order);
	}

	/*
	 * Handled client now, route not needed.
	 * 
	 * @PostMapping("/removeOrder") public void removeOrder(@RequestBody
	 * List<Integer> order) { menu.removeOrder(order); }
	 */

	@PostMapping("/startOrder")
	public List<Integer> startOrder(@RequestParam int rootId) {
		return menu.startOrder(rootId);
	}
}
