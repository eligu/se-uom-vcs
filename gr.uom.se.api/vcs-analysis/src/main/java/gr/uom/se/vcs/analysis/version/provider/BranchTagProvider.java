/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSBranch;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * A branch tag provider that will return all tags of a given branch.
 * <p>
 * This will resolve all tags of a given branch, by walking the branch head
 * commit back up to the first branch commit, and collecting the tags. If a
 * branch name is specified and it can be resolved by a repository it should
 * provide the tags of that branch, otherwise it will try to look at
 * {@linkplain BranchTagProvider#DEFAULT_BRANCH_NAMES default} branch names if
 * one of them is resolvable.
 * 
 * @author Elvis Ligu
 */
public class BranchTagProvider implements TagProvider {

   /**
    * The default branch names to look at if a branch name is not provided.
    */
   private static final LinkedList<String> DEFAULT_BRANCH_NAMES = new LinkedList<>();

   static {
      DEFAULT_BRANCH_NAMES.add("master");
      DEFAULT_BRANCH_NAMES.add("main");
      DEFAULT_BRANCH_NAMES.add("trunk");
   }

   /**
    * The branch to collect all tags from.
    */
   private final VCSBranch branch;

   /**
    * The repository from where to resolve the tags.
    */
   private final VCSRepository repo;

   /**
    * Given a repository and a branch name, try to resolve that branch and
    * collect the tags of that branch.
    * <p>
    * 
    * @param repo
    *           the repository from where to collect the tags and to find the
    *           branch
    * @param branch
    *           the branch name to look for tags. If null or it can not be
    *           resolved one of the default branch names will be used.
    * @throws VCSRepositoryException
    *            if a branch can not be resolved
    */
   public BranchTagProvider(VCSRepository repo, String branch)
         throws VCSRepositoryException {
      ArgsCheck.notNull("repo", repo);
      VCSBranch br = getBranch(repo, branch);
      if (br == null) {
         throw new IllegalArgumentException(
               "Can not resolve a branch from repo");
      }
      this.branch = br;
      this.repo = repo;
   }

   /**
    * Given a repository, try to resolve a main branch (usually master) and
    * collect the tags of that branch.
    * <p>
    * This will look up one of the following branches: master, main, trunk, in
    * the order they are specified.
    * 
    * @param repo
    *           the repository from where to collect the tags and to find the
    *           branch
    * @throws VCSRepositoryException
    *            if a branch can not be resolved
    */
   public BranchTagProvider(VCSRepository repo) throws VCSRepositoryException {
      this(repo, (String) null);
   }

   /**
    * Given a repository and a branch, collect the tags of that branch.
    * <p>
    * 
    * @param repo
    *           the repository from where to collect the tags and to find the
    *           branch
    * @param branch
    *           the branch to look for tags.
    * @throws VCSRepositoryException
    *            if a branch can not be resolved
    */
   public BranchTagProvider(VCSRepository repo, VCSBranch branch)
         throws VCSRepositoryException {
      ArgsCheck.notNull("repo", repo);
      ArgsCheck.notNull("branch", branch);
      this.branch = branch;
      this.repo = repo;
   }

   /**
    * Select a branch from the given repository.
    * <p>
    * 
    * @param repo
    * @param branch
    * @return return the selected branch or null if the branch can not be
    *         resolved.
    */
   private VCSBranch getBranch(VCSRepository repo, String branch) {
      LinkedList<String> names = new LinkedList<>();
      names.addAll(DEFAULT_BRANCH_NAMES);
      if (branch != null) {
         names.push(branch);
      }
      Collection<VCSBranch> branches = null;
      try {
         branches = repo.getBranches();

      } catch (VCSRepositoryException re) {
         return null;
      }
      if (branches.isEmpty()) {
         return null;
      }
      Iterator<String> it = names.iterator();
      while (it.hasNext()) {
         String name = it.next();

         for (VCSBranch br : branches) {
            String bname = br.getName();
            if (bname.endsWith(name)) {
               return br;
            }
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    * 
    * @throws VCSRepositoryException
    */
   @SuppressWarnings("unchecked")
   @Override
   public Collection<VCSTag> getTags() throws VCSRepositoryException {
      VCSBranch master = branch;
      Collection<VCSTag> tags = repo.getTags();
      final Map<VCSCommit, VCSTag> mappedTags = mapTags(tags);
      tags = null;
      // Walk the commits of this branch and find only the tags
      // that have a commit which is part of this branch
      final Collection<VCSTag> masterTags = new ArrayList<>();
      master.walkCommits(new AbstractCommitVisitor() {

         @Override
         public boolean visit(VCSCommit entity) {

            VCSTag tag = mappedTags.get(entity);
            if (tag != null) {
               masterTags.add(tag);
            }
            return true;
         }
      }, true);

      return masterTags;
   }

   /**
    * Map the given tags to their respective commits.
    * <p>
    * 
    * @param tags
    *           to be mapped
    * @return a mapping of commits to tags
    * @throws VCSRepositoryException
    *            if something goes wrong while resolving the commit of a tag.
    */
   private static Map<VCSCommit, VCSTag> mapTags(final Collection<VCSTag> tags)
         throws VCSRepositoryException {
      Map<VCSCommit, VCSTag> map = new HashMap<>();
      for (VCSTag tag : tags) {
         map.put(tag.getCommit(), tag);
      }
      return map;
   }
}
