package shared;

import models.CommandEncrypted;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
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

    public static DatagramPacket createPacketToReSend(String CommandType, String argumentWithTheCommand, DatagramPacket packet) throws IOException {
        byte[] message;
        List<String> messageToSendToNeighbour = new ArrayList<String>();
        messageToSendToNeighbour.add(CommandType);
        messageToSendToNeighbour.add(argumentWithTheCommand);
        message = getByteFromString(";",messageToSendToNeighbour);
        packet.setData(message,0,message.length);
        return packet;
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

    public static String logInfo(int node) {
        return System.currentTimeMillis()+";"+node+";";
    }
}
