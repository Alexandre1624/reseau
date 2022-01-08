package application;

import config.InjectionProperties;
import models.Event;
import models.FileFormatException;
import models.Link;
import models.Node;
import shared.Utils;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

public class Terminal {
    private ParserConfig parserConfig;
    private Map<Integer,RPIApp> devices = new HashMap<>();// we get device in O(1)
    private List<Event> events;

    public Terminal(String filePath) throws IOException, FileFormatException {
       this.parserConfig = new ParserConfig(filePath);
    }

    public void run() {
        this.initApps(this.parserConfig.getNodes(), this.parserConfig.getLinks(), this.parserConfig.getEvents());
    }

    private void initApps(List<Node> nodes, List<Link> links, List<Event> events) {   
        InjectionProperties injectionProperties = new InjectionProperties();
        Properties propsRPIApp = injectionProperties.getProperties().get("RPIApp.properties");
        int delay = Integer.valueOf(propsRPIApp.getProperty("delay"));
        double requestedTemperature = Double.valueOf(propsRPIApp.getProperty("temperature"));
        int precision = Integer.valueOf(propsRPIApp.getProperty("precision"));

        this.events = events;

        /**
         * Creation of the RootDevice (id==1) and others RPIApps
         * more legibility with stream java
         */
        devices = nodes.stream().collect(Collectors.toMap(node -> node.id, node -> {
            try {
                return node.id ==1 ? new RootDevice(node, requestedTemperature, precision): new RPIApp(node, delay);
            } catch (SocketException e) {
                //e.printStackTrace();
                Utils.log.warning(e.getMessage());
            }
            return null;
        }));

        /**
         * Linking each neighbors of RPIApp
         */
        for(Link link : links){
             devices.get(link.sourceId).addNeighbor(devices.get(link.destinationId));
        }
        //System.out.println(this.devices);
        this.runApps();
    }

    private void runApps() {
        /**
         * Run apps
         */
        devices.forEach((key, value) -> {
            if(key!=1) {
                value.createThread();
            }

        });
        this.runTraffic();
    }
    //just send list of event to different node
    private void runTraffic() {
        RootDevice rootDevice = (RootDevice) devices.get(1);
        rootDevice.setEvents(this.events);
        rootDevice.start();
    }
}
