package pl.edu.pw.ants.parsers.impl;

import org.springframework.stereotype.Component;
import pl.edu.pw.ants.models.Node;
import pl.edu.pw.ants.models.Problem;
import pl.edu.pw.ants.parsers.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Component
public class VrpFileParser implements Parser {

    @Override
    public Problem parse() {
        try {
            File file = new File("src/main/resources/vrp-data/A-n32-k5.vrp");
            Scanner scanner = new Scanner(file);
            Problem instance = new Problem();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("NAME")) {
                    instance.setName(line.split(":")[1].trim());
                } else if (line.startsWith("COMMENT")) {
                    instance.setComment(line.substring(line.indexOf(":") + 1).trim());
                } else if (line.startsWith("DIMENSION")) {
                    // Example of how to handle other fields if needed
                    // TODO: what is dimension?
                } else if (line.startsWith("CAPACITY")) {
                    instance.setVehicleCapacity(Integer.parseInt(line.split(":")[1].trim()));
                } else if (line.startsWith("NODE_COORD_SECTION")) {
                    while (scanner.hasNextInt()) {
                        int id = scanner.nextInt();
                        int x = scanner.nextInt();
                        int y = scanner.nextInt();
                        instance.getNodes().add(new Node(id - 1, x, y)); // Subtract 1 from id to start from 0
                    }
                } else if (line.startsWith("DEMAND_SECTION")) {
                    while (scanner.hasNextInt()) {
                        int id = scanner.nextInt();
                        int demand = scanner.nextInt();
                        instance.getDemands().put(id - 1, demand);
                    }
                } else if (line.startsWith("DEPOT_SECTION")) {
                    instance.setDepotId(scanner.nextInt());
                    // Assumes there's only one depot; break after reading it
                    break;
                }
            }
            instance.setNumberOfVehicles(30); // FIXME: Hardcoded for now

            scanner.close();
//            instance.print(); // Print the parsed data for verification
            return instance;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }
}
