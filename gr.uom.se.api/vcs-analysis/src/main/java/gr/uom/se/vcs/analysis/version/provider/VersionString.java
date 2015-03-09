package gr.uom.se.vcs.analysis.version.provider;

/**
 * A utility class, instances of whom keep information about a version.
 * <p>
 * 
 * @author Theodore Chaikalis
 * @author Elvis Ligu
 */
public class VersionString implements Comparable<VersionString> {

   /**
    * The major part of a version of type ##.##.##.
    */
   private String major;
   /**
    * The minor part of a version of type ##.##.##
    */
   private String minor;
   /**
    * The revision part of a version of type ##.##.##
    */
   private String revision;
   /**
    * The major part of a version as int of type ##.##.##.
    */
   private int majorInt;
   /**
    * The minor part of a version as int of type ##.##.##
    */
   private int minorInt;
   /**
    * The revision part of a version as int of type ##.##.##
    */
   private int revInt;

   /**
    * The version label for which this was constructed.
    */
   private final String label;

   /**
    * Construct a version by giving all its parts.
    * <p>
    * The type of the version is supposed to be ##.##.##. The label of the
    * version will not be stored within this object.
    * 
    * @param major
    *           the major part, if null it will be '0'
    * @param minor
    *           the minor part, if null it will be '0'
    * @param revision
    *           the revision part, if null it will be '0'
    */
   public VersionString(String major, String minor, String revision) {
      this(null, major, minor, revision);
   }

   /**
    * Construct a version by giving all its parts.
    * <p>
    * The type of the version is supposed to be ##.##.##. The label of the
    * version will be stored within this object.
    * 
    * @param word
    *           the label from which this version is constructed
    * @param major
    *           the major part, if null it will be '0'
    * @param minor
    *           the minor part, if null it will be '0'
    * @param revision
    *           the revision part, if null it will be '0'
    */
   public VersionString(String word, String major, String minor, String revision) {

      this.major = correct(major);
      this.minor = correct(minor);
      this.revision = correct(revision);
      majorInt = Integer.parseInt(this.major);
      minorInt = Integer.parseInt(this.minor);
      revInt = Integer.parseInt(this.revision);
      this.label = word;
   }

   /**
    * Correct a part of a version (major, minor, revision). If the part is null
    * set it as '0'.
    * <p>
    * 
    * @param ver
    *           the part to be corrected
    * @return the corrected part
    */
   private String correct(String ver) {
      if (ver != null) {
         return ver;
      } else {
         return "0";
      }
   }

   /**
    * @return the label from which this was constructed
    */
   public String getLabel() {
      return label;
   }

   /**
    * Get the major part of a version, of type ##.##.##.
    * <p>
    * 
    * @return
    */
   public String getMajor() {
      return major;
   }

   /**
    * Get the minor part of a version, of type ##.##.##.
    * <p>
    * 
    * @return
    */
   public String getMinor() {
      return minor;
   }

   /**
    * Get the revision part of a version, of type ##.##.##.
    * <p>
    * 
    * @return
    */
   public String getRevision() {
      return revision;
   }

   /**
    * Get the major part of a version as int, of type ##.##.##.
    * <p>
    * 
    * @return
    */
   public int getMajorInt() {
      return majorInt;
   }

   /**
    * Get the minor part of a version as int, of type ##.##.##.
    * <p>
    * 
    * @return
    */
   public int getMinorInt() {
      return minorInt;
   }

   /**
    * Get the revision part of a version as int, of type ##.##.##.
    * <p>
    * 
    * @return
    */
   public int getRevInt() {
      return revInt;
   }

   @Override
   public int compareTo(VersionString other) {
      if (majorInt > other.majorInt) {
         return 1;
      } else if (majorInt < other.majorInt) {
         return -1;
      }
      if (minorInt > other.minorInt) {
         return 1;
      } else if (minorInt < other.minorInt) {
         return -1;
      }
      int comp = Integer.compare(revInt, other.revInt);
      if (comp == 0) {
         String otherLabel = other.label;
         if (label != null) {
            if (otherLabel != null) {
               comp = label.compareTo(otherLabel);
            } else {
               // This version will be considered older
               // if this has a label and the other doesn't
               comp = -1;
            }
         } else {
            // This will be considered newer if this
            // doesn't have a label but the other have one
            if (otherLabel != null) {
               comp = 1;
            }
         }
      }
      return comp;
   }

   @Override
   public String toString() {
      return major + "." + minor + "." + revision;
   }

}
