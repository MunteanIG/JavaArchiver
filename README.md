# FileArchiver

FileArchiver is a Java Swing application that allows you to archive, compress, and encrypt files. It provides options for different compression methods (ZIP, GZIP) and encryption algorithms (AES, DES). This application can also decompress and decrypt files that were previously archived.

## Features

- **File Selection**: Select multiple files to archive.
- **Compression Options**: Choose between no compression, ZIP, or GZIP.
- **Encryption Options**: Choose between no encryption, AES, or DES.
- **Archive Creation**: Create a compressed and optionally encrypted archive of the selected files.
- **Decompression**: Decompress and decrypt previously archived files.

## Requirements

- Java Development Kit (JDK) 8 or higher

## Usage
### Archiving Files

1. Launch the application.
2. Click on the **"Browse"** button to select files for archiving.
3. Choose a compression method from the **"Compression"** dropdown menu.
4. If encryption is desired, select an encryption method from the **"Encryption"** dropdown menu and enter a password.
5. Click the **"Archive"** button to create the archive.
6. Choose a location to save the archived file.

### Decompressing Files
1. Launch the application.
2. Click the "Decompress" button and select the archive file.
3. If the archive was encrypted, select the appropriate encryption method and enter the password when prompted.
4. Choose a location to save the decompressed files.


## GUI Components
### File Selection

- **Select Files**: Opens a file chooser dialog to select multiple files.
- **Selected Files**: Displays the names of selected files.

### Compression Options

- **Compression**: Dropdown to select compression type (None, ZIP, GZIP).

### Password Field

- **Password**: Field to enter a password for encryption. Enabled only if an encryption method is selected.
  
### Encryption Options

- **Encryption**: Dropdown to select encryption type (None, AES, DES).
  
### Action Buttons

- **Archive**: Compresses and encrypts the selected files based on chosen options.
- **Decompress**: Decompresses and decrypts an archived file.

## Code Overview
### Main Class `FileArchiver`
The FileArchiver class extends JFrame and handles the GUI and user interactions. It initializes the UI components, sets up action listeners for buttons, and manages the file archiving and decompression processes.

### Utility Classes
- `FileCompressor`: Handles file compression and decompression.
- `FileEncryptor`: Manages file encryption and decryption.
- `Utils`: Provides utility methods for generating IVs (Initialization Vectors).
  
### Key Methods
#### FileArchiver
- `createArchive(String compression, String password, String encryption)`: Compresses and optionally encrypts selected files.
  
#### FileCompressor
- `decompressFile(File file, String encryption, String password)`: Decrypts and decompresses a file.

#### FileEncryptor
- `encryptData(String encryption, String password, byte[] data)`: Encrypts data using the specified algorithm.
- `decryptData(String encryption, String password, byte[] encryptedData)`: Decrypts data using the specified algorithm.

### Helper Methods
- `generateKey(String algorithm, String password)`: Generates a secret key for AES encryption.
- `generateDesKey(String password)`: Generates a secret key for DES encryption.
- `generateIv(int size)`: Generates an IV for encryption.

 ## Notes
- **Security**: The provided code uses a hardcoded salt for key generation. In practice, use a unique and secure salt for each encryption operation.
- **Error Handling**: The application shows error dialogs for exceptions during file operations. Ensure robust error handling for production use.
- **Extensiosn**: The application can be extended to support additional compression and encryption algorithms as needed.
