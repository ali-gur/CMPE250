// Generic Pair class to store key-value pairs
class Pair<K, V> {
    // Key of the pair (immutable since it's final)
    private final K key;

    // Value of the pair (immutable since it's final)
    private final V value;

    // Constructor to initialize the key and value
    public Pair(K key, V value) {
        this.key = key;     // Assign the key
        this.value = value; // Assign the value
    }

    // Getter method to retrieve the key
    public K getKey() {
        return key;
    }

    // Getter method to retrieve the value
    public V getValue() {
        return value;
    }

    // Override toString method to provide a readable string representation of the Pair
    @Override
    public String toString() {
        return "(" + key + ", " + value + ")"; // Format as "(key, value)"
    }
}
