package pl.edu.pw.ants.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class Problem {
    String name;
    String comment;
    int numberOfVehicles;
    int vehicleCapacity;
    int vehicleRange;
    int depotId;
    ArrayList<Node> nodes = new ArrayList<>();
    HashMap<Integer, Integer> demands = new HashMap<>();

    public void print() {
        System.out.println("Name: " + name);
        System.out.println("Comment: " + comment);
        System.out.println("Number of Vehicles: " + numberOfVehicles);
        System.out.println("Vehicle Capacity: " + vehicleCapacity);
        System.out.println("Vehicle Range: " + vehicleRange);
        System.out.println("Depot ID: " + depotId);
        System.out.println("Nodes:");
        for (Node node : nodes) {
            System.out.println("ID: " + node.id() + " X: " + node.x() + " Y: " + node.y());
        }
        System.out.println("Demands:");
        demands.forEach((id, demand) -> System.out.println("ID: " + id + " Demand: " + demand));
    }
}
