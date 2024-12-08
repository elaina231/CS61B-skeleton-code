package gitlet;

import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {
    /** The id of the file in add stage. */
    public Map<String, String> addNameToBlobId;
    /** The id of the file in remove stage. */
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
