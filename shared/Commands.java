package shared;

public enum Commands {
    SAVE(0), RETRIEVE(1), SAVE_DIR(2), RETRIEVE_DIR(3), MKDIR(4), LS(5), CD(6);
    
    private byte value;
    Commands(int v) {
        value = (byte)v;
    }
    public byte getNumber() { return value; }
}
