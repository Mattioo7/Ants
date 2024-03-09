package pl.edu.pw.ants.algorithms;

import pl.edu.pw.ants.models.Node;

import java.util.List;

public interface CvrpAlgorithm {
    List<List<Node>> solve();
    void printRoutes(List<List<Node>> routes);
}
