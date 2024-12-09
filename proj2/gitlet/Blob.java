package gitlet;

import java.io.Serializable;

import static gitlet.Utils.*;

/** Represents a file.
 * */
public class Blob implements Serializable {

    /** The contents of this file. */
    public String contents;
    /** The name of this file. */
    public String filename;
    /** The sha1 id of blob. */
    public String blobId;

    /** Construction method. */
    public Blob(String filename, String contents) {
        this.filename = filename;
        this.contents = contents;
        this.blobId = sha1(contents, filename);
    }

    /** Print for debugging. */
    public void print() {
        System.out.println(filename);
        System.out.println(blobId);
    }
}
