package bstmap;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private class Node {
        K key;
        V val;
        Node left, right;

        Node(K key, V val) {
            this.key = key;
            this.val = val;
            left = right = null;
        }
    }

    private Node root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Node n = root;
        while (n != null) {
            if (key.compareTo(n.key) == 0) {
                return true;
            } else if (key.compareTo(n.key) < 0) {
                n = n.left;
            } else if (key.compareTo(n.key) > 0) {
                n = n.right;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        if (!containsKey(key)) {
            return null;
        }
        Node n = root;
        while (true) {
            if (key.compareTo(n.key) == 0) {
                return n.val;
            } else if (key.compareTo(n.key) < 0) {
                n = n.left;
            } else if (key.compareTo(n.key) > 0) {
                n = n.right;
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (containsKey(key)) {
            return;
        }
        root = insert(root, key, value);
        size++;
    }

    private Node insert(Node x, K key, V value) {
        if (x == null) {
            return new Node(key, value);
        }
        if (key.compareTo(x.key) < 0) {
            x.left = insert(x.left, key, value);
        }
        if (key.compareTo(x.key) > 0) {
            x.right = insert(x.right, key, value);
        }
        return x;
    }

    public void printInOrder() {
        System.out.print("{");
        traverseAndPrintInOrder(root);
        System.out.print("}");
        System.out.println();
    }

    private void traverseAndPrintInOrder(Node x) {
        if (x == null) {
            return;
        }
        traverseInOrder(x.left);
        System.out.print("[" + x.key + ":" + x.val + "]");
        traverseInOrder(x.right);
    }

    @Override
    public Set<K> keySet() {
//        throw new UnsupportedOperationException();
        Set<K> s = new HashSet<K>();
        traverseInOrder(root, s);
        return s;
    }

    private void traverseInOrder(Node x, Set<K> s) {
        if (x == null) {
            return;
        }
        traverseInOrder(x.left, s);
        s.add(x.key);
        traverseInOrder(x.right, s);
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

}
