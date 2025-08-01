package teneo.MenuExplorer.client;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonObject;

import feign.Headers;

/**
 * Contract which states how the menu API is to be used by the client. See {@link MenuExplorer} for function reference.
 */
@FeignClient(name = "menuapi", url = "${menu.api.url}")
public interface FMenu {

	@PostMapping("/api/getOrderTitle")
	@Headers("Content-Type: application/json")
	String getOrderTitle(@RequestBody List<Integer> order);

	@GetMapping("/api/search")
	String search(@RequestParam("query") String query);

	@GetMapping("/api/searchTop10")
	String searchTop10(@RequestParam("query") String query);

	@PostMapping("/api/addMultipleToOrder")
	@Headers("Content-Type: application/json")
	List<Integer> addMultipleToOrder(@RequestParam("ids") List<Integer> ids, @RequestBody List<Integer> order);

	@PostMapping("/api/addToOrder")
	@Headers("Content-Type: application/json")
	List<Integer> addToOrder(@RequestParam("id") int id, @RequestBody List<Integer> order);

	@GetMapping("/api/getAllSubIds")
	Set<Integer> getAllSubIds(@RequestParam("rootId") int rootId);

	@PostMapping("/api/getCartTotalPrice")
	int getCartTotalPrice(@RequestBody List<List<Integer>> cart);

	@GetMapping("/api/getDescriptionFromId")
	String getDescriptionFromId(@RequestParam("id") int id);

	@PostMapping("/api/getOrderPrice")
	@Headers("Content-Type: application/json")
	int getOrderPrice(@RequestBody List<Integer> order);

	@GetMapping("/api/getProductById")
	JsonObject getProductById(@RequestParam("id") int id);

	@GetMapping("/api/getRefById")
	JsonObject getRefById(@RequestParam("id") int id);

	@GetMapping("/api/getSubIdsUnder")
	Set<Integer> getSubIdsUnder(@RequestParam("baseId") int baseId, @RequestParam("cutoffId") int cutoffId);

	@GetMapping("/api/getTitleForId")
	String getTitleForId(@RequestParam("id") int id);

	@PostMapping("/api/printCart")
	String printCart(@RequestBody List<List<Integer>> cart);

	@PostMapping("/api/printOrder")
	@Headers("Content-Type: application/json")
	String printOrder(@RequestBody List<Integer> order);

	@PostMapping("/api/printOrderOptions")
	@Headers("Content-Type: application/json")
	String printOrderOptions(@RequestBody List<Integer> order);

	@PostMapping("/api/removeFromOrder")
	@Headers("Content-Type: application/json")
	List<Integer> removeFromOrder(@RequestParam("id") int id, @RequestBody List<Integer> order);

	@PostMapping("/api/removeMultipleFromOrder")
	@Headers("Content-Type: application/json")
	List<Integer> removeMultipleFromOrder(@RequestParam("ids") List<Integer> ids, @RequestBody List<Integer> order);

	@PostMapping("/api/startOrder")
	List<Integer> startOrder(@RequestParam("rootId") int rootId);
}