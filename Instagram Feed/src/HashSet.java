import java.util.ArrayList;

public class HashSet<K> {
    private static final Object PRESENT = new Object(); // Dummy value
    private HashMap<K, Object> map;

    public HashSet() {
        map = new HashMap<>();
    }

    public HashSet(int capacity, float loadFactor) {
        map = new HashMap<>(capacity, loadFactor);
    }

    public boolean add(K key) {
        if (map.get(key) == null) {
            map.put(key, PRESENT);
            return true;
        }
        return false;
    }

    public boolean contains(K key) {
        return map.get(key) != null;
    }

    public boolean remove(K key) {
        return map.remove(key) != null;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public ArrayList<K> getKeys() {
        return map.getKeys();
    }
}

