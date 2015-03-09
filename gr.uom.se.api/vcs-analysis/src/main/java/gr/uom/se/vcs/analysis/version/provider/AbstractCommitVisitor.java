package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * Simple visitor to be used by methods of this package.
 * 
 * @author Elvis Ligu
 */
abstract class AbstractCommitVisitor implements CommitVisitor {

   /**
    * {@inheritDoc}
    */
   @Override
   public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public VCSCommitFilter getFilter() {
      return null;
   }
}