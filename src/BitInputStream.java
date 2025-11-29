import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements AutoCloseable{

    private InputStream input;
    private int currentByte;
    private int bitsRemaining; // How many bits are left in the current byte
    
    // We need to know when to stop so we don't read the padding zeros as real data
    private long bytesToRead; 

    /**
     * Constructor used when starting fresh from a file
     */
    public BitInputStream(String filePath, long bytesToRead) throws IOException {
        this.input = new FileInputStream(filePath);
        this.bytesToRead = bytesToRead;
        this.currentByte = 0;
        this.bitsRemaining = 0;
    }

    /**
     * Constructor used when we have already read the Header
     */
    public BitInputStream(InputStream existingStream, long bytesToRead) {
        this.input = existingStream;
        this.bytesToRead = bytesToRead;
        this.currentByte = 0;
        this.bitsRemaining = 0;
    }

    /**
     * @return The next bit (0 or 1), or -1 if we are done.
     */
    public int readBit() throws IOException {
        if (bytesToRead == 0) {
            return -1; 
        }

        // If buffer empty, fetch next byte from disk
        if (bitsRemaining == 0) {
            currentByte = input.read();
            if (currentByte == -1) {
                return -1; // End of file
            }
            bitsRemaining = 8;
        }

        // Extract the Left-Most bit
        // Logic: Shift right to move the target bit to the end, then & 1 to isolate it.
        int bit = (currentByte >> (bitsRemaining - 1)) & 1;
        
        bitsRemaining--;
        return bit;
    }

    /**
     * Call this when you successfully decode a full character (8 bits).
     */
    public void byteDecoded() {
        bytesToRead--;
    }

    public void close() throws IOException {
        input.close();
    }
}