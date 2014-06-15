/**
 * 
 */
package gr.uom.se.vcs.analysis;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSDirectory;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.walker.ResourceVisitor;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@SuppressWarnings("unchecked")
public class ResourceAnalyzer<T extends VCSResource> extends Analyzer<T>
      implements ResourceVisitor<T> {

   // Register this analyzer
   static {
      register(VCSResource.class, ResourceAnalyzer.class);
      register(VCSDirectory.class, ResourceAnalyzer.class);
      register(VCSFile.class, ResourceAnalyzer.class);
   }
   /**
    * The resource filter to be used by this visitor.
    * <p>
    */
   protected VCSResourceFilter<VCSResource> resourceFilter;

   /**
    * If this visitor should include directories.
    * <p>
    */
   protected final boolean includeDirs;

   /**
    * If this visitor should include files.
    * <p>
    */
   protected final boolean includeFiles;

   // Used by builder to build this analyzer
   protected ResourceAnalyzer() {
      this(false, true, null);
   }

   /**
    * Creates a resource analyzer based on the given filter.
    * <p>
    * By default this will include only files but not directories.
    * 
    * @param filter
    *           the resource filter, may be null.
    */
   public ResourceAnalyzer(VCSResourceFilter<VCSResource> filter) {
      this(false, true, filter);
   }

   /**
    * Creates a resource analyzer based on the given filter.
    * <p>
    * If both dirs and files are false a IllegalArgumentException will be
    * thrown.
    * 
    * @param dirs
    *           true if the directories should be included
    * @param files
    *           true if the files should be included
    * @param filter
    *           the resource filter, may be null.
    */
   public ResourceAnalyzer(boolean dirs, boolean files,
         VCSResourceFilter<VCSResource> filter) {
      ArgsCheck.isTrue("dirs || files", dirs || files);
      this.includeDirs = dirs;
      this.includeFiles = files;
      this.resourceFilter = filter;
   }

   @Override
   public VCSResourceFilter<T> getFilter() {
      return (VCSResourceFilter<T>) resourceFilter;
   }

   @Override
   public boolean includeDirs() {
      return includeDirs;
   }

   @Override
   public boolean includeFiles() {
      return includeFiles;
   }
}
