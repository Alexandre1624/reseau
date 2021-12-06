package application;

import models.Node;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

public class RPIApp extends Thread{
    private int idNode;
    private int port;
    private InetAddress adress;
    private List<RPIApp> neighboors;
    private RPIApp bestReceiver = null;
    private int bestDistance;
    private UdpManager udpManager;
    private Logger log;

    public RPIApp(Node node) {

    }

    public void run() {// method from thread

    }

    public void addNeighboors(RPIApp rPIApp ) {

    }

    protected void sendPacketDistance() {

    }

    protected void receivePacketDistance() {

    }



}
