import java.io.*;

public class BitOutputStream implements AutoCloseable {

    private OutputStream output; // Change type to generic OutputStream
    private int currentByte;
    private int bitsCount;

    public BitOutputStream(String filePath, boolean append) throws IOException {
        // --- THE SPEED FIX ---
        // Wrap FileOutputStream in BufferedOutputStream
        this.output = new BufferedOutputStream(new FileOutputStream(filePath, append)); 
        
        this.currentByte = 0;
        this.bitsCount = 0;
    }

    // ... (Keep writeBit and writeCode exactly the same) ...

    public void writeBit(int bit) throws IOException {
        // (Same logic as before)
        if (bit != 0 && bit != 1) throw new IllegalArgumentException("Bit must be 0 or 1");
        currentByte = currentByte << 1;
        if (bit == 1) currentByte = currentByte | 1;
        bitsCount++;
        if (bitsCount == 8) {
            output.write(currentByte);
            currentByte = 0;
            bitsCount = 0;
        }
    }
    
    public void writeCode(String code) throws IOException {
        for (int i = 0; i < code.length(); i++) {
            writeBit(code.charAt(i) == '1' ? 1 : 0);
        }
    }

    public void close() throws IOException {
        if (bitsCount > 0) {
            int padding = 8 - bitsCount;
            currentByte = currentByte << padding;
            output.write(currentByte);
        }
        
        // FLUSHING IS CRITICAL WITH BUFFERS
        output.flush(); 
        output.close();
    }
}