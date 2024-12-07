package gitlet;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Li
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */

    /** Turn the time to standard formation. */
    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    /** Creates a new Gitlet version-control system in the current directory. */
    public static void init() throws IOException {
        /* Make objects directory. */
        if (!GITLET_DIR.mkdirs()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        Tree.BLOB_DIR.mkdirs();
        Tree.COMMIT_DIR.mkdirs();
        /* Make initial commit. */
        Commit ini = new Commit("initial commit", new Date(0), null);
        ini.branchName = "master";
        String ini_id = ini.generateId();
        File ini_file = join(Tree.COMMIT_DIR, ini_id + ".txt");
        ini_file.createNewFile();
        writeObject(ini_file, ini);
        /* Make master head. */
        File master = join(Tree.REFS_DIR, "master");
        master.mkdirs();
        File master_file = join(master, "1.txt");
        master_file.createNewFile();
        writeContents(master_file, ini_id);
        /* Make HEAD directory which point to current commit and point it to initial commit. */
        File head = Tree.HEAD_DIR;
        head.mkdirs();
        File h = join(head, "1.txt");
        h.createNewFile();
        writeContents(h, ini_id);
        /* Make stage directory. */
        Tree.STAGE_DIR.mkdirs();
        /* Create stage object and store in stage directory. */
        Stage s = new Stage();
        File stage_file = join(Tree.STAGE_DIR, "1.txt");
        stage_file.createNewFile();
        writeObject(stage_file, s);
    }

    /** Get the commit object according to id. */
    private static Commit getCommit(String id) {
        File f = join(Tree.COMMIT_DIR, id + ".txt");
        Commit c = readObject(f, Commit.class);
        return c;
    }

    /** Get the current commit. */
    private static Commit getCurrentCommit() {
        File f = join(Tree.HEAD_DIR, "1.txt");
        String id = readContentsAsString(f);
        return getCommit(id);
    }

    /** Get the stage object. */
    private static Stage getStage() {
        File f = join(Tree.STAGE_DIR, "1.txt");
        return readObject(f, Stage.class);
    }

    /** Write a blob into object. */
    private static void writeBlob(Blob b) {
        File f = join(Tree.BLOB_DIR, b.blobId + ".txt");
        writeObject(f, b);
    }

    /** Add a blob to addstage. */
    private static void addToStage(Stage s, Blob b) {
        if (s.addNameToBlobId.containsKey(b.filename)) {
            if (s.addNameToBlobId.get(b.filename).equals(b.blobId)) {
//                            System.out.println("addToStage:same file");
            } else {
//                            System.out.println("addToStage:override file");
                s.addNameToBlobId.put(b.filename, b.blobId);
            }
        } else {
//                        System.out.println("addToStage:no file and add it");
            s.addNameToBlobId.put(b.filename, b.blobId);
        }
    }

    /** Write back the new stage*/
    private static void writeStage(Stage s) {
        File f = join(Tree.STAGE_DIR, "1.txt");
        writeObject(f, s);
    }

    /** Adds a copy of the file as it currently exists to the staging area. */
    public static void add(String addFileName) {
        Commit c = getCurrentCommit();
        Stage s = getStage();
        File f = join(Tree.CWD, addFileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        /* Compare to current Commit. */
        Blob addFile = new Blob(addFileName, readContentsAsString(f));
        writeBlob(addFile);
        /* whether current commit contain the file. */
        if (c.nameToBlobId.containsKey(addFile.filename)) {
            if (c.nameToBlobId.get(addFile.filename).equals(addFile.blobId)) {
                /* if equals, check stage and delete if it exist. */
                if (s.addNameToBlobId.containsKey(addFile.filename)) {
                    s.addNameToBlobId.remove(addFile.filename);
                }
            } else {
                /* add it to add stage. */
                addToStage(s, addFile);
            }
        } else {
        /* not exist in commit, add it to stage. */
            addToStage(s, addFile);
        }
        writeStage(s);
    }

    /** Saves a snapshot of tracked files in the current commit and staging area so they
     *  can be restored at a later time, creating a new commit.*/
    public static void commit(String message) {
        /* Clone parent commit and take the stage. */
        if (Objects.equals(message, "")) {
            System.out.println("Please enter a commit message.");
            return;
        }
//                    System.out.println("Last Commit:");
        Commit parent = getCurrentCommit();
//                    parent.print();
        Commit newCommit = Commit.cloneACommit(parent, message);
//                    System.out.println();
//                    System.out.println("new clone commit:");
//                    newCommit.print();
        Stage s = getStage();
        if (s.addNameToBlobId.isEmpty() && s.removeName.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        /* Process the add stage. */
        for (String fileName : s.addNameToBlobId.keySet()) {
            String fileId = s.addNameToBlobId.get(fileName);
            newCommit.nameToBlobId.put(fileName, fileId);
        }
        /* process the rm stage. */
        for (String fileName : s.removeName) {
            newCommit.nameToBlobId.remove(fileName);
        }
        /* Clear the staging area. */
        s.addNameToBlobId.clear();
        s.removeName.clear();
        writeStage(s);
        /* Update the commit tree, HEAD, branch head. */
        File head = join(Tree.HEAD_DIR, "1.txt");
        writeContents(head, newCommit.generateId());
        File branchHead = join(Tree.REFS_DIR, newCommit.branchName, "1.txt");
        writeContents(branchHead, newCommit.generateId());
        /* Write commit into objects. */
        File com = join(Tree.COMMIT_DIR, newCommit.generateId() + ".txt");
        writeObject(com, newCommit);
//                    System.out.println();
//                    System.out.println("newCommit:");
//                    newCommit.print();
    }

    /** Unstage the file if it is currently staged for addition. If the file is tracked
     * in the current commit, stage it for removal and remove the file from the working
     * directory if the user has not already done so (do not remove it unless it is tracked
     * in the current commit).*/
    public static void rm(String fileName) {
        Stage s = getStage();
        Commit c = getCurrentCommit();
        /* If the file is neither staged nor tracked by the head commit,
        print the error message No reason to remove the file. */
        if (!s.addNameToBlobId.containsKey(fileName) && !c.nameToBlobId.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
        }
        if (s.addNameToBlobId.containsKey(fileName)) {
            s.addNameToBlobId.remove(fileName);
        }
        if (c.nameToBlobId.containsKey(fileName)) {
            s.removeName.add(fileName);
        }
        writeStage(s);
        File f = join(Tree.CWD, fileName);
        if (f.exists()) {
            f.delete();
        }
    }

    /** Return a commit which is the first parent of the given commit,
     * if did not have parent, return a commit with message "end". */
    private static Commit getFirstParentCommit(Commit c) {
        if (c.parents.isEmpty()) {
            return new Commit("end",null, null);
        }
        String id = c.parents.get(0);
        File f = join(Tree.COMMIT_DIR, id + ".txt");
        return readObject(f, Commit.class);
    }

    /** Starting at the current head commit, display information about each commit
     *  backwards along the commit tree until the initial commit. */
    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (!currentCommit.message.equals("end")) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.generateId());
            if (currentCommit.parents.size() > 1) {
                System.out.print("Merge: ");
                for (String parent : currentCommit.parents) {
                    String s = parent.substring(0, 7);
                    System.out.print(s + " ");
                }
                System.out.println();
            }
            System.out.println("Date: " + dateToTimeStamp(currentCommit.time));
            System.out.println(currentCommit.message);
            System.out.println();
            currentCommit = getFirstParentCommit(currentCommit);
        }
    }
}
