package application;

import config.InjectionProperties;
import models.Event;
import models.FileFormatException;
import models.Link;
import models.Node;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

public class Terminal {
    private ParserConfig parserConfig;
    private Map<Integer,RPIApp> devices = new HashMap<>();// we get device in O(1)
    private List<Event> events;

    public Terminal(String filePath) throws  FileFormatException, IOException, FileFormatException {
       this.parserConfig = new ParserConfig(filePath);
    }
    public void run() throws InterruptedException, IOException {

        this.initApps(this.parserConfig.getNodes(), this.parserConfig.getLinks(), this.parserConfig.getEvents());
    }
    private void initApps(List<Node> nodes, List<Link> links, List<Event> events) throws InterruptedException, IOException {
        /**
         * Creation of the RootDevice (id==1) and others RPIApps
         * more legibility with stream java
         */
        InjectionProperties injectionProperties = new InjectionProperties();
        Properties delayRPIApp = injectionProperties.getProperties().get("RPIApp.properties");
        int delay = Integer.valueOf(delayRPIApp.getProperty("delay"));
        this.events = events;
        devices = nodes.stream().collect(Collectors.toMap(node -> node.id, node -> {
            try {
                return node.id ==1 ? new RootDevice(node): new RPIApp(node, delay);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }));

        /**
         * Linking each neighbors of RPIApp
         */
        for(Link link : links){
             devices.get(link.sourceId).addNeighbor(devices.get(link.destinationId));
        }
        System.out.println(this.devices);
        this.runApps();
    }

    private void runApps() throws InterruptedException, IOException {
        /**
         * Run apps
         */
        devices.forEach((key, value) -> {
            value.createThread();
        });
        this.runTraffic();
    }
    //just send des list of event to different node
    private void runTraffic() throws InterruptedException, IOException {
        for (Event event : this.events) {
            if (event.nodeId==1) {
                RootDevice rootDevice = (RootDevice) devices.get(event.nodeId);
                rootDevice.sendMessage(event);
            }
        }
    }
}
