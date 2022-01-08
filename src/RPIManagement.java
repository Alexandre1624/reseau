import application.Terminal;
import models.FileFormatException;
import java.io.IOException;

public class RPIManagement {

    public static void main(String[] args) throws FileFormatException, IOException, InterruptedException {
        String scenarioConfigFilePath = "";

        // Lecture du paramètre
        switch (args.length) {
            case 1:
                scenarioConfigFilePath = args[0];
                break;
        
            default:
                scenarioConfigFilePath = System.getProperty("user.dir") + "/scenario.config";
                break;
        }
        Terminal terminal = new application.Terminal(scenarioConfigFilePath);
        terminal.run();
    }
}
