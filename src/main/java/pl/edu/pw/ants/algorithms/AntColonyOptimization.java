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
    public List<List<Node>> solve(boolean printIteration, boolean printRoutes) {
        initializePheromones();
        calculateDistances();

        Solution bestSolution = null;
        double bestCost = Double.MAX_VALUE;

        for (int iter = 0; iter < iterations; iter++) {
            List<Solution> solutions = constructSolutions();

            Solution bestSolutionInIteration = solutions.stream().min(Comparator.comparingDouble(Solution::getCost)).orElse(null);

            if (bestSolutionInIteration == null) {
                continue;
            }

            if (bestSolutionInIteration.getCost() < bestCost) {
                bestCost = bestSolutionInIteration.getCost();
                bestSolution = bestSolutionInIteration;

                if (printIteration) {
                    System.out.println("Iteration " + iter + " Best cost: " + bestCost);
                }
                if (printRoutes) {
                    bestSolution.printRoutes(false);
                }
            }

            updatePheromones(solutions);
        }

        if (bestSolution == null) {
            return null;
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
        for (int currentAnt = 0; currentAnt < antCount; currentAnt++) {
            List<Node> unvisited = new ArrayList<>(problem.getNodes());
            List<Node> route = new ArrayList<>();
            List<List<Node>> routes = new ArrayList<>();
            double remainingCapacity = problem.getVehicleCapacity();
            double remainingRange = problem.getVehicleRange();
            Node depot = unvisited.stream().filter(node -> node.id() == problem.getDepotId()).findFirst().orElse(null);
            Node current = depot;

            boolean isSolutionFeasible = true;

            unvisited.remove(depot);
            route.add(depot);

            while (!unvisited.isEmpty()) {
                Node next = selectNextNodeBasedOnDemandAndCapacity(current, unvisited, remainingCapacity, remainingRange);
                if (next == null) { // No next node found or constraints reached
                    route.add(depot); // Return to depot
                    routes.add(new ArrayList<>(route)); // Save this route
                    route.clear(); // Start new route
                    route.add(depot);
                    remainingCapacity = problem.getVehicleCapacity(); // Reset capacity for the new route
                    remainingRange = problem.getVehicleRange(); // Reset range for the new route
                    current = depot;

                    Node canReachAnyOfRemainingNodes = selectNextNodeBasedOnDemandAndCapacity(current, unvisited, remainingCapacity, remainingRange);
                    if (canReachAnyOfRemainingNodes != null) {
                    } else {
                        isSolutionFeasible = false;
                        break; // No more nodes can be visited
                    }

                    continue;
                }
                double distanceToNext = distances[current.id()][next.id()];
                route.add(next);
                unvisited.remove(next);
                remainingCapacity -= problem.getDemands().get(next.id());
                remainingRange -= distanceToNext; // Update remaining range after moving to the next node
                current = next;
            }
            route.add(depot); // Ensure to return to depot for the last route
            routes.add(route);

            if (isSolutionFeasible) {
                solutions.add(new Solution(calculateRoutesCost(routes), routes));
            }
        }
        return solutions;
    }

    private Node selectNextNodeBasedOnDemandAndCapacity(Node current, List<Node> unvisited, double remainingCapacity, double remainingRange) {
        double[] probabilities = new double[unvisited.size()];
        double probabilitySum = 0.0;
        boolean isCandidateAvailable = false;

        // Calculate probabilities for each unvisited node, considering remaining capacity and range
        for (int i = 0; i < unvisited.size(); i++) {
            Node candidate = unvisited.get(i);
            double distanceToCandidate = distances[current.id()][candidate.id()];
            double returnToDepotDistance = distances[candidate.id()][problem.getDepotId()];

            // Check if the candidate node can be visited without exceeding capacity and if there's enough range to return to the depot
            if (problem.getDemands().get(candidate.id()) <= remainingCapacity && (distanceToCandidate + returnToDepotDistance) <= remainingRange) {
                isCandidateAvailable = true;
                double pheromoneLevel = pheromones[current.id()][candidate.id()];
                double visibility = 1.0 / distanceToCandidate;
                probabilities[i] = Math.pow(pheromoneLevel, alpha) * Math.pow(visibility, beta);
                probabilitySum += probabilities[i];
            } else {
                probabilities[i] = 0; // This candidate is not feasible due to capacity or range constraint
            }
        }

        if (!isCandidateAvailable) return null; // If no candidate is available due to capacity or range constraints

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
            double pheromoneDeposit = Q / solution.getCost();
            for (List<Node> route : solution.getRoutes()) {
                for (int i = 0; i < route.size() - 1; i++) {
                    int from = route.get(i).id();
                    int to = route.get(i + 1).id();
                    pheromones[from][to] += pheromoneDeposit;
//                    pheromones[to][from] += pheromoneDeposit;
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
    public void printRoutes(List<List<Node>> routes, boolean skipDepot) {
        int routeNumber = 1;
        double totalCost = 0.0;

        for (List<Node> route : routes) {
            System.out.print("Route #" + routeNumber++ + ": ");
            for (int i = 0; i < route.size(); i++) {
                if (skipDepot && (i == 0 || i == route.size() - 1)) {
                    continue;
                }
                System.out.print(route.get(i).id() + (i < route.size() - 1 ? " " : ""));
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
