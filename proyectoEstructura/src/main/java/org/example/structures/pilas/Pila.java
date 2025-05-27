package org.example.structures.pilas;

import java.io.Serializable;
import java.util.EmptyStackException;

public class Pila<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static class NodoPila<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        private T dato;
        private NodoPila<T> siguiente;

        public NodoPila(T dato) {
            this.dato = dato;
        }
    }

    private NodoPila<T> tope;
    private int tamanio;

    public Pila() {
        this.tope = null;
        this.tamanio = 0;
    }

    /**
     * Añade un elemento a la cima de la pila.
     * @param elemento El elemento a añadir.
     */
    public void push(T elemento) {
        NodoPila<T> nuevoNodo = new NodoPila<>(elemento);
        nuevoNodo.siguiente = tope;
        tope = nuevoNodo;
        tamanio++;
    }

    /**
     * Elimina y devuelve el elemento de la cima de la pila.
     * @return El elemento de la cima.
     * @throws EmptyStackException si la pila está vacía.
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T dato = tope.dato;
        tope = tope.siguiente;
        tamanio--;
        return dato;
    }

    /**
     * Devuelve el elemento de la cima de la pila sin eliminarlo.
     * @return El elemento de la cima.
     * @throws EmptyStackException si la pila está vacía.
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return tope.dato;
    }

    /**
     * Verifica si la pila está vacía.
     * @return true si la pila está vacía, false en caso contrario.
     */
    public boolean isEmpty() {
        return tope == null; // o tamanio == 0
    }

    /**
     * Devuelve el número de elementos en la pila.
     * @return El tamaño de la pila.
     */
    public int size() {
        return tamanio;
    }

    /**
     * Elimina todos los elementos de la pila.
     */
    public void clear() {
        tope = null;
        tamanio = 0;
    }
} 