package application;

import models.Event;
import models.FileFormatException;
import models.Link;
import models.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Terminal {
    private ParserConfig parserConfig;
    private List<RPIApp> apps = new LinkedList();

    public Terminal(String filePath) throws IOException, FileFormatException {
       this.parserConfig = new ParserConfig(filePath);
    }
    public void run() {
        this.initApps(this.parserConfig.getNodes(), this.parserConfig.getLinks(), this.parserConfig.getEvents());
    }
    private void initApps(List<Node> nodes, List<Link> links, List<Event> events) {
        List<RPIApp> neighbors = new ArrayList();

        /**
         * Creation of the RootDevice (id==1) and others RPIApps
         */
        for(Node node : nodes){
            if (node.id == 1) {
                apps.add(0, new RootDevice(node));
            } else {
                apps.add((node.id-1), new RPIApp(node));
            }
        }

        /**
         * Linking each neighbors of RPIApp
         */
        for(Link link : links){
            RPIApp rPIAppTemp = apps.get(link.destinationId-1);
            apps.get(link.sourceId-1).addNeighbor(rPIAppTemp);
        }
        System.out.println(apps);

    }
}
