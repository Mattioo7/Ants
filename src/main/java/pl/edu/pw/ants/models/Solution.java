package pl.edu.pw.ants.models;

import lombok.Data;

import java.util.List;

@Data
public class Solution {
    private final double cost;
    private final List<List<Node>> routes;
}
