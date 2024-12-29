import java.util.ArrayList;
import static java.util.Objects.hash;

public class HashMap<K, V> {

    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private int capacity;
    private float loadFactor;
    private int size;
    private Entry<K, V>[] table;

    public HashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int capacity, float loadFactor) {
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        this.size = 0;
        this.table = new Entry[capacity];
    }

    public boolean containsKey(K key) {
        int index = getIndex(key);
        Entry<K, V> head = table[index];

        while (head != null) {
            if (head.key == null || head.key.equals(key)) {
                return true;
            }
            head = head.next;
        }

        return false;
    }

    private int getIndex(K key) {
        return Math.abs(hash(key)) % capacity;
    }

    public void put(K key, V value) {
        int index = getIndex(key);
        Entry<K, V> entry = new Entry<>(key, value);
        Entry<K, V> head = table[index];

        if (head == null) {
            table[index] = entry;
            size++;
        } else {
            Entry<K, V> prev = null;
            while (head != null) {
                if (head.key == null || head.key.equals(key)) {
                    head.value = value; // Update existing key
                    return;
                }
                prev = head;
                head = head.next;
            }
            prev.next = entry; // Add new key-value pair
            size++;
        }

        if (size > capacity * loadFactor) {
            resize();
        }
    }

    public V get(K key) {
        int index = getIndex(key);
        Entry<K, V> head = table[index];

        while (head != null) {
            if (head.key == null || head.key.equals(key)) {
                return head.value;
            }
            head = head.next;
        }

        return null;
    }

    public V remove(K key) {
        int index = getIndex(key);
        Entry<K, V> head = table[index];
        Entry<K, V> prev = null;

        while (head != null) {
            if (head.key == null || head.key.equals(key)) {
                if (prev == null) {
                    table[index] = head.next; // Remove head of bucket
                } else {
                    prev.next = head.next; // Remove middle or last node
                }
                size--;
                return head.value;
            }
            prev = head;
            head = head.next;
        }

        return null;
    }

    private void resize() {
        capacity *= 2;
        Entry<K, V>[] newTable = new Entry[capacity];
        for (Entry<K, V> entry : table) {
            while (entry != null) {
                Entry<K, V> next = entry.next;
                int index = getIndex(entry.key);
                entry.next = newTable[index];
                newTable[index] = entry;
                entry = next;
            }
        }
        table = newTable;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public ArrayList<K> getKeys() {
        ArrayList<K> keys = new ArrayList<>();
        for (Entry<K, V> bucket : table) {
            Entry<K, V> current = bucket;
            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }
        return keys;
    }
}

