package teneo.MenuExplorer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "allergenClient", url = "${allergen.server.url}")  // You can replace with hardcoded URL if needed
public interface FAllergens {

    @GetMapping("/api/getMenuAllergens/searchAllergens")
    String searchAllergens(@RequestParam String query);

    @GetMapping("/api/getMenuAllergens/getAllergens")
    Map<String, Boolean> getAllergens(@RequestParam String name);

    @GetMapping("/api/getMenuAllergens/getIngredients")
    String getIngredients(@RequestParam String name);

    // These are debug methods - may be logged or printed
    @PostMapping("/api/getMenuAllergens/printAllAllergens")
    void printAllAllergens();

    @PostMapping("/api/getMenuAllergens/printAllergens")
    void printAllergens(@RequestParam String name);
}
