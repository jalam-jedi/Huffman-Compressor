import java.util.PriorityQueue;

public class HuffmanTree {

    /**
     * Step 1: Build the Huffman Tree from a frequency table.
     * @param frequencies Array of size 256 where index = byte value, value = count.
     * @return The root node of the Huffman Tree.
     */
    public static HuffmanNode buildTree(int[] frequencies) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        // 1. Create a leaf node for every byte that appears in the file
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                // Constructor 1: Leaf Node (Data + Frequency)
                queue.add(new HuffmanNode(i, frequencies[i]));
            }
        }

        // Edge Case: Empty file
        if (queue.isEmpty()) {
            return null;
        }

        // Edge Case: File with only 1 type of character (e.g., "AAAAA")
        // We need at least one edge to generate a code (like "0"), so we artificially add a parent.
        if (queue.size() == 1) {
            HuffmanNode onlyNode = queue.poll();
            // Manually create a parent that points to this single node
            HuffmanNode parent = new HuffmanNode(-1, onlyNode.frequency);
            parent.left = onlyNode; 
            return parent;
        }

        // 2. The Core Huffman Algorithm
        // While we have more than 1 node, combine the two smallest
        while (queue.size() > 1) {
            HuffmanNode min1 = queue.poll(); // Smallest
            HuffmanNode min2 = queue.poll(); // Second smallest

            // Constructor 2: Internal Node (Left + Right)
            // This automatically calculates the sum of frequencies and sets data to -1
            HuffmanNode parent = new HuffmanNode(min1, min2);

            // Put parent back into queue
            queue.add(parent);
        }

        // The last remaining node is the Root
        return queue.poll();
    }

    /**
     * Step 2: Generate the Lookup Table (e.g., 'A' -> "110")
     * @param root The root of the tree.
     * @return A String array where index = byte value, value = binary code string.
     */
    public static String[] generateCodes(HuffmanNode root) {
        String[] codes = new String[256];
        generateRecursive(root, "", codes);
        return codes;
    }

    // Helper recursive function to walk the tree
    private static void generateRecursive(HuffmanNode node, String currentCode, String[] codes) {
        if (node == null) return;

        // If we found a leaf, save the code
        if (node.isLeaf()) {
            // Safety check: Ensure data is within valid byte range (0-255)
            if (node.data >= 0 && node.data < 256) {
                codes[node.data] = currentCode;
            }
            return;
        }

        // Go Left (Append '0')
        generateRecursive(node.left, currentCode + "0", codes);

        // Go Right (Append '1')
        generateRecursive(node.right, currentCode + "1", codes);
    }
}