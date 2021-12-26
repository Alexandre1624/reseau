package application;


import models.CommandDecrypted;
import models.CommandEncrypted;
import models.Node;
import shared.Utils;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class RPIApp extends Thread{
    protected int idNode;
    protected InetAddress address;
    protected int port;
    protected List<RPIApp> neighbors = new ArrayList();
    protected RPIApp bestReceiver = null;
    protected int bestDistance = 9999;
    
    protected Logger log;
    protected DatagramSocket socket;
    // taille maximale d'un datagramme, utilisée pour le buffer de reception
    static int MAX_DGRAM_SIZE = 100;

    /**
     * Constructor of the class
     * @param Node node
     */
    public RPIApp(Node node) {
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
        System.out.println(this.getClass().getSimpleName() + port);
        try {
            this.socket = new DatagramSocket(port);
            log.info( Utils.logInfo(this.idNode)+"start");
            this.onReceiveMessage();
            
        } catch (SocketException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    /**
     * 
     * @param packet
     * @throws IOException
     */
    protected void flooding(DatagramPacket packet) throws IOException {

        for(RPIApp rpi: this.neighbors) {
            // On renvoie le paquet à tous les voisins excepté à la source
            if ((rpi.getAddress() != packet.getAddress()) && (rpi.getPort() != packet.getPort())) {
                System.out.println(this.getAddress() + ":" + this.getPort() + " send packet to " + rpi.getAddress() + ":" + rpi.getPort());

                packet.setAddress(rpi.getAddress());
                packet.setPort(rpi.getPort());
                this.socket.send(packet);

            }
            
        }
    }

    private void onReceiveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[MAX_DGRAM_SIZE], MAX_DGRAM_SIZE);
        socket.receive(packet);
        RPIApp rpiSource = this.findNeigbor(packet.getAddress(), packet.getPort());

        String [] commandReceived = Utils.splitDataIntoArguments(new String (packet.getData()));
        CommandDecrypted commandDecrypted = CommandDecrypted.valueOfCommandToDecrypt(commandReceived[0].hashCode()) != null ? CommandDecrypted.valueOfCommandToDecrypt(commandReceived[0].hashCode()) : null;

        byte[] message = new byte[0];
        switch (commandDecrypted) {
            case advertise:
                log.info("receive distance");
                int bestDistanceNew = Integer.valueOf(commandReceived[1]) + 1;
                System.out.println("distance=" + bestDistanceNew);
                // On recherche le bestReceiver (ca devrait etre sender) ainsi que la bestDistance
                if (this.bestReceiver == null) this.bestReceiver = rpiSource; 
                if (bestDistanceNew < this.bestDistance) {
                    this.bestDistance = bestDistanceNew;
                    this.bestReceiver = rpiSource;
                } else if (bestDistanceNew == this.bestDistance) {
                    if (rpiSource.getIdNode() < this.bestReceiver.getIdNode()) this.bestReceiver = rpiSource;
                }
                List<String> messageToSendToNeighboor = new ArrayList<String>();
                messageToSendToNeighboor.add("advertise");
                messageToSendToNeighboor.add(String.valueOf(this.bestDistance));
                message = Utils.getByteFromString(";",messageToSendToNeighboor);
                packet.setData(message);
                break;
            case vanne:
                log.info("receive vanne");
                break;

        }

        this.flooding(packet);

    }

    private String[] splitDataIntoArguments(String packet) {
        return packet.split(";");
    }

    protected void sendPacketDistance() {

    }

    protected void receivePacketDistance() {

    }

    public void createThread() {
        this.start();
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

    /**
     * Find RPIApp neighbor by address and port
     * @param InetAddress address
     * @param int port
     * @return RPIApp
     */
    public RPIApp findNeigbor(InetAddress address, int port) {
        for(RPIApp rpi: this.neighbors) {
            if ((rpi.getAddress() != address) && (rpi.getPort() != port)) return rpi;
        }

        return null;
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
}
