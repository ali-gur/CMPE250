import java.util.EmptyStackException;

public class Stack<E> {
    public static class StackNode<E> {
        E element;
        StackNode<E> next;
        public StackNode(E element) {
            this.element = element;
            this.next = null;
        }
    }

    StackNode<E> top;

    public Stack() {
        this.top = null;
    }

    public void push(E element) {
        StackNode<E> newNode = new StackNode<>(element);
        newNode.next = this.top;
        this.top = newNode;
    }

    public E pop() {
        if (this.top == null) {
            throw new EmptyStackException();
        }
        E element = this.top.element;
        this.top = this.top.next;
        return element;
    }

    public boolean isEmpty() {
        return this.top == null;
    }

}
