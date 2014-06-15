/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Skip n first commits, where n is specified in constructor.
 * <p>
 * 
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class SkipFilter implements VCSCommitFilter {

   /**
    * The number of commits to skip.
    * <p>
    */
   private int block;

   /**
    * The current number of passed commits.
    * <p>
    */
   private AtomicInteger counter = new AtomicInteger(0);

   /**
    * Create a new filter that will return true for each nth commit.
    * <p>
    * 
    * @param skip
    *           the n for which the commits will be skipped. Must be greater
    *           than 0.
    */
   public SkipFilter(int skip) {
      if (skip < 1) {
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
    * {@inheritDoc} For each nth commit return true, where n is the argument
    * specified when this filter was created.
    */
   @Override
   public boolean include(VCSCommit entity) {

      return counter.getAndAdd(1) > block;
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
      SkipFilter other = (SkipFilter) obj;
      if (block != other.block)
         return false;
      return true;
   }
}
