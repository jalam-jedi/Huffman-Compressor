import java.io.FileOutputStream;
import java.io.IOException;

public class BitOutputStream implements AutoCloseable{

    private FileOutputStream output;
    private int currentByte; // The "Waiting Room" for bits
    private int bitsCount;   // How many bits are currently waiting (0-7)

    /**
     * @param filePath Path to the file.
     * @param append If true, adds to the end of the file. If false, overwrites it.
     */
    public BitOutputStream(String filePath, boolean append) throws IOException {
        this.output = new FileOutputStream(filePath, append);
        this.currentByte = 0;
        this.bitsCount = 0;
    }

    /**
     * Adds a single bit (0 or 1) to the buffer.
     */
    public void writeBit(int bit) throws IOException {
        if (bit != 0 && bit != 1) {
            throw new IllegalArgumentException("Bit must be 0 or 1");
        }

        // 1. Shift buffer to make room
        currentByte = currentByte << 1;
        
        // 2. Add the bit
        if (bit == 1) {
            currentByte = currentByte | 1;
        }

        bitsCount++;

        // 3. If full (8 bits), write to disk and reset
        if (bitsCount == 8) {
            output.write(currentByte);
            currentByte = 0;
            bitsCount = 0;
        }
    }

    /**
     * Helper to write a whole string like "11010"
     */
    public void writeCode(String code) throws IOException {
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            writeBit(c == '1' ? 1 : 0);
        }
    }

    /**
     * MUST be called to finish the file.
     * If we have 3 bits waiting (e.g., 101), we add 5 zeros to make it a full byte (10100000) and write it.
     */
    public void close() throws IOException {
        if (bitsCount > 0) {
            int padding = 8 - bitsCount;
            currentByte = currentByte << padding;
            output.write(currentByte);
        }
        output.close();
    }
}