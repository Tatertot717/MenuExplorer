package teneo.MenuExplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;

public class MenuSmartSearch {

	//record ScoredItem(MenuItem item, double score) {}
	//The above is not available in java 11, use this mess below
	public static class ScoredItem {
	    public final MenuItem item;
	    public final double score;

	    public ScoredItem(MenuItem item, double score) {
	        this.item = item;
	        this.score = score;
	    }

	    public MenuItem item() {
	        return item;
	    }

	    public double score() {
	        return score;
	    }
	}
	
	public static class MenuItem {
		public final String name;
		public final int id;
		public final INDArray vector;

		public MenuItem(String name, int id, INDArray vector) {
			this.name = name;
			this.id = id;
			this.vector = vector;
		}
	}

	private static WordVectors wordVectors = null;

	public static void loadModel(String glovePath) throws FileNotFoundException {
		if (wordVectors == null) {
			wordVectors = WordVectorSerializer.readWord2VecModel(new File(glovePath));
		}
	}

	public static boolean searchEnabled() {
		if (wordVectors == null)
			return false;
		return true;
	}

	private static INDArray embedSentence(String sentence) {
		String[] tokens = sentence.toLowerCase().split("\\s+");
		List<String> validTokens = new ArrayList<>();
		for (String token : tokens) {
			if (wordVectors.hasWord(token)) {
				validTokens.add(token);
			}
		}
		if (validTokens.isEmpty())
			return null;
		return wordVectors.getWordVectorsMean(validTokens);
	}

	public static void addMenuItem(List<MenuItem> menu, String name, int id) {
		INDArray vec = embedSentence(name);
		if (vec != null) {
			menu.add(new MenuItem(name, id, vec));
		}
	}

	public static MenuItem match(String userInput, List<MenuItem> menu) {
		INDArray inputVec = embedSentence(userInput);
		if (inputVec == null)
			return null;

		MenuItem best = null;
		double bestScore = -1;

		for (MenuItem item : menu) {
			double sim = cosineSimilarity(inputVec, item.vector);
			if (sim > bestScore) {
				bestScore = sim;
				best = item;
			}
		}
		return best;
	}
	
	public static List<MenuItem> matchTop10(String userInput, List<MenuItem> menu) {
	    INDArray inputVec = embedSentence(userInput);
	    if (inputVec == null)
	        return Collections.emptyList();

	    List<ScoredItem> scoredItems = new ArrayList<>();

	    for (MenuItem item : menu) {
	        double sim = cosineSimilarity(inputVec, item.vector);
	        scoredItems.add(new ScoredItem(item, sim));
	    }

	    scoredItems.sort((a, b) -> Double.compare(b.score(), a.score()));

	    List<MenuItem> top10 = new ArrayList<>();
	    for (int i = 0; i < Math.min(10, scoredItems.size()); i++) {
	        top10.add(scoredItems.get(i).item());
	    }

	    return top10;
	}


	private static double cosineSimilarity(INDArray v1, INDArray v2) {
		return v1.mul(v2).sumNumber().doubleValue() / (v1.norm2Number().doubleValue() * v2.norm2Number().doubleValue());
	}

	public static void main(String[] args) throws Exception {
		// load model, takes forever!
		Instant start = Instant.now();
		MenuSmartSearch.loadModel("glove.2024.wikigiga.50d.zip"); // about 1gb in size, uncompressed
		Instant end = Instant.now();

		long secondsElapsed = Duration.between(start, end).getSeconds();
		System.out.println("Finished loading model in: " + secondsElapsed + " seconds");

		// Add menu items
		List<MenuItem> menu = new ArrayList<>();
		start = Instant.now();
		MenuSmartSearch.addMenuItem(menu, "Royale with Cheese", 2233);
		MenuSmartSearch.addMenuItem(menu, "Classic Cheeseburger", 2234);
		MenuSmartSearch.addMenuItem(menu, "French Fries", 2235);
		MenuSmartSearch.addMenuItem(menu, "Coke Zero", 2236);
		end = Instant.now();

		secondsElapsed = Duration.between(start, end).toMillis();
		System.out.println("Finished loading menu into model in: " + secondsElapsed + " miliseconds");

		// Match user input
		String input = "can I get a burger with cheese";
		start = Instant.now();
		MenuItem result = MenuSmartSearch.match(input, menu);
		end = Instant.now();

		secondsElapsed = Duration.between(start, end).toMillis();
		System.out.println("Finished searching in: " + secondsElapsed + " miliseconds");

		if (result != null) {
			System.out.println("Matched: " + result.name + " (id " + result.id + ")");
		} else {
			System.out.println("No match found.");
		}
	}
}
