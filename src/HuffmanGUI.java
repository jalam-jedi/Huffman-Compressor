import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;

public class HuffmanGUI extends JFrame {

    private JTextArea logArea;
    private JProgressBar progressBar;
    private File selectedCompressFile;
    private File selectedDecompressFile;
    private JLabel compressFileLabel;
    private JLabel decompressFileLabel;

    // Colors for styling
    private final Color COLOR_PRIMARY = new Color(0, 122, 255); // Blue
    private final Color COLOR_SUCCESS = new Color(40, 167, 69); // Green

    public HuffmanGUI() {
        setTitle("Universal Huffman Compressor");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 30, 30));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("HUFFMAN COMPRESSOR PRO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Main Tabs ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("  Compression Dashboard  ", createCompressPanel());
        tabbedPane.addTab("  Decompression Utility  ", createDecompressPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // --- 3. Status Footer ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        logArea = new JTextArea(6, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 240, 240)); 
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(100, 25));

        footerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        footerPanel.add(progressBar, BorderLayout.SOUTH);
        
        add(footerPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        log("✅ System Initialized. Ready.");
    }

    private JPanel createCompressPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel step1 = new JLabel("Step 1: Input Source");
        step1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton selectBtn = new JButton("📂 Select File");
        compressFileLabel = new JLabel("No file selected");
        compressFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        JLabel step2 = new JLabel("Step 2: Analysis");
        step2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel analysisButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton viewTableBtn = new JButton("📊 View Code Table");
        JButton viewTreeBtn = new JButton("🌳 Visualize Tree");
        analysisButtons.add(viewTableBtn);
        analysisButtons.add(viewTreeBtn);

        JLabel step3 = new JLabel("Step 3: Execute");
        step3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton runBtn = new JButton("💾 Save Compressed File As...");
        runBtn.setBackground(COLOR_SUCCESS);
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runBtn.setFocusPainted(false);

        // -- Layout --
        gbc.gridx = 0; gbc.gridy = 0; panel.add(step1, gbc);
        gbc.gridy = 1; panel.add(selectBtn, gbc);
        gbc.gridy = 2; panel.add(compressFileLabel, gbc);
        gbc.gridy = 3; panel.add(new JSeparator(), gbc);
        gbc.gridy = 4; panel.add(step2, gbc);
        gbc.gridy = 5; panel.add(analysisButtons, gbc);
        gbc.gridy = 6; panel.add(new JSeparator(), gbc);
        gbc.gridy = 7; panel.add(step3, gbc);
        gbc.gridy = 8; gbc.ipady = 15; panel.add(runBtn, gbc);

        // -- Listeners --
        selectBtn.addActionListener(e -> {
            // UPDATED: Start in User's Documents/Home folder
            JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedCompressFile = fc.getSelectedFile();
                compressFileLabel.setText(selectedCompressFile.getName());
                log("File selected: " + selectedCompressFile.getName());
            }
        });

        viewTableBtn.addActionListener(e -> analyzeFile(false));
        viewTreeBtn.addActionListener(e -> analyzeFile(true));
        runBtn.addActionListener(e -> startCompression());

        return panel;
    }

    private JPanel createDecompressPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel step1 = new JLabel("Step 1: Select Compressed File");
        step1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton selectBtn = new JButton("📂 Select .huff File");
        decompressFileLabel = new JLabel("No file selected");
        decompressFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        JLabel step2 = new JLabel("Step 2: Restore");
        step2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton runBtn = new JButton("↺ START DECOMPRESSION");
        runBtn.setBackground(COLOR_PRIMARY);
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runBtn.setFocusPainted(false);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(step1, gbc);
        gbc.gridy = 1; panel.add(selectBtn, gbc);
        gbc.gridy = 2; panel.add(decompressFileLabel, gbc);
        gbc.gridy = 3; panel.add(new JSeparator(), gbc);
        gbc.gridy = 4; panel.add(step2, gbc);
        gbc.gridy = 5; gbc.ipady = 15; panel.add(runBtn, gbc);

        selectBtn.addActionListener(e -> {
            // UPDATED: Start in User's Documents/Home folder
            JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fc.setFileFilter(new FileNameExtensionFilter("Huffman Files (.huff)", "huff"));
            
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedDecompressFile = fc.getSelectedFile();
                decompressFileLabel.setText(selectedDecompressFile.getName());
                log("Archive selected: " + selectedDecompressFile.getName());
            }
        });

        runBtn.addActionListener(e -> startDecompression());

        return panel;
    }

    // --- Core Logic ---

    private void analyzeFile(boolean visualizeTree) {
        if (selectedCompressFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
            return;
        }
        
        try {
            int[] frequencies = new int[256];
            try (FileInputStream fis = new FileInputStream(selectedCompressFile)) {
                int b;
                while ((b = fis.read()) != -1) frequencies[b]++;
            }

            HuffmanNode root = HuffmanTree.buildTree(frequencies);

            if (visualizeTree) {
                JDialog dialog = new JDialog(this, "Huffman Tree Visualization", true);
                TreePanel treePanel = new TreePanel(root);
                JScrollPane scroll = new JScrollPane(treePanel);
                dialog.add(scroll);
                dialog.setSize(800, 600);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            } else {
                String[] codes = HuffmanTree.generateCodes(root);
                String[] cols = {"Byte", "Char", "Freq", "Code"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);
                for(int i=0; i<256; i++) {
                    if(frequencies[i] > 0) {
                        String ch = (i > 32 && i < 127) ? String.valueOf((char)i) : "0x"+Integer.toHexString(i);
                        model.addRow(new Object[]{i, ch, frequencies[i], codes[i]});
                    }
                }
                JTable table = new JTable(model);
                JDialog d = new JDialog(this, "Encoding Table");
                d.add(new JScrollPane(table));
                d.setSize(500, 600);
                d.setLocationRelativeTo(this);
                d.setVisible(true);
            }
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void startCompression() {
        if (selectedCompressFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
            return;
        }

        // --- NEW: Save Dialog ---
        JFileChooser fileChooser = new JFileChooser(selectedCompressFile.getParent());
        fileChooser.setDialogTitle("Save Compressed File As");
        // Default name: original.huff
        fileChooser.setSelectedFile(new File(selectedCompressFile.getName() + ".huff"));
        
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure extension is .huff
            if (!fileToSave.getName().toLowerCase().endsWith(".huff")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".huff");
            }

            final String outputPath = fileToSave.getAbsolutePath();
            progressBar.setIndeterminate(true);
            log("⏳ Compressing to: " + fileToSave.getName());
            
            new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    HuffmanCompressor.compress(selectedCompressFile.getAbsolutePath(), outputPath);
                    long time = System.currentTimeMillis() - start;
                    
                    File original = new File(selectedCompressFile.getAbsolutePath());
                    File compressed = new File(outputPath);
                    
                    // Calculate Ratio
                    long origSize = original.length();
                    long compSize = compressed.length();
                    double spaceSaved = 0.0;
                    if (origSize > 0) spaceSaved = 100.0 * (origSize - compSize) / origSize;

                    final double finalSaved = spaceSaved;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(100);
                        log("✅ Complete in " + time + "ms");
                        log("   Original: " + origSize + " bytes");
                        log("   Compressed: " + compSize + " bytes");
                        
                        if (finalSaved >= 0) {
                            log(String.format("   Space Saved: %.2f%%", finalSaved));
                        } else {
                            log(String.format("   Size Change: %.2f%% (Expanded)", finalSaved));
                        }
                        JOptionPane.showMessageDialog(this, "Compression Successful!\nSaved to: " + outputPath);
                    });
                } catch (Exception e) { handleError(e); }
            }).start();
        } else {
            log("🚫 Compression cancelled by user.");
        }
    }

    private void startDecompression() {
        if (selectedDecompressFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first.");
            return;
        }
        
        progressBar.setIndeterminate(true);
        log("⏳ Decompressing...");
        
        new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                HuffmanCompressor.decompress(selectedDecompressFile.getAbsolutePath());
                long time = System.currentTimeMillis() - start;
                
                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    log("✅ Decompression Complete in " + time + "ms");
                    JOptionPane.showMessageDialog(this, "Decompression Successful!");
                });
            } catch (Exception e) { handleError(e); }
        }).start();
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void handleError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            log("❌ Error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}