import java.io.File;

public class TestBitStreams {
    public static void main(String[] args) {
        String testFile = "test_bits.bin";
        
        System.out.println("--- Phase 2: Bit Stream Verification ---");

        // 1. WRITE bits
        try {
            System.out.println("Writing bits: 1, 0, 1, 1, 1...");
            BitOutputStream bos = new BitOutputStream(testFile, false);
            
            // Write 5 bits: 10111
            bos.writeBit(1);
            bos.writeBit(0);
            bos.writeBit(1);
            bos.writeBit(1);
            bos.writeBit(1);
            
            bos.close(); // This should add 3 padding zeros -> 10111000
            System.out.println("Write successful.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. READ bits
        try {
            System.out.println("Reading bits back...");
            // We expect to read 1 byte of data (though we only care about the first 5 bits)
            BitInputStream bis = new BitInputStream(testFile, 1); 
            
            int b1 = bis.readBit();
            int b2 = bis.readBit();
            int b3 = bis.readBit();
            int b4 = bis.readBit();
            int b5 = bis.readBit();
            
            System.out.println("Read: " + b1 + "" + b2 + "" + b3 + "" + b4 + "" + b5);
            
            if (b1==1 && b2==0 && b3==1 && b4==1 && b5==1) {
                System.out.println("[PASS] Bit streams are working correctly.");
            } else {
                System.out.println("[FAIL] Bits do not match.");
            }
            
            bis.close();
            new File(testFile).delete(); // Cleanup
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}