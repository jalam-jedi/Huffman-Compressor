⚙️ Installation & Setup

Prerequisites

Java Development Kit (JDK) 8 or higher.

(Optional) Git to clone the repository.

1. Clone the Repository

git clone [https://github.com/YourUsername/HuffmanCompressor.git](https://github.com/YourUsername/HuffmanCompressor.git)
cd HuffmanCompressor


2. Compile the Project

Navigate to the source folder and compile. We include the library path for the UI theme.

Windows (PowerShell):

cd src
javac -cp ".;../lib/flatlaf-3.5.4.jar" Main.java


Linux / Mac (Bash):

cd src
javac -cp ".:../lib/flatlaf-3.5.4.jar" Main.java


▶️ Usage Guide

Running the Application

To launch the GUI, run the Main class with the library classpath:

Windows:

java -cp ".;../lib/flatlaf-3.5.4.jar" Main


Linux / Mac:

java -cp ".:../lib/flatlaf-3.5.4.jar" Main


How to Compress

Go to the Compression Dashboard tab.

Click Select File to choose your target file.

(Optional) Click Visualize Tree to see how the algorithm organizes the data.

Click Start Compression.

Choose a location to save your .huff file.

How to Decompress

Go to the Decompression Utility tab.

Select a .huff file.

Click Start Decompression.

The original file will be restored in the same folder.

🧠 Algorithm Details

This project demonstrates proficiency in Data Structures and Algorithms (CLO-5):

Frequency Analysis: Reads the file byte-by-byte to build a Frequency Map.

Priority Queue (Min-Heap): Used to efficiently select the two least frequent nodes.

Binary Tree Construction: Builds the Huffman Tree bottom-up. Frequent bytes are near the root (short codes); rare bytes are deep leaves (long codes).

Bit Manipulation: Uses custom Bit Streams to pack variable-length codes (e.g., 3 bits + 5 bits) into standard 8-bit bytes for storage.
