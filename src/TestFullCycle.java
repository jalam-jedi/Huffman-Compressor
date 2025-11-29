import java.io.File;
import java.io.FileOutputStream;

public class TestFullCycle {
    public static void main(String[] args) {
        System.out.println("--- Phase 3: Full Integration Test ---");

        String original = "test_image.bmp"; // We will create a fake image
        String compressed = original + ".huff";

        try {
            // 1. Create a dummy file with random bytes (simulating an image)
            createDummyFile(original);
            File f1 = new File(original);
            System.out.println("Original Size: " + f1.length() + " bytes");

            // 2. Compress
            System.out.print("Compressing...");
            long start = System.currentTimeMillis();
            HuffmanCompressor.compress(original, compressed);
            System.out.println(" Done in " + (System.currentTimeMillis() - start) + "ms");
            
            File f2 = new File(compressed);
            System.out.println("Compressed Size: " + f2.length() + " bytes");

            // 3. Decompress
            System.out.print("Decompressing...");
            HuffmanCompressor.decompress(compressed);
            System.out.println(" Done.");

            // 4. Verify
            // The logic creates "Restored_test_image.bmp"
            File f3 = new File("Restored_" + original);
            if (f1.length() == f3.length()) {
                System.out.println("[PASS] Sizes match.");
                // Clean up
                f1.delete(); f2.delete(); f3.delete();
            } else {
                System.out.println("[FAIL] Sizes differ! Original: " + f1.length() + " vs Restored: " + f3.length());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDummyFile(String name) throws Exception {
        FileOutputStream fos = new FileOutputStream(name);
        // Write a repeating pattern (easy to compress)
        for (int i = 0; i < 1000; i++) {
            fos.write("AAAAABBBCC".getBytes());
        }
        fos.close();
    }
}