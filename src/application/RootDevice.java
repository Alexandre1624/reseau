package application;

import models.Event;
import models.Node;
import shared.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

public class RootDevice extends RPIApp {

    /**
     * Constructor of the class
     * @param Node node
     */
    public RootDevice(Node node) throws SocketException {
        super(node,0);
        super.bestDistance = 0;

    }

    /**
     * Overload toSTring() method
     * @return String
     */
    public String toString(){
        return "RootDevice => " + super.toString();
    }

    // ne faudrait-il pas générer le packet dans cette méthode (donc pas de parametre)
    // et envoyer celui-ci en parametre dans la méthode utilisée de UDPManager ?
    public void advertise(DatagramPacket packet) {

    }

    public void sendMessage(Event event) throws InterruptedException, IOException {
        //Utils.logInfo(this.idNode);
        byte[] message;
        sleep(event.delay);
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
        for(RPIApp rpi: this.neighbors) {
            DatagramPacket sendPacket = new DatagramPacket(message,message.length, rpi.getAddress(), rpi.getPort());
            socket.send(sendPacket);

            // LOG //
            //log.info(this.getAddress() + ":" + this.getPort() + " send packet to " + rpi.getAddress() + ":" + rpi.getPort());
        }
    }

    // TODO, il faudra un onReceive pour récupérer l'état des différents RPI
    // et ainsi faire appel à Utils.logEventReceivedState(...)
}
