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
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class RPIApp extends Thread{
    protected int idNode;
    protected InetAddress address;
    protected int port;
    protected List<RPIApp> neighbors = new ArrayList();
    private RPIApp bestSender = null;
    protected int bestDistance = Integer.MAX_VALUE;
    private static double maxTemperature = 20;
    private static double minTemperature = 10;
    private int vannePosition = 0;
    private double temperature = Double.valueOf(Math.random()*(maxTemperature-minTemperature+1)+minTemperature);
    private int delay;
    
    protected DatagramSocket socket;
    // taille maximale d'un datagramme, utilisée pour le buffer de reception
    static int MAX_DGRAM_SIZE = 100;

    // Logger pour le debug (à placer ailleurs Utils, Terminal, ... ???)
    protected static Logger log = Logger.getLogger(RPIApp.class.getName());
    static {
        try {
            FileHandler fh = new FileHandler("log.txt");
            log.addHandler(fh);
            
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        // LOG EVENT //
        Utils.logEventSendState(this.idNode, this.temperature, this.vannePosition);
    }

    /**
     * Overload toSTring() method
     * @return String
     */
    public String toString(){
        List<Integer> temp = new ArrayList();
        for(RPIApp neighbor : neighbors){ temp.add(neighbor.idNode); }
        return String.format("[RPIApp id:#%d | address:%s | port:%d | neighbors:%s | bestSender:%d]"+System.lineSeparator(),idNode, address, port, temp.toString(), bestSender.getIdNode());
    }

    public void run() {// method from thread
        // LOG EVENT //
        Utils.logEventStart(this.idNode);

        try {
            this.socket = new DatagramSocket(port);
            
            long time = System.currentTimeMillis();
            this.socket.setSoTimeout(5000);
            while(true) {
                // TODO, il faut envoyer un paquet à RootDevice pour qu'il le recoive l'info des états de chaque RPI
                long d = System.currentTimeMillis();
                if (delay != 0 && d > time + delay) {
                    // LOG EVENT //
                    Utils.logEventSendState(this.idNode, this.temperature, this.vannePosition);
                    
                    time = System.currentTimeMillis();
                }
                this.onReceiveMessage();
            }
            
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            // TODO ou pas, j'ai commenté pour éviter d'avoir l'exception "java.net.SocketTimeoutException: Receive timed out" qui est déclenchée à la fin pour chaque Thread
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Method to flood packet to neigboors of RPI (except source of this packet)
     * @param packet
     * @throws IOException
     */
    protected void flooding(DatagramPacket packet) throws IOException {
        for (RPIApp rpi : this.neighbors) {
            // On renvoie le paquet à tous les voisins excepté à la source
            if (!(rpi.getAddress().equals(packet.getAddress())) || (rpi.getPort() != packet.getPort())) {
                // DEBUG //
                //log.info(this.getAddress() + ":" + this.getPort() + " send packet to " + rpi.getAddress() + ":" + rpi.getPort());

                packet.setAddress(rpi.getAddress());
                packet.setPort(rpi.getPort());
                this.socket.send(packet);

            }

        }
    }

    private void onReceiveMessage() throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[MAX_DGRAM_SIZE], MAX_DGRAM_SIZE);
        socket.receive(packet);

        RPIApp rpiSource = this.findNeighbor(packet.getAddress(), packet.getPort());
        String [] commandReceived = Utils.splitDataIntoArguments(new String (packet.getData(), 0, packet.getLength()));
        CommandDecrypted commandDecrypted = CommandDecrypted.valueOfCommandToDecrypt(commandReceived[0].hashCode());
        boolean toBeResend = false;

        switch (commandDecrypted) {
            case advertise:
                int distanceReceived = Integer.valueOf(commandReceived[1]);

                // LOG EVENT
                Utils.logEventReceivedDistance(this.idNode, distanceReceived, rpiSource.getIdNode());
                //System.out.println(rpiSource);

                // On recherche le bestSender ainsi que la bestDistance
                int bestDistanceNew = distanceReceived + 1;
                if (this.bestSender == null) this.bestSender = rpiSource;
                if (bestDistanceNew < this.bestDistance) {
                    toBeResend = true;
                    this.bestDistance = bestDistanceNew;
                    this.bestSender = rpiSource;

                    // LOG EVENT //
                    Utils.logEventNewRoute(this.idNode, rpiSource.getIdNode());

                } else if (bestDistanceNew == this.bestDistance) {
                    if (this.bestSender != null && (rpiSource.getIdNode() < this.bestSender.getIdNode())) {
                        toBeResend = true;
                        this.bestSender = rpiSource;

                        // LOG EVENT //
                        Utils.logEventNewRoute(this.idNode, rpiSource.getIdNode());

                    }
                }
                // On vérifie si le paquet doit être réenvoyé
                if (toBeResend) {
                    // LOG EVENT
                    Utils.logEventAdvertise(this.idNode, this.bestDistance);

                    packet = Utils.createPacketToReSend("advertise",String.valueOf(this.bestDistance),packet);
                    this.flooding(packet);

                }

                break;
                
            case vanne:
                int nodeId = Integer.valueOf(commandReceived[1]);
                int vannePosition = Integer.valueOf(commandReceived[2]);
                if (nodeId == this.idNode) {
                    toBeResend = false;

                    // LOG EVENT
                    Utils.logEventNewState(this.idNode, vannePosition, this.vannePosition);

                    this.vannePosition = vannePosition;
                    this.temperature = this.temperatureGiven();

                } else {
                    // Comme le RPI source est considéré comme le bestSender, on peut lancer le flooding
                    if (rpiSource.equals(this.bestSender)) {
                        toBeResend = true;

                        // On retransmet le packet comme à l'origine car il contient toutes les infos nécessaires (sans besoin de modifs)
                        this.flooding(packet);

                    }

                }
                
                break;

        }

    }

    public Double temperatureGiven() {
        return Double.valueOf(Math.random() * (maxTemperature-minTemperature+1)+minTemperature) * (1 + (this.vannePosition * 0.15));
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
    public RPIApp findNeighbor(InetAddress address, int port) {
        for(RPIApp rpi: this.neighbors) {
            if ((rpi.getAddress().equals(address)) && (rpi.getPort() == port)) 
                return rpi;
        }
        return null;
    }
    /**
     * Find RPIApp neighbor by id inode
     * @param int idInode
     * @return RPIApp
     */
    public RPIApp findNeighbor(int idNode) {
        for(RPIApp rpi: this.neighbors) {
            if (rpi.getIdNode() == idNode)
                return rpi;
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
    public RPIApp getbestSender() {
        return bestSender;
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
    public void setbestSender(RPIApp bestSender) {
        this.bestSender = bestSender;
    }
    public final void setBestDistance(int bestDistance) {
        this.bestDistance = bestDistance;
    }
}
