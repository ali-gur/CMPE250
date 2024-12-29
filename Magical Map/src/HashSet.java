// A simple implementation of HashSet using a HashMap for internal storage
public class HashSet<K> {
    // A dummy value used as a placeholder in the HashMap since HashSet only cares about keys
    private static final Object PRESENT = new Object();

    // Internal HashMap to store the keys of the HashSet
    private HashMap<K, Object> map;

    // Default constructor initializes the internal HashMap
    public HashSet() {
        map = new HashMap<>();
    }

    // Adds a key to the HashSet if it is not already present
    public boolean add(K key) {
        // Check if the key already exists in the map
        if (map.get(key) == null) {
            // Add the key with a dummy value to the map
            map.put(key, PRESENT);
            return true; // Key successfully added
        }
        return false; // Key already exists
    }

    // Checks if a key exists in the HashSet
    public boolean contains(K key) {
        // If the key exists in the map, return true
        return map.get(key) != null;
    }

    // Returns the number of keys in the HashSet
    public int size() {
        return map.size();
    }

    // Checks if the HashSet is empty
    public boolean isEmpty() {
        return map.isEmpty();
    }
}

