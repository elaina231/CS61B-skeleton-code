package gitlet;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Li
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");


    /**
     * Converts the time to a standard format.
     */
    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    /**
     * Creates a new Gitlet version-control system in the current directory.
     */
    public static void init() throws IOException {
        /* Make objects directory. */
        if (!GITLET_DIR.mkdirs()) {
            System.out.println("A Gitlet version-control system " +
                    "already exists in the current directory.");
            return;
        }
        Tree.BLOB_DIR.mkdirs();
        Tree.COMMIT_DIR.mkdirs();
        /* Make initial commit. */
        Commit ini = new Commit("initial commit", new Date(0), null);
        ini.branchName = "master";
        String iniId = ini.generateId();
        File iniFile = join(Tree.COMMIT_DIR, iniId + ".txt");
        iniFile.createNewFile();
        writeObject(iniFile, ini);
        /* Make master head. */
        File master = join(Tree.REFS_DIR, "master");
        master.mkdirs();
        File masterFile = join(master, "1.txt");
        masterFile.createNewFile();
        writeContents(masterFile, iniId);
        /* Make HEAD directory which point to current commit and point it to initial commit. */
        File head = Tree.HEAD_DIR;
        head.mkdirs();
        File h = join(head, "1.txt");
        h.createNewFile();
        writeContents(h, iniId);
        /* Make stage directory. */
        Tree.STAGE_DIR.mkdirs();
        /* Create stage object and store in stage directory. */
        Stage s = new Stage();
        File stageFile = join(Tree.STAGE_DIR, "1.txt");
        stageFile.createNewFile();
        writeObject(stageFile, s);
    }

    /**
     * Retrieves the commit object associated with the given ID. If no commit
     * with the given id exists, return null.
     */
    private static Commit getCommit(String id) {
        File f = join(Tree.COMMIT_DIR, id + ".txt");
        if (!f.exists()) {
            return null;
        }
        Commit c = readObject(f, Commit.class);
        return c;
    }

    /**
     * Retrieves the current commit.
     */
    private static Commit getCurrentCommit() {
        File f = join(Tree.HEAD_DIR, "1.txt");
        String id = readContentsAsString(f);
        return getCommit(id);
    }

    /**
     * Retrieves the stage object.
     */
    private static Stage getStage() {
        File f = join(Tree.STAGE_DIR, "1.txt");
        return readObject(f, Stage.class);
    }

    /**
     * Writes a blob object to the blob directory.
     */
    private static void writeBlob(Blob b) {
        File f = join(Tree.BLOB_DIR, b.blobId + ".txt");
        writeObject(f, b);
    }

    /**
     * Add a blob to addstage.
     */
    private static void addToStage(Stage s, Blob b) {
        if (s.addNameToBlobId.containsKey(b.filename)) {
            if (!s.addNameToBlobId.get(b.filename).equals(b.blobId)) {
                s.addNameToBlobId.put(b.filename, b.blobId);
            }
        } else {
            s.addNameToBlobId.put(b.filename, b.blobId);
        }
    }

    /**
     * Write back the new stage
     */
    private static void writeStage(Stage s) {
        File f = join(Tree.STAGE_DIR, "1.txt");
        writeObject(f, s);
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     */
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
        s.removeName.remove(addFileName);
        writeStage(s);
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area so they
     * can be restored at a later time, creating a new commit.
     */
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

    /**
     * Unstage the file if it is currently staged for addition. If the file is tracked
     * in the current commit, stage it for removal and remove the file from the working
     * directory if the user has not already done so (do not remove it unless it is tracked
     * in the current commit).
     */
    public static void rm(String fileName) {
        Stage s = getStage();
        Commit c = getCurrentCommit();
        /* If the file is neither staged nor tracked by the head commit,
        print the error message No reason to remove the file. */
        if (!s.addNameToBlobId.containsKey(fileName) && !c.nameToBlobId.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (s.addNameToBlobId.containsKey(fileName)) {
            s.addNameToBlobId.remove(fileName);
        }
        if (c.nameToBlobId.containsKey(fileName)) {
            s.removeName.add(fileName);
            File f = join(Tree.CWD, fileName);
            if (f.exists()) {
                f.delete();
            }
        }
        writeStage(s);
    }

    /**
     * Returns the commit that is the first parent of the given commit.
     * If the commit has no parent, returns a commit with the message "end".
     */
    private static Commit getFirstParentCommit(Commit c) {
        if (c.parents.isEmpty()) {
            return new Commit("end", null, null);
        }
        String id = c.parents.get(0);
        File f = join(Tree.COMMIT_DIR, id + ".txt");
        return readObject(f, Commit.class);
    }

    private static void commitPrint(Commit currentCommit) {
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
    }

    /**
     * Starting at the current head commit, display information about each commit
     * backwards along the commit tree until the initial commit.
     */
    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (!currentCommit.message.equals("end")) {
            commitPrint(currentCommit);
            currentCommit = getFirstParentCommit(currentCommit);
        }
    }

    /**
     * Displays information about all commits ever made.
     */
    public static void globalLog() {
        List<String> l = plainFilenamesIn(Tree.COMMIT_DIR);
        assert l != null;
        for (String filename : l) {
            /* because the filename including ".txt", so forgo the last four character. */
            String id = filename.substring(0, filename.length() - 4);
            commitPrint(getCommit(id));
        }
    }

    /**
     * Prints out the ids of all commits that have the given commit message.
     */
    public static boolean find(String m) {
        boolean isAny = false;
        List<String> l = plainFilenamesIn(Tree.COMMIT_DIR);
        assert l != null;
        for (String filename : l) {
            /* because the filename including ".txt", so forgo the last four character. */
            String id = filename.substring(0, filename.length() - 4);
            Commit c = getCommit(id);
            if (c.message.equals(m)) {
                isAny = true;
                System.out.println(c.generateId());
            }
        }
        return isAny;
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     */
    public static void status() {
        /* Branches. */
        System.out.println("=== Branches ===");
        File[] dir = Tree.REFS_DIR.listFiles();
        Arrays.sort(dir, Comparator.comparing(File::getName));
        String currentBranch = getCurrentCommit().branchName;
        for (File f : dir) {
            if (f.getName().equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(f.getName());
        }
        System.out.println();
        /* Staged Files. */
        System.out.println("=== Staged Files ===");
        Stage s = getStage();
        for (String fileName : s.addNameToBlobId.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        /* Removed Files. */
        System.out.println("=== Removed Files ===");
        for (String fileName : s.removeName) {
            System.out.println(fileName);
        }
        System.out.println();
        /* Modification Not Staged For Commit. */
        System.out.println("=== Modifications Not Staged For Commit ===");
//      TODO
        System.out.println();
        /* Untracked Files. */
        System.out.println("=== Untracked Files ===");
//      TODO
        System.out.println();
    }

    /**
     * Takes the version of the file as it exists in the commit with the given id, and
     * puts it in the working directory, overwriting the version of the file that’s already
     * there if there is one. The new version of the file is not staged.
     */
    public static void checkoutCommit(String fileName, String commitId) {
        Commit c = getCommit(commitId);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (!c.nameToBlobId.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobId = c.nameToBlobId.get(fileName);
        Blob b = readObject(join(Tree.BLOB_DIR, blobId + ".txt"), Blob.class);
        File f = join(Tree.CWD, fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(f, b.contents);
    }

    /** Takes the version of the file as it exists in the head commit and puts it in the
     *  working directory, overwriting the version of the file that’s already there if there
     *  is one. The new version of the file is not staged.*/
    public static void checkoutCurrentCommit(String fileName) {
        File f = join(Tree.HEAD_DIR, "1.txt");
        String id = readContentsAsString(f);
        checkoutCommit(fileName, id);
    }

}