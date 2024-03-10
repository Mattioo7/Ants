package pl.edu.pw.ants.models;

import lombok.Data;

import java.util.List;

@Data
public class Solution {
    private final double cost;
    private final List<List<Node>> routes;

    public void printRoutes(boolean skipDepot) {
        int routeNumber = 1;

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
        }

        System.out.println("Cost " + (int)cost);
    }
}
