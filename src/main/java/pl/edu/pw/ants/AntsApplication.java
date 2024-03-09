package pl.edu.pw.ants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AntsApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(AntsApplication.class, args)));
    }

}
