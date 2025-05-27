package org.example.structures.listasSimples;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SimpleList<T> implements Iterable<T>{
    private SimpleNode<T> firstNode;
    private SimpleNode<T> lastNode;
    private int size;

    public SimpleList() {
        this.size = 0;
        this.firstNode = null;
        this.lastNode = null;
    }

    public SimpleNode<T> getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(SimpleNode<T> firstNode) {
        this.firstNode = firstNode;
    }

    public SimpleNode<T> getLastNode() {
        return lastNode;
    }

    public void setLastNode(SimpleNode<T> lastNode) {
        this.lastNode = lastNode;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public void addEnd(T value){
        SimpleNode<T> node = new SimpleNode<T>(value);
        if(isEmpty()){
            firstNode =  node;
        }else{
            lastNode.setNextNode(node);

        }
        lastNode = node;
        size++;
    }
    public void addFirst (T value){
        SimpleNode<T> node = new SimpleNode<T>(value);
        if(isEmpty()){
            firstNode = node;
            lastNode = node;
        }
        else{
            node.setNextNode(firstNode);
            firstNode = node;
        }
        size++;
    }
    public void imprimirLista(){
        if(isEmpty()){
            System.out.println("Lista Vacia\n");
        }else{
            SimpleNode<T> aux = firstNode;
            while(aux != null){
                System.out.printf(aux.getValue() + " ");
                aux = aux.getNextNode();
            }
        }
    }
    public void imprimirLista2(){
        if(isEmpty()){
            System.out.println("Lista Vacia\n");
        }
        else{
            for (SimpleNode<T> aux = firstNode; aux != null; aux = aux.getNextNode()) {
                System.out.printf(aux.getValue() + " ");
            }
        }
    }
    public void imprimirListaPosicionesPares (){
        if(isEmpty()){
            System.out.println("Lista Vacia\n");
        }else {
            int i = 0;
            for (SimpleNode<T> aux = firstNode; aux != null; aux = aux.getNextNode()) {
                if(i % 2 == 0) {
                    System.out.println(aux.getValue() + " ");
                }
                i++;
            }
        }
    }
    public void printRecursiveList(){
        printRecursiveList(firstNode);
    }
    private void printRecursiveList(SimpleNode<T> aux) {
        if(aux == null){
            System.out.println("Termine");
        }
        else{
            System.out.println(aux.getValue() + " ");
            printRecursiveList(aux.getNextNode());
        }
    }
    public void deleteFirst(){
        if(isEmpty()){
            throw new RuntimeException("Lista Vacia");
        }else{
            firstNode = firstNode.getNextNode();
        }
        size--;
    }
    public void deleteEnd(){
        if(isEmpty()){
            throw new RuntimeException("Lista Vacia");
        }
        if(size == 1){
            deleteAll();
        }else {
            SimpleNode<T> aux = firstNode;
            while(aux.getNextNode() != lastNode){
                aux = aux.getNextNode();
            }
            lastNode = aux;
            aux.setNextNode(null);
        }
        size--;
    }
    public void deleteAll(){
        firstNode = null;
        lastNode = null;
        size = 0;
    }
    public SimpleList<T> posicionesImpares(){
        SimpleList<T> auxImpares = new SimpleList<>();
        if(isEmpty()){
             throw new RuntimeException("Lista Vacia");
         }else{
             int i = 0;
             for (SimpleNode<T> aux = firstNode; aux != null; aux = aux.getNextNode()) {
                 if(i % 2 != 0) {
                     auxImpares.addEnd(aux.getValue());
                 }
                 i++;
             }
         }
        return auxImpares;
    }
    public SimpleList<T> parValue(){
        SimpleList<T> auxPar = new SimpleList<>();
        if(isEmpty()){
            throw new RuntimeException("Lista Vacia");
        }else{
            for (SimpleNode<T> aux = firstNode; aux != null; aux = aux.getNextNode()) {
                if(isPar(aux.getValue())) {
                    auxPar.addEnd(aux.getValue());
                }
            }
        }
        return auxPar;
    }
    public Integer repeatedValue(T comparar){
        if(isEmpty()){
            throw new RuntimeException("Lista Vacia");
        }else{
            Integer i = 0;
            for (SimpleNode<T> aux = firstNode; aux != null; aux = aux.getNextNode()) {
                if(aux.getValue().equals(comparar)) {
                    i ++;
                }
            }
            return i;
        }
    }
    public void deletePar(){
        if(isEmpty()){
            throw new RuntimeException("Lista Vacia");
        }else{
            while (firstNode != null && isPar(firstNode.getNextNode().getValue())){
                firstNode = firstNode.getNextNode();
            }
            SimpleNode<T> actual = firstNode;
            while(actual.getNextNode() != null){
                if(isPar(actual.getNextNode().getValue())){
                    actual.setNextNode(actual.getNextNode().getNextNode());
                    size--;
                }else {
                    actual = actual.getNextNode();
                }
            }
        }
    }

    private boolean isPar(T dato) {
        if (dato instanceof Integer) {
            return ((Integer) dato) % 2 == 0;
        }
        return false;
    }


    @Override
    public Iterator<T> iterator() {
        return new IteradorLista();
    }

    public SimpleList<T> joinList(SimpleList<T> lista1, SimpleList<T> lista2) {
        SimpleList<T> listaUnida = new SimpleList<>();
        if(lista1.isEmpty() && lista2.isEmpty()){
            System.out.println("Listas Vacia\n");
        }else {
            for (SimpleNode<T> aux = lista1.firstNode; aux != null; aux = aux.getNextNode()) {
                listaUnida.addEnd(aux.getValue());
            }
            for (SimpleNode<T> aux = lista2.firstNode; aux != null; aux = aux.getNextNode()){
                listaUnida.addEnd(aux.getValue());
            }
        }
        return listaUnida;
    }

    private class IteradorLista implements Iterator<T> {
        private SimpleNode<T> actual = firstNode;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T valor = actual.getValue();
            actual = actual.getNextNode();
            return valor;
        }
    }
}
