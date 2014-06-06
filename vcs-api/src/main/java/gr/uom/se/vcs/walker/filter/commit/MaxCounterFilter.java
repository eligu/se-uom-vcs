/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;

/**
 * Select the first n commits.
 * <p>
 * 
 * When applied this filter will return true only for the first n commits and
 * false for others. n argument is specified in constructor. This will ensure
 * that a desired number of commits will be included in results.
 * <p>
 * NOTE: if there are less commits than the specified <code>size</code> this
 * filter has no effect.
 * <p>
 * Generally speaking iterating over a chunk of commits is desirable when
 * information is required for each chunk of n commits. However for this to make
 * sense, the implementations should take care of when walking commits to return
 * them in order.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MaxCounterFilter<T extends VCSCommit> implements
      VCSCommitFilter<T> {

   /**
    * The size of commits chunk.
    * <p>
    */
   private int size;
   /**
    * The current number of commits that are filtered.
    * <p>
    * Must be smaller or equal than size.
    */
   private int counter = 0;

   /**
    * Creates a new filter that will filter the first <code>n</code> commits.
    * <p>
    * 
    * @param size
    *           the first number of commits to filter. Must be greater than 0.
    */
   public MaxCounterFilter(int size) {
      if (size <= 0) {
         throw new IllegalArgumentException("size must be greater than 0");
      }
      this.size = size;
   }

   /**
    * @return the number of commits that should be allowed
    */
   public int getSize() {
      return size;
   }

   /**
    * {@inheritDoc} Return true for the first <code>n</code> commits.
    * <p>
    */
   @Override
   public boolean include(T entity) {

      return counter++ < size;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + size;
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
      MaxCounterFilter<?> other = (MaxCounterFilter<?>) obj;
      if (size != other.size)
         return false;
      return true;
   }
}
