package teneo.MenuExplorer.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// You can adjust scanning if your shared logic is in other packages
@SpringBootApplication
@ComponentScan(basePackages = "teneo.MenuExplorer")
public class MenuExplorerServer {

    public static void main(String[] args) {
        SpringApplication.run(MenuExplorerServer.class, args);
    }
}
