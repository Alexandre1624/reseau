package models;

import java.util.ArrayList;
import java.util.List;

public class Event{
    public final int nodeId;
    public final int delay;
    public final List<String> args;

    public Event(int nodeId, int delay, ArrayList<String> args){
        this.nodeId = nodeId;
        this.delay = delay;
        this.args = args;
    }
    public String toString(){
        return String.format("[Event for Node#%d | delay:%dms | args:%s]",nodeId, delay, args);
    }
}