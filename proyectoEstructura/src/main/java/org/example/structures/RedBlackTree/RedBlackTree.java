package org.example.structures.RedBlackTree;

// No se usarán colecciones de Java para la estructura interna.
// Se puede usar java.util.List o similar para devolver resultados de recorridos si se desea.
import org.example.structures.doubleList.DoubleList; // Usar nuestra DoubleList
import java.io.Serializable; // Importar

public class RedBlackTree<T extends Comparable<T>> implements Serializable { // Implementar
    private static final long serialVersionUID = 1L;
    private RedBlackNode<T> root;
    private final RedBlackNode<T> TNULL; // Nodo sentinela nulo

    public RedBlackTree() {
        TNULL = new RedBlackNode<>(null); // El tipo de dato de TNULL no importa realmente
        TNULL.color = Color.BLACK;
        TNULL.left = null;
        TNULL.right = null;
        root = TNULL;
    }

    private void leftRotate(RedBlackNode<T> x) {
        RedBlackNode<T> y = x.right;
        x.right = y.left;
        if (y.left != TNULL) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == null) { // x era la raíz
            this.root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(RedBlackNode<T> y) {
        RedBlackNode<T> x = y.left;
        y.left = x.right;
        if (x.right != TNULL) {
            x.right.parent = y;
        }
        x.parent = y.parent;
        if (y.parent == null) { // y era la raíz
            this.root = x;
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }
        x.right = y;
        y.parent = x;
    }

    public void insert(T key) {
        RedBlackNode<T> node = new RedBlackNode<>(key);
        node.parent = null;
        node.data = key;
        node.left = TNULL;
        node.right = TNULL;
        node.color = Color.RED; // Nuevos nodos son rojos

        RedBlackNode<T> y = null;
        RedBlackNode<T> x = this.root;

        while (x != TNULL) {
            y = x;
            if (node.data.compareTo(x.data) < 0) {
                x = x.left;
            } else {
                x = x.right;
            }
        }

        node.parent = y;
        if (y == null) { // Árbol estaba vacío
            root = node;
        } else if (node.data.compareTo(y.data) < 0) {
            y.left = node;
        } else {
            y.right = node;
        }

        if (node.parent == null) { // Si es el primer nodo
            node.color = Color.BLACK;
            return;
        }

        if (node.parent.parent == null) { // Si el padre es la raíz
            return;
        }

        fixInsert(node);
    }

    private void fixInsert(RedBlackNode<T> k) {
        RedBlackNode<T> u;
        while (k.parent.color == Color.RED) {
            if (k.parent == k.parent.parent.right) { // Padre de k es hijo derecho del abuelo
                u = k.parent.parent.left; // Tío de k
                if (u.color == Color.RED) { // Caso 1: Tío es ROJO
                    u.color = Color.BLACK;
                    k.parent.color = Color.BLACK;
                    k.parent.parent.color = Color.RED;
                    k = k.parent.parent;
                } else { // Tío es NEGRO
                    if (k == k.parent.left) { // Caso 2: k es hijo izquierdo (Triángulo)
                        k = k.parent;
                        rightRotate(k);
                    } // Caso 3: k es hijo derecho (Línea)
                    k.parent.color = Color.BLACK;
                    k.parent.parent.color = Color.RED;
                    leftRotate(k.parent.parent);
                }
            } else { // Padre de k es hijo izquierdo del abuelo (simétrico al anterior)
                u = k.parent.parent.right; // Tío de k
                if (u.color == Color.RED) { // Caso 1: Tío es ROJO
                    u.color = Color.BLACK;
                    k.parent.color = Color.BLACK;
                    k.parent.parent.color = Color.RED;
                    k = k.parent.parent;
                } else { // Tío es NEGRO
                    if (k == k.parent.right) { // Caso 2: k es hijo derecho (Triángulo)
                        k = k.parent;
                        leftRotate(k);
                    }
                    // Caso 3: k es hijo izquierdo (Línea)
                    k.parent.color = Color.BLACK;
                    k.parent.parent.color = Color.RED;
                    rightRotate(k.parent.parent);
                }
            }
            if (k == root) {
                break;
            }
        }
        root.color = Color.BLACK;
    }
    
    // --- Métodos de búsqueda y recorridos (pueden adaptarse de BinaryTree) ---
    public T search(T data) {
        return searchRecursive(this.root, data);
    }

    private T searchRecursive(RedBlackNode<T> node, T data) {
        if (node == TNULL || data == null) {
            return null;
        }
        int compareResult = data.compareTo(node.data);
        if (compareResult == 0) {
            return node.data;
        }
        if (compareResult < 0) {
            return searchRecursive(node.left, data);
        } else {
            return searchRecursive(node.right, data);
        }
    }

    public DoubleList<T> inOrderTraversal() {
        DoubleList<T> result = new DoubleList<>();
        inOrderRecursive(this.root, result);
        return result;
    }

    private void inOrderRecursive(RedBlackNode<T> node, DoubleList<T> result) {
        if (node != TNULL) {
            inOrderRecursive(node.left, result);
            result.addLast(node.data); // Añadir al final para orden in-order
            inOrderRecursive(node.right, result);
        }
    }
    
    // Implementar preOrder y postOrder devolviendo DoubleList también
    public DoubleList<T> preOrderTraversal() {
        DoubleList<T> result = new DoubleList<>();
        preOrderRecursive(this.root, result);
        return result;
    }

    private void preOrderRecursive(RedBlackNode<T> node, DoubleList<T> result) {
        if (node != TNULL) {
            result.addLast(node.data);
            preOrderRecursive(node.left, result);
            preOrderRecursive(node.right, result);
        }
    }

    public DoubleList<T> postOrderTraversal() {
        DoubleList<T> result = new DoubleList<>();
        postOrderRecursive(this.root, result);
        return result;
    }

    private void postOrderRecursive(RedBlackNode<T> node, DoubleList<T> result) {
        if (node != TNULL) {
            postOrderRecursive(node.left, result);
            postOrderRecursive(node.right, result);
            result.addLast(node.data);
        }
    }
    
    // --- Eliminación (Esbozo, es la parte más compleja) ---
    // El método de eliminación en un Árbol Rojo-Negro es significativamente más complejo
    // debido a la necesidad de mantener las propiedades del árbol después de la eliminación.
    // Implica reemplazar el nodo y luego llamar a una función fixDelete que maneja
    // múltiples casos de recoloración y rotaciones.

    // public void delete(T data) { ... } 
    // private void fixDelete(RedBlackNode<T> x) { ... }
    // private void transplant(RedBlackNode<T> u, RedBlackNode<T> v) { ... }
    // private RedBlackNode<T> minimum(RedBlackNode<T> node) { ... } // Para encontrar el sucesor in-order

    public boolean isEmpty() {
        return root == TNULL;
    }

    public void clear() {
        root = TNULL;
    }
} 