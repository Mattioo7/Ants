package pl.edu.pw.ants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.edu.pw.ants.algorithms.AntColonyOptimization;
import pl.edu.pw.ants.algorithms.CvrpAlgorithm;
import pl.edu.pw.ants.algorithms.GreedyAlgorithm;
import pl.edu.pw.ants.config.AppConfig;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;
import pl.edu.pw.ants.parsers.Parser;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AntsCommandLineRunner implements CommandLineRunner {

    private final Parser problemParser;
    private final AppConfig appConfig;

    @Override
    public void run(String... args) {
        log.info("Starting AntsCommandLineRunner");

        if (appConfig.isUseCommandLineParameters()) {
            Options options = new Options();
            options.addOption("p", "printProblem", false, "Print the problem details");
            options.addOption("i", "printIteration", false, "Print iterations with best cost");
            options.addOption("r", "printRoutes", false, "Print routes for iterations");
            options.addOption("f", "file", true, "Put path to file");
            options.addOption("e", "numberOfEliteAnts", true, "Number of elite ants");
            options.addOption("ee", "reinforcementOnEliteAnts", true, "Reinforcement for elite ants");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;

            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.err.println("Error parsing command line arguments: " + e.getMessage());
                System.exit(1);
            }
            appConfig.setPrintProblem(cmd.hasOption("printProblem"));
            appConfig.setPrintIterations(cmd.hasOption("printIteration"));
            appConfig.setPrintRoutes(cmd.hasOption("printRoutes"));
            appConfig.setFilePath(cmd.getOptionValue("file"));
            appConfig.setNumberOfEliteAnts(Integer.parseInt(cmd.getOptionValue("numberOfEliteAnts", "0")));
            appConfig.setReinforcementOnEliteAnts(Integer.parseInt(cmd.getOptionValue("reinforcementOnEliteAnts", "0")));
        }

        Problem problem = problemParser.parse(appConfig.getFilePath());

        if (appConfig.isPrintProblem()) {
            problem.print();
        }

        boolean skipDepot = false;
        List<List<Node>> routes;

        for (int i = 0; i < 1; i++) {
            System.out.println("\nRunning greedy algorithm");
            CvrpAlgorithm greedyAlgorithm = new GreedyAlgorithm(problem);
            routes = greedyAlgorithm.solve(appConfig.isPrintIterations(), appConfig.isPrintRoutes());
            greedyAlgorithm.printRoutes(routes, skipDepot);
        }

        for (int i = 0; i < 1; i++) {
            System.out.println("\nRunning ant colony optimization algorithm");

            // FIXME: put parameters in a VO
            CvrpAlgorithm antColonyOptimization = new AntColonyOptimization(
                    problem,
                    10,
                    1.0,
                    2.0,
                    0.5,
                    10.0,
                    1.0,
                    5000,
                    appConfig.getNumberOfEliteAnts(),
                    appConfig.getReinforcementOnEliteAnts()
            );
            routes = antColonyOptimization.solve(appConfig.isPrintIterations(), appConfig.isPrintRoutes());
            if (routes == null) {
                System.out.println("No solution found");
                return;
            }
            antColonyOptimization.printRoutes(routes, skipDepot);
        }
    }
}
