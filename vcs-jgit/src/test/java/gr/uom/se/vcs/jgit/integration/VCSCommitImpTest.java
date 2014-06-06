/**
 * 
 */
package gr.uom.se.vcs.jgit.integration;

import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.and;
import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.author;
import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.iteration;
import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.merge;
import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.message;
import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.range;
import static gr.uom.se.vcs.walker.filter.commit.CommitFilterUtility.skip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;


/**
 * A test case that conducts tests on a real repository for VCSCommitImp.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSCommitImpTest extends MainSuite {

   @Test
   public void testGetPrevious() throws VCSRepositoryException  {
      
      // The following commit has two parents
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");
      assertNotNull(commit);
      
      Collection<VCSCommit> parents = commit.getPrevious();
      assertEquals(2, parents.size());
      
      VCSCommit parent1 = thisRepo
            .resolveCommit("f5edc0718da0abb83b26c8cf6797090e64fc8114");
      assertNotNull(parent1);
      
      VCSCommit parent2 = thisRepo
            .resolveCommit("3e974d7375c0697eb464fdb1c0149b5f41495bc8");
      assertNotNull(parent2);
      
      assertTrue(parents.contains(parent1));
      assertTrue(parents.contains(parent2));
      
      // The following commit contains only one parent
      commit = thisRepo
            .resolveCommit("f5edc0718da0abb83b26c8cf6797090e64fc8114");
      assertNotNull(commit);
      parents = commit.getPrevious();
      assertEquals(1, parents.size());
      
      parent1 = thisRepo
            .resolveCommit("ed359d36f2d49c412c929ce1fbca824f26c07b34");
      assertNotNull(parent1);
      assertEquals(parents.iterator().next(), parent1);
   }
   
   @Test
   public void testGetNext() throws VCSRepositoryException {
      // The following commit has three children
      VCSCommit commit = thisRepo
            .resolveCommit("77add59a4a661fa8e9c787c07b4e1ed76b6b3270");
      assertNotNull(commit);
      
      Collection<VCSCommit> children = commit.getNext();
      assertEquals(3, children.size());
      
      VCSCommit child1 = thisRepo
            .resolveCommit("8c583f87276cc5609ffb31fa15fabc35f4f3340c");
      assertNotNull(child1);
      assertTrue(children.contains(child1));
      
      VCSCommit child2 = thisRepo
            .resolveCommit("71022007663ce4480c1e7ec79d762330d4e4207c");
      assertNotNull(child2);
      assertTrue(children.contains(child2));
      
      VCSCommit child3 = thisRepo
            .resolveCommit("c775d45608da117c5bd3732cb331fc38fccf484f");
      assertNotNull(child3);
      assertTrue(children.contains(child3));
   }
   
   @Test
   public void testWalkCommits() throws VCSRepositoryException {
      
      // This is the last commit, at the time this
      // test was written
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");
      assertNotNull(commit);
      
      final List<VCSCommit> commits = new ArrayList<VCSCommit>();
      
      // Walking commits with now filter
      commit.walkCommits(new CommitVisitor<VCSCommit>() {

         @Override
         public boolean visit(VCSCommit entity) {
            commits.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }

         @SuppressWarnings("unchecked")
         @Override
         public
         VCSCommitFilter<VCSCommit> getFilter() {
            return null;
         }
         
      }, true);
      // We know that there are 355 commits (including this one)
      // previous of this commit.
      assertEquals(355, commits.size());
   }
   
   @Test
   public void testWalkCommitsWithCommitFilter() throws VCSRepositoryException {
      
      // This is the last commit, at the time this
      // test was written
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");
      assertNotNull(commit);
      
      final List<VCSCommit> commits = new ArrayList<VCSCommit>();
      
      // 5th of April 2012
      Calendar start = new GregorianCalendar(2012, 3, 5);
      // 1st of April 2014
      Calendar end = new GregorianCalendar(2014, 3, 1);
      
      // Will construct a filter
      @SuppressWarnings("unchecked")
      final VCSCommitFilter<VCSCommit> commitFilter = and(
             message("((\\W|^)fixed(\\W|$)|(\\W|^)fix(\\W|$)|(\\W|^)fixes(\\W|$))")
            ,author("fernandezpablo85")
            ,range(start.getTime(), end.getTime())
            ,skip(3)
            ,iteration(3)
            ,merge()
            );
      
      // Walking commits with now filter
      commit.walkCommits(new CommitVisitor<VCSCommit>() {

         @Override
         public boolean visit(VCSCommit entity) {
            commits.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }

         @SuppressWarnings("unchecked")
         @Override
         public
         VCSCommitFilter<VCSCommit> getFilter() {
            return commitFilter;
         }
         
      }, true);
      
      // We know that there are 6 commits that comply
      // with our filter, but we skip first three commits
      // so we get only three, and we iterate over each three
      // commits . The last commit remained is a merge commit
      // so the merge filter will include it
      assertEquals(1, commits.size());
   }
}
