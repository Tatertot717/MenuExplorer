package teneo.MenuExplorer.client;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

import java.util.List;

import org.springframework.cloud.openfeign.support.SpringMvcContract;

public class ClientTest {

    public static void main(String[] args) {
        String baseUrl = "http://localhost:8080";

        FMenu menuClient = Feign.builder()
        	    .contract(new SpringMvcContract())
        	    .encoder(new GsonEncoder())
        	    .decoder(new GsonDecoder())
        	    .target(FMenu.class, baseUrl);

        List<Integer> order = List.of(1, 2, 3);

        String orderTitle = menuClient.getOrderTitle(order);
        System.out.println("Order title: " + orderTitle);
    }
}