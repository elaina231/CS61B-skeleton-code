package gitlet;

import java.io.Serializable;

import static gitlet.Utils.*;

/** Represents a file.
 * */
public class Blob implements Serializable {

    /** The contents of this file. */
    private String contents;
    /** The name of this file. */
    private String filename;
    /** The sha1 id of blob. */
    private String blobId;

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

    /** Get the contents of the blob. */
    public String getContents() {
        return contents;
    }

    /** Get the filename of the blob. */
    public String getFilename() {
        return filename;
    }

    /** Get the id of the blob. */
    public String getBlobId() {
        return blobId;
    }
}
