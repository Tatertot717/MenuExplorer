package teneo.MenuExplorer.shared;

import java.util.Map;

public interface IAllergen {

	String searchAllergens(String query);

	//print allergens for all products, debug
	void printAllAllergens();

	void printAllergens(String name);

	Map<String, Boolean> getAllergens(String name);

	String getIngredients(String name);

}