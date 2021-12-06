package application;

import models.Node;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

public class RootDevice extends RPIApp{

    public RootDevice(Node node) {
        super(node);

    }
    public void advertise(DatagramPacket packet) {

    }
}
