package org.example.structures.listasSimples;

public class SimpleNode<T>{
    private T value;
    private SimpleNode<T> nextNode;

    public SimpleNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public SimpleNode<T> getNextNode() {
        return nextNode;
    }

    public void setNextNode(SimpleNode<T> nextNode) {
        this.nextNode = nextNode;
    }

}
