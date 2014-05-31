/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;

/**
 * Filter only the resources that are modified at a given commit.
 * <p>
 * 
 * A modified resource should return true when {@link VCSResource#isAdded()} ||
 * {@link VCSResource#isModified()}.
 * <p>
 * This is a singleton class.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ModifiedFilter<T extends VCSResource> extends ExactFilter<T>
      implements VCSResourceFilter<T> {

   /**
    * Placeholder for the singleton instance.<p>
    */
   private static ModifiedFilter<?> INSTANCE = null;
   
   /**
    * @return the instance of modified filter
    */
   @SuppressWarnings("unchecked")
   public static <T extends VCSResource> ModifiedFilter<T> getInstance() {
      if(INSTANCE == null) {
         INSTANCE = new ModifiedFilter<T>();
      }
      return (ModifiedFilter<T>) INSTANCE;
   }
   private ModifiedFilter() {}
   
   @Override
   public boolean include(T entity) {
      try {
         return entity.isAdded() || entity.isModified();
      } catch (VCSRepositoryException e) {
         throw new IllegalArgumentException(e);
      }
   }

   @Override
   public VCSResourceFilter<T> not() {
      return new ModifiedFilter<T>() {
         @Override
         public boolean include(T entity) {
            return !super.include(entity);
         }

         @Override
         public VCSResourceFilter<T> not() {
            return new ModifiedFilter<T>();
         }
      };
   }

   @Override
   protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
      if (filter instanceof ModifiedFilter) {
         return true;
      }
      return false;
   }
   
   @Override
   public boolean equals(Object obj) {
      if(obj == null) {
         return false;
      }
      if(obj.getClass() == getClass()) {
         return true;
      }
      return false;
   }
}
