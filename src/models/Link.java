package models;

public class Link{
    public final int sourceId, destinationId;

    public Link(int sourceId, int destinationId){
        this.sourceId = sourceId;
        this.destinationId = destinationId;
    }
    public String toString(){
        return String.format("[Node#%d is connected to Node#%d]",sourceId, destinationId);
    }
}