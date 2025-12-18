import java.io.*;

public class Unarchiver {

    /**
     * Unpacks a single archive file back into its original folder structure.
     * @param archiveFile The input file (e.g., "archive.dat")
     * @param destFolder The folder where files should be extracted
     */
    public void unpackArchive(File archiveFile, File destFolder) throws IOException {
        if (!destFolder.exists()) {
            destFolder.mkdirs(); // Create destination if it doesn't exist
        }

        try (FileInputStream fis = new FileInputStream(archiveFile);
             DataInputStream in = new DataInputStream(new BufferedInputStream(fis))) {

            System.out.println("Starting Unpack...");

            while (true) {
                // 1. Read Path Length
                // If we hit the end of the stream unexpectedly, break
                try {
                    // We use mark/reset logic or check available, 
                    // but DataInputStream usually throws EOFException if empty.
                    // However, our Archiver explicitly writes '0' at the end.
                    int pathLength = in.readInt();
                    
                    if (pathLength == 0) {
                        System.out.println("End of Archive marker found.");
                        break; // STOP: We reached the end
                    }

                    // 2. Read Relative Path
                    byte[] pathBytes = new byte[pathLength];
                    in.readFully(pathBytes);
                    String relativePath = new String(pathBytes, "UTF-8");

                    // 3. Read File Size
                    long fileSize = in.readLong();

                    // 4. Recreate the File
                    File outputFile = new File(destFolder, relativePath);
                    
                    // Ensure parent directories exist (e.g., for "docs/resume.pdf", ensure "docs" exists)
                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    System.out.println("Extracting: " + relativePath + " (" + fileSize + " bytes)");

                    // 5. Write File Content
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[4096];
                        long bytesRemaining = fileSize;
                        int bytesRead;

                        while (bytesRemaining > 0) {
                            // Only read up to 'bytesRemaining' or buffer size, whichever is smaller
                            int bytesToRead = (int) Math.min(buffer.length, bytesRemaining);
                            bytesRead = in.read(buffer, 0, bytesToRead);
                            
                            if (bytesRead == -1) {
                                throw new IOException("Unexpected End of File while reading content for: " + relativePath);
                            }

                            fos.write(buffer, 0, bytesRead);
                            bytesRemaining -= bytesRead;
                        }
                    }

                } catch (EOFException e) {
                    // This handles cases where the '0' marker might be missing or stream ends abruptly
                    break;
                }
            }
            System.out.println("Unpack Complete.");
        }
    }
}