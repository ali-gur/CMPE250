import java.util.ArrayList;

public class Heap {
    public ArrayList<Post> elements;
    public int size;

    public Heap(ArrayList<Post> elements) {
        this.elements = elements;
        this.size = elements.size();
        buildHeap();
    }

    public void buildHeap() {
        for (int i = size - 1; i >= 0; i--) {
            percolateDown(i);
        }
    }

    public void insert(Post post) {
        elements.add(post);
        size++;
        percolateUp(size - 1);
    }

    public Post remove() {
        if (size != 0) {
            Post root = elements.get(0);
            elements.set(0, elements.get(size - 1));
            elements.remove(size - 1);
            size--;
            percolateDown(0);
            return root;
        }
        return null;
    }

    public void percolateUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (compare(elements.get(index), elements.get(parent)) <= 0) {
                break;
            }
            Post temp = elements.get(index);
            elements.set(index, elements.get(parent));
            elements.set(parent, temp);
            index = parent;
        }
    }

    public void percolateDown(int index) {
        while (index < size / 2) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;

            if (left < size && compare(elements.get(left), elements.get(largest)) > 0) {
                largest = left;
            }
            if (right < size && compare(elements.get(right), elements.get(largest)) > 0) {
                largest = right;
            }
            if (largest == index) {
                break;
            }
            Post temp = elements.get(index);
            elements.set(index, elements.get(largest));
            elements.set(largest, temp);
            index = largest;
        }
    }

    /**
     * Comparison logic:
     * - Higher likes come first.
     * - If likes are equal, lexicographically larger ID comes first.
     */
    private int compare(Post p1, Post p2) {
        if (p1.getLikes() != p2.getLikes()) {
            return Integer.compare(p1.getLikes(), p2.getLikes());
        }
        return p1.getPostId().compareTo(p2.getPostId());
    }
}
