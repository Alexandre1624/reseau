package application;

import models.Event;
import models.FileFormatException;
import models.Link;
import models.Node;

import java.io.IOException;
import java.util.List;

public class Terminal {
    private ParserConfig parserConfig;
    private RPIApp apps;

    Terminal(String filePath) throws IOException, FileFormatException {
       this.parserConfig = new ParserConfig(filePath);
    }
    public void run() {

    }
    private void initApps(List<Node> nodes, List<Link> links, List<Event> events) {

    }
}
