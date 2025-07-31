package teneo.MenuExplorer.client;

import feign.Feign;
import feign.gson.GsonEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.springframework.cloud.openfeign.support.SpringMvcContract;

public class ClientTest {
	private static FMenu explorer;
	private static List<Integer> order = new ArrayList<>();
	private static final Scanner scanner = new Scanner(System.in);
	private static List<List<Integer>> cart;

    public static void main(String[] args) {
        String baseUrl = "http://localhost:8080";

        explorer = Feign.builder()
        	    .contract(new SpringMvcContract())
        	    .encoder(new GsonEncoder())
        	    .decoder(new StringDecoder())
        	    .requestInterceptor(new JsonRequestInterceptor())
        	    .target(FMenu.class, baseUrl);
        cart = explorer.getCart();

        System.out.println("Menu Explorer CLI started. Type 'help' for list of commands.");

		while (true) {
			System.out.print("> ");
			String input = scanner.nextLine().trim();
			if (input.isEmpty())
				continue;

			String[] parts = input.split("\\s+");
			String cmd = parts[0].toLowerCase();

			try {
				switch (cmd) {
				case "q":
				case "quit":
					System.out.println("Exiting.");
					return;

				case "help":
					printHelp();
					break;

				case "s":
				case "start":
					if (parts.length < 2) {
						System.out.println("Usage: s <rootId>");
					} else {
						int rootId = Integer.parseInt(parts[1]);
						order = explorer.startOrder(rootId);
						System.out.println("Started new order from ID " + rootId);
					}
					break;

				case "a":
				case "add":
					if (parts.length < 2) {
						System.out.println("Usage: a <itemId>");
					} else {
						int itemId = Integer.parseInt(parts[1]);
						explorer.addToOrder(itemId, order);
						System.out.println("Added item " + itemId);
					}
					break;

				case "r":
				case "remove":
					if (parts.length < 2) {
						System.out.println("Usage: r <itemId>");
					} else {
						int itemId = Integer.parseInt(parts[1]);
						explorer.removeFromOrder(itemId, order);
						System.out.println("Removed item " + itemId);
					}
					break;

				case "poo":
				case "printorderoptions":
					if (order == null || order.isEmpty())
						break;
					System.out.println(explorer.printOrderOptions(order));
					break;

				case "po":
				case "printorder":
					System.out.println(explorer.printOrder(order));
					break;

				case "pc":
				case "printcart":
					System.out.println(explorer.printCart());
					break;

				case "prc":
				case "pricecart":
					System.out.println("Cart Total Price: " + explorer.getCartTotalPrice());
					break;

				case "pro":
				case "priceorder":
					System.out.println("Order Price: " + explorer.getOrderPrice(order));
					break;

				case "sao":
				case "switchactiveorder":
					if (parts.length < 2) {
						System.out.println("Usage: sao <orderId>");
					} else {
						if (cart.isEmpty()) {
							System.out.println("Cart is empty.");
							break;
						}
						int id = Integer.parseInt(parts[1]);
						if (cart.size() - 1 > id || id < 1) {
							System.out.println("Invalid choice.");
							break;
						}

						order = cart.get(id - 1);
						System.out.println("Switched to active order " + id);
					}
					break;
					
				case "sm":
				case "search":
					if (parts.length < 2) {
						System.out.println("Usage: sm <query>");
					} else {
						System.out.println(explorer.searchTop10(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length))));
					}
					break;
					
				case "ro":
				case "removeorder":
					explorer.removeOrder(order);
					order = null;
					System.out.println("Removed current order");
					break;

				default:
					System.out.println("Unknown command. Type 'help' for list.");
					break;
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
    }
    
    private static void printHelp() {
		//text blocks not supported in java 11
		System.out.println(
				"Commands:\n" +
				"  s <id>        - start order from root ID\n" +
				"  a <id>        - add item to order\n" +
				"  r <id>        - remove item from order\n" +
				"  po            - print current order (just selected)\n" +
				"  poo           - print order options from current order\n" +
				"  pc            - print cart\n" +
				"  prc           - print cart total price\n" +
				"  pro           - print current order price\n" +
				"  sao <id>      - switch to active order ID\n" +
				"  ro            - remove current order\n" +
				"  sm <query>    - search menu by natural language\n" +
				"  help          - show this help\n" +
				"  q             - quit"
		);
	}
}