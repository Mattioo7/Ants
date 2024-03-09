package pl.edu.pw.ants.algorithms;

import lombok.RequiredArgsConstructor;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GreedyAlgorithm implements CvrpAlgorithm {

    private final Problem problem;
    private final int vehicleRange; // Assuming this is the maximum distance a vehicle can travel

    public List<List<Node>> solve() {
        List<List<Node>> routes = new ArrayList<>();
        List<Node> unvisitedNodes = new ArrayList<>(problem.getNodes());
        Node depot = unvisitedNodes.stream().filter(node -> node.id() == problem.getDepotId()).findFirst().orElse(null);
        unvisitedNodes.remove(depot);

        while (!unvisitedNodes.isEmpty()) {
            List<Node> route = new ArrayList<>();
            route.add(depot); // Start from the depot
            Node current = depot;
            double rangeLeft = vehicleRange;

            while (!unvisitedNodes.isEmpty()) {
                Node next = selectNextNode(current, unvisitedNodes, rangeLeft, depot);
                if (next == null) {
                    // No reachable node that allows return to depot, break to start new route
                    break;
                }
                route.add(next);
                rangeLeft -= distance(current, next) + distance(next, depot); // Update range left considering return to depot
                current = next;
                unvisitedNodes.remove(next);
            }

            route.add(depot); // Return to depot
            routes.add(route);
        }

        return routes;
    }

    private Node selectNextNode(Node current, List<Node> unvisitedNodes, double rangeLeft, Node depot) {
        Node next = null;
        double minDistance = Double.MAX_VALUE;

        for (Node candidate : unvisitedNodes) {
            double toCandidate = distance(current, candidate);
            double returnToDepot = distance(candidate, depot);
            if (toCandidate + returnToDepot <= rangeLeft && toCandidate < minDistance) {
                minDistance = toCandidate;
                next = candidate;
            }
        }

        return next;
    }

    private double distance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x() - b.x(), 2) + Math.pow(a.y() - b.y(), 2));
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

    private double calculateRouteCost(List<Node> route) {
        double cost = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            cost += distance(route.get(i), route.get(i + 1));
        }
        return cost;
    }
}

