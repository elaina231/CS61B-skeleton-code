package gitlet;

import java.io.File;

import static gitlet.Utils.join;

/** Directory structures mapping names to references to blobs and other trees (subdirectories). */
public class Tree {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The object directory. */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    /** The directory of Blob. */
    public static final File BLOB_DIR = join(OBJECT_DIR, "blobs");
    /** The directory of Commit. */
    public static final File COMMIT_DIR = join(OBJECT_DIR, "commits");
    /** The directory of refs to branch head. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The directory of HEAD which point to current commit. */
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");
    /** The directory of stage. */
    public static final File STAGE_DIR = join(GITLET_DIR, "stage");

}
