package application;

import models.CommandDecrypted;
import models.Node;
import shared.Utils;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RPIApp extends Thread{
    protected int idNode;
    protected InetAddress address;
    protected int port;
    protected List<RPIApp> neighbors = new ArrayList<RPIApp>();
    private RPIApp bestSender = null;
    protected int bestDistance = Integer.MAX_VALUE;
    protected static int MAX_VALVE_POSITION = 5;
    protected static double MAX_TEMPERATURE = 20;
    protected static double MIN_TEMPERATURE = 10;
    private int vannePosition = 0;
    private double temperature = Double.valueOf(Math.random()*(MAX_TEMPERATURE-MIN_TEMPERATURE+1)+MIN_TEMPERATURE);
    private int delay;
    protected DatagramChannel channel;
    
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

        // LOG EVENT //
        //Utils.logEventSendState(this.idNode, this.temperature, this.vannePosition);
    }

    /**
     * Overload toSTring() method
     * @return String
     */
    public String toString(){
        List<Integer> temp = new ArrayList<Integer>();
        for(RPIApp neighbor : neighbors){ temp.add(neighbor.idNode); }
        return String.format("[RPIApp id:#%d | address:%s | port:%d | neighbors:%s | bestSender:%d]"+System.lineSeparator(),idNode, address, port, temp.toString(), bestSender.getIdNode());
    }

    public void bootSocket() throws IOException {

        try {
            this.channel = DatagramChannel.open();
            SocketAddress address = new InetSocketAddress(this.address,this.port);
            this.socket = channel.socket();//créer une channel permettant d'avoir un socket non bloquant
            this.socket.bind(address);
            this.socket.getChannel().configureBlocking(false);
            long time = System.currentTimeMillis();
            ByteBuffer buffer = ByteBuffer.allocate(MAX_DGRAM_SIZE);
            while(true) {
                long d = System.currentTimeMillis();

                if (delay != 0 && d > time + delay) {
                    time = System.currentTimeMillis();

                    // LOG EVENT //
                    Utils.logEventSendState(this.idNode, this.temperature, this.vannePosition);
                    
                    // On simule l'évolution de la température et on envoie l'état au RootDevice
                    this.temperature = this.temperatureGiven();
                    this.sendStateToRootDevice(buffer);
                }

                this.onReceiveMessage();
            }
        } catch (SocketException e) {
            //e.printStackTrace();
            Utils.log.warning(e.getMessage());
        } catch (IOException e) {
            //e.printStackTrace();
            Utils.log.warning(e.getMessage());
        } finally {
            Utils.log.warning("thread interrupted");
        }
    }

    public void run() {// method from thread
        // LOG EVENT //
        Utils.logEventStart(this.idNode);
        try {
            this.bootSocket();
        } catch (IOException e) {
            //e.printStackTrace();
            Utils.log.warning(e.getMessage());
        }
    }

    /**
     * Method to flood packet to neigboors of RPI (except source of this packet)
     * @param buffer
     * @param socket
     * @throws IOException
     */
    protected void flooding(ByteBuffer buffer, InetSocketAddress socket) throws IOException {
        for (RPIApp rpi : this.neighbors) {
            // On renvoie le paquet à tous les voisins excepté à la source
            if (!(rpi.getAddress().equals(socket.getAddress())) || (rpi.getPort() != socket.getPort())) {
                // DEBUG //
                buffer.rewind();
                this.channel.send(buffer,new InetSocketAddress(rpi.getAddress(),rpi.getPort()));
            }
        }
    }
    /** création du paquet à envoyer vers le rootDevice une fois que l'arbre couvrant a été mis en place
     * et que l'app sait avec quel autre communiqué */
    private void sendStateToRootDevice(ByteBuffer buffer) throws IOException {
        List<String> arguments = Stream.of(this.idNode, this.temperature, this.vannePosition).map( arg -> String.valueOf(arg)).collect(Collectors.toList());
        buffer = Utils.createPacketToReSend("state", arguments, buffer);
        if (this.bestSender!=null) {
            this.channel.send(buffer,new InetSocketAddress(this.bestSender.getAddress(),this.bestSender.getPort()));
        }
    }

    protected void onReceiveMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_DGRAM_SIZE);
        InetSocketAddress sourceSocket = (InetSocketAddress) this.channel.receive(buffer);
        buffer.flip();
        String[] commandReceived = Utils.splitDataIntoArguments(new String(buffer.array()));
        if (commandReceived.length > 1) {

            RPIApp rpiSource = this.findNeighbor(sourceSocket.getAddress(), sourceSocket.getPort());
            buffer.rewind();// le buffer commence a la position zero

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

                        buffer = Utils.createPacketToReSend("advertise", Arrays.asList(String.valueOf(this.bestDistance)), buffer);
                        this.flooding(buffer, sourceSocket);

                    }

                    break;
                case vanne:
                    int nodeId = Integer.valueOf(commandReceived[1]);
                    int vannePosition = Integer.valueOf(commandReceived[2]);
                    if (nodeId == this.idNode) {
                        // LOG EVENT
                        Utils.logEventNewState(this.idNode, vannePosition, this.vannePosition);

                        this.vannePosition = vannePosition;
                        this.temperature = this.temperatureGiven();

                    } else {
                        // Comme le RPI source est considéré comme le bestSender, on peut lancer le flooding
                        if (rpiSource.equals(this.bestSender)) {
                            // On retransmet le packet comme à l'origine car il contient toutes les infos nécessaires (sans besoin de modifs)
                            this.flooding(buffer, sourceSocket);
                        }
                    }
                    break;
                case state:
                    // LOG EVENT //
                    //Utils.logEventReceivedState(this.idNode, fromNodeId, temperature, vannePosition);

                    this.channel.send(buffer,new InetSocketAddress(this.bestSender.getAddress(),this.bestSender.getPort()));

                    break;
                default:
                    break;

            }
        }

    }

    public Double temperatureGiven() {
        return Double.valueOf(Math.random() * (MAX_TEMPERATURE-MIN_TEMPERATURE+1)+MIN_TEMPERATURE) * (1 + (this.vannePosition * 0.15));
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
