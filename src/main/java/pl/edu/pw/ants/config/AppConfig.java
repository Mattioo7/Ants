package pl.edu.pw.ants.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private boolean useCommandLineParameters = false;
    private boolean printProblem = false;
    private boolean printIterations = false;
    private boolean printRoutes = false;
    private String filePath;
    private int numberOfEliteAnts = 0;
    private int reinforcementOnEliteAnts = 0;
}
