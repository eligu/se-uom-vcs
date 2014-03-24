package se.uom.vcs.jgit.usecases;

import java.util.Arrays;
import java.util.Collection;

import se.uom.vcs.VCSBranch;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSRepository;
import se.uom.vcs.VCSResource;
import se.uom.vcs.VCSTag;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.exceptions.VCSResourceNotFound;
import se.uom.vcs.jgit.VCSRepositoryImp;
import se.uom.vcs.walker.ModifyingPathVisitor;
import se.uom.vcs.walker.Visitor;

/**
 * Hello world!
 * 
 */
public class UseCase {
	static final String REMOTE_URL = "https://github.com/TooTallNate/Java-WebSocket.git";
	static final String LOCAL_PATH = "src/test/resources/Java-WebSocket";

	static VCSRepository repo = null;

	public static void main(String[] args) throws VCSRepositoryException {
		// cloneRemote();
		open();
		printInfo();
		listBranches();
		listTags();
		System.out
				.println("---------------------TREE FOR HEAD (DEFAULT BRANCH)--------------------------");
		walkTreeHead();
		System.out
				.println("---------------------COMMITS OF A PATH----------------------------------------");
		walkModifyingPathCommits();
		repo.close();
	}

	private static VCSRepository cloneRemote() throws VCSRepositoryException {
		repo = new VCSRepositoryImp(LOCAL_PATH, REMOTE_URL);
		repo.cloneRemote();
		return repo;
	}

	private static VCSRepository open() throws VCSRepositoryException {

		if (repo == null) {
			if (VCSRepositoryImp.containsGitDir(LOCAL_PATH))
				repo = new VCSRepositoryImp(LOCAL_PATH, REMOTE_URL);
			else
				repo = cloneRemote();

		} else {
			repo = new VCSRepositoryImp(LOCAL_PATH, REMOTE_URL);
		}

		return repo;
	}

	private static void listBranches() throws VCSRepositoryException {

		if (repo == null) {
			repo = open();
		}

		System.out.println("[branches]");
		for (VCSBranch branch : repo.getBranches()) {
			System.out.println(branch.getID());
		}
	}

	private static void listTags() throws VCSRepositoryException {

		if (repo == null) {
			repo = open();
		}

		System.out.println("[tags]");
		for (VCSTag tag : repo.getTags()) {
			System.out.println(tag.getID());
		}
	}

	private static void printInfo() throws VCSRepositoryException {

		if (repo == null) {
			repo = open();
		}

		System.out.println("Selected branch: " + repo.getSelectedBranch());
		System.out.println("Head commit: " + repo.getHead());
		System.out.println("Modified at: " + repo.getHead().getDate());
		System.out.println("Commiter: " + repo.getHead().getCommiter());
	}

	private static void walkTreeHead() throws VCSRepositoryException {

		if (repo == null) {
			repo = open();
		}

		VCSCommit head = repo.getHead();
		System.out.println("Walking head commit: " + head.getID());

		head.walkTree(new Visitor<VCSResource>() {

			public boolean visit(VCSResource entity) {
				System.out.println(entity.getPath());
				return true;
			}

		});
	}

	private static void walkModifyingPathCommits()
			throws VCSRepositoryException {

		if (repo == null) {
			repo = open();
		}

		final String path = "src/main/java/org/java_websocket/WebSocketImpl.java";
		VCSCommit head = repo.getHead();
		System.out.println("Walking commits modifying path: " + path);

		head.walkCommitBack(new ModifyingPathVisitor<VCSCommit>() {

			public Collection<String> getPaths() {
				return Arrays.asList(path);
			}

			public boolean visit(VCSCommit commit) {
				VCSResource resource;
				try {

					resource = commit.getResource(path);
					if (resource == null) {
						System.out.print("DELETE: ");
					} else if (resource.isAdded()) {
						System.out.print("ADD: ");
					} else if (resource.isModified()) {
						System.out.print("MODIFY: ");
					}
					System.out.println(commit.getID());
					System.out.println(commit.getCommiter() + " at "
							+ commit.getDate());

				} catch (VCSRepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VCSResourceNotFound e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return true;
			}
		});
	}
}
