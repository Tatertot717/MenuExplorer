package teneo.MenuExplorer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

import feign.Headers;

@FeignClient(name = "menuapi", url = "${menu.api.url}")
public interface FMenu {

	@PostMapping("/api/getOrderTitle")
	@Headers("Content-Type: application/json")
	String getOrderTitle(@RequestBody List<Integer> order);

	@PostMapping("/api/setNewMenuAllergens")
	void setNewMenuAllergens(@RequestParam("allergensFile") String allergensFile);

	@GetMapping("/api/getMenuAllergens")
	FAllergens getMenuAllergens();

	@GetMapping("/api/search")
	String search(@RequestParam("query") String query);

	@GetMapping("/api/searchTop10")
	String searchTop10(@RequestParam("query") String query);

	@PostMapping("/api/addMultipleToOrder")
	void addMultipleToOrder(@RequestParam("ids") List<Integer> ids, @RequestBody List<Integer> order);

	@PostMapping("/api/addToOrder")
	void addToOrder(@RequestParam("id") int id, @RequestBody List<Integer> order);

	@GetMapping("/api/getAllSubIds")
	Set<Integer> getAllSubIds(@RequestParam("rootId") int rootId);

	@GetMapping("/api/getCart")
	List<List<Integer>> getCart();

	@GetMapping("/api/getCartTotalPrice")
	int getCartTotalPrice();

	@GetMapping("/api/getDescriptionFromId")
	String getDescriptionFromId(@RequestParam("id") int id);

	@PostMapping("/api/getOrderPrice")
	int getOrderPrice(@RequestBody List<Integer> order);

	@GetMapping("/api/getProductById")
	JsonObject getProductById(@RequestParam("id") int id);

	@GetMapping("/api/getRefById")
	JsonObject getRefById(@RequestParam("id") int id);

	@GetMapping("/api/getSubIdsUnder")
	Set<Integer> getSubIdsUnder(@RequestParam("baseId") int baseId, @RequestParam("cutoffId") int cutoffId);

	@GetMapping("/api/getTitleForId")
	String getTitleForId(@RequestParam("id") int id);

	@GetMapping("/api/printCart")
	String printCart();

	@PostMapping("/api/printOrder")
	String printOrder(@RequestBody List<Integer> order);

	@PostMapping("/api/printOrderOptions")
	String printOrderOptions(@RequestBody List<Integer> order);

	@PostMapping("/api/removeFromOrder")
	void removeFromOrder(@RequestParam("id") int id, @RequestBody List<Integer> order);

	@PostMapping("/api/removeMultipleFromOrder")
	void removeMultipleFromOrder(@RequestParam("ids") List<Integer> ids, @RequestBody List<Integer> order);

	@PostMapping("/api/removeOrder")
	void removeOrder(@RequestBody List<Integer> order);

	@PostMapping("/api/startOrder")
	List<Integer> startOrder(@RequestParam("rootId") int rootId);
}
