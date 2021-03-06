package application;

import models.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ParserConfig {

    private ParsingState currentState;
    private ArrayList<Node> nodes;
    private ArrayList<Link> links;
    private ArrayList<Event> events;

    public ParserConfig(String path) throws IOException, FileFormatException{
        currentState = ParsingState.NODES;

        nodes = new ArrayList<Node>();
        links = new ArrayList<Link>();
        events = new ArrayList<Event>();

        FileReader fr= new FileReader(path);
        BufferedReader br= new BufferedReader(fr);
        parseFile(br);
    }

    public ArrayList<Node> getNodes(){
        return nodes;
    }

    public ArrayList<Link> getLinks(){
        return links;
    }

    public ArrayList<Event> getEvents(){
        return events;
    }

    private void parseFile(BufferedReader reader) throws IOException, FileFormatException {
        String line;
        while((line = reader.readLine()) != null){
            if(line.equals("")){
                currentState = currentState.nextState();
            }
            else{
                parseLine(line);

            }
        }
    }

    private void parseLine(String line) throws IOException, FileFormatException{
        switch(currentState){
            case NODES:
                String[] args = line.split(" ");
                if(args.length != 3){
                    throw new FileFormatException("Format for node definition must be 'NODEID ADDRESS PORT'");
                }
                int nodeId = Integer.parseInt(args[0]);
                String address =  args[1];
                int port = Integer.parseInt(args[2]);
                nodes.add(new Node(nodeId, address, port));
                break;
            case LINKS:
                args = line.split(" ");
                if(args.length < 2){
                    throw new FileFormatException("Format for link definition must be 'NODEID NEIGHBOR1 NEIGHBOR2 ..'");
                }
                nodeId = Integer.parseInt(args[0]);
                for(int i = 1; i < args.length; i++){
                    int neighborId = Integer.parseInt(args[i]);
                    links.add(new Link(nodeId,neighborId));
                }
                break;
            case EVENTS:
                args = line.split(" ");
                if(args.length < 3){
                    throw new FileFormatException("Format for event definition must contain at least 3 arguments");
                }
                nodeId = Integer.parseInt(args[0]);
                int delay = Integer.parseInt(args[1]);
                ArrayList<String> eventArgs = new ArrayList<String>();
                for(int i = 2; i < args.length; i++){
                    eventArgs.add(args[i]);
                }
                events.add(new Event(nodeId, delay, eventArgs));
                break;
            default:
                break;
        }
    }

}
