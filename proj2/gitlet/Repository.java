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
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        Tree.BLOB_DIR.mkdirs();
        Tree.COMMIT_DIR.mkdirs();
        /* Make initial commit. */
        Commit ini = new Commit("initial commit", new Date(0), null);
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
        File h = join(head, "master.txt");
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
        if (id.length() == 40) {
            File f = join(Tree.COMMIT_DIR, id + ".txt");
            if (!f.exists()) {
                return null;
            }
            return readObject(f, Commit.class);
        } else {
            File[] files = Tree.COMMIT_DIR.listFiles((dir, name) -> name.startsWith(id));
            if (files == null || files.length == 0) {
                return null;
            } else if (files.length == 1) {
                return readObject(files[0], Commit.class);
            } else {
                System.out.println("There are more than 1 commit start with abbreviation. ");
                return null;
            }
        }
    }

    /**
     * Retrieves the current commit.
     */
    private static Commit getCurrentCommit() {
        File f = join(Tree.HEAD_DIR, getBranchName() + ".txt");
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
        File f = join(Tree.BLOB_DIR, b.getBlobId() + ".txt");
        writeObject(f, b);
    }

    /**
     * Add a blob to addstage.
     */
    private static void addToStage(Stage s, Blob b) {
        if (s.addContains(b.getFilename())) {
            if (!s.addGet(b.getFilename()).equals(b.getBlobId())) {
                s.addPut(b.getFilename(), b.getBlobId());
            }
        } else {
            s.addPut(b.getFilename(), b.getBlobId());
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
        if (c.getNameToBlobId().containsKey(addFile.getFilename())) {
            if (c.getNameToBlobId().get(addFile.getFilename()).equals(addFile.getBlobId())) {
                /* if equals, check stage and delete if it exist. */
                s.addRemove(addFile.getFilename());
            } else {
                /* add it to add stage. */
                addToStage(s, addFile);
            }
        } else {
            /* not exist in commit, add it to stage. */
            addToStage(s, addFile);
        }
        s.getRemoveName().remove(addFileName);
        writeStage(s);
    }

    /** Get the current branch name. */
    private static String getBranchName() {
        File[] files = Tree.HEAD_DIR.listFiles((dir, name) -> name.endsWith(".txt"));
        assert files != null;
        String name = files[0].getName().substring(0, files[0].getName().length() - 4);
        if (files.length == 1) {
            return name;
        } else {
            System.out.println("No txt file found or multiple txt files exist.");
            return null;
        }
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area so they
     * can be restored at a later time, creating a new commit.
     */
    public static void commit(String message, Commit secParent) {
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
        if (secParent != null) {
            newCommit.getParents().add(secParent.generateId());
        }
        Stage s = getStage();
        if (s.addIsEmpty() && s.getRemoveName().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        /* Process the add stage. */
        for (String fileName : s.addKeySet()) {
            String fileId = s.addGet(fileName);
            newCommit.getNameToBlobId().put(fileName, fileId);
        }
        /* process the rm stage. */
        for (String fileName : s.getRemoveName()) {
            newCommit.getNameToBlobId().remove(fileName);
        }
        /* Clear the staging area. */
        s.addClear();
        s.getRemoveName().clear();
        writeStage(s);
        /* Update the commit tree, HEAD, branch head. */
        File head = join(Tree.HEAD_DIR, getBranchName() + ".txt");
        writeContents(head, newCommit.generateId());
        File branchHead = join(Tree.REFS_DIR, getBranchName(), "1.txt");
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
        if (!s.addContains(fileName) && !c.getNameToBlobId().containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        s.addRemove(fileName);
        if (c.getNameToBlobId().containsKey(fileName)) {
            s.getRemoveName().add(fileName);
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
        if (c.getParents().isEmpty()) {
            return new Commit("end", null, null);
        }
        String id = c.getParents().get(0);
        File f = join(Tree.COMMIT_DIR, id + ".txt");
        return readObject(f, Commit.class);
    }

    private static void commitPrint(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + currentCommit.generateId());
        if (currentCommit.getParents().size() > 1) {
            System.out.print("Merge: ");
            for (String parent : currentCommit.getParents()) {
                String s = parent.substring(0, 7);
                System.out.print(s + " ");
            }
            System.out.println();
        }
        System.out.println("Date: " + dateToTimeStamp(currentCommit.getTime()));
        System.out.println(currentCommit.getMessage());
        System.out.println();
    }

    /**
     * Starting at the current head commit, display information about each commit
     * backwards along the commit tree until the initial commit.
     */
    public static void log() {
        Commit currentCommit = getCurrentCommit();
        while (!currentCommit.getMessage().equals("end")) {
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
            if (c.getMessage().equals(m)) {
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
        String currentBranch = getBranchName();
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
        for (String fileName : s.addKeySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        /* Removed Files. */
        System.out.println("=== Removed Files ===");
        for (String fileName : s.getRemoveName()) {
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
        if (!c.getNameToBlobId().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobId = c.getNameToBlobId().get(fileName);
        Blob b = readObject(join(Tree.BLOB_DIR, blobId + ".txt"), Blob.class);
        File f = join(Tree.CWD, fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(f, b.getContents());
    }

    /** Takes the version of the file as it exists in the head commit and puts it in the
     *  working directory, overwriting the version of the file that’s already there if there
     *  is one. The new version of the file is not staged.*/
    public static void checkoutCurrentCommit(String fileName) {
        File f = join(Tree.HEAD_DIR,  getBranchName() + ".txt");
        String id = readContentsAsString(f);
        checkoutCommit(fileName, id);
    }

    /** Takes all files in the given commit , and puts them in the
     * working directory, overwriting the versions of the files that are already there if
     * they exist. And clear staging area.  */
    private static void checkoutAllFileCommit(Commit newCommit) {
        Commit currentCommit = getCurrentCommit();
        List<String> l = plainFilenamesIn(Tree.CWD);
        Stage s = getStage();
        for (String fileName : l) {
            if (!currentCommit.getNameToBlobId().containsKey(fileName)
                    && !s.addContains(fileName)) {
                System.out.println("There is an untracked file in the way"
                        + "; delete it, or add and commit it first.");
                return;
            }
        }
        for (String fileName : l) {
            if (!newCommit.getNameToBlobId().containsKey(fileName)) {
                File f = join(Tree.CWD, fileName);
                f.delete();
            }
        }
        for (String fileName : newCommit.getNameToBlobId().keySet()) {
            File f = join(Tree.CWD, fileName);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            String blobId = newCommit.getNameToBlobId().get(fileName);
            Blob b = readObject(join(Tree.BLOB_DIR, blobId + ".txt"), Blob.class);
            writeContents(f, b.getContents());
        }
        s.clear();
        writeStage(s);
    }

    /** Takes all files in the commit at the head of the given branch, and puts them in the
     * working directory, overwriting the versions of the files that are already there if
     * they exist. */
    public static void checkoutBranch(String branchName) {
        File branch = join(Tree.REFS_DIR, branchName, "1.txt");
        if (!branch.exists()) {
            System.out.println("No such branch exist.");
            return;
        }
        String commitId = readContentsAsString(branch);
        if (getBranchName().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit newCommit = getCommit(commitId);
        checkoutAllFileCommit(newCommit);
        /* Change HEAD to current branch. */
        File head = join(Tree.HEAD_DIR, getBranchName() + ".txt");
        head.delete();
        File newHead = join(Tree.HEAD_DIR, branchName + ".txt");
        writeContents(newHead, newCommit.generateId());
    }

    /** Creates a new branch with the given name, and points it at the current head commit. */
    public static void branch(String branchName) {
        File newBranch = join(Tree.REFS_DIR, branchName, "1.txt");
        newBranch.getParentFile().mkdirs();
        try {
            if (!newBranch.createNewFile()) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Commit c = getCurrentCommit();
        writeContents(newBranch, c.generateId());
    }

    /** Deletes the branch with the given name. */
    public static void rmbranch(String branchName) {
        File branch = join(Tree.REFS_DIR, branchName);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (Objects.equals(getBranchName(), branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File h = join(branch, "1.txt");
        h.delete();
        branch.delete();
    }

    /** Checks out all the files tracked by the given commit. Removes
     *  tracked files that are not present in that commit. Also moves the current
     *  branch’s head to that commit node.*/
    public static void reset(String commitId) {
        Commit c = getCommit(commitId);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        checkoutAllFileCommit(c);
        File head = join(Tree.HEAD_DIR, getBranchName() + ".txt");
        writeContents(head, c.generateId());
        File branchHead = join(Tree.REFS_DIR, getBranchName(), "1.txt");
        writeContents(branchHead, c.generateId());
    }

    /** Return the head commit of a branch. */
    private static Commit getCommitOfBranch(String branchName) {
        File branch = join(Tree.REFS_DIR, branchName, "1.txt");
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return null;
        }
        return getCommit(readContentsAsString(branch));
    }

    /** Find the latest common ancestor of two commit. Return
     * null if does not exist. */
    private static Commit findAncestor(Commit a, Commit b) {
        Set<String> s = new TreeSet<>();
        Queue<Commit> q = new LinkedList<>();
        q.add(a);
        while (!q.isEmpty()) {
            Commit c = q.remove();
            s.add(c.generateId());
            for (String string : c.getParents()) {
                q.add(getCommit(string));
            }
        }
        q.add(b);
        while (!q.isEmpty()) {
            Commit c = q.remove();
            if (s.contains(c.generateId())) {
                return c;
            }
            for (String string : c.getParents()) {
                q.add(getCommit(string));
            }
        }
        return null;
    }

    /** Check if a file have been modified.(both in split and c) */
    private static boolean checkModified(Commit split, Commit c, String fName) {
        Map<String, String> sp = split.getNameToBlobId();
        Map<String, String> cp = c.getNameToBlobId();
        if (sp.containsKey(fName) && cp.containsKey(fName)) {
            return !sp.get(fName).equals(cp.get(fName));
        }
        return false;
    }

    /** Check if a file have been removed.(in split and not in c) */
    private static boolean checkRemoved(Commit split, Commit c, String fName) {
        Map<String, String> sp = split.getNameToBlobId();
        Map<String, String> cp = c.getNameToBlobId();
        return sp.containsKey(fName) && !cp.containsKey(fName);
    }

    /** Check whether present in given commit. */
    private static boolean checkPresent(Commit c, String fName) {
        return c.getNameToBlobId().containsKey(fName);
    }

    /** Check whether conflict. */
    private static boolean checkConflict(Commit split, Commit cur, Commit given, String fName) {
        Map<String, String> sp = split.getNameToBlobId();
        Map<String, String> cp = cur.getNameToBlobId();
        Map<String, String> gp = given.getNameToBlobId();
        if (!sp.containsKey(fName)) {
            return checkPresent(cur, fName) && checkPresent(given, fName)
                    && !cp.get(fName).equals(cp.get(fName));
        } else {
            if (!cp.containsKey(fName)
                    && gp.containsKey(fName)
                    && !gp.get(fName).equals(sp.get(fName))) {
                return true;
            }
            if (!gp.containsKey(fName)
                    && cp.containsKey(fName)
                    && !cp.get(fName).equals(sp.get(fName))) {
                return true;
            }
            return gp.containsKey(fName) && cp.containsKey(fName)
                    && !gp.get(fName).equals(sp.get(fName))
                    && !cp.get(fName).equals(sp.get(fName))
                    && !gp.get(fName).equals(cp.get(fName));
        }
    }

    /** Merges files from the given branch into the current branch. */
    public static void merge(String branchName) {
        Stage s = getStage();
        if (!s.addIsEmpty() || !s.getRemoveName().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (branchName.equals(getBranchName())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit currentCommit = getCommitOfBranch(getBranchName());
        Commit givenCommit = getCommitOfBranch(branchName);
        if (currentCommit == null || givenCommit == null) {
            return;
        }
        List<String> l = plainFilenamesIn(Tree.CWD);
        for (String fileName : l) {
            if (!currentCommit.getNameToBlobId().containsKey(fileName)
                    && !s.addContains(fileName)) {
                System.out.println("There is an untracked file in the way"
                        + "; delete it, or add and commit it first.");
                return;
            }
        }
        Commit split = findAncestor(givenCommit, currentCommit);
        if (split.generateId().equals(givenCommit.generateId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.generateId().equals(currentCommit.generateId())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(branchName);
            return;
        }
        Set<String> fileName = new TreeSet<>();
        fileName.addAll(split.getNameToBlobId().keySet());
        fileName.addAll(givenCommit.getNameToBlobId().keySet());
        fileName.addAll(currentCommit.getNameToBlobId().keySet());
        boolean isConfilct = false;
        for (String fName : fileName) {
            /* case 1 */
            if (!checkModified(split, currentCommit, fName) && checkModified(split, givenCommit, fName)) {
                checkoutCommit(fName, givenCommit.generateId());
                add(fName);
            }
            /* case 5 */
            if (!checkPresent(split, fName)
                    && checkPresent(givenCommit, fName)
                    && !checkPresent(currentCommit, fName)) {
                checkoutCommit(fName, givenCommit.generateId());
                add(fName);
            }
            /* case 6 */
            if (checkPresent(split, fName)
                    && !checkModified(split, currentCommit, fName)
                    && !checkPresent(givenCommit, fName)) {
                File f = join(Tree.CWD, fName);
                rm(fName);
                System.out.println(fName);
                f.delete();
            }
            /* case 8 */
            if (checkConflict(split, currentCommit, givenCommit, fName)) {
                File cur = join(Tree.BLOB_DIR, currentCommit.getNameToBlobId().get(fName) + ".txt");
                String a = "";
                String b = "";
                if (cur.exists()) {
                    Blob curB = readObject(cur, Blob.class);
                    a = curB.getContents();
                }
                File giv = join(Tree.BLOB_DIR, givenCommit.getNameToBlobId().get(fName) + ".txt");
                if (giv.exists()) {
                    Blob givB = readObject(giv, Blob.class);
                    b = givB.getContents();
                }
                String result = "<<<<<<< HEAD\n" + a + "=======\n" + b + ">>>>>>>\n";
                File f = join(Tree.CWD, fName);
                writeContents(f, result);
                isConfilct = true;
                add(fName);
            }
        }
        commit("Merged " + branchName + " into " + getBranchName() + ".", givenCommit);
        if (isConfilct) {
            System.out.println("Encountered a merge conflict.");
        }
    }
}
