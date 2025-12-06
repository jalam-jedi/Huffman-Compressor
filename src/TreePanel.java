import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class TreePanel extends JPanel {
    private final HuffmanNode root;
    private final int NODE_SIZE = 40;
    private final int VERTICAL_SPACING = 80;
    private final int HORIZONTAL_SPACING = 50; // Minimum space between leaves

    // We store the calculated positions here so we don't need to change the Node class
    private final Map<HuffmanNode, Point> nodePositions = new HashMap<>();
    
    // Helper to track where to place the next leaf
    private int currentLeafX = 0;

    public TreePanel(HuffmanNode root) {
        this.root = root;
        this.setBackground(new Color(30, 30, 30));
        
        // 1. Calculate positions BEFORE drawing
        calculatePositions(root, 0);
        
        // 2. Adjust panel size based on the tree width/height so scrolling works
        int width = Math.max(800, currentLeafX + 100);
        int height = getTreeDepth(root) * VERTICAL_SPACING + 100;
        this.setPreferredSize(new Dimension(width, height));
    }

    /**
     * Smart Algorithm:
     * - Leaves get placed sequentially (Left to Right).
     * - Parents get placed exactly in the middle of their children.
     */
    private void calculatePositions(HuffmanNode node, int depth) {
        if (node == null) return;

        // Post-Order Traversal: Process children first
        calculatePositions(node.left, depth + 1);
        calculatePositions(node.right, depth + 1);

        int y = 30 + (depth * VERTICAL_SPACING);
        int x;

        if (node.isLeaf()) {
            // It's a leaf: Place it at the next available spot
            x = 30 + currentLeafX;
            currentLeafX += HORIZONTAL_SPACING;
        } else {
            // It's a parent: Center it over its children
            Point leftPos = nodePositions.get(node.left);
            Point rightPos = nodePositions.get(node.right);
            
            if (leftPos != null && rightPos != null) {
                x = (leftPos.x + rightPos.x) / 2;
            } else if (leftPos != null) {
                x = leftPos.x;
            } else {
                x = currentLeafX; // Should rarely happen in Huffman
            }
        }

        nodePositions.put(node, new Point(x, y));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable smooth graphics (Anti-aliasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (root != null) {
            drawEdges(g2, root);
            drawNodes(g2, root);
        }
    }

    private void drawEdges(Graphics2D g, HuffmanNode node) {
        if (node == null) return;

        Point parentPos = nodePositions.get(node);
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(2));

        if (node.left != null) {
            Point childPos = nodePositions.get(node.left);
            g.drawLine(parentPos.x, parentPos.y + NODE_SIZE/2, childPos.x, childPos.y + NODE_SIZE/2);
            drawEdges(g, node.left);
        }
        if (node.right != null) {
            Point childPos = nodePositions.get(node.right);
            g.drawLine(parentPos.x, parentPos.y + NODE_SIZE/2, childPos.x, childPos.y + NODE_SIZE/2);
            drawEdges(g, node.right);
        }
    }

    private void drawNodes(Graphics2D g, HuffmanNode node) {
        if (node == null) return;

        Point pos = nodePositions.get(node);
        
        // 1. Fill Circle
        if (node.isLeaf()) g.setColor(new Color(40, 167, 69)); // Green for Leaf
        else g.setColor(new Color(70, 130, 180)); // Blue for Internal
        g.fillOval(pos.x - NODE_SIZE/2, pos.y, NODE_SIZE, NODE_SIZE);

        // 2. Draw Border
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawOval(pos.x - NODE_SIZE/2, pos.y, NODE_SIZE, NODE_SIZE);

        // 3. Draw Text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String text;
        if (node.isLeaf()) {
            // Show character or hex code
            if (node.data > 32 && node.data < 127) text = String.valueOf((char)node.data);
            else text = "0x" + Integer.toHexString(node.data);
        } else {
            text = String.valueOf(node.frequency);
        }
        
        FontMetrics fm = g.getFontMetrics();
        int textX = pos.x - fm.stringWidth(text) / 2;
        int textY = pos.y + (NODE_SIZE + fm.getAscent()) / 2 - 4;
        g.drawString(text, textX, textY);

        // Recurse for children
        drawNodes(g, node.left);
        drawNodes(g, node.right);
    }

    private int getTreeDepth(HuffmanNode node) {
        if (node == null) return 0;
        return 1 + Math.max(getTreeDepth(node.left), getTreeDepth(node.right));
    }
}