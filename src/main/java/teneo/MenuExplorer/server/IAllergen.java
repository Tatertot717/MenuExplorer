package teneo.MenuExplorer.server;

import java.util.Map;

/**
 * Shared interface for allergen- and ingredient-related operations.
 * <p>
 * Provides query capabilities for both direct lookups and smart search.
 * Intended to be used across server and client components.
 */
public interface IAllergen {

    /**
     * Performs a smart search for allergens using a product or sub-item name.
     *
     * @param query the search string
     * @return a formatted string representing the allergen information
     */
    String searchAllergens(String query);

    /**
     * Retrieves allergen information for a given product or sub-item.
     *
     * @param name the name of the product or sub-item
     * @return a map of allergen names to booleans indicating presence
     */
    Map<String, Boolean> getAllergens(String name);

    /**
     * Retrieves the ingredient list for a given sub-item.
     *
     * @param name the name of the sub-item
     * @return a string containing the ingredient list, or {@code null} if not found
     */
    String getIngredients(String name);

    /**
     * Performs a smart search for ingredients using a sub-item name.
     *
     * @param query the search string
     * @return a formatted string representing the ingredient list
     */
    String searchIngredients(String query);
}