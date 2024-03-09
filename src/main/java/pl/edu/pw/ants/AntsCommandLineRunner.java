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
//        problem.print();

        List<List<Node>> routes;

//        CvrpAlgorithm greedyAlgorithm = new GreedyAlgorithm(problem, 1000);
//        routes = greedyAlgorithm.solve();
//        greedyAlgorithm.printRoutes(routes);

        CvrpAlgorithm antColonyOptimization = new AntColonyOptimization(problem, 10, 1.0, 2.0, 0.5, 10.0, 1.0, 5000);
        routes = antColonyOptimization.solve();
        antColonyOptimization.printRoutes(routes);
    }
}
