/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import se.uom.vcs.VCSResource;

/**
 * A filter that checks if a resource's path ends with a given suffix.
 * <p>
 * 
 * This will use {@link String#endsWith(String)} method to check for suffixes.
 * This filter can not be combined in an AND clause with the following:
 * <ul>
 * <li>{@link PathFilter}</li>
 * <li>{@link SuffixFilter}</li>
 * </ul>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class SuffixFilter<T extends VCSResource> extends ExactFilter<T>
      implements VCSResourceFilter<T> {

   /**
    * The set of suffixes to check each resource.
    * <p>
    */
   private Set<String> suffixes;

   /**
    * Creates a new suffix filter based on given patterns.
    * <p>
    * 
    * @param patterns
    *           must not be null, not or empty or contain any null element
    */
   public SuffixFilter(Collection<String> patterns) {
      if (patterns == null) {
         throw new IllegalArgumentException("patterns must not be null");
      }
      if (patterns.isEmpty()) {
         throw new IllegalArgumentException("patterns must not be empty");
      }

      this.suffixes = new LinkedHashSet<String>();

      for (String str : patterns) {
         if (str == null) {
            throw new IllegalArgumentException("patterns must not contain null");
         }
         this.suffixes.add(AbstractPathFilter.correctAndCheckPath(str));
      }
   }

   @Override
   protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
      if (filter instanceof PathFilter) {
         return true;
      }
      if (filter instanceof SuffixFilter) {
         return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    * <p>
    * Returns true only if the path of the given resource ends with one of the
    * supplied suffixes.
    */
   @Override
   public boolean include(T entity) {
      String path = entity.getPath();

      if (path.equals("/")) {
         return false;
      }
      path = AbstractPathFilter.correctAndCheckPath(path);

      for (String pattern : suffixes) {
         if (path.endsWith(pattern)) {
            return true;
         }
      }
      return false;
   }

   /**
    * @return the suffixes this filter contains
    */
   public Set<String> getSuffixes() {
      return suffixes;
   }

   @Override
   public VCSResourceFilter<T> not() {

      return new SuffixFilter<T>(suffixes) {
         @Override
         public boolean include(T entity) {
            return !super.include(entity);
         }

         @Override
         public VCSResourceFilter<T> not() {
            return new SuffixFilter<T>(suffixes);
         }
      };
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((suffixes == null) ? 0 : suffixes.hashCode());
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
      SuffixFilter<?> other = (SuffixFilter<?>) obj;
      if (suffixes == null) {
         if (other.suffixes != null)
            return false;
      } else if (!suffixes.equals(other.suffixes))
         return false;
      return true;
   }
}
