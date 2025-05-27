package org.example.structures.doubleList;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class DoubleList<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private DoubleNode<T> head;
    private DoubleNode<T> tail;
    private int size;

    public DoubleList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(T data) {
        DoubleNode<T> newNode = new DoubleNode<>(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.setNext(head);
            head.setPrev(newNode);
            head = newNode;
        }
        size++;
    }

    public void addLast(T data) {
        DoubleNode<T> newNode = new DoubleNode<>(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrev(tail);
            tail = newNode;
        }
        size++;
    }

    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = head.getData();
        head = head.getNext();
        if (head == null) { // List became empty
            tail = null;
        } else {
            head.setPrev(null);
        }
        size--;
        return data;
    }

    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = tail.getData();
        tail = tail.getPrev();
        if (tail == null) { // List became empty
            head = null;
        } else {
            tail.setNext(null);
        }
        size--;
        return data;
    }

    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }
        DoubleNode<T> current = head;
        while (current != null) {
            if ((data == null && current.getData() == null) || (data != null && data.equals(current.getData()))) {
                if (current == head) {
                    removeFirst();
                } else if (current == tail) {
                    removeLast();
                } else {
                    current.getPrev().setNext(current.getNext());
                    current.getNext().setPrev(current.getPrev());
                    size--;
                }
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public T peekFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        return head.getData();
    }

    public T peekLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        return tail.getData();
    }

    public boolean contains(T data) {
        DoubleNode<T> current = head;
        while (current != null) {
            if ((data == null && current.getData() == null) || (data != null && data.equals(current.getData()))) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    private DoubleNode<T> getNode(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        DoubleNode<T> current;
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.getPrev();
            }
        }
        return current;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        DoubleNode<T> current;
        if (index < size / 2) { // Optimización: empezar desde la cabeza si el índice está en la primera mitad
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }
        } else { // Empezar desde la cola si el índice está en la segunda mitad
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.getPrev();
            }
        }
        return current.getData();
    }

    /**
     * Reemplaza el elemento en la posición especificada en esta lista con el elemento especificado.
     * @param index índice del elemento a reemplazar.
     * @param data elemento a ser almacenado en la posición especificada.
     * @return el elemento previamente en la posición especificada.
     * @throws IndexOutOfBoundsException si el índice está fuera de rango (index < 0 || index >= size()).
     */
    public T set(int index, T data) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        DoubleNode<T> current;
        // Optimización para encontrar el nodo (similar a get)
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.getNext();
            }
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.getPrev();
            }
        }
        T oldData = current.getData();
        current.setData(data); // Asumiendo que DoubleNode tiene setData(T data)
        return oldData;
    }

    public void add(T data, int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            addFirst(data);
        } else if (index == size) {
            addLast(data);
        } else {
            DoubleNode<T> current = getNode(index);
            DoubleNode<T> newNode = new DoubleNode<>(data);
            newNode.setNext(current);
            newNode.setPrev(current.getPrev());
            current.getPrev().setNext(newNode);
            current.setPrev(newNode);
            size++;
        }
    }

    public T removeAtIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            return removeFirst();
        }
        if (index == size - 1) {
            return removeLast();
        }
        DoubleNode<T> nodeToRemove = getNode(index);
        T data = nodeToRemove.getData();
        nodeToRemove.getPrev().setNext(nodeToRemove.getNext());
        nodeToRemove.getNext().setPrev(nodeToRemove.getPrev());
        size--;
        return data;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private DoubleNode<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = current.getData();
                current = current.getNext();
                return data;
            }

            @Override
            public void remove() {
                // Opcional: Dejar sin implementar o implementar si es necesario
                throw new UnsupportedOperationException("Remove operation is not supported.");
            }
        };
    }
} 