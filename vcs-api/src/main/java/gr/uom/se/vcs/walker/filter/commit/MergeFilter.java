/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;

/**
 * Filter commits that comes from a merge result.
 * <p>
 * 
 * Usually commits that are merge results have two or more parents. However this
 * depends on implementation of {@link VCSCommit#isMergeCommit()}.
 * <p>
 * This class is a singleton, because we can not construct two different
 * instances of this class.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MergeFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

   /**
    * The holder of this singleton instance.
    * <p>
    */
   private static MergeFilter<?> INSTANCE = null;

   private MergeFilter() {}

   /**
    * @return the instance of this filter
    */
   @SuppressWarnings("unchecked")
   public static <T extends VCSCommit> MergeFilter<T> getInstance() {

      if (INSTANCE == null) {
         INSTANCE = new MergeFilter<T>();
      }
      return (MergeFilter<T>) INSTANCE;
   }

   /**
    * {@inheritDoc}
    * <p>
    * Returns true only if entity's <code>isMergeCommit()</code> method returns
    * true.
    */
   @Override
   public boolean include(T entity) {
      return entity.isMergeCommit();
   }

   @Override
   public int hashCode() {
      return 31;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      return true;
   }
}
