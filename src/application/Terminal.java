package application;

import models.Event;
import models.FileFormatException;
import models.Link;
import models.Node;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Terminal {
    private ParserConfig parserConfig;
    private Map<Integer,RPIApp> devices = new HashMap<>();// we get device in O(1)

    public Terminal(String filePath) throws IOException, FileFormatException {
       this.parserConfig = new ParserConfig(filePath);
    }
    public void run() {
        this.initApps(this.parserConfig.getNodes(), this.parserConfig.getLinks(), this.parserConfig.getEvents());
    }
    private void initApps(List<Node> nodes, List<Link> links, List<Event> events) {
        /**
         * Creation of the RootDevice (id==1) and others RPIApps
         * more legibility with stream java
         */
        devices = nodes.stream().collect(Collectors.toMap(node -> node.id, node -> node.id ==1 ? new RootDevice(node): new RPIApp(node)));

        /**
         * Linking each neighbors of RPIApp
         */
        for(Link link : links){
             devices.get(link.sourceId).addNeighbor(devices.get(link.destinationId));
        }

        this.runApps();
    }

    private void runApps() {
        /**
         * Run apps
         */
        devices.forEach((key, value) -> {
            value.run();
        });
    }

}
