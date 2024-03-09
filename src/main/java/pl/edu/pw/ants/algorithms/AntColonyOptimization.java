package pl.edu.pw.ants.algorithms;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class AntColonyOptimization implements CvrpAlgorithm {

    private final Problem problem;
    private final int antCount;
    private final double alpha; // Pheromone importance
    private final double beta; // Distance importance
    private final double evaporation;
    private final double Q; // Pheromone deposit factor
    private final double pheromoneInitial;
    private final int iterations;
    private final Random random = new Random();

    private double[][] pheromones;
    private double[][] distances;

    @Override
    public List<List<Node>> solve() {
        initializePheromones();
        calculateDistances();

        List<List<Node>> bestSolution = null;
        double bestCost = Double.MAX_VALUE;

        for (int iter = 0; iter < iterations; iter++) {
            List<List<Node>> solution = constructSolution();

            double cost = calculateSolutionCost(solution);
            if (cost < bestCost) {
                bestCost = cost;
                bestSolution = solution;
            }

            updatePheromones(solution, cost);
        }

        return bestSolution;
    }

    private void initializePheromones() {
        int n = problem.getNodes().size();
        pheromones = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                pheromones[i][j] = pheromoneInitial;
            }
        }
    }

    private void calculateDistances() {
        List<Node> nodes = problem.getNodes();
        int n = nodes.size();
        distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distances[i][j] = distance(nodes.get(i), nodes.get(j));
            }
        }
    }

    private List<List<Node>> constructSolution() {
        // TODO: Maybe I can do it in parallel?

        List<List<Node>> solutions = new ArrayList<>();
        for (int k = 0; k < antCount; k++) {
            List<Node> unvisited = new ArrayList<>(problem.getNodes());
            List<Node> route = new ArrayList<>();
            Node depot = unvisited.stream().filter(node -> node.id() == problem.getDepotId()).findFirst().orElse(null);
            Node current = depot;

            unvisited.remove(depot);
            route.add(depot);

            while (!unvisited.isEmpty()) {
                Node next = selectNextNode(current, unvisited); // TODO: check capacity and range constraints
                if (next == null) { // No next node found (probably due to capacity constraints)
                    route.add(depot); // Return to depot
                    solutions.add(new ArrayList<>(route)); // Save this route
                    route.clear(); // Start new route
                    route.add(depot);
                    current = depot;
                    continue;
                }
                route.add(next);
                unvisited.remove(next);
                current = next;
            }
            route.add(depot); // Return to depot for the last route
            solutions.add(route);
        }
        return solutions;
    }

    private Node selectNextNode(Node current, List<Node> unvisited) {
        double[] probabilities = new double[unvisited.size()];
        double probabilitySum = 0.0;

        // Calculate probabilities for each unvisited node
        for (int i = 0; i < unvisited.size(); i++) {
            Node candidate = unvisited.get(i);
            double pheromoneLevel = pheromones[current.id()][candidate.id()];
            double visibility = 1.0 / distances[current.id()][candidate.id()];
            probabilities[i] = Math.pow(pheromoneLevel, alpha) * Math.pow(visibility, beta);
            probabilitySum += probabilities[i];
        }

        // Normalize probabilities
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= probabilitySum;
        }

        // Select next node based on calculated probabilities
        return selectNextNodeBasedOnProbability(unvisited, probabilities);
    }

    private Node selectNextNodeBasedOnProbability(List<Node> unvisited, double[] probabilities) {
        double randomValue = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (int i = 0; i < unvisited.size(); i++) {
            cumulativeProbability += probabilities[i];
            if (randomValue <= cumulativeProbability) {
                return unvisited.get(i);
            }
        }

        return unvisited.get(unvisited.size() - 1); // FIXME: Fallback to prevent null in case of rounding errors
    }

    private void updatePheromones(List<List<Node>> solutions, double cost) {
        for (double[] row : pheromones) {
            Arrays.fill(row, row[0] * (1 - evaporation));
        }

        for (List<Node> route : solutions) {
            double routeCost = calculateRouteCost(route);
            double deposit = Q / routeCost;
            for (int i = 0; i < route.size() - 1; i++) {
                Node a = route.get(i);
                Node b = route.get(i + 1);
                pheromones[a.id()][b.id()] += deposit;
                pheromones[b.id()][a.id()] += deposit;
            }
        }
    }

    private double calculateSolutionCost(List<List<Node>> solutions) {
        double totalCost = 0;
        for (List<Node> route : solutions) {
            totalCost += calculateRouteCost(route);
        }
        return totalCost;
    }

    private double calculateRouteCost(List<Node> route) {
        double cost = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            cost += distances[route.get(i).id()][route.get(i + 1).id()];
        }
        return cost;
    }


    @Override
    public void printRoutes(List<List<Node>> routes) {
        int routeNumber = 1;
        double totalCost = 0.0;

        for (List<Node> route : routes) {
            System.out.print("Route #" + routeNumber++ + ": ");
            for (int i = 1; i < route.size() - 1; i++) { // Start from 1 and end before last to skip depot
                System.out.print(route.get(i).id() + (i < route.size() - 2 ? " " : ""));
            }
            System.out.println();
            totalCost += calculateRouteCost(route);
        }

        System.out.println("Cost " + (int)totalCost);
    }

    private double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x() - b.x(), 2) + Math.pow(a.y() - b.y(), 2));
    }
}