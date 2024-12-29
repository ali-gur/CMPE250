public class Queue<E> {
    public static class QueueNode<E> {
        public E element;
        public QueueNode<E> next;
        public QueueNode(E element) {
            this.element = element;
        }
    }

    public QueueNode<E> front;
    public QueueNode<E> rear;
    public int size;

    public Queue() {
        front = null;
        rear = null;
        size = 0;
    }

    public void enqueue(E element) {
        QueueNode<E> newQueueNode = new QueueNode<>(element);
        if (front == null) {
            front = newQueueNode;
            rear = newQueueNode;
        } else {
            rear.next = newQueueNode;
            rear = newQueueNode;
        }
        size++;
    }

    public E dequeue() {
        if (front == null) {
            return null;
        } else {
            E element = front.element;
            front = front.next;
            size--;
            return element;
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
