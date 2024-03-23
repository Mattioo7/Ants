package pl.edu.pw.ants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.edu.pw.ants.config.AppConfig;

@SpringBootApplication
@EnableConfigurationProperties({AppConfig.class})
public class AntsApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(AntsApplication.class, args)));
    }

}
