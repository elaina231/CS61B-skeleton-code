package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Li
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    public String message;
    /** The time when this Commit was commit. */
    public Date time;
    /** The id of the parent of this Commit. */
    public List<String> parents;
    /** The id of the file in this Commit. */
    public Map<String, String> nameToBlobId;
    /** The branch name of this commit. */
    public String branchName;

    /** Construction method. */
    public Commit(String message, Date time, String parent) {
        this.message = message;
        this.time = time;
        this.parents = new ArrayList<>();
        this.nameToBlobId = new TreeMap<>();
        if (parent != null) {
            this.parents.add(parent);
        }
    }

    public static Commit cloneACommit(Commit parent, String message) {
        Commit newCommit = new Commit(message, null, null);
        newCommit.time = new Date();
        newCommit.parents.add(parent.generateId());
        newCommit.nameToBlobId = new TreeMap<>(parent.nameToBlobId);
        newCommit.branchName = parent.branchName;
        return newCommit;
    }

    /** Generate id of this Commit. */
    public String generateId() {
        return sha1(message, time.toString(), parents.toString(), nameToBlobId.toString());
    }

    /** Print out commit for debugging. */
    public void print() {
        System.out.println("message: " + message);
        System.out.println("id: " + generateId());
        System.out.println("time: " + time);
        System.out.println("parents: " + parents);
        System.out.println("nameToBlobId: " + nameToBlobId);
        System.out.println("branchName: " + branchName);
    }
}
