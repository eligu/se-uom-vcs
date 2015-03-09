/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version name resolver based on regexes.
 * <p>
 * 
 * This implementation will try to match a given version label if it conforms to
 * a defined regex. The ({@linkplain #DEFAULT_VERSION_REGEX default}) regex
 * requires a version to be of the form ##.##.##. where dots can be where dots
 * can be .-_:.. However it doesn't requires all parts of the version, that is a
 * 'v2' will be recognized as a version. If a version is matched it than will be
 * split by a split regex. The {@linkplain #DEFAULT_SPLIT_REGEX default split)}
 * regex will use one of the predefined separators to split the version part.
 * Clients may specify other regexes to identify and split versions.
 * 
 * @author Elvis Ligu
 */
public class DefaultVersionNameResolver implements VersionNameResolver {

   /**
    * The default version regex to identify version labels.
    * <p>
    * The versioning scheme should be ##.##.## where dots can be .-_:.
    * <p>
    * It is not required for a version to have all three parts, and this will
    * identify even versions with one parts such as 'v1'.
    */
   public static final String DEFAULT_VERSION_REGEX = "[0-9]+((\\.|\\:|\\-|\\_)[0-9]+((\\.|\\:|\\-|\\_)[0-9]+)?)?";
   /**
    * The default version regex to split version into parts.
    * <p>
    * The versioning scheme should be ##.##.## where dots can be .-_:.
    * <p>
    * The number of split depends on the number of the parts a version may have.
    */
   public static final String DEFAULT_SPLIT_REGEX = "(\\.|\\:|\\-|\\_)";
   /**
    * Precompiled version identification patter.
    */
   private final Pattern pattern;
   /**
    * Precompiled version split pattern.
    */
   private final Pattern splitPattern;

   /**
    * Create a version name resolver based on the given regex.
    * <p>
    * This resolver will identify a label if it conforms to the given
    * {@code versionRegex}, if not it will return null. Also the split regex is
    * expected to split the version label into three parts. However that is not
    * required.
    * 
    * @param versionRegex
    *           the regex to identify the version label if it is a known label.
    *           If null a default regex ({@linkplain #DEFAULT_VERSION_REGEX
    *           default}) will be used.
    * @param splitRegex
    *           the regex to split the version into discrete parts (usually the
    *           well known major.minor.revision scheme. If null a default regex
    *           ({@linkplain #DEFAULT_SPLIT_REGEX default split)}) will be used.
    */
   public DefaultVersionNameResolver(String versionRegex, String splitRegex) {
      if (versionRegex == null) {
         versionRegex = DEFAULT_VERSION_REGEX;
      }
      if (splitRegex == null) {
         splitRegex = DEFAULT_SPLIT_REGEX;
      }
      pattern = Pattern.compile(versionRegex);
      splitPattern = Pattern.compile(splitRegex);
   }

   /**
    * Create a version name resolver based on the default regexes.
    * <p>
    * This resolver will identify a label if it conforms to the defaults (
    * {@linkplain #DEFAULT_VERSION_REGEX default regex}), if not it will return
    * null. Also the split regex is expected to split the version label into
    * three parts. However that is not required. This will use a
    * {@linkplain #DEFAULT_SPLIT_REGEX default split)} regex.
    * 
    * @param versionRegex
    *           the regex to identify the version label if it is a known label.
    *           If null a default regex ({@linkplain #DEFAULT_VERSION_REGEX
    *           default}) will be used.
    * @param splitRegex
    *           the regex to split the version into discrete parts (usually the
    *           well known major.minor.revision scheme. If null a default regex
    *           ({@linkplain #DEFAULT_SPLIT_REGEX default split)}) will be used.
    */
   public DefaultVersionNameResolver() {
      this(null, null);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VersionString resolveVersionString(String label) {
      ArgsCheck.notNull("label", label);
      Matcher m = pattern.matcher(label);
      VersionString verString = null;
      if (m.find()) {
         String vStr = m.group(0);
         String[] splitted = splitPattern.split(vStr);
         String major = splitted[0];
         String minor = (splitted.length > 1 ? splitted[1] : null);
         String revision = (splitted.length > 2 ? splitted[2] : null);
         verString = new VersionString(label, major, minor, revision);
      }
      return verString;
   }

}
