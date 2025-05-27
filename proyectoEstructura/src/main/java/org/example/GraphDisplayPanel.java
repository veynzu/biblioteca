package org.example;

import org.example.model.User;
import org.example.structures.graph.Graph;
import org.example.structures.doubleList.DoubleList;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class GraphDisplayPanel extends JPanel {

    private Graph<User> graph;
    private Map<User, Point> nodePositions;
    private static final int PADDING = 50;
    private static final int NODE_RADIUS = 20;

    public GraphDisplayPanel(Graph<User> graph) {
        this.graph = graph;
        this.nodePositions = new HashMap<>();
        setPreferredSize(new Dimension(600, 600));
        calculateNodePositions();
    }

    private void calculateNodePositions() {
        if (graph == null || graph.getVertices().isEmpty()) {
            return;
        }

        Set<User> vertexSet = graph.getVertices();
        DoubleList<User> vertices = new DoubleList<>();
        for (User user : vertexSet) {
            vertices.addLast(user);
        }
        
        int numNodes = vertices.size();
        if (numNodes == 0) return;

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (panelWidth == 0 || panelHeight == 0) {
            panelWidth = 500;
            panelHeight = 500;
        }

        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;
        int layoutRadius = Math.min(centerX, centerY) - PADDING - NODE_RADIUS;

        for (int i = 0; i < numNodes; i++) {
            User user = vertices.get(i);
            double angle = 2 * Math.PI * i / numNodes;
            int x = (int) (centerX + layoutRadius * Math.cos(angle));
            int y = (int) (centerY + layoutRadius * Math.sin(angle));
            nodePositions.put(user, new Point(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (nodePositions.isEmpty() && graph != null && !graph.getVertices().isEmpty()) {
            calculateNodePositions();
        }
        
        if (graph == null || graph.getVertices().isEmpty()) {
            g2d.drawString("No hay datos del grafo para mostrar.", 20, 20);
            return;
        }

        Set<User> vertexSet = graph.getVertices();
        DoubleList<User> vertices = new DoubleList<>();
        for (User user : vertexSet) {
            vertices.addLast(user);
        }

        g2d.setColor(Color.GRAY);
        for (int i = 0; i < vertices.size(); i++) {
            User u1 = vertices.get(i);
            Point p1 = nodePositions.get(u1);
            if (p1 == null) continue;

            DoubleList<User> neighbors = graph.getNeighbors(u1);
            for (int j = 0; j < neighbors.size(); j++) {
                User u2 = neighbors.get(j);
                Point p2 = nodePositions.get(u2);
                if (p2 == null) continue;
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        for (int i = 0; i < vertices.size(); i++) {
            User user = vertices.get(i);
            Point p = nodePositions.get(user);
            if (p == null) continue;

            g2d.setColor(Color.CYAN);
            g2d.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);
            
            String username = user.getUsername();
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(username);
            int textHeight = fm.getAscent() - fm.getDescent();
            g2d.drawString(username, p.x - textWidth / 2, p.y + textHeight / 2);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
} 