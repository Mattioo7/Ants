package pl.edu.pw.ants.parsers;

import pl.edu.pw.ants.models.Problem;

public interface Parser {
    Problem parse(String filePath);
}
