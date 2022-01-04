package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class InjectionProperties {
    private static Map<String, Properties> properties = new HashMap<>();

    static {
        // Parcours tous les fichiers .properties et les place dans properties
        try {
            Files.walk(Paths.get("./"), 3).filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith("properties")).forEach(p -> {
                        String fileName = p.toString();
                        fileName = fileName.substring(2);
                        // Parcours tous le fichier properties
                        try (FileInputStream file = new FileInputStream(fileName)) {
                            Properties prop = new Properties();
                            prop.load(file);
                            fileName = fileName.replace("\\","/");
                            String[] fileNameSplited = fileName.split("/");

                            properties.put(fileNameSplited[fileNameSplited.length-1], prop);
                        } catch (Exception exception) {
                            System.out.println("Injection properties a cesser de fonctionner");
                            exception.printStackTrace();
                            throw new RuntimeException();
                        }
                    });
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public InjectionProperties() {}

    public Map<String, Properties> getProperties() {
        return properties;
    }
}
