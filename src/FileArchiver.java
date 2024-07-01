import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.*;

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
                    byte[] compressedArchive = createArchive(compression, password, encryption);

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

            private byte[] createArchive(String compression, String password, String encryption) throws IOException, GeneralSecurityException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // Step 1: Compress the files
                ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream();
                if ("ZIP".equals(compression)) {
                    try (ZipOutputStream zipOut = new ZipOutputStream(compressedBaos)) {
                        for (File file : selectedFiles) {
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            ZipEntry zipEntry = new ZipEntry(file.getName());
                            zipOut.putNextEntry(zipEntry);
                            zipOut.write(fileContent);
                            zipOut.closeEntry();
                        }
                    }
                } else if ("GZIP".equals(compression)) {
                    try (GZIPOutputStream gzipOut = new GZIPOutputStream(compressedBaos)) {
                        for (File file : selectedFiles) {
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            gzipOut.write(fileContent);
                        }
                    }
                } else {
                    // No compression
                    for (File file : selectedFiles) {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        compressedBaos.write(fileContent);
                    }
                }
                byte[] compressedData = compressedBaos.toByteArray();

                // Step 2: Encrypt the compressed data if encryption is selected
                if (!"None".equals(encryption)) {
                    Cipher cipher;
                    if ("AES".equals(encryption)) {
                        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        SecretKey secretKey = generateKey("AES", password);
                        IvParameterSpec iv = generateIv(cipher.getBlockSize());
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
                        baos.write(iv.getIV()); // Write IV at the beginning of the output stream
                    } else if ("DES".equals(encryption)) {
                        cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                        SecretKeySpec secretKey = generateDesKey(password);
                        IvParameterSpec iv = generateIv(cipher.getBlockSize());
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
                        baos.write(iv.getIV()); // Write IV at the beginning of the output stream
                    } else {
                        throw new GeneralSecurityException("Unsupported encryption type");
                    }
                    byte[] encryptedData = cipher.doFinal(compressedData);
                    baos.write(encryptedData);
                } else {
                    // No encryption
                    baos.write(compressedData);
                }

                return baos.toByteArray();
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
                        byte[] compressedContent = Files.readAllBytes(file.toPath());

                        // Decrypt the content if encryption is selected
                        byte[] decryptedContent = compressedContent;
                        if (!"None".equals(encryption)) {
                            Cipher cipher = initializeCipherForDecryption(encryption, password, compressedContent);
                            decryptedContent = cipher.doFinal(compressedContent, cipher.getBlockSize(), compressedContent.length - cipher.getBlockSize());
                        }

                        // Decompress the file and extract the original file name
                        String fileName = file.getName();
                        byte[] decompressedFile = null;
                        if (fileName.endsWith(".zip")) {
                            // Decompress ZIP file
                            decompressedFile = decompressZipFile(decryptedContent);
                            // Extract the original file name from the ZIP entry
                            try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(decryptedContent))) {
                                ZipEntry entry;
                                while ((entry = zipIn.getNextEntry()) != null) {
                                    if (!entry.isDirectory()) {
                                        fileName = entry.getName();
                                        break; // Use the name from the first non-directory entry
                                    }
                                }
                            }
                        } else if (fileName.endsWith(".gz")) {
                            // Decompress GZIP file
                            decompressedFile = decompressGzipFile(decryptedContent);
                            fileName = fileName.substring(0, fileName.length() - 3); // Remove ".gz" extension
                        } else {
                            // No compression
                            decompressedFile = decryptedContent;
                        }

                        // Save the decompressed file with its original name
                        JFileChooser saveFileChooser = new JFileChooser();
                        saveFileChooser.setDialogTitle("Save Decompressed File");

                        // Use the original file name if available
                        if (fileName != null) {
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

            private Cipher initializeCipherForDecryption(String encryptionAlgorithm, String password, byte[] compressedData) throws GeneralSecurityException {
                Cipher cipher;
                if ("AES".equals(encryptionAlgorithm)) {
                    cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    SecretKey secretKey = generateKey("AES", password);
                    IvParameterSpec iv = new IvParameterSpec(compressedData, 0, cipher.getBlockSize());
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
                } else if ("DES".equals(encryptionAlgorithm)) {
                    cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                    SecretKeySpec secretKey = generateDesKey(password);
                    IvParameterSpec iv = new IvParameterSpec(compressedData, 0, cipher.getBlockSize());
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
                } else {
                    throw new GeneralSecurityException("Unsupported encryption type");
                }
                return cipher;
            }

            private byte[] decompressZipFile(byte[] compressedData) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(compressedData))) {
                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = zipIn.read(buffer)) != -1) {
                                baos.write(buffer, 0, bytesRead);
                            }
                            break; // Only decompress the first file in ZIP
                        }
                    }
                }
                return baos.toByteArray();
            }

            private byte[] decompressGzipFile(byte[] compressedData) throws IOException {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gzipIn.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                }
                return baos.toByteArray();
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

    private SecretKey generateKey(String algorithm, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Use a secure method to generate the key from the password
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), "someRandomSalt".getBytes(), 65536, "AES".equals(algorithm) ? 128 : 64);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), algorithm);
    }

    private SecretKeySpec generateDesKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = "someRandomSalt".getBytes(); // Use a secure salt in practice
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 56); // 56 bits for DES key
        SecretKey tmp = factory.generateSecret(spec);
        byte[] key = Arrays.copyOf(tmp.getEncoded(), 8); // DES key size is 8 bytes
        return new SecretKeySpec(key, "DES");
    }

    private IvParameterSpec generateIv(int size) {
        byte[] iv = new byte[size];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
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
