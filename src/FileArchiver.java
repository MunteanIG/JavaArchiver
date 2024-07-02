import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class FileArchiver extends JFrame {

    private List<File> selectedFiles;
    private JTextField fileField;
    private JComboBox<String> compressionCombo;
    private JPasswordField passwordField;
    private JComboBox<String> encryptionCombo;

    public FileArchiver() {
        setTitle("File Archiver");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        selectedFiles = new ArrayList<>();

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(7, 2, 10, 10));

        // File selection
        JLabel fileLabel = new JLabel("Select Files:");
        fileField = new JTextField();
        JButton browseButton = new JButton("Browse");

        // Compression options
        JLabel compressionLabel = new JLabel("Compression:");
        String[] compressionOptions = {"None", "ZIP", "GZIP"};
        compressionCombo = new JComboBox<>(compressionOptions);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        // Encryption options
        JLabel encryptionLabel = new JLabel("Encryption:");
        String[] encryptionOptions = {"None", "AES", "DES"};
        encryptionCombo = new JComboBox<>(encryptionOptions);

        // Archive and Decompress buttons
        JButton archiveButton = new JButton("Archive");
        JButton decompressButton = new JButton("Decompress");

        // Add components to panel
        mainPanel.add(fileLabel);
        mainPanel.add(fileField);
        mainPanel.add(new JLabel()); // empty placeholder
        mainPanel.add(browseButton);
        mainPanel.add(compressionLabel);
        mainPanel.add(compressionCombo);
        mainPanel.add(passwordLabel);
        mainPanel.add(passwordField);
        mainPanel.add(encryptionLabel);
        mainPanel.add(encryptionCombo);
        mainPanel.add(archiveButton);
        mainPanel.add(decompressButton);

        add(mainPanel);

        // Action listeners
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                int option = fileChooser.showOpenDialog(FileArchiver.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    selectedFiles.clear();
                    for (File file : fileChooser.getSelectedFiles()) {
                        selectedFiles.add(file);
                    }
                    updateSelectedFilesField();
                }
            }
        });

        encryptionCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String encryption = (String) encryptionCombo.getSelectedItem();
                passwordField.setEnabled(!"None".equals(encryption));
            }
        });

        archiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String compression = (String) compressionCombo.getSelectedItem();
                String password = new String(passwordField.getPassword());
                String encryption = (String) encryptionCombo.getSelectedItem();

                try {
                    // Create a compressed and optionally encrypted archive
                    byte[] compressedArchive = FileCompressor.createArchive(selectedFiles, compression, password, encryption);

                    // Save archive file
                    JFileChooser saveFileChooser = new JFileChooser();
                    int saveOption = saveFileChooser.showSaveDialog(FileArchiver.this);
                    if (saveOption == JFileChooser.APPROVE_OPTION) {
                        File archiveFile = saveFileChooser.getSelectedFile();
                        String outputFilePath = archiveFile.getAbsolutePath();

                        // Automatically add the appropriate extension based on compression type
                        if ("ZIP".equals(compression) && !outputFilePath.endsWith(".zip")) {
                            outputFilePath += ".zip";
                        } else if ("GZIP".equals(compression) && !outputFilePath.endsWith(".gz")) {
                            outputFilePath += ".gz";
                        }

                        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                            fos.write(compressedArchive);
                        }
                        JOptionPane.showMessageDialog(FileArchiver.this,
                                "Files archived successfully!",
                                "Archive Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException | GeneralSecurityException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(FileArchiver.this,
                            "Error archiving files: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(FileArchiver.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    // Prompt for encryption type and password if necessary
                    String encryption = (String) JOptionPane.showInputDialog(
                            FileArchiver.this,
                            "Select encryption type used:",
                            "Encryption Type",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"None", "AES", "DES"},
                            "None"
                    );

                    String password = "";
                    if (!"None".equals(encryption)) {
                        JPasswordField pwd = new JPasswordField(10);
                        int action = JOptionPane.showConfirmDialog(
                                FileArchiver.this,
                                pwd,
                                "Enter Password",
                                JOptionPane.OK_CANCEL_OPTION
                        );
                        if (action == JOptionPane.OK_OPTION) {
                            password = new String(pwd.getPassword());
                        } else {
                            return; // If password is not provided, cancel the operation
                        }
                    }

                    try {
                        byte[] decompressedFile = FileCompressor.decompressFile(file, encryption, password);

                        // Save the decompressed file with its original name
                        JFileChooser saveFileChooser = new JFileChooser();
                        saveFileChooser.setDialogTitle("Save Decompressed File");

                        String fileName = file.getName();
                        // Use the original file name if available
                        if (fileName != null) {
                            if (fileName.endsWith(".zip")) {
                                fileName = fileName.substring(0, fileName.length() - 4);
                            } else if (fileName.endsWith(".gz")) {
                                fileName = fileName.substring(0, fileName.length() - 3);
                            }
                            saveFileChooser.setSelectedFile(new File(fileName));
                        }

                        int saveOption = saveFileChooser.showSaveDialog(FileArchiver.this);
                        if (saveOption == JFileChooser.APPROVE_OPTION) {
                            File saveFile = saveFileChooser.getSelectedFile();
                            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                                fos.write(decompressedFile);
                            }
                            JOptionPane.showMessageDialog(FileArchiver.this,
                                    "File decompressed successfully!",
                                    "Decompression Complete", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (IOException | GeneralSecurityException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(FileArchiver.this,
                                "Error decompressing file: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void updateSelectedFilesField() {
        StringBuilder sb = new StringBuilder();
        for (File file : selectedFiles) {
            sb.append(file.getName()).append("; ");
        }
        fileField.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileArchiver().setVisible(true);
            }
        });
    }
}
