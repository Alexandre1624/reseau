package models;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node{
    public final int id;
    public final InetAddress address;
    public final int port;

    public Node(int id, String address, int port) throws UnknownHostException {
        this.id = id;
        this.address = InetAddress.getByName(address);
        this.port = port;
    }
    public String toString(){
        return String.format("[Node#%d | address:%s | port:%d]",id, address, port);
    }
}