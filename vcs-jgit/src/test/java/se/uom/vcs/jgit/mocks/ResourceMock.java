/**
 * 
 */
package se.uom.vcs.jgit.mocks;

import java.util.ArrayList;
import java.util.List;

import se.uom.vcs.jgit.VCSResourceImp;

/**
 * Test mock for class {@link VCSResourceImp}.<p>
 * 
 * Each resource mock contains the path of resource, a list of commits that create the resource,
 * a list of commits that delete the resource, a list of commits that modify the resource, 
 * and a list of commits where this resource is available.
 * <p>
 * {@link #RESOURCES} array contains all the resources that may have been or are already present in
 * repository. And {@link #DIRS} contains a list of directories.
 * 
 * @author Elvis Ligu
 *
 */
public class ResourceMock {

	/**
	 * Is used as a reference to store the length of {@link #COMMITS}.<p>
	 * All commits in the array are stored from new to old, and if we make other commits
	 * in repository, the commit references that each mock have will remain the same,
	 * because they are referred to as the distance from the length of the array.
	 */
	private static final int len = CommitMock.COMMITS.length;

	/**
	 * Resources of small repository, and their history.<p>
	 */
	public static final ResourceMock[] RESOURCES = new ResourceMock[] {
		new ResourceMock("src/Test.txt", new int[]{len-1}, new int[]{len-8}, new int[0], new int[]{len-1, len-2, len-3, len-4, len-5, len-6, len-7}),
		new ResourceMock("test_2.txt", new int[]{len-2}, new int[]{len-8}, new int[0], new int[]{ len-2, len-3, len-4, len-5, len-6, len-7}),
		new ResourceMock("test_3.txt", new int[]{len-5}, new int[0], new int[]{len-8}, new int[]{len-5, len-8}),
		new ResourceMock("src/Test_renamed.txt", new int[]{len-8}, new int[0], new int[0], new int[]{len-8}),
		new ResourceMock("main/test_3.txt", new int[]{len-3}, new int[0], new int[0], new int[]{len-3, len-4, len-6, len-7}),
		new ResourceMock("Test_4.txt", new int[]{len-4}, new int[0], new int[0], new int[]{len-4, len-6, len-7}),
		new ResourceMock("src/java/test_src.txt", new int[]{len-6}, new int[0], new int[0], new int[]{len-6, len-7}),
		new ResourceMock("src/java/another.txt", new int[]{len-7}, new int[0], new int[0], new int[]{len-7}),
	};

	/**
	 * Directories of small repository and their history.<p>
	 */
	public static final ResourceMock[] DIRS = new ResourceMock[] {
		new ResourceMock("src", new int[]{len-1}, new int[]{len-8}, new int[0], new int[]{len-1, len-2, len-3, len-4, len-5, len-6, len-7}),
		new ResourceMock("main", new int[]{len-3}, new int[0], new int[0], new int[]{len-3, len-4, len-6, len-7}),
		new ResourceMock("src/java", new int[]{len-6}, new int[0], new int[0], new int[]{len-6, len-7}),
	};

	/**
	 * Find from resource mocks if the given path is available at the given commit.<p>
	 * 
	 * @param path of resource
	 * @param cid commit id
	 * @return
	 */
	public static ResourceMock resource(String path, final String cid) {
		path = path.replace('\\', '/');
		for(final ResourceMock resource : RESOURCES) {
			if(resource.path.equals(path)) {
				if(resource.isAvailable(cid)){
					return resource;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Get all resources in this commit.<p>
	 * 
	 * @param cid commit id
	 * @return
	 */
	public static List<ResourceMock> resources(final String cid) {
		final List<ResourceMock> resources = new ArrayList<ResourceMock>();
		for(final ResourceMock resource : RESOURCES) {
			if(resource.isAvailable(cid)){
				resources.add(resource);
			}
		}
		return resources;
	}

	/**
	 * Get all resources under the given prefix in the given commit.<p>
	 * 
	 * @param prefix
	 * @param cid
	 * @return
	 */
	public static List<ResourceMock> resources(final String prefix, final String cid) {
		final List<ResourceMock> resources = new ArrayList<ResourceMock>();
		final ResourceMock dir = dir(prefix, cid);
		if(dir == null) {
			return resources;
		}
		for(final ResourceMock resource : RESOURCES) {
			if(resource.isAvailable(cid) && resource.isChildOf(dir)){
				resources.add(resource);
			}
		}
		return resources;
	}

	/**
	 * Get from dir mocks the directory at the specified commit, if any.<p>
	 * 
	 * @param path of directory
	 * @param cid commit id
	 * @return
	 */
	public static ResourceMock dir(String path, final String cid) {
		path = path.replace('\\', '/');
		for(final ResourceMock resource : DIRS) {
			if(resource.path.equals(path)) {
				if(resource.isAvailable(cid)) {
					return resource;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Check if this resource is child of given dir.<p>
	 * 
	 * @param dir
	 * @return
	 */
	public boolean isChildOf(final ResourceMock dir) {
		final String[] rsegments = this.path.split("/");
		if(rsegments.length == 1) {
			return false;
		}
		final String[] dsegments = dir.path.split("/");
		if(dsegments.length != (rsegments.length-1)) {
			return false;
		}
		for(int i = 0; i < dsegments.length; i++) {
			if(!dsegments[i].equals(rsegments[i])) {
				return false;
			}
		}
		return true;
	}

	/** 
	 * The path of this resource.<p>
	 */
	private final String path;

	/**
	 * Indices of commits that creates this resource.<p>
	 */
	private final int[] created;

	/**
	 * Indices of commits that deletes this resource.<p>
	 */
	private final int[] deleted;

	/**
	 * Indices of commits that modify this resource.<p>
	 */
	private final int[] modified;

	/**
	 * Indices of commits this resource is available.<p>
	 */
	private final int[] commits;

	public ResourceMock(final String path, final int[] created, final int[] deleted, final int[] modified, final int[] commits) {

		this.path = path;
		this.created = created;
		this.deleted = deleted;
		this.modified = modified;
		this.commits = commits;
	}

	/**
	 * @return path of this resource
	 */
	public String path() {
		return this.path;
	}

	/**
	 * @return commits that created this resource
	 */
	public CommitMock[] created() {
		final CommitMock[] commits = new CommitMock[this.created.length];
		int j = 0;
		for(final int i : this.created) {
			commits[j++] = CommitMock.COMMITS[i];
		}
		return commits;
	}

	/**
	 * @return commits that deleted this resource
	 */
	public CommitMock[] deleted() {
		final CommitMock[] commits = new CommitMock[this.deleted.length];
		int j = 0;
		for(final int i : this.deleted) {
			commits[j++] = CommitMock.COMMITS[i];
		}
		return commits;
	}

	/**
	 * @return commits that modified this resource
	 */
	public CommitMock[] modified() {
		final CommitMock[] commits = new CommitMock[this.modified.length];
		int j = 0;
		for(final int i : this.modified) {
			commits[j++] = CommitMock.COMMITS[i];
		}
		return commits;
	}

	/**
	 * 
	 * @return commits where this resource is available
	 */
	public CommitMock[] commits() {
		final CommitMock[] commits = new CommitMock[this.commits.length];
		int j = 0;
		for(final int i : this.commits) {
			commits[j++] = CommitMock.COMMITS[i];
		}
		return commits;
	}

	/**
	 * @param cid commit id
	 * @return true if resource is available at the given commit
	 */
	public boolean isAvailable(final String cid) {
		for(final CommitMock cm : this.commits()) {
			if(cm.id.equals(cid)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param id commit id
	 * @return true if resource is modified at this commit
	 */
	public boolean isModified(final String id) {
		for(final int i : this.modified) {
			if(CommitMock.COMMITS[i].id.equals(id)){
				return true;
			}
		}
		return false;
	}

	public boolean isAdded(final String id) {
		for(final int i : this.created) {
			if(CommitMock.COMMITS[i].id.equals(id)){
				return true;
			}
		}
		return false;
	}

	public boolean isDeleted(final String id) {
		for(final int i : this.deleted) {
			if(CommitMock.COMMITS[i].id.equals(id)){
				return true;
			}
		}
		return false;
	}

	public CommitMock head() {
		return CommitMock.COMMITS[this.commits[this.commits.length-1]];
	}
}
