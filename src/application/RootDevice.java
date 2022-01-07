package application;

import models.CommandDecrypted;
import models.Event;
import models.Node;
import shared.Utils;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RootDevice extends RPIApp {
    private double requestedTemperature;
    private int precision;
    private List<Event> events;

    /**
     * Constructor of the class
     * @param Node node
     */
    public RootDevice(Node node, double requestedTemperature, int precision) throws SocketException {
        super(node,0);
        super.bestDistance = 0;
        this.requestedTemperature = requestedTemperature;
        this.precision = precision;
    }

    public void bootSocket() throws IOException {

        try {
            this.channel = DatagramChannel.open();
            SocketAddress address = new InetSocketAddress(this.address,this.port);
            this.socket = channel.socket();//créer une channel permettant d'avoir un socket non bloquant
            this.socket.bind(address);
            this.socket.getChannel().configureBlocking(false);
            long time = System.currentTimeMillis();
            Event event = this.events.remove(0);
            /*boucle permettant d'envoyer les commandes avec un delay entre chaque envoie de paquet*/
            while(true) {
                long d = System.currentTimeMillis();
                //envoie des events en fonctions de leurs delays
                if (event!=null && event.delay != 0 && d > time + event.delay) {
                    time = System.currentTimeMillis();
                    this.sendMessage(event);
                    event = this.events.size() > 0 ? this.events.remove(0): null;
                }

                this.onReceiveMessage();
            }
        } catch (SocketException e) {
            //e.printStackTrace();
            log.warning(e.getMessage());
        } catch (IOException e) {
            //e.printStackTrace();
            log.warning(e.getMessage());
        } finally {
            log.warning("thread interrupted");
        }
    }

    public void run() {// method from thread
        // LOG EVENT //
        Utils.logEventStart(this.idNode);
        try {
            this.bootSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overload toSTring() method
     * @return String
     */
    public String toString(){
        return "RootDevice => " + super.toString();
    }

    protected final void onReceiveMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_DGRAM_SIZE);
        InetSocketAddress sourceSocket = (InetSocketAddress) this.channel.receive(buffer);
        buffer.flip();
        String[] commandReceived = Utils.splitDataIntoArguments(new String(buffer.array()));
        if (commandReceived.length > 1) {
            buffer.rewind();// le buffer commence a la position zero
            CommandDecrypted commandDecrypted = CommandDecrypted.valueOfCommandToDecrypt(commandReceived[0].hashCode());
    
            switch (commandDecrypted) {
                case state:
                    int fromNodeId = Integer.valueOf(commandReceived[1]);
                    Double temperature = Double.valueOf(commandReceived[2]);
                    int vannePosition =  Integer.valueOf(commandReceived[3]);
                    int vannePositionNew = vannePosition;
    
                    // LOG EVENT //
                    Utils.logEventReceivedState(this.idNode, fromNodeId, temperature, vannePosition);
                    
                    // On vérifie si la température n'est pas au dessus ou en dessous de la t° demandée (à la précision près en °)
                    // Si c'est le cas, on diminue ou on augmente la position de la valve
                    boolean changeVannePosition = false;
                    if (temperature > (this.requestedTemperature + this.precision)) {
                        if (vannePosition > 0) {
                            changeVannePosition = true;
                            vannePositionNew--;
                        }
                        
                    } else if (temperature < (this.requestedTemperature - this.precision)) {
                        if (vannePosition < RPIApp.MAX_VALVE_POSITION) {
                            changeVannePosition = true;
                            vannePositionNew++;
                        }
                    }

                    // On envoie le changement de position uniquement si le flag est true
                    if (changeVannePosition) {
                        List<String> arguments = Stream.of(fromNodeId, vannePositionNew).map( arg -> String.valueOf(arg)).collect(Collectors.toList());
                        buffer = Utils.createPacketToReSend("vanne", arguments, buffer);

                        // LOG EVENT //
                        Utils.logEventSetState(1, vannePositionNew, fromNodeId);

                        this.channel.send(buffer, sourceSocket);
                    }
    
                    break;
                default:
                    break;
    
            }
        }
    
    }

    public void sendMessage(Event event) throws IOException {
        byte[] message;
        /*envoie une commande en fonction de l'argument */
        switch (event.args.get(0)) {
            case "advertise":
                // LOG EVENT //
                Utils.logEventAdvertise(1, 0);

                event.args.add("0");
                message = Utils.getByteFromString(";", event.args);
                sendPacket(message);
                break;

            case "vanne":
                int nodeIdDestination = Integer.valueOf(event.args.get(1));
                int vannePosition = Integer.valueOf(event.args.get(2));

                // LOG EVENT //
                Utils.logEventSetState(1, vannePosition, nodeIdDestination);

                message = Utils.getByteFromString(";", event.args);
                sendPacket(message);
                break;
            default:
                break;
        }
    }
    /**
     * envoie une commande à tous les voisins de l'ordinateur centrale 
     */
    private void sendPacket(byte[] message) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(message.length);
        buffer.put(message);
        buffer.order(ByteOrder.BIG_ENDIAN);
        for(RPIApp rpi: this.neighbors) {
            buffer.flip();
            SocketAddress server = new InetSocketAddress(rpi.getAddress(),rpi.getPort());
            channel.send(buffer,server);
            // LOG //
            //log.info(this.getAddress() + ":" + this.getPort() + " send packet to " + rpi.getAddress() + ":" + rpi.getPort());
        }
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
