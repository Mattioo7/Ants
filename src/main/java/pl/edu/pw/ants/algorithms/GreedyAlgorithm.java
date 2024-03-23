package pl.edu.pw.ants.algorithms;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class GreedyAlgorithm implements CvrpAlgorithm {

    private final Problem problem;

    public List<List<Node>> solve(boolean printIteration, boolean printRoutes) {
        List<List<Node>> routes = new ArrayList<>();
        List<Node> unvisitedNodes = new ArrayList<>(problem.getNodes());
        Node depot = unvisitedNodes.stream().filter(node -> node.id() == problem.getDepotId()).findFirst().orElse(null);
        unvisitedNodes.remove(depot);
        HashMap<Integer, Integer> demands = problem.getDemands();
        double vehicleCapacity = problem.getVehicleCapacity();

        while (!unvisitedNodes.isEmpty()) {
            List<Node> route = new ArrayList<>();
            route.add(depot); // Start from the depot
            Node current = depot;
            double rangeLeft = problem.getVehicleRange();
            double capacityLeft = vehicleCapacity; // Reset capacity for each vehicle

//            System.out.println("\nRange left: " + String.format("%.2f", rangeLeft) + " Capacity left: " + (capacityLeft < 10 ? " " : "") + capacityLeft);
            while (!unvisitedNodes.isEmpty() && capacityLeft > 0) {
//                Node next = selectNextNode(current, unvisitedNodes, rangeLeft, depot, capacityLeft);
                Node next = selectNearestNode(current, unvisitedNodes, rangeLeft, depot, capacityLeft);
                if (next == null) {
                    // No reachable node that allows return to depot or capacity exceeded, break to start new route
                    break;
                }
                double demand = demands.get(next.id());
                route.add(next);
                rangeLeft -= distance(current, next);
                capacityLeft -= demand;
                current = next;
                unvisitedNodes.remove(next);

//                System.out.println("Range left: " + String.format("%.2f", rangeLeft) + " Capacity left: " + (capacityLeft < 10 ? " " : "") + capacityLeft);
            }

            route.add(depot); // Return to depot
            routes.add(route);
        }

        return routes;
    }

    // Selects the next node to visit that meets the capacity constraints
    private Node selectNextNode(Node current, List<Node> unvisitedNodes, double rangeLeft, Node depot, double capacityLeft) {
        Node next = null;
        double minDistance = Double.MAX_VALUE;
        HashMap<Integer, Integer> demands = problem.getDemands();

        for (Node candidate : unvisitedNodes) {
            double toCandidate = distance(current, candidate);
            double returnToDepot = distance(candidate, depot);
            double demand = demands.get(candidate.id());
            if (toCandidate + returnToDepot <= rangeLeft && toCandidate < minDistance && demand <= capacityLeft) {
                minDistance = toCandidate;
                next = candidate;
            }
        }

        return next;
    }

    // Select the nearest node to visit, null if nearest node exceeds capacity
    private Node selectNearestNode(Node current, List<Node> unvisitedNodes, double rangeLeft, Node depot, double capacityLeft) {
        Node next = null;
        double minDistance = Double.MAX_VALUE;
        HashMap<Integer, Integer> demands = problem.getDemands();

        for (Node candidate : unvisitedNodes) {
            double toCandidate = distance(current, candidate);
            double returnToDepot = distance(candidate, depot);
            if (toCandidate + returnToDepot <= rangeLeft && toCandidate < minDistance) {
                minDistance = toCandidate;
                next = candidate;
            }
        }

        if (next == null) {
            throw new RuntimeException();
        }

        double demand = demands.get(next.id());
        if (demand > capacityLeft) {
            return null;
        }

        return next;
    }

    private double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x() - b.x(), 2) + Math.pow(a.y() - b.y(), 2));
    }

    private double calculateRouteCost(List<Node> route) {
        double cost = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            cost += distance(route.get(i), route.get(i + 1));
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
                // Check if depot should be skipped
                if (skipDepot && (i == 0 || i == route.size() - 1)) {
                    continue; // Skip printing the depot node if skipDepot is true
                }
                System.out.print(route.get(i).id() + (i < route.size() - 1 ? " " : ""));
            }
            System.out.println();
            totalCost += calculateRouteCost(route);
        }

        System.out.println("Cost " + (int)totalCost);
    }
}

