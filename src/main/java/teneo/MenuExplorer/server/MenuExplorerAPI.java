package teneo.MenuExplorer.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import teneo.MenuExplorer.shared.IAllergen;
import teneo.MenuExplorer.shared.IMenu;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonObject;

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

    @PostMapping("/setNewMenuAllergens")
    public void setNewMenuAllergens(@RequestParam String allergensFile) throws FileNotFoundException {
        menu.setNewMenuAllergens(allergensFile);
    }

    @GetMapping("/getMenuAllergens")
    public IAllergen getMenuAllergens() {
        return menu.getMenuAllergens();
    }

    @GetMapping("/search")
    public String search(@RequestParam String query) {
        return menu.search(query);
    }

    @GetMapping("/searchTop10")
    public String searchTop10(@RequestParam String query) {
        return menu.searchTop10(query);
    }

    @PostMapping("/addMultipleToOrder")
    public void addMultipleToOrder(@RequestParam List<Integer> ids, @RequestBody List<Integer> order) {
        menu.addMultipleToOrder(ids, order);
    }

    @PostMapping("/addToOrder")
    public void addToOrder(@RequestParam int id, @RequestBody List<Integer> order) {
        menu.addToOrder(id, order);
    }

    @GetMapping("/getAllSubIds")
    public Set<Integer> getAllSubIds(@RequestParam int rootId) {
        return menu.getAllSubIds(rootId);
    }

    @GetMapping("/getCart")
    public List<List<Integer>> getCart() {
        return menu.getCart();
    }

    @GetMapping("/getCartTotalPrice")
    public int getCartTotalPrice() {
        return menu.getCartTotalPrice();
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

    @GetMapping("/printCart")
    public String printCart() {
        return menu.printCart();
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
    public void removeFromOrder(@RequestParam int id, @RequestBody List<Integer> order) {
        menu.removeFromOrder(id, order);
    }

    @PostMapping("/removeMultipleFromOrder")
    public void removeMultipleFromOrder(@RequestParam List<Integer> ids, @RequestBody List<Integer> order) {
        menu.removeMultipleFromOrder(ids, order);
    }

    @PostMapping("/removeOrder")
    public void removeOrder(@RequestBody List<Integer> order) {
        menu.removeOrder(order);
    }

    @PostMapping("/startOrder")
    public List<Integer> startOrder(@RequestParam int rootId) {
        return menu.startOrder(rootId);
    }
}
