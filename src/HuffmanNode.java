public class HuffmanNode implements Comparable<HuffmanNode> {
    
    public int frequency;
    public int data; // Using int to handle bytes (0-255). -1 indicates an internal node.
    public HuffmanNode left;
    public HuffmanNode right;

    // 1. Constructor for Leaf Nodes (Actual data)
    public HuffmanNode(int data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    // 2. CRITICAL MISSING PIECE: Constructor for Internal Nodes
    // This merges two smaller nodes into one parent node
    public HuffmanNode(HuffmanNode left, HuffmanNode right) {
        this.data = -1; // -1 means "I am just a connector, I hold no data"
        this.frequency = left.frequency + right.frequency;
        this.left = left;
        this.right = right;
    }

    /*
     * Checks if this node is a leaf (has no children).
     * Leaf nodes contain the actual data (byte values).
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * This method allows the PriorityQueue to sort nodes.
     * We want the SMALLEST frequency to be at the top of the queue.
     */
    @Override
    public int compareTo(HuffmanNode other) {
        return this.frequency - other.frequency;
    }
    
    @Override
    public String toString() {
        // Safe printing: If data is a printable character, show it. Otherwise show the number.
        String dataStr = (data > 32 && data < 127) ? "'" + (char)data + "'" : String.valueOf(data);
        String type = isLeaf() ? "Leaf: " + dataStr : "Internal";
        return type + " (" + frequency + ")";
    }
}