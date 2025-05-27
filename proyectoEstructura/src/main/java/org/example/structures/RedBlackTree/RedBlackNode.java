package org.example.structures.RedBlackTree;

import java.io.Serializable;

enum Color {
    RED,
    BLACK
}

public class RedBlackNode<T extends Comparable<T>> implements Serializable {
    private static final long serialVersionUID = 1L;
    T data;
    Color color;
    RedBlackNode<T> left, right, parent;

    public RedBlackNode(T data) {
        this.data = data;
        this.color = Color.RED; // Nuevos nodos son usualmente rojos por defecto
        this.left = null;
        this.right = null;
        this.parent = null;
    }
    
    // Getters y setters básicos si son necesarios externamente,
    // aunque muchas operaciones del árbol rojo-negro manipulan estos directamente.

    @Override
    public String toString() {
        // Ayuda para la depuración
        String c = (color == Color.RED) ? "R" : "B";
        return data + "(" + c + ")";
    }
} 