/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;

/**
 * Skip block of commits based on a counter.
 * <p>
 * 
 * When applied this filter will pick each nth commit while iterating, starting
 * from the first one. Generally speaking, querying information from every nth
 * commit of a repository comes in handy when someone needs to create reports
 * based on each nth commit, and this makes sense only if the commit walking is
 * sorted. However when walking over commits the implementation is not required
 * to return them ordered.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class IterationFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

   /**
    * The number of commits to skip.
    * <p>
    */
   private int block;
   /**
    * The current number of commits that are skipped.
    * <p>
    */
   private int counter = 0;

   /**
    * Create a new filter that will return true for each nth commit.
    * <p>
    * 
    * @param skip
    *           the n for which the commits will be skipped. Must be greater
    *           than 1.
    */
   public IterationFilter(int skip) {
      if (skip < 2) {
         throw new IllegalArgumentException("skip must be grater than 1");
      }
      block = skip;
   }

   /**
    * @return the number of commits to skip
    */
   public int getBlock() {
      return block;
   }

   /**
    * @return the number of current commits that are skipped.
    */
   public int getCounter() {
      return counter;
   }

   /**
    * {@inheritDoc} For each nth commit return true, where n is the argument
    * specified when this filter was created.
    */
   @Override
   public boolean include(T entity) {

      return (counter++ % block) == 0;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + block;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      IterationFilter<?> other = (IterationFilter<?>) obj;
      if (block != other.block)
         return false;
      return true;
   }
}
