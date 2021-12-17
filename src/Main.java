import application.Terminal;
import models.FileFormatException;

import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileFormatException, IOException {
        Terminal terminal = new application.Terminal(System.getProperty("user.dir") + "/scenario1.config");
        terminal.run();

        /*
        Map m = new HashMap<Integer, List<Integer>>();
        List a =  new ArrayList<Integer>();

        a.add(13493);
        a.add(10);
        m.put(1,a);
        String test = "openVanne;";

            ArrayList<Integer> atransformer = (ArrayList<Integer>) m.get(1);
        for( Integer s: atransformer) {
            test+= s.toString()+";";
        }
        byte[] c = test.getBytes();

        System.out.println(new String(c));*/
    }
}
