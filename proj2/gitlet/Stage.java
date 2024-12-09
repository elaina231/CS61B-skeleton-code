package gitlet;

import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {
    /** Key:filename Value:id  The map of file in add stage */
    public Map<String, String> addNameToBlobId;
    /** Key:filename  The set of the file in rm stage */
    public Set<String> removeName;

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
}
