package pl.edu.pw.ants.algorithms;

import pl.edu.pw.ants.models.Node;

import java.util.List;

public interface CvrpAlgorithm {
    List<List<Node>> solve(boolean printIteration, boolean printRoutes);
    void printRoutes(List<List<Node>> routes, boolean skipDepot);
}
