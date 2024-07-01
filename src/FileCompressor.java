import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.zip.*;

public class FileCompressor {

    public static byte[] createArchive(List<File> selectedFiles, String compression, String password, String encryption) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

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
            for (File file : selectedFiles) {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                compressedBaos.write(fileContent);
            }
        }
        byte[] compressedData = compressedBaos.toByteArray();

        if (!"None".equals(encryption)) {
            baos.write(FileEncryptor.encryptData(encryption, password, compressedData));
        } else {
            baos.write(compressedData);
        }

        return baos.toByteArray();
    }

    public static Map<String, byte[]> decompressFile(File file, String encryption, String password) throws IOException, GeneralSecurityException {
        byte[] compressedContent = Files.readAllBytes(file.toPath());

        byte[] decryptedContent = compressedContent;
        if (!"None".equals(encryption)) {
            decryptedContent = FileEncryptor.decryptData(encryption, password, compressedContent);
        }

        String fileName = file.getName();
        Map<String, byte[]> decompressedFiles = new HashMap<>();
        if (fileName.endsWith(".zip")) {
            decompressedFiles = decompressZipFile(decryptedContent);
        } else if (fileName.endsWith(".gz")) {
            decompressedFiles.put(fileName.replace(".gz", ""), decompressGzipFile(decryptedContent));
        } else {
            decompressedFiles.put(fileName, decryptedContent);
        }

        return decompressedFiles;
    }

    private static Map<String, byte[]> decompressZipFile(byte[] compressedData) throws IOException {
        Map<String, byte[]> decompressedFiles = new HashMap<>();
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(compressedData))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zipIn.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    decompressedFiles.put(entry.getName(), baos.toByteArray());
                }
            }
        }
        return decompressedFiles;
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
