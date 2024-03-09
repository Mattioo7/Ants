package pl.edu.pw.ants.algorithms;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;
import pl.edu.pw.ants.models.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

        Solution bestSolution = null;
        double bestCost = Double.MAX_VALUE;

        for (int iter = 0; iter < iterations; iter++) {
            List<Solution> solutions = constructSolutions();

            Solution bestSolutionInIteration = solutions.stream().min(Comparator.comparingDouble(Solution::getCost)).orElse(null);
            if (bestSolutionInIteration.getCost() < bestCost) {
                bestCost = bestSolutionInIteration.getCost();
                bestSolution = bestSolutionInIteration;
            }

            updatePheromones(solutions);
        }

        return bestSolution.getRoutes(); // FIXME: Return best solution
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

    private List<Solution> constructSolutions() {
        List<Solution> solutions = new ArrayList<>();
        for (int k = 0; k < antCount; k++) {
            List<Node> unvisited = new ArrayList<>(problem.getNodes());
            List<Node> route = new ArrayList<>();
            List<List<Node>> routes = new ArrayList<>();
            double remainingCapacity = problem.getVehicleCapacity();
            Node depot = unvisited.stream().filter(node -> node.id() == problem.getDepotId()).findFirst().orElse(null);
            Node current = depot;

            unvisited.remove(depot);
            route.add(depot);

            while (!unvisited.isEmpty()) {
                Node next = selectNextNodeBasedOnDemandAndCapacity(current, unvisited, remainingCapacity);
                if (next == null) { // No next node found or capacity constraints reached
                    route.add(depot); // Return to depot
                    routes.add(new ArrayList<>(route)); // Save this route
                    route.clear(); // Start new route
                    route.add(depot);
                    remainingCapacity = problem.getVehicleCapacity(); // Reset capacity for the new route
                    current = depot;
                    continue;
                }
                route.add(next);
                unvisited.remove(next);
                remainingCapacity -= problem.getDemands().get(next.id());
                current = next;
            }
            route.add(depot); // Return to depot for the last route
            routes.add(route);
            solutions.add(new Solution(calculateRoutesCost(routes), routes));
        }
        return solutions;
    }

    private Node selectNextNodeBasedOnDemandAndCapacity(Node current, List<Node> unvisited, double remainingCapacity) {
        double[] probabilities = new double[unvisited.size()];
        double probabilitySum = 0.0;
        boolean isCandidateAvailable = false;

        // Calculate probabilities for each unvisited node, considering remaining capacity
        for (int i = 0; i < unvisited.size(); i++) {
            Node candidate = unvisited.get(i);
            if (problem.getDemands().get(candidate.id()) <= remainingCapacity) {
                isCandidateAvailable = true;
                double pheromoneLevel = pheromones[current.id()][candidate.id()];
                double visibility = 1.0 / distances[current.id()][candidate.id()];
                probabilities[i] = Math.pow(pheromoneLevel, alpha) * Math.pow(visibility, beta);
                probabilitySum += probabilities[i];
            } else {
                probabilities[i] = 0; // This candidate is not feasible due to capacity constraint
            }
        }

        if (!isCandidateAvailable) return null; // If no candidate is available due to capacity constraints

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

    private void updatePheromones(List<Solution> solutions) {
        for (double[] row : pheromones) {
            Arrays.fill(row, row[0] * (1 - evaporation));
        }

        for (Solution solution : solutions) {
            double pheromoneDeposit = Q / solution.getCost(); // FIXME: Check if this is correct
            for (List<Node> route : solution.getRoutes()) {
                for (int i = 0; i < route.size() - 1; i++) {
                    int from = route.get(i).id();
                    int to = route.get(i + 1).id();
                    pheromones[from][to] += pheromoneDeposit;
                    pheromones[to][from] += pheromoneDeposit;
                }
            }
        }
    }

    private double calculateRoutesCost(List<List<Node>> routes) {
        double cost = 0;
        for (List<Node> route : routes) {
            cost += calculateRouteCost(route);
        }
        return cost;
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
