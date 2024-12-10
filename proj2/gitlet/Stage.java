package gitlet;

import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {
    /** Key:filename Value:id  The map of file in add stage */
    private Map<String, String> addNameToBlobId;
    /** Key:filename  The set of the file in rm stage */
    private Set<String> removeName;

    /** Construction method. */
    public Stage() {
        addNameToBlobId = new TreeMap<>();
        removeName = new TreeSet<>();
    }

    /** Print out stage for debugging. */
    public void print() {
        System.out.println(addNameToBlobId);
        System.out.println(removeName);
    }

    /** Clear the staging area. */
    public void clear() {
        addNameToBlobId.clear();
        removeName.clear();
    }

    public void addPut(String key, String value) {
        addNameToBlobId.put(key, value);
    }

    public boolean addContains(String key) {
        return addNameToBlobId.containsKey(key);
    }

    public String addGet(String key) {
        return addNameToBlobId.get(key);
    }

    public void addRemove(String key) {
        addNameToBlobId.remove(key);
    }

    public boolean addIsEmpty() {
        return addNameToBlobId.isEmpty();
    }

    public Set<String> addKeySet() {
        return addNameToBlobId.keySet();
    }

    public void addClear() {
        addNameToBlobId.clear();
    }

    public Set<String> getRemoveName() {
        return removeName;
    }
}
