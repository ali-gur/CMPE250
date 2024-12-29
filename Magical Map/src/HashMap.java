import java.util.ArrayList;

import static java.util.Objects.hash;

public class HashMap<K, V> {

    // A private static inner class to represent an entry in a hash map or linked list
    private static class Entry<K, V> {
        // Key of the entry
        K key;

        // Value associated with the key
        V value;

        // Reference to the next entry in case of collisions (used in chaining)
        Entry<K, V> next;

        // Constructor to initialize the key-value pair and set next to null
        public Entry(K key, V value) {
            this.key = key;      // Initialize the key
            this.value = value;  // Initialize the value
            this.next = null;    // Initialize next as null (no chaining yet)
        }
    }

    // Default initial capacity for the hash table (number of buckets)
    private static final int DEFAULT_CAPACITY = 16;

    // Default load factor for resizing the hash table
    // Load factor determines when to resize the table (e.g., 0.75 means resize when 75% full)
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // Current capacity of the hash table (number of buckets)
    private int capacity;

    // Load factor threshold for resizing the table
    private float loadFactor;

    // Current number of key-value pairs (entries) stored in the table
    private int size;

    // Array of Entry<K, V> representing the hash table buckets
    // Each index holds a linked list of entries to handle hash collisions
    private Entry<K, V>[] table;


    // Default constructor for the HashMap
    // Initializes the hash table with default capacity and load factor
    public HashMap() {
        // Call the parameterized constructor with DEFAULT_CAPACITY and DEFAULT_LOAD_FACTOR
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }


    // Parameterized constructor for the HashMap
    // Allows specifying initial capacity and load factor for the hash table
    public HashMap(int capacity, float loadFactor) {
        // Set the initial capacity of the hash table
        this.capacity = capacity;

        // Set the load factor threshold for resizing the table
        this.loadFactor = loadFactor;

        // Initialize the size of the HashMap to 0 (no entries added yet)
        this.size = 0;

        // Initialize the table (array of Entry<K, V>) with the specified capacity
        this.table = new Entry[capacity];
    }


    // Method to check if a specific key exists in the HashMap
    public boolean containsKey(K key) {
        // Calculate the index of the bucket for the given key using a hashing function
        int index = getIndex(key);

        // Retrieve the head of the linked list at the calculated index
        Entry<K, V> head = table[index];

        // Traverse the linked list at this index to search for the key
        while (head != null) {
            // If the key in the current entry is null or matches the provided key, return true
            if (head.key == null || head.key.equals(key)) {
                return true; // Key exists in the map
            }
            // Move to the next entry in the linked list
            head = head.next;
        }

        // Return false if the key was not found after traversing the list
        return false;
    }


    // Method to calculate the index (bucket) for a given key
    private int getIndex(K key) {
        // Compute the hash code for the given key and ensure it's positive using Math.abs
        // Take the modulus with 'capacity' to map the hash code to a valid index within the table size
        return Math.abs(hash(key)) % capacity;
    }


    // Method to add or update a key-value pair in the HashMap
    public void put(K key, V value) {
        // Calculate the index of the bucket where the key-value pair will be stored
        int index = getIndex(key);

        // Create a new entry for the key-value pair
        Entry<K, V> entry = new Entry<>(key, value);

        // Retrieve the head of the linked list at the calculated index
        Entry<K, V> head = table[index];

        // If the bucket is empty, insert the new entry and increment the size
        if (head == null) {
            table[index] = entry;
            size++;
        } else {
            // Traverse the linked list to check for existing keys or add a new entry
            Entry<K, V> prev = null;
            while (head != null) {
                // If the key already exists, update its value
                if (head.key == null || head.key.equals(key)) {
                    head.value = value; // Update the value for the existing key
                    return; // Exit the method
                }
                prev = head;   // Move the 'prev' pointer to the current node
                head = head.next; // Move to the next node in the list
            }

            // Add the new entry to the end of the linked list
            prev.next = entry;
            size++; // Increment the size of the HashMap
        }

        // Check if the size exceeds the load factor threshold, and resize if necessary
        if (size > capacity * loadFactor) {
            resize();
        }
    }


    // Method to retrieve the value associated with a given key in the HashMap
    public V get(K key) {
        // Calculate the index of the bucket where the key might be stored
        int index = getIndex(key);

        // Retrieve the head of the linked list at the calculated index
        Entry<K, V> head = table[index];

        // Traverse the linked list to search for the key
        while (head != null) {
            // Check if the current entry's key matches the given key
            if (head.key == null || head.key.equals(key)) {
                return head.value; // Return the value if the key matches
            }
            head = head.next; // Move to the next entry in the linked list
        }

        // Return null if the key is not found in the table
        return null;
    }


    // Method to remove a key-value pair from the HashMap and return the associated value
    public V remove(K key) {
        // Calculate the index of the bucket where the key might be stored
        int index = getIndex(key);

        // Retrieve the head of the linked list at the calculated index
        Entry<K, V> head = table[index];
        Entry<K, V> prev = null; // To keep track of the previous node during traversal

        // Traverse the linked list to find the key
        while (head != null) {
            // Check if the current entry's key matches the given key
            if (head.key == null || head.key.equals(key)) {
                // If the key is found, remove the current node from the linked list
                if (prev == null) {
                    // Case 1: The node to be removed is the head of the bucket
                    table[index] = head.next;
                } else {
                    // Case 2: The node to be removed is in the middle or at the end
                    prev.next = head.next;
                }

                // Decrement the size of the HashMap
                size--;

                // Return the value associated with the removed key
                return head.value;
            }

            // Move to the next node in the linked list
            prev = head;
            head = head.next;
        }

        // Return null if the key was not found in the HashMap
        return null;
    }


    // Method to resize (expand) the hash table when the load factor threshold is exceeded
    private void resize() {
        // Double the capacity of the hash table
        capacity *= 2;

        // Create a new table (array) with the updated capacity
        Entry<K, V>[] newTable = new Entry[capacity];

        // Rehash all existing entries and move them to the new table
        for (Entry<K, V> entry : table) { // Iterate over all buckets in the current table
            while (entry != null) { // Traverse the linked list in each bucket
                Entry<K, V> next = entry.next; // Store the next entry before rehashing

                // Recalculate the new bucket index for the current entry's key
                int index = getIndex(entry.key);

                // Insert the current entry into the new table
                entry.next = newTable[index]; // Point to the existing head of the new bucket
                newTable[index] = entry; // Set the current entry as the new head of the bucket

                // Move to the next entry in the current bucket
                entry = next;
            }
        }

        // Replace the old table with the new resized table
        table = newTable;
    }


    // Method to return the current number of key-value pairs in the HashMap
    public int size() {
        return size; // Return the value of the 'size' variable
    }


    // Method to check if the HashMap is empty (contains no key-value pairs)
    public boolean isEmpty() {
        // Return true if the size is 0, otherwise return false
        return size == 0;
    }


    // Method to retrieve all keys stored in the HashMap
    public ArrayList<K> getKeys() {
        // Create an ArrayList to store the keys
        ArrayList<K> keys = new ArrayList<>();

        // Iterate through each bucket in the hash table
        for (Entry<K, V> bucket : table) {
            Entry<K, V> current = bucket; // Start at the head of the linked list in the bucket

            // Traverse the linked list in the current bucket
            while (current != null) {
                // Add the current key to the list of keys
                keys.add(current.key);

                // Move to the next entry in the linked list
                current = current.next;
            }
        }

        // Return the list of keys
        return keys;
    }

    // Method to get the value associated with a key, or return a default value if the key is not found
    public V getOrDefault(K key, V defaultValue) {
        // Use the 'get' method to attempt retrieving the value for the given key
        V value = get(key);

        // If the value is null (key not found), return the provided default value
        return value == null ? defaultValue : value;
    }
}

