import java.util.ArrayList;
import java.util.Comparator;

// Generic implementation of a Heap data structure
public class Heap<T> {
    // Internal ArrayList to store heap elements
    private final ArrayList<T> heap;

    // Comparator to determine the order of elements (min-heap or max-heap)
    private final Comparator<T> comparator;

    // Constructor to initialize the heap with a comparator
    public Heap(Comparator<T> comparator) {
        this.heap = new ArrayList<>(); // Initialize an empty list to store heap elements
        this.comparator = comparator;  // Store the comparator for element comparison
    }

    // Adds a new element to the heap and restores heap property
    public void add(T element) {
        heap.add(element);             // Add the element to the end of the list
        heapifyUp(heap.size() - 1);    // Restore the heap property by "bubbling up"
    }

    // Removes and returns the root element (smallest/largest based on comparator)
    public T poll() {
        if (heap.isEmpty()) {          // If the heap is empty, return null
            return null;
        }

        T root = heap.get(0);          // Save the root element
        T lastElement = heap.remove(heap.size() - 1); // Remove the last element

        if (!heap.isEmpty()) {         // If the heap still has elements
            heap.set(0, lastElement);  // Move the last element to the root
            heapifyDown(0);            // Restore the heap property by "bubbling down"
        }

        return root;                   // Return the original root
    }

    // Checks if the heap is empty
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    // Restores the heap property by moving an element up the tree
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2; // Calculate the parent index

            // Compare the current node with its parent
            if (comparator.compare(heap.get(index), heap.get(parentIndex)) >= 0) {
                break; // Stop if the current node is in the correct position
            }

            swap(index, parentIndex); // Swap with the parent
            index = parentIndex;      // Move to the parent's index
        }
    }

    // Restores the heap property by moving an element down the tree
    private void heapifyDown(int index) {
        int size = heap.size(); // Get the size of the heap

        while (index < size) {
            int leftChildIndex = 2 * index + 1;  // Index of the left child
            int rightChildIndex = 2 * index + 2; // Index of the right child
            int smallest = index;               // Assume the current node is the smallest

            // Check if the left child is smaller than the current node
            if (leftChildIndex < size && comparator.compare(heap.get(leftChildIndex), heap.get(smallest)) < 0) {
                smallest = leftChildIndex;
            }

            // Check if the right child is smaller than the smallest node so far
            if (rightChildIndex < size && comparator.compare(heap.get(rightChildIndex), heap.get(smallest)) < 0) {
                smallest = rightChildIndex;
            }

            if (smallest == index) {
                break; // Stop if the current node is already in the correct position
            }

            swap(index, smallest); // Swap the current node with the smallest child
            index = smallest;      // Move to the position of the smallest child
        }
    }

    // Swaps two elements in the heap
    private void swap(int i, int j) {
        T temp = heap.get(i);     // Temporary storage for the element at index i
        heap.set(i, heap.get(j)); // Replace element at index i with the one at index j
        heap.set(j, temp);        // Replace element at index j with the temporary value
    }
}
