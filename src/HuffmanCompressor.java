import java.io.*;

public class HuffmanCompressor {

    /**
     * Helper: Counts how many times every byte appears in the file.
     */
    private static int[] getFrequencies(File inputFile) throws IOException {
        int[] frequencies = new int[256];
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            int byteRead;
            while ((byteRead = fis.read()) != -1) {
                frequencies[byteRead]++;
            }
        }
        return frequencies;
    }

    /**
     * COMPRESS: Turns a normal file into a .huff file
     */
    public static void compress(String inputFilePath, String outputFilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        
        // 1. Analyze File
        int[] frequencies = getFrequencies(inputFile);
        HuffmanNode root = HuffmanTree.buildTree(frequencies);
        String[] codes = HuffmanTree.generateCodes(root);

        // 2. Write Header (Standard Java DataOutputStream)
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFilePath))) {
            
            // A. Write original filename length & name
            String filename = inputFile.getName();
            dos.writeByte(filename.length());
            dos.writeBytes(filename);

            // B. Write Frequency Table (256 integers)
            for (int f : frequencies) {
                dos.writeInt(f);
            }
        }

        // 3. Write Body (Our Custom BitOutputStream)
        try (BitOutputStream bos = new BitOutputStream(outputFilePath, true)) {
            try (FileInputStream fis = new FileInputStream(inputFile)) {
                int byteRead;
                while ((byteRead = fis.read()) != -1) {
                    String code = codes[byteRead];
                    bos.writeCode(code);
                }
            }
        }
    }

    /**
     * DECOMPRESS: Turns a .huff file back into the original
     */
    public static void decompress(String inputFilePath) throws IOException {
        
        // 1. Read Header
        try (DataInputStream dis = new DataInputStream(new FileInputStream(inputFilePath))) {
            
            // A. Read filename
            int nameLength = dis.readByte();
            byte[] nameBytes = new byte[nameLength];
            dis.readFully(nameBytes);
            String originalName = new String(nameBytes);

            // B. Read Frequencies
            int[] frequencies = new int[256];
            long totalBytes = 0;
            for (int i = 0; i < 256; i++) {
                frequencies[i] = dis.readInt();
                totalBytes += frequencies[i];
            }

            // C. Rebuild Tree
            HuffmanNode root = HuffmanTree.buildTree(frequencies);

            // 2. Decode Body
            // We use 'dis' (already past header) for the BitReader
            try (BitInputStream bis = new BitInputStream(dis, totalBytes)) {
                
                // --- FIX: HANDLE FILE PATHS CORRECTLY ---
                File compressedFile = new File(inputFilePath);
                File parentDir = compressedFile.getParentFile();
                
                File outputFile;
                if (parentDir == null) {
                    // File is in current directory
                    outputFile = new File("Restored_" + originalName);
                } else {
                    // File is in a specific folder, keep output there
                    outputFile = new File(parentDir, "Restored_" + originalName);
                }
                // ----------------------------------------
                
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    HuffmanNode current = root;
                    long decoded = 0;

                    while (decoded < totalBytes) {
                        int bit = bis.readBit();
                        if (bit == -1) break;

                        // Walk the tree
                        if (bit == 0) current = current.left;
                        else current = current.right;

                        // Found a byte!
                        if (current.isLeaf()) {
                            fos.write(current.data);
                            current = root; // Reset to top
                            decoded++;
                            bis.byteDecoded();
                        }
                    }
                }
            }
        }
    }
}