package models;

public enum CommandDecrypted { // Nous envoyons unn identifiant unique sous forme de int mais nous devons obligatoirement le transformer en string,
    // de ce fait la conversion change
    advertise("��('".hashCode()),// "��('" = advertise
    state("\u0006�đ".hashCode()),// "\u0006�đ = state
    temperature("\u0013,�t".hashCode());

    private int hashCode;

    CommandDecrypted(int hashCode) {
        this.hashCode = hashCode;
    }

    private int getHashCode() {
        return this.hashCode;
    }

    public static CommandDecrypted valueOfCommandToDecrypt(int hashCode) {
        for (CommandDecrypted commandEncrypted : values()) {
            if (commandEncrypted.hashCode == hashCode) {
                return commandEncrypted;
            }
        }
        return null;
    }
}
