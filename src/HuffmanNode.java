
public class HuffmanNode implements Comparable<HuffmanNode> {
    
    public int frequency;
    public int data; // Using int to handle bytes (0-255). -1 indicates an internal node.
    public HuffmanNode left;
    public HuffmanNode right;

    // Constructor
    public HuffmanNode(int data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
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
        String type = isLeaf() ? "Leaf: " + (char)data : "Internal";
        return type + " (" + frequency + ")";
    }
}
