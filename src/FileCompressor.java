import java.io.*;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.zip.*;

public class FileCompressor {

    public static byte[] createArchive(List<File> selectedFiles, String compression, String password, String encryption) throws IOException, GeneralSecurityException {
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
            byte[] encryptedData = FileEncryptor.encryptData(encryption, password, compressedData);
            baos.write(encryptedData);
        } else {
            // No encryption
            baos.write(compressedData);
        }

        return baos.toByteArray();
    }

    public static byte[] decompressFile(File file, String encryption, String password) throws IOException, GeneralSecurityException {
        byte[] compressedContent = Files.readAllBytes(file.toPath());

        // Decrypt the content if encryption is selected
        byte[] decryptedContent = compressedContent;
        if (!"None".equals(encryption)) {
            decryptedContent = FileEncryptor.decryptData(encryption, password, compressedContent);
        }

        // Decompress the file and extract the original file name
        String fileName = file.getName();
        byte[] decompressedFile = null;
        if (fileName.endsWith(".zip")) {
            // Decompress ZIP file
            decompressedFile = decompressZipFile(decryptedContent);
        } else if (fileName.endsWith(".gz")) {
            // Decompress GZIP file
            decompressedFile = decompressGzipFile(decryptedContent);
        } else {
            // No compression
            decompressedFile = decryptedContent;
        }

        return decompressedFile;
    }

    private static byte[] decompressZipFile(byte[] compressedData) throws IOException {
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

    private static byte[] decompressGzipFile(byte[] compressedData) throws IOException {
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
}
