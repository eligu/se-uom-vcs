/**
 * 
 */
package gr.uom.se.vcs.analysis;

import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.walker.ChangeVisitor;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Collection;

/**
 * An analyzer used to work only with changes. It serves as a change visitor to.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */

@SuppressWarnings("unchecked")
public class ChangeAnalyzer<T extends VCSChange<?>> extends Analyzer<T>
      implements ChangeVisitor<T> {

   // Registering known analyzer.
   static {
      register(VCSChange.class, ChangeAnalyzer.class);
      register(VCSFileDiff.class, ChangeAnalyzer.class);
   }

   /**
    * The change filter this visitor will use.
    * <p>
    */
   protected VCSFilter<T> changeFilter;

   /**
    * The resource filter to be used by this visitor.
    * <p>
    */
   protected VCSResourceFilter<? extends VCSResource> resourceFilter;

   // Used by analyzer builder
   protected ChangeAnalyzer() {
   }

   /**
    * Creates a new change analyzer based on given processors, and the filters.
    * <p>
    * 
    * @param processors
    *           to be run by this analyzer
    * @param changeFilter
    *           the change filter that will be used by this visitor
    * @param resourceFilter
    *           the resource filter that will be used by this visitor
    */
   public ChangeAnalyzer(Collection<Processor<T>> processors,
         VCSFilter<T> changeFilter,
         VCSResourceFilter<? extends VCSResource> resourceFilter) {
      super(processors);

      this.changeFilter = changeFilter;
      this.resourceFilter = resourceFilter;
   }

   @Override
   public <F extends VCSFilter<T>> F getFilter() {
      return (F) this.changeFilter;
   }

   @Override
   public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
      return (VCSResourceFilter<R>) this.resourceFilter;
   }

}
