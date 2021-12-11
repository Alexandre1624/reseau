package application;

import models.Node;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

public class RootDevice extends RPIApp{

    /**
     * Constructor of the class
     * @param Node node
     */
    public RootDevice(Node node) {
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
}
