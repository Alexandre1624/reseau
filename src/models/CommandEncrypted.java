package models;

public enum CommandEncrypted {
    advertise("advertise".hashCode()),
    state("state".hashCode()),
    temperature("temperature".hashCode()),
    alex("alex".hashCode());

    private int hashCode;

    private CommandEncrypted(int hashCode) {
        this.hashCode = hashCode;
    }

    private int getHashCode() {
        return this.hashCode;
    }
    public static int valueOfCommand(String command) {
        for (CommandEncrypted commandEncrypted : values()) {
            if (commandEncrypted.hashCode == command.hashCode()) {
                return commandEncrypted.hashCode;
            }
        }
        return 0;
    }
}
