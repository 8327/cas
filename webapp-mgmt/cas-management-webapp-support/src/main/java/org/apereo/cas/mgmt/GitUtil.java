package org.apereo.cas.mgmt;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharSet;
import org.apereo.cas.mgmt.authentication.CasUserProfile;
import org.apereo.cas.mgmt.services.web.beans.Commit;
import org.apereo.cas.mgmt.services.web.beans.History;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.notes.Note;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class used to help with manipulating git repositories.
 *
 * @author Travis Schmidt
 * @since 5.2
 */
public class GitUtil {

    private final Git git;

    public GitUtil(final Git git) {
        this.git = git;
    }

    /**
     * Returns Commit objects for the last n commits
     *
     * @param n - number of commits to return
     * @return - List of Commit objects
     * @throws Exception - failed.
     */
    public List<Commit> getLastNCommits(final int n) throws Exception {
        return StreamSupport.stream(git.log().setMaxCount(n).call().spliterator(),false)
                .map(c -> new Commit(c.abbreviate(40).name(), c.getFullMessage()))
                .collect(Collectors.toList());
    }

    public List<Commit> getUnpublishedCommits() throws Exception {
        return StreamSupport.stream(git.log().addRange(getPublished().getPeeledObjectId(),git.getRepository().resolve("HEAD"))
                .call().spliterator(),false).map(c -> new Commit(c.abbreviate(40).name(),c.getFullMessage()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a branch with the passed name and commit id from wich to start the branch.
     *
     * @param branchName - The name of the new branch to create.
     * @param startPoint - The commit from which to start the branch.
     * @throws Exception - failed.
     */
    public void createBranch(final String branchName, final String startPoint) throws Exception {
        git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint(startPoint)
                .call();
    }

    /**
     * Cherry picks a single commit to be merge in the current branch.
     *
     * @param commit - RevCommit to be included.
     * @throws Exception - failed.
     */
    public void cherryPickCommit(final RevCommit commit) throws Exception {
        git.cherryPick().include(commit).setNoCommit(true).call();
    }

    /**
     * Creates a branch in the remote repository from which the the current git repository was cloned.
     * The branch is created from the commit passed and given the name that is passed in.
     *
     * @param commit - RevCommit that is to be pushed.
     * @param submitName - The name of the remote branch to be created.
     * @throws Exception - failed.
     */
    public void createPullRequest(final RevCommit commit, final String submitName) throws Exception {
        markAsSubmitted(commit);
        git.push()
                .setRemote("origin")
                .setPushAll()
                .setForce(true)
                .setRefSpecs(new RefSpec("HEAD:refs/heads/" + submitName))
                .call();
    }

    /**
     * Commits all working changes to the repository with the passed commit message.
     *
     * @param user - CasUserProfile of the logged in user.
     * @param msg - Commit message.
     * @return - RevCommit of the new commit.
     * @throws Exception - failed.
     */
    public RevCommit commit(final CasUserProfile user, final String msg) throws Exception {
        return git.commit()
                .setAll(true)
                .setCommitter(getCommitterId(user))
                .setMessage(msg)
                .call();
    }

    /**
     * Checks out the passed ref to be the current branch of the repository.
     *
     * @param ref - String representing a commit in the repository.
     * @throws Exception - failed.
     */
    public void checkout(final String ref) throws Exception {
        git.checkout()
                .setName(ref)
                .call();
    }

    /**
     * Checks out a single file from a commit in the repository and adds it to the working dir.
     *
     * @param path - Full path to the file.
     * @param ref - String representing a commit in the repository.
     * @throws Exception - failed.
     */
    public void checkout(final String path, final String ref) throws Exception {
        git.checkout()
                .setStartPoint(ref)
                .addPath(path)
                .call();
    }

    /**
     * Adds unversioned files to be tracked by the repository.
     *
     * @throws Exception - failed.
     */
    public void addWorkingChanges() throws Exception {
        Status status = git.status().call();
        status.getUntracked()
                .forEach(f -> addFile(f));
    }

    /**
     * Scans the working dir for active changes and returns a list of differences.
     *
     * @return - List of DiffEntry.
     * @throws Exception - failed.
     */
    public List<DiffEntry> scanWorkingDiffs() throws Exception {
        FileTreeIterator workTreeIterator = new FileTreeIterator(git.getRepository());
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        ObjectReader reader = git.getRepository().newObjectReader();
        oldTreeIter.reset(reader, git.getRepository().resolve("HEAD^{tree}"));
        DiffFormatter formatter = new DiffFormatter(System.out);
        formatter.setRepository(git.getRepository());
        return formatter.scan(oldTreeIter, workTreeIterator);
    }

    public String readFromWorkingTree(String id) throws Exception {
        return readFormWorkingTree(ObjectId.fromString(id));
    }

    public String readFormWorkingTree(ObjectId id) throws Exception {
        FileTreeIterator workTreeIterator = new FileTreeIterator(git.getRepository());
        while(!workTreeIterator.eof() && !workTreeIterator.getEntryObjectId().equals(id)) {
            workTreeIterator.next(1);
        }
        return IOUtils.toString(workTreeIterator.openEntryStream(), Charset.defaultCharset());
    }

    /**
     * Returns a RawText representation of a file in the passed repository.  Used in creating diffs.
     *
     * @param repo - The repository to pull the change.
     * @param path - The path to the file.
     * @return - RawText representation of the file.
     * @throws Exception - failed.
     */
    public RawText raw(final Repository repo, final String path) throws Exception {
        File file = new File(repo.getWorkTree().getAbsolutePath()+"/"+path);
        return new RawText(FileUtils.readFileToByteArray(file));
    }

    /**
     * Returns the file as a String form the repository indexed by the passed String
     * representing its ObjectId
     *
     * @param id - String id of a file in the repository.
     * @return - File returned as String.
     * @throws Exception - failed.
     */
    public String readObject(final String id) throws Exception {
        return readFormWorkingTree(ObjectId.fromString(id));
        /*
        ObjectReader reader = git.getRepository().newObjectReader();
        AbbreviatedObjectId aid = AbbreviatedObjectId.fromString(id);
        ObjectId oid = reader.resolve(aid).iterator().next();
        return new String(reader.open(oid).getBytes());
        */
    }

    /**
     * Returns the file as a String form the repository indexed by its ObjectId.
     *
     * @param id - ObjectID of the file.
     * @return - File returned as String.
     * @throws Exception - failed.
     */
    public String readObject(final ObjectId id) throws Exception {
        ObjectReader reader = git.getRepository().newObjectReader();
        if (reader.has(id)) {
            return new String(reader.open(id).getBytes());
        } else {
            return readFormWorkingTree(id);
        }
    }


    /**
     * Merges the branch represented by the passed branchId in the current branch.
     *
     * @param branchId - String representation of an ObjectId
     * @throws Exception - failed.
     */
    public void merge(final String branchId) throws Exception {
        git.merge()
                .setCommit(true)
                .include(ObjectId.fromString(branchId))
                .call();
    }

    /**
     * Returns a RevCommit object looked up from the passed string id.
     *
     * @param id - String representing an ObjectID for a RevCommit.
     * @return - RevCommit
     * @throws Exception - failed.
     */
    public RevCommit getCommit(final String id) throws Exception {
        return new RevWalk(git.getRepository())
                .parseCommit(ObjectId.fromString(id));
    }

    /**
     * Appends a note to a commit that already has notes.
     *
     * @param com - RevObject representing the commit to add the note to.
     * @param msg - The note to append.
     * @throws Exception - failed.
     */
    public void appendNote(final RevObject com, final String msg) throws Exception {
        Note note = note(com);
        StringBuffer buffer = new StringBuffer();
        if (note != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            git.getRepository().open(note.getData()).copyTo(bytes);
            buffer.append(bytes.toString()+"\n\n");
        }
        buffer.append(msg);
        addNote(com,buffer.toString());
        git.close();
    }

    /**
     * Creates a note to a commit.
     *
     * @param com - the RevObject fo the commit
     * @param note - the note text.
     * @throws Exception - failed.
     */
    public void addNote(final RevObject com, final String note) throws Exception {
        git.notesAdd()
                .setObjectId(com)
                .setMessage(note)
                .call();
    }

    /**
     * Returns the note attached to the commit with the passed id.
     *
     * @param id - String representing a commit id.
     * @return - Note attached to commit.
     * @throws Exception - failed.
     */
    public Note note(final String id) throws Exception {
        return note(getCommit(id));
    }

    /**
     * Returns the Note attached to the passed commit.
     *
     * @param com - RevObject of the commit.
     * @return - Returns Note from the commit.
     * @throws Exception - failed.
     */
    public Note note(final RevObject com) throws Exception {
        return git.notesShow()
                .setObjectId(com)
                .call();
    }

    /**
     * Returns the history of a file in the repository.
     *
     * @param path - the file path
     * @return - List of History objects
     * @throws Exception - failed.
     */
    public List<History> history(final String path) throws Exception {
        return logs(path)
                .map(r -> createHistory(r,path))
                .filter(h -> h != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns the logs for a file in the repository.
     *
     * @param path - The file path.
     * @return - Stream of RevCommits the file is in.
     * @throws Exception - failed.
     */
    public Stream<RevCommit> logs(final String path)  throws Exception {
        return StreamSupport.stream(git.log().addPath(path).call().spliterator(),false);
    }

    /**
     * Checksout a file into the working directory.
     *
     * @param path - The file path.
     * @throws Exception - failed.
     */
    public void checkoutFile(final String path) throws Exception {
        git.checkout()
                .addPath(path)
                .call();
    }

    /**
     * Returns the logs for a specified commit.
     *
     * @param com - The commit to retrieive logs for.
     * @return - Stream of RevCommits contained in the passed commit.
     * @throws Exception - failed.
     */
    public Stream<RevCommit> commitLogs(final RevCommit com) throws Exception {
        return StreamSupport.stream(git.log().add(com).call().spliterator(), false);
    }

    /**
     * Peforms a hard reset on the repository to the passed commit.
     *
     * @param reset - the RevCommit to reset the repository to.
     * @throws Exception - failed.
     */
    public void reset(final RevCommit reset) throws Exception {
        git.reset()
                .setRef(reset.abbreviate(40).name())
                .setMode(ResetCommand.ResetType.HARD)
                .call();
    }

    /**
     * Creates a History object for the passed file in the passed commit.
     *
     * @param r - The commit to pull the History from.
     * @param path - The file path.
     * @return - History of the path for the passed commit.
     */
    public History createHistory(final RevCommit r, final String path) {
        try {
            TreeWalk treeWalk  = historyWalk(r,path);
            if (treeWalk.next()) {
                History history = new History();
                history.setId(treeWalk.getObjectId(0).abbreviate(40).name());
                history.setCommit(r.abbreviate(40).name());
                history.setPath(treeWalk.getPathString());
                history.setMessage(r.getFullMessage());
                history.setTime(new Date(r.getCommitTime() * 1000l).toString());
                history.setCommitter(r.getCommitterIdent().getName());
                return history;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a TreeWalk object for the passed commit and file path.
     *
     * @param r - The commit to start the walk from.
     * @param path - The file path.
     * @return - TreeWalk
     * @throws Exception - failed.
     */
    public TreeWalk historyWalk(final RevCommit r, final String path) throws Exception {
        TreeWalk treeWalk = new TreeWalk(git.getRepository());
        treeWalk.addTree(r.getTree());
        treeWalk.setFilter(new HistoryTreeFilter(path));
        return treeWalk;
    }

    /**
     * Method adds an untracked file to the git index.
     *
     * @param file - the file.
     */
    public void addFile(final String file) {
        try {
            git.add().addFilepattern(file).call();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks a commit as being submitted for a pull request.
     *
     * @param c - The RevObject of the commit to mark as submitted.
     * @throws Exception -failed.
     */
    public void markAsSubmitted(final RevObject c) throws Exception {
        appendNote(c,"SUBMITTED on "+new Date().toString()+"\n    ");
    }

    /**
     * Returns a the Note of a commit as a String.
     *
     * @param com - The RevObkect of the commit to pull the note from.
     * @return - Returns the note text as a String.
     * @throws Exception -failed.
     */
    public String noteText(final RevObject com) throws Exception {
        Note note = note(com);
        if(note != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            git.getRepository().open(note.getData()).copyTo(bytes);
            return bytes.toString();
        }
        return "";
    }

    /**
     * Creates a Person Identity to add to the commit.
     *
     * @param user - CasUserProfile of the logged in user.
     * @return - PersonIden object to be added to a commit.
     */
    public PersonIdent getCommitterId(final CasUserProfile user) {
        String displayName = user.getDisplayName();
        String email = user.getEmail() != null ? user.getEmail() : "mgmt@cas.com";
        return new PersonIdent(user.getId() + " - " + displayName, email);
    }

    public void setPublished() {
        try {
            git.tagDelete().setTags("published").call();
            git.tag().setName("published").call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Ref getPublished() {
        try {
            Ref ref = git.tagList().call().get(0);
            return git.getRepository().peel(ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Class used to define a TreeFilter to only pull history for a single path.
     */
    public class HistoryTreeFilter extends TreeFilter {
        String path;

        public HistoryTreeFilter(final String path) {
            this.path = path;

        }
        @Override
        public boolean include(final TreeWalk treeWalk) throws MissingObjectException, IncorrectObjectTypeException, IOException {
            return treeWalk.getPathString().equals(path);
        }

        @Override
        public boolean shouldBeRecursive() {
            return false;
        }

        @Override
        public TreeFilter clone() {
            return null;
        }
    }

    /**
     * Closes the git repository.
     */
    public void close() {
        git.close();
    }

    /**
     * Method to determine if there is not wrapped repository.
     *
     * @return - true if no git repository is present.
     */
    public boolean isNull() {
        return git == null;
    }

    /**
     * Returns a new ObjectReader for the repository.
     *
     * @return - ObjectReader.
     */
    public ObjectReader objectReader() {
        return git.getRepository().newObjectReader();
    }

    /**
     * Returns the root path of the repository.
     *
     * @return -String representing the root directory.
     */
    public String repoPath() {
        return git.getRepository().getDirectory().getParent().toString();
    }

    /**
     * Returns a stream of Branches that are contained in the repository.
     *
     * @return - Stream of Branch Refs
     * @throws Exception - failed.
     */
    public Stream<Ref> branches() throws Exception {
        return git.branchList().call().stream();
    }

    /**
     * Returns the repository wrapped by this utility.
     *
     * @return - Git repository.
     */
    public Git getGit() {
        return git;
    }

    /**
     * Pulls the text form a Note object and writes it to the passes Outputstream.
     *
     * @param note - The Note contained in the repository to read.
     * @param output - The stream to ouput the note text.
     * @throws Exception - failed.
     */
    public void writeNote(final Note note, final OutputStream output) throws Exception {
        git.getRepository().open(note.getData()).copyTo(output);
    }

    /*
    public void setGit(Git git) {
        this.git = git;
    }
    */

    /**
     * Pulls changes form the default remote repository into the wrapped repository.
     *
     * @throws Exception - failed.
     */
    public void pull() throws Exception {
        git.pull().call();
    }

    /**
     * Fetches changed form the default remote repository into the wrapped repository.
     *
     * @throws Exception - failed.
     */
    public void fetch() throws Exception {
        git.fetch().call();
    }

    /**
     * Returns how many commits behind a branch is from its upstream origin.
     *
     * @param branch - The branch to check.
     * @return - int count of number commits behind.
     * @throws Exception - failed.
     */
    public int behindCount(final String branch) throws Exception {
        return BranchTrackingStatus.of(git.getRepository(),branch).getBehindCount();
    }

    /**
     * Returns a BranchMap for the commit passed as a Ref.
     *
     * @param r - Ref commit to generate the BranchMap for.
     * @return - BranchMap.
     */
    public BranchMap mapBranches(final Ref r) {
        try {
            RevWalk revWalk = new RevWalk(git.getRepository());
            return new BranchMap(this,r,revWalk.parseCommit(git.getRepository().resolve(r.getName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list fo differences between the last two commits in a branch.
     *
     * @param branch - The branch to check for differences against.
     * @return - List of DiffEntry.
     * @throws Exception - failed.
     */
    public List<DiffEntry> getDiffs(final String branch) throws Exception {
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        ObjectReader reader = git.getRepository().newObjectReader();
        oldTreeIter.reset(reader,git.getRepository().resolve(branch+"~1^{tree}"));
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader,git.getRepository().resolve(branch+"^{tree}"));
        return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
    }

    /**
     * Overloaded method to return a formatted diff by using two ObjectIds.
     *
     * @param oldId - ObjectId.
     * @param newId - ObectId.
     * @return - Formatted diff in a byte[].
     * @throws Exception -failed.
     */
    public byte[] getFormatter(final ObjectId oldId, final ObjectId newId) throws Exception {
        return getFormatter(rawText(oldId),rawText(newId));
    }

    /**
     * Overloaded method to return a formatted diff by using a RawText and an ObjectId.
     *
     * @param oldText - RawText.
     * @param newId - ObjectId.
     * @return - Formatted diff in a byte[].
     * @throws Exception -failed.
     */
    public byte[] getFormatter(final RawText oldText, final ObjectId newId) throws Exception {
        return getFormatter(oldText,rawText(newId));
    }

    /**
     * Overloaded method to return a formatted diff by using a RawText and an ObjectId.
     *
     * @param oldId - ObjectId.
     * @param newText - RawText.
     * @return - Formatted diff in a byte[].
     * @throws Exception - failed.
     */
    public byte[] getFormatter(final ObjectId oldId, final RawText newText) throws Exception {
        return getFormatter(rawText(oldId),newText);
    }

    /**
     * Compares the RawText of two files and creates a formateted diff to return.
     *
     * @param oldText - RawText.
     * @param newText - RawText.
     * @return - Formatted diff in a byte[].
     * @throws Exception -failed.
     */
    public byte[] getFormatter(final RawText oldText, final RawText newText) throws Exception {
        DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm((DiffAlgorithm.SupportedAlgorithm)git.getRepository().getConfig().getEnum("diff", (String)null, "algorithm", DiffAlgorithm.SupportedAlgorithm.HISTOGRAM));
        EditList editList = diffAlgorithm.diff(RawTextComparator.DEFAULT,oldText,newText);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(bytes);
        df.setRepository(git.getRepository());
        df.format(editList,oldText,newText);
        df.flush();
        return bytes.toByteArray();
    }

    /**
     * Returns the RawText of file specified by ObjectId.
     *
     * @param id - ObjectId of a file.
     * @return - RawText.
     * @throws Exception - failed.
     */
    public RawText rawText(final ObjectId id) throws Exception {
        if (objectReader().has(id)) {
            return new RawText(objectReader().open(id).getBytes());
        } else {
            return new RawText(readFormWorkingTree(id).getBytes());
        }
    }

    /**
     * Returns the RawText of a file specified by its path.
     *
     * @param path - File path.
     * @return - RawText.
     * @throws Exception - failed.
     */
    public RawText rawText(final String path) throws Exception {
        File file = new File(git.getRepository().getWorkTree().getAbsolutePath()+"/"+path);
        return new RawText(FileUtils.readFileToByteArray(file));
    }

    /**
     * Returns the last commit before the commit that was submitted as a pull request.
     *
     * @param branchName - Name given to the branch when submitted.
     * @return - RevCommit of the previous commit.
     * @throws Exception - failed.
     */
    public RevCommit findCommitBeforeSubmit(final String branchName) throws Exception {
        RevCommit com = findSubmitCommit(branchName);
        RevCommit before = commitLogs(com).skip(1).findFirst().get();
        return before;
    }

    /**
     * Returns the commit used to submit the pull request.
     *
     * @param branchName - Name given to the branch when submitted.
     * @return - RevCommit used to submit the pull request.
     * @throws Exception - failed.
     */
    public RevCommit findSubmitCommit(final String branchName) throws Exception {
        return git.branchList().call().stream()
                .map(r -> mapBranches(r))
                .filter(r -> r.getRef().getName().contains(branchName.split("_")[1]))
                .findFirst().get().revCommit;
    }

    /**
     * Marks a pull request as being reverted by the person who submitted it.
     *
     * @param branch - Ref of the branch to revert.
     * @param user - CasUserProfile of the logged in user.
     * @throws Exception - failed.
     */
    public void markAsReverted(final String branch, final CasUserProfile user) throws Exception {
        RevWalk revWalk = new RevWalk(git.getRepository());
        RevCommit com = revWalk.parseCommit(git.getRepository().resolve(branch));
        String msg = "REVERTED by "+user.getId()+" on "+new Date().toString()+"\n    ";
        appendNote(com, msg);
    }

    /**
     * Rebases the wrapped repository to the remote it was created form.
     */
    public void rebase() {
        try {
            git.pull().setRebase(true).call();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method determines if a branch has been rejected by an admin.
     *
     * @param com - RevObject of the commit.
     * @return - trues if commit is marked as rejected.
     */
    public boolean isRejected(final RevObject com) {
        try {
            return noteText(com).contains("REJECTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method determines if a branch has been rejected by an admin.
     *
     * @param com - RevObject of the commit.
     * @return - true if the commit is marked as reverted.
     */
    public boolean isReverted(final RevObject com) {
        try {
            return noteText(com).contains("REVERTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method determines if a branch has been accepted by an admin.
     *
     * @param com - RevObject of the commit.
     * @return - true if the commit is marked as accpeted.
     */
    public boolean isAccepted(final RevObject com) {
        try {
            return noteText(com).contains("ACCEPTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the repository contains any deleted files in working directory that have not been committed.
     *
     * @return - Stream of DiffEntry
     * @throws Exception - failed.
     */
    public Stream<DiffEntry> checkForDeletes() throws Exception {
        FileTreeIterator workTreeIterator = new FileTreeIterator(git.getRepository());
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        ObjectReader reader = git.getRepository().newObjectReader();
        oldTreeIter.reset(reader, git.getRepository().resolve("HEAD^{tree}"));
        DiffFormatter formatter = new DiffFormatter(System.out);
        formatter.setRepository(git.getRepository());
        return formatter.scan(oldTreeIter, workTreeIterator).stream()
                .filter(d -> d.getChangeType() == DiffEntry.ChangeType.DELETE);
    }

    /**
     * Object used to represent the history of a branch.
     */
    public class BranchMap {
        private Ref ref;
        private RevCommit revCommit;
        private GitUtil git;

        public BranchMap(final GitUtil git) {
           this.git = git;
        }

        public BranchMap(final GitUtil git, final Ref ref, final RevCommit revCommit) {
            this(git);
            this.ref = ref;
            this.revCommit = revCommit;
        }

        public Ref getRef() {
            return ref;
        }

        public void setRef(final Ref ref) {
            this.ref = ref;
        }

        public RevCommit getRevCommit() {
            return revCommit;
        }

        public void setRevCommit(final RevCommit revCommit) {
            this.revCommit = revCommit;
        }

        public String getName() {
            return ref.getName();
        }

        public String getFullMessage() {
            return revCommit.getFullMessage();
        }

        public String getCommitter() {
            return revCommit.getCommitterIdent().getName();
        }

        public int getCommitTime() {
            return revCommit.getCommitTime();
        }

        public String getId() {
            return revCommit.abbreviate(40).name();
        }

        public boolean isAccepted() {
            return git.isAccepted(revCommit);
        }

        public boolean isRejected() {
            return git.isRejected(revCommit);
        }

        public boolean isReverted() {
            return git.isReverted(revCommit);
        }
    }
}
