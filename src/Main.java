import application.Terminal;
import config.InjectionProperties;
import models.FileFormatException;

import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileFormatException, IOException, InterruptedException {
        InjectionProperties injectionProperties = new InjectionProperties();
        System.out.println(injectionProperties.getProperties());
        Terminal terminal = new application.Terminal(System.getProperty("user.dir") + "/scenario1.config");
        terminal.run();
    }
}
