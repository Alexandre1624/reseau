package application;

import models.Node;
import shared.Utils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RPIApp extends Thread{
    protected int idNode;
    protected InetAddress address;
    protected int port;
    protected List<RPIApp> neighbors = new ArrayList();
    protected RPIApp bestReceiver = null;
    protected int bestDistance = 0;
    private UDPManager udpManager = null;
    protected Logger log;
    protected DatagramSocket socket;
    // taille maximale d'un datagramme, utilisée pour le buffer de reception
    static int MAX_DGRAM_SIZE = 100;

    /**
     * Constructor of the class
     * @param Node node
     */
    public RPIApp(Node node) throws SocketException {
        this.idNode = node.id;
        this.address = node.address;
        this.port = node.port;

        log = Logger.getLogger(this.getClass().getSimpleName() + " " +  this.idNode);
    }

    /**
     * Overload toSTring() method
     * @return String
     */
    public String toString(){
        List<Integer> temp = new ArrayList();
        for(RPIApp neighbor : neighbors){ temp.add(neighbor.idNode); }
        return String.format("[RPIApp id:#%d | address:%s | port:%d | neighbors:%s]"+System.lineSeparator(),idNode, address, port, temp.toString());
    }

    public void run() {// method from thread
       /* try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        System.out.println(this.getClass().getSimpleName() +port);
        try {
            this.socket = new DatagramSocket(port);
            log.info( Utils.logInfo(this.idNode)+"start");
            DatagramPacket packet = new DatagramPacket(new byte[MAX_DGRAM_SIZE], MAX_DGRAM_SIZE);
            socket.receive(packet);
            System.out.println("receive "+ packet.toString());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Add a RPIApp neighbor to the neighbors list
     * @param RPIApp rPIApp
     */
    public void addNeighbor(RPIApp rPIApp) {
        this.neighbors.add(rPIApp);
    }

    /**
     * Remove a RPIApp neighbor to the neighbors list
     * @param rPIApp
     */
    public void removeNeighbor(RPIApp rPIApp) {
        // ne devrait être utilisé que dans la partie bonus + à adapter
        this.neighbors.remove(rPIApp);
    }

    // GETTERS //
    public int getIdNode() {
        return idNode;
    }
    public InetAddress getAddress() {
        return address;
    }
    public int getPort() {
        return port;
    }
    public List<RPIApp> getNeighbors() {
        return neighbors;
    }
    public RPIApp getBestReceiver() {
        return bestReceiver;
    }
    public int getBestDistance() {
        return bestDistance;
    }

    // SETTERS //
    public void setIdNode(int idNode) {
        this.idNode = idNode;
    }
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setNeighbors(List<RPIApp> neighbors) {
        this.neighbors = neighbors;
    }
    public void setBestReceiver(RPIApp bestReceiver) {
        this.bestReceiver = bestReceiver;
    }
    public final void setBestDistance(int bestDistance) {
        this.bestDistance = bestDistance;
    }

    protected void sendPacketDistance() {

    }

    protected void receivePacketDistance() {

    }

    public void createThread() {
        this.start();
    }
}
