package org.example.structures.BinaryTree;

// No se usarán colecciones de Java para la estructura interna.
// Se puede usar java.util.List o similar para devolver resultados de recorridos si se desea.
import org.example.structures.doubleList.DoubleList; // Usar nuestra DoubleList

public class BinaryTree<T extends Comparable<T>> {
    private BinaryNode<T> root;

    public BinaryTree() {
        this.root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void clear() {
        root = null;
    }

    // Insertar un elemento
    public void insert(T data) {
        root = insertRecursive(root, data);
    }

    private BinaryNode<T> insertRecursive(BinaryNode<T> current, T data) {
        if (current == null) {
            return new BinaryNode<>(data);
        }

        if (data.compareTo(current.getData()) < 0) {
            current.setLeft(insertRecursive(current.getLeft(), data));
        } else if (data.compareTo(current.getData()) > 0) {
            current.setRight(insertRecursive(current.getRight(), data));
        } else {
            return current;
        }
        return current;
    }

    // Buscar un elemento
    public T search(T data) {
        return searchRecursive(root, data);
    }

    private T searchRecursive(BinaryNode<T> current, T data) {
        if (current == null || data == null) {
            return null;
        }
        int compareResult = data.compareTo(current.getData());
        if (compareResult == 0) {
            return current.getData();
        }
        if (compareResult < 0) {
            return searchRecursive(current.getLeft(), data);
        } else {
            return searchRecursive(current.getRight(), data);
        }
    }

    // Recorrido InOrder (Izquierda, Raíz, Derecha)
    public DoubleList<T> inOrderTraversal() {
        DoubleList<T> result = new DoubleList<>();
        inOrderRecursive(root, result);
        return result;
    }

    private void inOrderRecursive(BinaryNode<T> node, DoubleList<T> result) {
        if (node != null) {
            inOrderRecursive(node.getLeft(), result);
            result.addLast(node.getData());
            inOrderRecursive(node.getRight(), result);
        }
    }

    // Recorrido PreOrder (Raíz, Izquierda, Derecha)
    public DoubleList<T> preOrderTraversal() {
        DoubleList<T> result = new DoubleList<>();
        preOrderRecursive(root, result);
        return result;
    }

    private void preOrderRecursive(BinaryNode<T> node, DoubleList<T> result) {
        if (node != null) {
            result.addLast(node.getData());
            preOrderRecursive(node.getLeft(), result);
            preOrderRecursive(node.getRight(), result);
        }
    }

    // Recorrido PostOrder (Izquierda, Derecha, Raíz)
    public DoubleList<T> postOrderTraversal() {
        DoubleList<T> result = new DoubleList<>();
        postOrderRecursive(root, result);
        return result;
    }

    private void postOrderRecursive(BinaryNode<T> node, DoubleList<T> result) {
        if (node != null) {
            postOrderRecursive(node.getLeft(), result);
            postOrderRecursive(node.getRight(), result);
            result.addLast(node.getData());
        }
    }

    // Eliminar un elemento (simplificado)
    public void delete(T data) {
        root = deleteRecursive(root, data);
    }

    private BinaryNode<T> deleteRecursive(BinaryNode<T> current, T data) {
        if (current == null) {
            return null;
        }

        if (data.equals(current.getData())) {
            // Nodo encontrado, manejar los 3 casos de eliminación
            // Caso 1: Nodo sin hijos (hoja)
            if (current.getLeft() == null && current.getRight() == null) {
                return null;
            }
            // Caso 2: Nodo con un solo hijo
            if (current.getRight() == null) {
                return current.getLeft();
            }
            if (current.getLeft() == null) {
                return current.getRight();
            }
            // Caso 3: Nodo con dos hijos
            // Encontrar el sucesor in-order (el valor más pequeño en el subárbol derecho)
            T smallestValue = findSmallestValue(current.getRight());
            current.setData(smallestValue); // Reemplazar el dato del nodo actual con el del sucesor
            current.setRight(deleteRecursive(current.getRight(), smallestValue)); // Eliminar el sucesor del subárbol derecho
            return current;

        } else if (data.compareTo(current.getData()) < 0) {
            current.setLeft(deleteRecursive(current.getLeft(), data));
            return current;
        } else {
            current.setRight(deleteRecursive(current.getRight(), data));
            return current;
        }
    }

    private T findSmallestValue(BinaryNode<T> rootNode) {
        return rootNode.getLeft() == null ? rootNode.getData() : findSmallestValue(rootNode.getLeft());
    }
} 