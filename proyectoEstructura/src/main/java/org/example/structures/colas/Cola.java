package org.example.structures.colas;

import java.io.Serializable;
import java.util.NoSuchElementException;

// NodoCola (si es clase interna, también debe ser Serializable)
class NodoCola<T> implements Serializable { // Asumiendo que NodoCola está aquí o es accesible
    private static final long serialVersionUID = 1L;
    T data;
    NodoCola<T> next;

    public NodoCola(T data) {
        this.data = data;
        this.next = null;
    }
}

public class Cola<T> implements Serializable { // Implementar Serializable
    private static final long serialVersionUID = 1L;
    private NodoCola<T> frente;
    private NodoCola<T> finalCola;
    private int tamanio;

    public Cola() {
        this.frente = null;
        this.finalCola = null;
        this.tamanio = 0;
    }

    /**
     * Añade un elemento al final de la cola.
     * @param elemento El elemento a añadir.
     */
    public void enqueue(T elemento) {
        NodoCola<T> nuevoNodo = new NodoCola<>(elemento);
        if (isEmpty()) {
            frente = nuevoNodo;
        } else {
            finalCola.next = nuevoNodo;
        }
        finalCola = nuevoNodo;
        tamanio++;
    }

    /**
     * Elimina y devuelve el elemento del frente de la cola.
     * @return El elemento del frente.
     * @throws NoSuchElementException si la cola está vacía.
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        T dato = frente.data;
        frente = frente.next;
        if (frente == null) { // Si la cola queda vacía
            finalCola = null;
        }
        tamanio--;
        return dato;
    }

    /**
     * Devuelve el elemento del frente de la cola sin eliminarlo.
     * @return El elemento del frente.
     * @throws NoSuchElementException si la cola está vacía.
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return frente.data;
    }

    /**
     * Verifica si la cola está vacía.
     * @return true si la cola está vacía, false en caso contrario.
     */
    public boolean isEmpty() {
        return frente == null; // o tamanio == 0
    }

    /**
     * Devuelve el número de elementos en la cola.
     * @return El tamaño de la cola.
     */
    public int size() {
        return tamanio;
    }

    /**
     * Elimina todos los elementos de la cola.
     */
    public void clear() {
        frente = null;
        finalCola = null;
        tamanio = 0;
    }
} 