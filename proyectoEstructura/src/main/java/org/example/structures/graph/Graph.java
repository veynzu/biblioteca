package org.example.structures.graph;

import org.example.structures.doubleList.DoubleList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;
import java.util.Collections;

public class Graph<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<T, DoubleList<T>> adjList;

    public Graph() {
        this.adjList = new HashMap<>();
    }

    /**
     * Añade un vértice al grafo.
     * @param vertex el vértice a añadir.
     * @return true si el vértice fue añadido, false si ya existía.
     */
    public boolean addVertex(T vertex) {
        if (vertex == null || adjList.containsKey(vertex)) {
            return false;
        }
        adjList.put(vertex, new DoubleList<>());
        return true;
    }

    /**
     * Añade una arista no dirigida entre dos vértices.
     * Si los vértices no existen, se añaden primero.
     * @param vertex1 primer vértice.
     * @param vertex2 segundo vértice.
     */
    public boolean addEdge(T vertex1, T vertex2) {
        if (vertex1 == null || vertex2 == null || !adjList.containsKey(vertex1) || !adjList.containsKey(vertex2) || vertex1.equals(vertex2)) {
            return false;
        }
        if (!adjList.get(vertex1).contains(vertex2)) {
            adjList.get(vertex1).addLast(vertex2);
        }
        if (!adjList.get(vertex2).contains(vertex1)) {
            adjList.get(vertex2).addLast(vertex1);
        }
        return true;
    }

    /**
     * Elimina un vértice del grafo, incluyendo todas sus aristas.
     * @param vertex el vértice a eliminar.
     * @return true si el vértice fue eliminado, false si no existía.
     */
    public boolean removeVertex(T vertex) {
        if (vertex == null || !adjList.containsKey(vertex)) {
            return false;
        }
        DoubleList<T> neighbors = adjList.get(vertex);
        if (neighbors != null) {
            for (int i = 0; i < neighbors.size(); i++) {
                T neighbor = neighbors.get(i);
                if (adjList.containsKey(neighbor) && adjList.get(neighbor) != null) {
                    adjList.get(neighbor).remove(vertex);
                }
            }
        }
        adjList.remove(vertex);
        return true;
    }

    /**
     * Elimina la arista no dirigida entre dos vértices.
     * @param vertex1 primer vértice.
     * @param vertex2 segundo vértice.
     * @return true si la arista fue eliminada, false si no existía.
     */
    public boolean removeEdge(T vertex1, T vertex2) {
        if (vertex1 == null || vertex2 == null || !adjList.containsKey(vertex1) || !adjList.containsKey(vertex2)) {
            return false;
        }
        boolean removed1 = false;
        if (adjList.get(vertex1) != null) {
            removed1 = adjList.get(vertex1).remove(vertex2);
        }
        boolean removed2 = false;
        if (adjList.get(vertex2) != null) {
            removed2 = adjList.get(vertex2).remove(vertex1);
        }
        return removed1 && removed2;
    }

    /**
     * Obtiene la lista de vecinos de un vértice dado.
     * @param vertex el vértice.
     * @return una lista de sus vecinos, o null si el vértice no existe.
     */
    public DoubleList<T> getNeighbors(T vertex) {
        if (vertex == null || !adjList.containsKey(vertex)) {
            return new DoubleList<>();
        }
        return adjList.get(vertex);
    }

    /**
     * Verifica si un vértice existe en el grafo.
     * @param vertex el vértice.
     * @return true si el vértice existe, false en caso contrario.
     */
    public boolean hasVertex(T vertex) {
        return adjList.containsKey(vertex);
    }

    /**
     * Verifica si existe una arista entre dos vértices.
     * @param vertex1 primer vértice.
     * @param vertex2 segundo vértice.
     * @return true si la arista existe, false en caso contrario.
     */
    public boolean hasEdge(T vertex1, T vertex2) {
        return adjList.containsKey(vertex1) && adjList.get(vertex1) != null && adjList.get(vertex1).contains(vertex2);
    }

    /**
     * Obtiene un conjunto de todos los vértices en el grafo.
     * @return un Set con todos los vértices.
     */
    public Set<T> getVertices() {
        return adjList.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T vertex : adjList.keySet()) {
            sb.append(vertex.toString()).append(": ");
            sb.append(adjList.get(vertex).toString()).append("\n");
        }
        return sb.toString();
    }
} 