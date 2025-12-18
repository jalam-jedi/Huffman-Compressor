import java.io.*;

public class Archiver {

    // 1. The Entry Point: Call this to start archiving a folder
    public void createArchive(File sourceFolder, File destinationFile) throws IOException {
        // Use DataOutputStream to write Integers (lengths) and Longs (sizes) easily
        try (FileOutputStream fos = new FileOutputStream(destinationFile);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fos))) {

            // Start the recursion
            addDirectoryToArchive(sourceFolder, sourceFolder, out);

            // MARKER: End of Archive
            // We write a 0 length name to tell the decompressor "We are done!"
            out.writeInt(0); 
            
            System.out.println("Archive created successfully!");
        }
    }

    // 2. The Recursive Walker: Crawls the folder tree
    private void addDirectoryToArchive(File currentFile, File rootBase, DataOutputStream out) throws IOException {
        if (currentFile.isDirectory()) {
            // If it's a folder, list all files inside and call this method for each
            File[] files = currentFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    addDirectoryToArchive(file, rootBase, out);
                }
            }
        } else {
            // If it's a file, write it to the stream
            writeSingleFile(currentFile, rootBase, out);
        }
    }

    // 3. The Writer: Writes the Header + Body (as we discussed)
    private void writeSingleFile(File file, File rootBase, DataOutputStream out) throws IOException {
        System.out.println("Archiving: " + file.getName());

        // A. Calculate Relative Path 
        // We want "docs/resume.pdf", NOT "C:/Users/Admin/docs/resume.pdf"
        String relativePath = rootBase.toURI().relativize(file.toURI()).getPath();
        byte[] nameBytes = relativePath.getBytes("UTF-8");

        // B. Write Header
        out.writeInt(nameBytes.length); // 1. Name Length
        out.write(nameBytes);           // 2. Name String
        out.writeLong(file.length());   // 3. File Size

        // C. Write Body (The actual file content)
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096]; // 4KB Buffer
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}