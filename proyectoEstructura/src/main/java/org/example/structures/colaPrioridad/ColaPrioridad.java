package org.example.structures.colaPrioridad;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.io.Serializable;

// Implementación de una Cola de Prioridad utilizando un Min-Heap.
// Los elementos con menor valor tienen mayor prioridad.
public class ColaPrioridad<T extends Comparable<T>> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] heap; // Usamos Object[] y hacemos casting, o T[] con supresión de advertencias
    private int size;

    @SuppressWarnings("unchecked")
    public ColaPrioridad() {
        heap = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T elemento) {
        if (elemento == null) {
            throw new NullPointerException("El elemento no puede ser nulo.");
        }
        ensureCapacity();
        heap[size] = elemento;
        size++;
        heapifyUp(size - 1);
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola de prioridad está vacía.");
        }
        return (T) heap[0];
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola de prioridad está vacía.");
        }
        T result = (T) heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null; // Ayudar al GC
        size--;
        if (size > 0) {
            heapifyDown(0);
        }
        return result;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    private void ensureCapacity() {
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }
    }

    private void heapifyUp(int index) {
        int parentIndex = parent(index);
        while (index > 0 && compare(heap[index], heap[parentIndex]) < 0) {
            swap(index, parentIndex);
            index = parentIndex;
            parentIndex = parent(index);
        }
    }

    private void heapifyDown(int index) {
        int smallest = index;
        int left = leftChild(index);
        int right = rightChild(index);

        if (left < size && compare(heap[left], heap[smallest]) < 0) {
            smallest = left;
        }
        if (right < size && compare(heap[right], heap[smallest]) < 0) {
            smallest = right;
        }

        if (smallest != index) {
            swap(index, smallest);
            heapifyDown(smallest);
        }
    }

    @SuppressWarnings("unchecked")
    private int compare(Object o1, Object o2) {
        return ((T) o1).compareTo((T) o2);
    }

    private int parent(int i) {
        return (i - 1) / 2;
    }

    private int leftChild(int i) {
        return 2 * i + 1;
    }

    private int rightChild(int i) {
        return 2 * i + 2;
    }

    private void swap(int i, int j) {
        Object temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
} 