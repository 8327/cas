package org.apereo.cas.mgmt;

import org.apache.commons.io.FileUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
        ObjectReader reader = git.getRepository().newObjectReader();
        AbbreviatedObjectId aid = AbbreviatedObjectId.fromString(id);
        ObjectId oid = reader.resolve(aid).iterator().next();
        return new String(reader.open(oid).getBytes());
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
        return new String(reader.open(id).getBytes());
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

    public History createHistory(RevCommit r, String path) {
        try {
            TreeWalk treeWalk  = historyWalk(r,path);
            if (treeWalk.next()) {
                History history = new History();
                history.setId(treeWalk.getObjectId(0).abbreviate(40).name());
                history.setCommit(r.abbreviate(40).name());
                history.setPath(path = treeWalk.getPathString());
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

    public TreeWalk historyWalk(RevCommit r, String path) throws Exception {
        TreeWalk treeWalk = new TreeWalk(git.getRepository());
        treeWalk.addTree(r.getTree());
        treeWalk.setFilter(new HistoryTreeFilter(path));
        return treeWalk;
    }

    /**
     * Method adds an untracked file to the git index.
     *
     * @param file
     */
    public void addFile(String file) {
        try {
            git.add().addFilepattern(file).call();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void markAsSubmitted(RevObject c) throws Exception {
        appendNote(c,"SUBMITTED on "+new Date().toString()+"\n    ");
    }

    public String noteText(RevObject com) throws Exception {
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
     * @param user
     * @return
     */
    public PersonIdent getCommitterId(CasUserProfile user) {
        String displayName = user.getDisplayName();
        String email = user.getEmail();
        return new PersonIdent(user.getId() + " - " + displayName, email);
    }

    public class HistoryTreeFilter extends TreeFilter {
        String path;

        public HistoryTreeFilter(String path) {
            this.path = path;

        }
        @Override
        public boolean include(TreeWalk treeWalk) throws MissingObjectException, IncorrectObjectTypeException, IOException {
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

    public void close() {
        git.close();
    }

    public boolean isNull() {
        return git == null;
    }

    public ObjectReader objectReader() {
        return git.getRepository().newObjectReader();
    }

    public String repoPath() {
        return git.getRepository().getDirectory().getParent().toString();
    }

    public Stream<Ref> branches() throws Exception {
        return git.branchList().call().stream();
    }

    public Git getGit() {
        return git;
    }

    public void writeNote(Note note, OutputStream output) throws Exception {
        git.getRepository().open(note.getData()).copyTo(output);
    }

    /*
    public void setGit(Git git) {
        this.git = git;
    }
    */

    public void pull() throws Exception {
        git.pull().call();
    }

    public void fetch() throws Exception {
        git.fetch().call();
    }

    public int behindCount(String branch) throws Exception {
        return BranchTrackingStatus.of(git.getRepository(),branch).getBehindCount();
    }

    public BranchMap mapBranches(Ref r) {
        try {
            RevWalk revWalk = new RevWalk(git.getRepository());
            return new BranchMap(this,r,revWalk.parseCommit(git.getRepository().resolve(r.getName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DiffEntry> getDiffs(String branch) throws Exception {
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        ObjectReader reader = git.getRepository().newObjectReader();
        oldTreeIter.reset(reader,git.getRepository().resolve(branch+"~1^{tree}"));
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader,git.getRepository().resolve(branch+"^{tree}"));
        return git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
    }

    public byte[] getFormatter(ObjectId oldId, ObjectId newId) throws Exception {
        return getFormatter(rawText(oldId),rawText(newId));
    }

    public byte[] getFormatter(RawText oldText, ObjectId newId) throws Exception {
        return getFormatter(oldText,rawText(newId));
    }

    public byte[] getFormatter(ObjectId oldId, RawText newText) throws Exception {
        return getFormatter(rawText(oldId),newText);
    }

    public byte[] getFormatter(RawText oldText, RawText newText) throws Exception {
        DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm((DiffAlgorithm.SupportedAlgorithm)git.getRepository().getConfig().getEnum("diff", (String)null, "algorithm", DiffAlgorithm.SupportedAlgorithm.HISTOGRAM));
        EditList editList = diffAlgorithm.diff(RawTextComparator.DEFAULT,oldText,newText);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(bytes);
        df.setRepository(git.getRepository());
        df.format(editList,oldText,newText);
        df.flush();
        return bytes.toByteArray();
    }

    public RawText rawText(ObjectId id) throws Exception {
        return new RawText(objectReader().open(id).getBytes());
    }

    public RawText rawText(String path) throws Exception {
        File file = new File(git.getRepository().getWorkTree().getAbsolutePath()+"/"+path);
        return new RawText(FileUtils.readFileToByteArray(file));
    }

    public RevCommit findCommitBeforeSubmit(String branchName) throws Exception {
        RevCommit com = findSubmitCommit(branchName);
        RevCommit before = commitLogs(com).skip(1).findFirst().get();
        return before;
    }

    public RevCommit findSubmitCommit(String branchName) throws Exception {
        return git.branchList().call().stream()
                .map(r -> mapBranches(r))
                .filter(r -> r.getRef().getName().contains(branchName.split("_")[1]))
                .findFirst().get().revCommit;
    }

    public void markAsReverted(String branch, CasUserProfile user) throws Exception {
        RevWalk revWalk = new RevWalk(git.getRepository());
        RevCommit com = revWalk.parseCommit(git.getRepository().resolve(branch));
        String msg = "REVERTED by "+user.getId()+" on "+new Date().toString()+"\n    ";
        appendNote(com, msg);
    }

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
     * @param com
     * @return
     */
    public boolean isRejected(RevObject com) {
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
     * @param com
     * @return
     */
    public boolean isReverted(RevObject com) {
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
     * @param com
     * @return
     */
    public boolean isAccepted(RevObject com) {
        try {
            return noteText(com).contains("ACCEPTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public class BranchMap {
        private Ref ref;
        private RevCommit revCommit;
        private GitUtil git;

        public BranchMap(GitUtil git) {
           this.git = git;
        }

        public BranchMap(GitUtil git,Ref ref, RevCommit revCommit) {
            this(git);
            this.ref = ref;
            this.revCommit = revCommit;
        }

        public Ref getRef() {
            return ref;
        }

        public void setRef(Ref ref) {
            this.ref = ref;
        }

        public RevCommit getRevCommit() {
            return revCommit;
        }

        public void setRevCommit(RevCommit revCommit) {
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
