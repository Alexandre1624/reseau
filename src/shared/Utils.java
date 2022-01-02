package shared;

import models.CommandEncrypted;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static int portBase = 4444;
    public static byte[] Encode(String message, ArrayList<Integer> Nodes) {
        return null;
    }
    public static byte[] Encode(String message) {
        return null;
    }
    public static String Decode( byte[] bytesToString) {
            return null;
    }

    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        if(result[0] < 0)
            result[0] += 256;
        if(result[1] < 0)
            result[1] += 256;
        if(result[2] < 0)
            result[2] += 256;
        if(result[3] < 0)
            result[3] += 256;

        return result;
    }

    public static ByteBuffer createPacketToReSend(String CommandType, String argumentWithTheCommand, ByteBuffer buffer) throws IOException {
        buffer.clear();
        byte[] message;
        List<String> messageToSendToNeighbour = new ArrayList<String>();
        messageToSendToNeighbour.add(CommandType);
        messageToSendToNeighbour.add(argumentWithTheCommand);
        message = getByteFromString(";",messageToSendToNeighbour);
        buffer.put(message,0,message.length);
        buffer.flip();
        return buffer;
    }

    public static byte[] getByteFromString(String delimiter, List<String> argumentsFromEvent) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (String arg : argumentsFromEvent) {
            int command = CommandEncrypted.valueOfCommand(arg);
            if (command != 0) {
                outputStream.write(intToBytes(command));
            } else {
                outputStream.write(arg.getBytes());
            }
            outputStream.write(delimiter.getBytes());
        }
        return  outputStream.toByteArray();
    }

    public static String[] splitDataIntoArguments(String packet) {
        return packet.split(";");
    }

    public long getTimeToSendCommand(String time) {
        return Long.valueOf(time) + System.currentTimeMillis();
    }

    // ********* //
    // LOG EVENT //
    // ********* //
    public static String logEvent(int nodeId, String message) {
        String logEvent = System.currentTimeMillis() + ";" + nodeId + ";" + message;
        System.out.println(logEvent);
        return logEvent;
    }
    public static String logEventStart(int nodeId) {
        return Utils.logEvent(nodeId, "start");
    }
    public static String logEventAdvertise(int nodeId, int distance) {
        return Utils.logEvent(nodeId, "advertise distance " + distance);
    }
    public static String logEventReceivedDistance(int nodeId, int distance, int nodeIdFrom) {
        return Utils.logEvent(nodeId, "received distance " + distance + " from node " + nodeIdFrom);
    }
    public static String logEventNewRoute(int nodeId, int nodeIdFrom) {
        return Utils.logEvent(nodeId, "new route via node " + nodeIdFrom);
    }
    public static String logEventSendState(int nodeId, double temperature, int state) {
        return Utils.logEvent(nodeId, "send state;temperature is " + String.format("%.2f", temperature) + ";state is " + state);
    }
    public static String logEventReceivedState(int nodeId, int nodeIdFrom, double temperature, int state) {
        return Utils.logEvent(nodeId, "received state from node " + nodeIdFrom + " ;temperature is " + String.format("%.2f", temperature) + ";state is " + state);
    }
    public static String logEventSetState(int nodeId, int state, int nodeIdTo) {
        return Utils.logEvent(nodeId, "set state " + state + " to node " + nodeIdTo);
    }
    public static String logEventNewState(int nodeId, int newState, int oldState) {
        return Utils.logEvent(nodeId, "new state " + newState + ";old state " + oldState);
    }
}
