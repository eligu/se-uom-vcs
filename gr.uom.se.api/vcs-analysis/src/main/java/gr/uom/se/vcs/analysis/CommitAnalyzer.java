/**
 * 
 */
package gr.uom.se.vcs.analysis;

import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Collection;

/**
 * An analyzer used to work only with commits. It serves as a commit visitor to.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitAnalyzer extends Analyzer<VCSCommit> implements
      CommitVisitor {

   // Register this analyzer to the known analyzers
   static {
      register(VCSCommit.class, CommitAnalyzer.class);
   }

   /**
    * @param commitFilter
    *           the commit filter to set
    */
   public void setCommitFilter(VCSCommitFilter commitFilter) {
      if (isStarted()) {
         throw new IllegalArgumentException(
               "can not set filter while this is running");
      }
      this.commitFilter = commitFilter;
   }

   /**
    * @param resourceFilter
    *           the resource filter to set
    */
   public void setResourceFilter(VCSResourceFilter<VCSResource> resourceFilter) {
      if (isStarted()) {
         throw new IllegalArgumentException(
               "can not set filter while this is running");
      }
      this.resourceFilter = resourceFilter;
   }

   /**
    * The resource filter to be used by this visitor.
    * <p>
    */
   protected VCSResourceFilter<VCSResource> resourceFilter;

   /**
    * The commit filter to be used by this visitor.
    * <p>
    */
   protected VCSCommitFilter commitFilter;

   // Used by analyzer builder
   protected CommitAnalyzer() {
   }

   /**
    * Creates a new commit analyzer based on given processors and filters.
    * <p>
    * 
    * @param processors to be run by this analyzer
    * @param commitFilter the commit filter
    * @param resourceFilter the resource filter
    */
   public CommitAnalyzer(Collection<Processor<VCSCommit>> processors,
         VCSCommitFilter commitFilter,
         VCSResourceFilter<VCSResource> resourceFilter) {
      super(processors);

      this.commitFilter = commitFilter;
      this.resourceFilter = resourceFilter;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
      return (VCSResourceFilter<R>) resourceFilter;
   }

   @SuppressWarnings("unchecked")
   @Override
   public VCSCommitFilter getFilter() {
      return commitFilter;
   }
}
