/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.walker.filter.VCSFilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Abstract class that requires filters based on patterns.
 * <p>
 * 
 * This implementation is based on Java patterns so any string passed at the
 * constructor should take care of the special characters used by Java.
 * <p>
 * <b>NOTE:</b> Pattern implementation may help to find complex author
 * informations. Two different implementations of VCS may use different author
 * representation (i.e. Git repositories John Smith <j.smith@example.com>), and
 * using patterns we can extract only emails for example. However, this
 * flexibility comes at a cost as this requires a lot of processing power to
 * match a lot of author info.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public abstract class PatternsFilter<T> implements VCSFilter<T> {

   /**
    * Java pattern collection used by this filter.
    * <p>
    */
   protected final Set<Pattern> patterns;

   /**
    * A simple cache to help improve the performance by skipping, string
    * matching for values that are already matched.
    * <p>
    * This is a LRU cache, that means the last accessed key will be the first
    * returned. This will allow for removal of 'right' keys if there is no
    * memory available in cache. Implementations may choose the amount of
    * characters this set contains.
    * <p>
    * <b>WARNING:</b> Care should be taken to check the length of the strings
    * stored in cache as this may increase the occupied memory.
    */
   protected final LinkedHashMap<String, Boolean> cache;

   /**
    * The cache size, measured in the number of characters.
    * <p>
    */
   protected int cacheSize = 0;

   /**
    * The maximum number of characters the cache can contains.
    * <p>
    * This equals to 20KB cache per filter.
    */
   protected static final int CACHE_MAX_SIZE = 10000;

   /**
    * Create a new instance based on given patterns.
    * <p>
    * 
    * All sub classes must call this constructor as it will check the given
    * patterns if it is null, empty, or contain any null element.
    * 
    * @param patterns
    */
   public PatternsFilter(Collection<String> patterns) {
      if (patterns == null) {
         throw new IllegalArgumentException("patterns must not be null");
      }
      if (patterns.isEmpty()) {
         throw new IllegalArgumentException("patterns must not be empty");
      }

      this.patterns = new LinkedHashSet<Pattern>();

      for (String str : patterns) {
         if (str == null) {
            throw new IllegalArgumentException("patterns must not contain null");
         }
         this.patterns.add(Pattern.compile(str));
      }
      // This will provide an accessing order, that means the most
      // recently used (will be the first returned item)
      cache = new LinkedHashMap<String, Boolean>(1000, 0.75f, true);
   }

   /**
    * This will check the {@link #cache} if the specified value is contained
    * within.
    * <p>
    * 
    * @param value
    *           to check if its contained in cache
    * @return true if the specified value is contained in cache
    */
   protected boolean checkCache(String value) {
      synchronized (cache) {
         // Case when this value is contained
         if (cache.containsKey(value)) {
            return cache.get(value);
         }
      }
      return false;
   }

   /**
    * Used whenever a new value will be added to cache.
    * <p>
    * 
    * If the cache is full and there is no memory for the new value, the least
    * accessed elements will be removed from memory until there is enough place
    * for the new value. However, there is a risk, in case the new value length
    * is closed to {@link #CACHE_MAX_SIZE} it will clean almost all old values.
    * If the length is greater than maximum the new key will be discarded.
    * <p>
    * <b>NOTE:</b> This method should be use in case you are sure the value is
    * not within cache, because it doesn't check for its containment.
    * 
    * @param value
    *           to add in cache
    * @param matching
    *           true if the new value is matched
    */
   protected void addInCache(String value, boolean matching) {
      synchronized (cache) {

         // Possibly there is enough memory to put the new value,
         // however if the value's length is greater then the maximum
         // cache size this can not be accomplished. In this case
         // we don't want to throw an exception, we just discard
         // the value
         int length = value.length();
         if (length > CACHE_MAX_SIZE) {
            return;
         }

         // Trying to free some memory if there is not
         // enough
         Iterator<String> it = cache.keySet().iterator();
         while ((cacheSize + length > CACHE_MAX_SIZE) && it.hasNext()) {
            cacheSize -= it.next().length();
            it.remove();
         }

         // At this point we are sure that there is enough memory to put
         // the new value
         cache.put(value, matching);
         cacheSize += length;
      }
   }

   /**
    * @return the patterns this filter was created
    */
   public Set<String> getPatterns() {

      Set<String> set = new LinkedHashSet<String>();
      for (Pattern p : patterns) {
         set.add(p.pattern());
      }
      return set;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((patterns == null) ? 0 : patterns.hashCode());
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
      PatternsFilter<?> other = (PatternsFilter<?>) obj;
      if (patterns == null) {
         if (other.patterns != null)
            return false;
      } else if (!patterns.equals(other.patterns))
         return false;
      return true;
   }
}
