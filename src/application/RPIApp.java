package application;


import models.CommandDecrypted;
import models.Node;
import shared.Utils;
import java.io.IOException;
import java.net.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class RPIApp extends Thread{
    protected int idNode;
    protected InetAddress address;
    protected int port;
    protected List<RPIApp> neighbors = new ArrayList();
    private RPIApp bestReceiver = null;
    protected int bestDistance = Integer.MAX_VALUE;
    private static double maxTemperature = 20;
    private static double minTemperature = 10;
    private int vannePosition = 0;
    private DecimalFormat df = new DecimalFormat("#.00");
    private double temperature = Double.valueOf(df.format(Math.random()*(maxTemperature-minTemperature+1)+minTemperature));
    private int delay;
    
    protected Logger log;
    protected DatagramSocket socket;
    // taille maximale d'un datagramme, utilisée pour le buffer de reception
    static int MAX_DGRAM_SIZE = 100;

    /**
     * Constructor of the class
     * @param Node node
     * @param delay
     */
    public RPIApp(Node node, int delay) {
        this.idNode = node.id;
        this.address = node.address;
        this.port = node.port;
        this.delay = delay;
        System.out.println("temperature" + temperature);
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
            long time = System.currentTimeMillis();
            this.socket.setSoTimeout(5000);
            while(true) {

                long d = System.currentTimeMillis();
                if (d > time + delay) {
                    log.info( Utils.logInfo(this.idNode)+ temperature + "temperature" + vannePosition);
                    time = System.currentTimeMillis();
                }
                this.onReceiveMessage();
            }
            
        } catch (SocketException e) {
            e.printStackTrace();

        } catch (SocketTimeoutException e) {

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
        // LA BOUCLE DEVENEMENT NE MARCHE PAS pcq c'est un circuit et nous envoyonns connstament des informations a nos voisins ce qui fait que le floading nne finira jamais.
        // il faut faire enn sorte de ne pas renvoye constament au voisins.
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

        RPIApp rpiSource = this.findNeighbour(packet.getAddress(), packet.getPort());
        String [] commandReceived = Utils.splitDataIntoArguments(new String (packet.getData(), 0, packet.getLength()));
        CommandDecrypted commandDecrypted = CommandDecrypted.valueOfCommandToDecrypt(commandReceived[0].hashCode());
        System.out.println(commandReceived);
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
                packet = Utils.createPacketToReSend("advertise",String.valueOf(this.bestDistance),packet);
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
    public RPIApp findNeighbour(InetAddress address, int port) {
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
