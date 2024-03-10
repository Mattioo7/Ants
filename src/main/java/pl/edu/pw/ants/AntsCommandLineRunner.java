package pl.edu.pw.ants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.edu.pw.ants.algorithms.AntColonyOptimization;
import pl.edu.pw.ants.algorithms.CvrpAlgorithm;
import pl.edu.pw.ants.algorithms.GreedyAlgorithm;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;
import pl.edu.pw.ants.parsers.Parser;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AntsCommandLineRunner implements CommandLineRunner {

    private final Parser parser;

    @Override
    public void run(String... args) {
        log.info("Starting AntsCommandLineRunner");
        Problem problem = parser.parse();
        problem.print();

        boolean skipDepot = false;
        List<List<Node>> routes;

        System.out.println("\nRunning greedy algorithm");
        CvrpAlgorithm greedyAlgorithm = new GreedyAlgorithm(problem);
        routes = greedyAlgorithm.solve();
        greedyAlgorithm.printRoutes(routes, skipDepot);

        System.out.println("\nRunning ant colony optimization algorithm");
        CvrpAlgorithm antColonyOptimization = new AntColonyOptimization(problem, 10, 1.0, 2.0, 0.5, 10.0, 1.0, 5000);
        routes = antColonyOptimization.solve();
        if (routes == null) {
            System.out.println("No solution found");
            return;
        }
        antColonyOptimization.printRoutes(routes, skipDepot);
    }
}
