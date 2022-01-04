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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RootDevice extends RPIApp {

    private List<Event> events;

    /**
     * Constructor of the class
     * @param Node node
     */
    public RootDevice(Node node) throws SocketException {
        super(node,0);
        super.bestDistance = 0;
    }

    public void bootSocket() throws IOException {

        try {
            this.channel = DatagramChannel.open();
            SocketAddress address = new InetSocketAddress(this.address,this.port);
            this.socket = channel.socket();//creer une channel permettant davoir un socket non bloquant--
            this.socket.bind(address);
            this.socket.getChannel().configureBlocking(false);
            long time = System.currentTimeMillis();
            Event event = this.events.remove(0);
            while(true) {
                // TODO, il faut envoyer un paquet à RootDevice pour qu'il le recoive l'info des états de chaque RPI
                long d = System.currentTimeMillis();
                //envoie des events en fonctions de leurs delays
                if (event!=null && event.delay != 0 && d > time + event.delay) {
                    // LOG EVENT //
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

                    // LOG EVENT //
                    Utils.logEventReceivedState(this.idNode, fromNodeId, temperature, vannePosition);         

                    break;
                default:
                    break;

            }
        }

    }

    public void sendMessage(Event event) throws IOException {
        //Utils.logInfo(this.idNode);
        byte[] message;
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
