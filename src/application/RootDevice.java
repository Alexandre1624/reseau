package application;

import models.Event;
import models.Node;
import shared.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RootDevice extends RPIApp {

    /**
     * Constructor of the class
     * @param Node node
     */
    public RootDevice(Node node) throws SocketException {
        super(node);
        super.bestDistance = 0;
        log = Logger.getLogger(this.getClass().getSimpleName() + " " + this.idNode);
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

    // TODO
    public void openValve(int idNode, int level) {

    }

    public void openValve(RPIApp rpiApp, int level) {
        int idNode = rpiApp.getIdNode();
    }
    public void closeValve(int idNode) {
        openValve(idNode, 0);
    }

    public void sendMessage(Event event) throws InterruptedException, IOException {
        Utils.logInfo(this.idNode);
        sleep(event.delay);
        switch (event.args.get(0)) {
            case "advertise":
                event.args.add("0");
                byte[] message = Utils.getByteFromString(";", event.args);
                for(RPIApp rpi: this.neighbors) {
                    DatagramPacket sendPacket = new DatagramPacket(message,message.length,this.address, rpi.getPort());
                    this.socket.send(sendPacket);
                }
                break;
            case "vanne":
                break;
            default:
                break;
        }
    }
}
