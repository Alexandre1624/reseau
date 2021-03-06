package models;

public enum CommandDecrypted { // Nous envoyons un identifiant unique sous forme de int mais nous devons obligatoirement le transformer en string,
    // de ce fait la conversion change
    advertise("��('".hashCode()),// "��('" = advertise
    vanne("\u0006��\u001A".hashCode()),// "�� = vanne
    state("\u0006�đ".hashCode());// "\u0006�đ = state

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
