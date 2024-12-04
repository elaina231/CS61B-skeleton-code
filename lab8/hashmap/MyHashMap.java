package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Li
 */
public class MyHashMap<K, V> implements Map61B<K, V>, Iterable<K> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;
    private int bucketSize;
    private double maxLoad;
    private HashSet<K> keySet;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this.buckets = createTable(16);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        this.bucketSize = 16;
        this.maxLoad = 0.75;
        this.size = 0;
        this.keySet = new HashSet<>();
    }

    public MyHashMap(int initialSize) {
        this.buckets = createTable(initialSize);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        this.bucketSize = initialSize;
        this.maxLoad = 0.75;
        this.size = 0;
        this.keySet = new HashSet<>();
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.buckets = createTable(initialSize);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        this.bucketSize = initialSize;
        this.maxLoad = maxLoad;
        this.size = 0;
        this.keySet = new HashSet<>();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!


    @Override
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        size = 0;
        keySet.clear();
    }

    @Override
    public boolean containsKey(K key) {
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    return true;
                }
            }
        }
        return false;
//        for (K k : this) {
//            if (k.equals(key)) {
//                return true;
//            }
//        }
//        return false;
    }

    @Override
    public V get(K key) {
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        Node n = createNode(key, value);
        int num = Math.floorMod(key.hashCode(), bucketSize);
        if (containsKey(key)) {
            for (Node node : buckets[num]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        } else {
            buckets[num].add(n);
            size++;
            keySet.add(key);
            double load = (1.0 * size) / bucketSize;
            if ( load > maxLoad) {
                resize();
            }
        }
    }

    private void resize() {
        bucketSize *= 2;
        Collection<Node>[] newBuckets = createTable(bucketSize);
        for (int i = 0; i < newBuckets.length; i++) {
            newBuckets[i] = createBucket();
        }
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                int num = Math.floorMod(node.key.hashCode(), bucketSize);
                newBuckets[num].add(node);
            }
        }
        buckets = newBuckets;
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                if (node.key.equals(key)) {
                    V value = node.value;
                    bucket.remove(node);
                    size--;
                    keySet.remove(key);
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                if (node.key.equals(key) && node.value.equals(value)) {
                    bucket.remove(node);
                    size--;
                    keySet.remove(key);
                    return value;
                }
            }
        }
        return null;
    }

    private class MyIterator implements Iterator<K> {
        Iterator<K> iterator;

        MyIterator() {
            iterator = keySet.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public K next() {
            return iterator().next();
        }
    }

    @Override
    public Iterator<K> iterator() {
        return keySet.iterator();
    }
}
