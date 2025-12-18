import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

public class HuffmanGUI extends JFrame {

    private JTextArea logArea;
    private JProgressBar progressBar;
    
    // --- State Variables ---
    private File selectedInputFile; // The file/folder user selected
    private JLabel inputFileLabel;
    
    // We track the last file we created so the next step is easy
    private File lastGeneratedFile; 
    
    // Decompression State
    private File selectedDecompressFile;
    private JLabel decompressFileLabel;

    // Networking State
    private JTextField ipField;

    public HuffmanGUI() {
        setTitle("Universal Secure Compressor");
        setSize(1000, 800); // Slightly taller for separated buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 30, 30));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("HUFFMAN SECURE SUITE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- Tabs ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab(" 1. Compression & Encryption ", createMainPanel());
        tabbedPane.addTab(" 2. Decryption & Restore ", createDecompressPanel());
        tabbedPane.addTab(" 3. Network Transfer ", createNetworkPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // --- Footer ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea(8, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        footerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        footerPanel.add(progressBar, BorderLayout.SOUTH);
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        log("✅ System Ready.");
    }

    // ==================== PANEL 1: SEPARATED COMPRESS / ENCRYPT ====================
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); // Better spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- UI ELEMENTS ---
        JButton selectBtn = new JButton("📂 Step 1: Select Source File/Folder");
        selectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inputFileLabel = new JLabel("No selection");
        inputFileLabel.setForeground(Color.BLUE);
        
        // Analysis Group
        JPanel analysisPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton viewTableBtn = new JButton("📊 View Frequencies");
        JButton viewTreeBtn = new JButton("🌳 Visualize Tree");
        analysisPanel.add(viewTableBtn);
        analysisPanel.add(viewTreeBtn);

        // Action Buttons (Separated)
        JButton compressBtn = new JButton("⬇ Step 2: COMPRESS (Huffman)");
        compressBtn.setBackground(new Color(0, 122, 255)); // Blue
        compressBtn.setForeground(Color.WHITE);
        compressBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton encryptBtn = new JButton("🔒 Step 3: ENCRYPT (AES-256)");
        encryptBtn.setBackground(new Color(220, 53, 69)); // Red
        encryptBtn.setForeground(Color.WHITE);
        encryptBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // --- LAYOUT ---
        gbc.gridx = 0; gbc.gridy = 0; panel.add(selectBtn, gbc);
        gbc.gridy = 1; panel.add(inputFileLabel, gbc);
        gbc.gridy = 2; panel.add(new JSeparator(), gbc);
        
        gbc.gridy = 3; panel.add(new JLabel("Analysis Tools (Optional):"), gbc);
        gbc.gridy = 4; panel.add(analysisPanel, gbc);
        gbc.gridy = 5; panel.add(new JSeparator(), gbc);
        
        gbc.gridy = 6; panel.add(compressBtn, gbc);
        gbc.gridy = 7; panel.add(new JLabel("<html><i>Creates a compressed .huff file</i></html>", SwingConstants.CENTER), gbc);
        
        gbc.gridy = 8; panel.add(new Box.Filler(new Dimension(0,20), new Dimension(0,20), new Dimension(0,20)), gbc); // Spacer
        
        gbc.gridy = 9; panel.add(encryptBtn, gbc);
        gbc.gridy = 10; panel.add(new JLabel("<html><i>Secures any file with a password (.enc)</i></html>", SwingConstants.CENTER), gbc);

        // --- LISTENERS ---
        selectBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedInputFile = fc.getSelectedFile();
                // Reset last generated so we encrypt the NEW selection, not the old one
                lastGeneratedFile = null; 
                String type = selectedInputFile.isDirectory() ? "[FOLDER] " : "[FILE] ";
                inputFileLabel.setText(type + selectedInputFile.getName());
                log("Selected: " + type + selectedInputFile.getAbsolutePath());
            }
        });

        viewTableBtn.addActionListener(e -> analyzeFile(false));
        viewTreeBtn.addActionListener(e -> analyzeFile(true));
        
        compressBtn.addActionListener(e -> startCompressionOnly());
        encryptBtn.addActionListener(e -> startEncryptionOnly());

        return panel;
    }

    // ==================== PANEL 2: DECOMPRESSION ====================
    private JPanel createDecompressPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton selectBtn = new JButton("📂 Select File (.enc or .huff)");
        decompressFileLabel = new JLabel("No selection");
        
        JButton decryptBtn = new JButton("🔓 Decrypt (.enc -> .huff)");
        decryptBtn.setBackground(new Color(220, 53, 69));
        decryptBtn.setForeground(Color.WHITE);
        
        JButton decompressBtn = new JButton("📈 Decompress (.huff -> Original)");
        decompressBtn.setBackground(new Color(40, 167, 69));
        decompressBtn.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Step 1: Choose File"), gbc);
        gbc.gridy = 1; panel.add(selectBtn, gbc);
        gbc.gridy = 2; panel.add(decompressFileLabel, gbc);
        gbc.gridy = 3; panel.add(new JSeparator(), gbc);
        gbc.gridy = 4; panel.add(decryptBtn, gbc);
        gbc.gridy = 5; panel.add(decompressBtn, gbc);

        selectBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedDecompressFile = fc.getSelectedFile();
                decompressFileLabel.setText(selectedDecompressFile.getName());
                log("Selected for Restore: " + selectedDecompressFile.getName());
            }
        });

        decryptBtn.addActionListener(e -> startDecryptionOnly());
        decompressBtn.addActionListener(e -> startDecompressionOnly());

        return panel;
    }

    // ==================== PANEL 3: NETWORK ====================
    private JPanel createNetworkPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sender
        JPanel senderPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        senderPanel.setBorder(BorderFactory.createTitledBorder("Sender Mode"));
        ipField = new JTextField("127.0.0.1");
        JButton sendBtn = new JButton("📡 Send Last Processed File");
        senderPanel.add(new JLabel("Receiver IP:"));
        senderPanel.add(ipField);
        senderPanel.add(sendBtn);

        // Receiver
        JPanel receiverPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        receiverPanel.setBorder(BorderFactory.createTitledBorder("Receiver Mode"));
        JButton startServerBtn = new JButton("🎧 Start Listening (Port 5000)");
        receiverPanel.add(startServerBtn);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(senderPanel, gbc);
        gbc.gridy = 1; panel.add(receiverPanel, gbc);

        sendBtn.addActionListener(e -> {
            File fileToSend = (lastGeneratedFile != null) ? lastGeneratedFile : selectedInputFile;
            if (fileToSend == null || !fileToSend.exists()) {
                JOptionPane.showMessageDialog(this, "No file selected or generated to send!");
                return;
            }
            NetworkModule.sendFile(fileToSend, ipField.getText(), 5000, logArea);
        });

        startServerBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                NetworkModule.startServer(5000, fc.getSelectedFile(), logArea);
                startServerBtn.setEnabled(false);
                startServerBtn.setText("Running...");
            }
        });

        return panel;
    }

    // ==================== LOGIC: COMPRESSION ====================
    private void startCompressionOnly() {
        if (selectedInputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file or folder first.");
            return;
        }

        // SAVE AS DIALOG
        JFileChooser fileChooser = new JFileChooser(selectedInputFile.getParent());
        fileChooser.setDialogTitle("Save Compressed File As");
        String defaultName = selectedInputFile.getName() + (selectedInputFile.isDirectory() ? ".tar.huff" : ".huff");
        fileChooser.setSelectedFile(new File(defaultName));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File destination = fileChooser.getSelectedFile();
        // Force extension
        if (!destination.getName().endsWith(".huff")) {
            destination = new File(destination.getAbsolutePath() + ".huff");
        }
        final File finalDest = destination;

        progressBar.setIndeterminate(true);
        new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                File input = selectedInputFile;
                File tempTar = null;

                // 1. If Folder -> Archive
                if (input.isDirectory()) {
                    log("📂 Folder detected. Archiving...");
                    tempTar = new File(input.getParent(), input.getName() + ".tar");
                    new Archiver().createArchive(input, tempTar);
                    input = tempTar; // Now we compress the tar
                }

                // 2. Compress
                log("📉 Compressing...");
                HuffmanCompressor.compress(input.getAbsolutePath(), finalDest.getAbsolutePath());

                // Cleanup
                if (tempTar != null) tempTar.delete();

                lastGeneratedFile = finalDest; // Store for encryption step
                long time = System.currentTimeMillis() - start;

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    log("✅ Compressed: " + finalDest.getName() + " (" + time + "ms)");
                    JOptionPane.showMessageDialog(this, "Compression Successful!\nSaved to: " + finalDest.getAbsolutePath());
                    
                    // Auto-update selection for next step
                    int response = JOptionPane.showConfirmDialog(this, "Do you want to Encrypt this file now?", "Next Step", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        selectedInputFile = finalDest;
                        inputFileLabel.setText("[FILE] " + finalDest.getName());
                    }
                });
            } catch (Exception e) {
                handleError(e);
            }
        }).start();
    }

    // ==================== LOGIC: ENCRYPTION ====================
    private void startEncryptionOnly() {
        if (selectedInputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file to Encrypt.");
            return;
        }
        if (selectedInputFile.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Cannot encrypt a folder directly.\nPlease COMPRESS it first.");
            return;
        }

        String password = JOptionPane.showInputDialog(this, "Create Password:");
        if (password == null || password.isEmpty()) return;

        JFileChooser fileChooser = new JFileChooser(selectedInputFile.getParent());
        fileChooser.setDialogTitle("Save Encrypted File As");
        fileChooser.setSelectedFile(new File(selectedInputFile.getName() + ".enc"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dest = fileChooser.getSelectedFile();
        if(!dest.getName().endsWith(".enc")) dest = new File(dest.getAbsolutePath() + ".enc");
        final File finalDest = dest;

        progressBar.setIndeterminate(true);
        new Thread(() -> {
            try {
                log("🔒 Encrypting...");
                CryptoModule.encrypt(selectedInputFile, finalDest, password);
                lastGeneratedFile = finalDest;
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    log("✅ Encrypted: " + finalDest.getName());
                    JOptionPane.showMessageDialog(this, "Encryption Complete!");
                });
            } catch (Exception e) { handleError(e); }
        }).start();
    }

    // ==================== LOGIC: RESTORATION ====================
    private void startDecryptionOnly() {
        if (selectedDecompressFile == null) return;
        String pass = JOptionPane.showInputDialog(this, "Enter Password:");
        if (pass == null) return;

        progressBar.setIndeterminate(true);
        new Thread(() -> {
            try {
                log("🔓 Decrypting...");
                String outName = selectedDecompressFile.getAbsolutePath().replace(".enc", "");
                if (!outName.endsWith(".huff")) outName += ".huff"; // Safe guess
                File outFile = new File(outName);

                CryptoModule.decrypt(selectedDecompressFile, outFile, pass);
                
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    log("✅ Decrypted to: " + outFile.getName());
                    JOptionPane.showMessageDialog(this, "Decryption Complete. Now you can Decompress.");
                    // Auto-select for next step
                    selectedDecompressFile = outFile;
                    decompressFileLabel.setText(outFile.getName());
                });
            } catch (Exception e) { handleError(e); }
        }).start();
    }

    private void startDecompressionOnly() {
        if (selectedDecompressFile == null) return;
        
        progressBar.setIndeterminate(true);
        new Thread(() -> {
            try {
                log("📈 Decompressing...");
                String restoredPath = HuffmanCompressor.decompress(selectedDecompressFile.getAbsolutePath());
                File restoredFile = new File(restoredPath);

                // Check for Archive
                if (restoredFile.getName().endsWith(".tar")) {
                    log("📦 Unpacking Archive...");
                    File outDir = new File(restoredFile.getParent(), restoredFile.getName().replace(".tar", ""));
                    new Unarchiver().unpackArchive(restoredFile, outDir);
                    restoredFile.delete(); 
                    log("✅ Restored Folder: " + outDir.getName());
                } else {
                    log("✅ Restored File: " + restoredFile.getName());
                }

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    JOptionPane.showMessageDialog(this, "Restoration Finished!");
                });
            } catch (Exception e) { handleError(e); }
        }).start();
    }

    // ==================== ANALYSIS LOGIC (FIXED) ====================
    private void analyzeFile(boolean visualizeTree) {
        if (selectedInputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
            return;
        }

        // --- FIXED: AUTO-GENERATE TEMP ARCHIVE FOR FOLDERS ---
        File fileToAnalyze = selectedInputFile;
        File tempTar = null;

        if (selectedInputFile.isDirectory()) {
            log("⏳ Creating temporary archive for visualization...");
            try {
                // Create a temp .tar just for the visualizer
                tempTar = File.createTempFile("huff_viz_", ".tar");
                new Archiver().createArchive(selectedInputFile, tempTar);
                fileToAnalyze = tempTar;
            } catch (Exception e) {
                handleError(e);
                return;
            }
        }

        final File finalFile = fileToAnalyze;
        final File tarCleanup = tempTar;

        progressBar.setIndeterminate(true);
        new Thread(() -> {
            try {
                int[] frequencies = new int[256];
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(finalFile))) {
                    int b;
                    while ((b = bis.read()) != -1) frequencies[b]++;
                }

                HuffmanNode root = HuffmanTree.buildTree(frequencies);

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    if (visualizeTree) {
                        JDialog d = new JDialog(this, "Tree Visualization", true);
                        d.add(new JScrollPane(new TreePanel(root)));
                        d.setSize(1000, 700);
                        d.setLocationRelativeTo(this);
                        d.setVisible(true);
                    } else {
                        // Show Table (Simplified for brevity, same as before)
                        String[] codes = HuffmanTree.generateCodes(root);
                        String[] cols = {"Byte", "Freq", "Code"};
                        DefaultTableModel model = new DefaultTableModel(cols, 0);
                        for(int i=0; i<256; i++) {
                            if(frequencies[i]>0) model.addRow(new Object[]{i, frequencies[i], codes[i]});
                        }
                        JDialog d = new JDialog(this, "Frequencies");
                        d.add(new JScrollPane(new JTable(model)));
                        d.setSize(500,600);
                        d.setVisible(true);
                    }
                });
                
                // Cleanup temp file
                if (tarCleanup != null) tarCleanup.delete();

            } catch (Exception e) { handleError(e); }
        }).start();
    }

    private void log(String msg) { SwingUtilities.invokeLater(() -> logArea.append(msg + "\n")); }
    private void handleError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            log("❌ Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        });
    }
}