import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoModule {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * Encrypts a file using AES-256.
     * Generates a random Initialization Vector (IV), writes it to the start of the file,
     * and then writes the encrypted data.
     */
    public static void encrypt(File inputFile, File outputFile, String password) throws Exception {
        // 1. Generate Key from Password
        SecretKeySpec key = generateKey(password);

        // 2. Generate Random IV (Initialization Vector)
        // This ensures that encrypting the same file twice produces different results.
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 3. Initialize Cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        // 4. Write Streams
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            // A. Write the IV unencrypted at the start of the file
            fos.write(iv);

            // B. Write the encrypted body
            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    /**
     * Decrypts an AES-256 encrypted file.
     * Reads the IV from the start of the file, then decrypts the rest.
     */
    public static void decrypt(File inputFile, File outputFile, String password) throws Exception {
        // 1. Generate Key from Password
        SecretKeySpec key = generateKey(password);

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            // 2. Read the IV (First 16 bytes)
            byte[] iv = new byte[16];
            int ivRead = fis.read(iv);
            if (ivRead < 16) {
                throw new IOException("File is too short or corrupted (Missing IV).");
            }
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 3. Initialize Cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            // 4. Decrypt and Write
            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    /**
     * Helper: Turns a simple text password (e.g., "1234") into a secure 256-bit AES Key.
     */
    private static SecretKeySpec generateKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = password.getBytes("UTF-8");
        key = sha.digest(key); // Hash the password
        // Use the full 32 bytes (256 bits) for AES-256
        return new SecretKeySpec(key, ALGORITHM);
    }
}