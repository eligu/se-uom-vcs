package gr.uom.se.vcs.analysis.util;

import gr.uom.se.vcs.Edit;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.walker.filter.VCSFilter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * A simple class that stores changes between two commits.
 * <p>
 * 
 * This class can be served as a helper, where storing changes between two
 * commits you can perform various queries for the type of changes and the paths
 * they manipulate.
 * <p>
 * This class is mostly for reading, however you can change the contents of this
 * (only the file changes). Although, the class is synchronized and so thread
 * safe.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitEdits implements Comparable<CommitEdits> {
   /** The old commit */
   private VCSCommit newC;
   /** The new commit */
   private VCSCommit oldC;
   /** Changes from old to new commit */
   private Collection<VCSFileDiff<?>> edits;
   /** Lock for changes */
   private ReadWriteLock editsLock = new ReentrantReadWriteLock();

   private VCSFilter<VCSFile> fileFilter;
   private VCSFilter<VCSFileDiff<?>> diffFilter;

   /**
    * Creates a new instance based on old and new commit and their changes from
    * old to new.
    * <p>
    * oldC and newC must not be equal.
    * 
    * @param oldC
    *           old commit, not null
    * @param newC
    *           new commit, not null
    * @param changes
    *           from old to new
    */
   public CommitEdits(VCSCommit oldC, VCSCommit newC,
         Collection<VCSFileDiff<?>> changes) {
      if (oldC == null) {
         throw new IllegalArgumentException("oldC must not be null");
      }
      if (newC == null) {
         throw new IllegalArgumentException("newC must not be null");
      }
      if (oldC.equals(newC)) {
         throw new IllegalArgumentException("oldC must not be equal to newC");
      }
      if (oldC.getCommitDate().after(newC.getCommitDate())) {
         throw new IllegalArgumentException("oldC must be before newC");
      }

      this.edits = new ArrayList<VCSFileDiff<?>>();
      if (changes != null) {
         for (VCSFileDiff<?> fd : changes) {
            if (fd == null) {
               throw new IllegalArgumentException(
                     "changes must not contain null element");
            }
            this.edits.add(fd);
         }
      }

      this.newC = newC;
      this.oldC = oldC;
   }

   public void setFilesFilter(VCSFilter<VCSFile> filter) {
      editsLock.writeLock().lock();
      try {
         this.fileFilter = filter;
      } finally {
         editsLock.writeLock().unlock();
      }
   }

   public void setDiffFilter(VCSFilter<VCSFileDiff<?>> filter) {
      editsLock.writeLock().lock();
      try {
         this.diffFilter = filter;
      } finally {
         editsLock.writeLock().unlock();
      }
   }

   public VCSCommit getNewCommit() {
      return newC;
   }

   public VCSCommit getOldCommit() {
      return oldC;
   }

   public Collection<Edit> getEdits() {
      editsLock.readLock().lock();
      try {
         Collection<Edit> eds = new ArrayList<Edit>();
         if (edits == null) {
            return eds;
         }
         for (VCSFileDiff<?> e : edits) {
            if (allowDiff(e, fileFilter, diffFilter)) {
               eds.addAll(e.getEdits());
            }
         }
         return eds;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   private static boolean allowDiff(VCSFileDiff<?> diff,
         VCSFilter<VCSFile> fileFilter, VCSFilter<VCSFileDiff<?>> diffFilter) {
      if (diffFilter != null) {
         if (!diffFilter.include(diff)) {
            return false;
         }
      }
      if (fileFilter != null) {
         VCSFile file = null;
         if (diff.getType().isAdd() || diff.getType().isModify()) {
            file = diff.getNewResource();
         } else if (diff.getType().isNone()) {
            return false;
         } else {
            file = diff.getOldResource();
         }
         if (!fileFilter.include(file)) {
            return false;
         }
      }
      return true;
   }

   private static boolean allowDiff(VCSChange.Type type, VCSFileDiff<?> diff,
         VCSFilter<VCSFile> fileFilter, VCSFilter<VCSFileDiff<?>> diffFilter) {
      if (type != null && !type.equals(diff.getType())) {
         return false;
      }
      return allowDiff(diff, fileFilter, diffFilter);
   }

   public int getNoNewLines() {
      editsLock.readLock().lock();
      try {
         return getNewLines(edits, fileFilter, diffFilter);
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public int getNoOldLines() {
      editsLock.readLock().lock();
      try {
         return getOldLines(edits, fileFilter, diffFilter);
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public static int getOldLines(Collection<VCSFileDiff<?>> edits,
         VCSFilter<VCSFile> fileFilter, VCSFilter<VCSFileDiff<?>> diffFilter) {
      int lines = 0;
      for (VCSFileDiff<?> diff : edits) {
         if (allowDiff(diff, fileFilter, diffFilter)) {
            lines += getRemovedLines(diff);
         }
      }
      return lines;
   }

   public static int getNewLines(Collection<VCSFileDiff<?>> edits,
         VCSFilter<VCSFile> fileFilter, VCSFilter<VCSFileDiff<?>> diffFilter) {
      int lines = 0;
      for (VCSFileDiff<?> diff : edits) {
         if (allowDiff(diff, fileFilter, diffFilter)) {
            lines += getAddedLines(diff);
         }
      }
      return lines;
   }

   public int getNumberOfFilesWithChange(VCSChange.Type type,
         VCSFilter<VCSFile> fileFilter, VCSFilter<VCSFileDiff<?>> diffFilter) {
      editsLock.readLock().lock();
      try {
         int lines = 0;
         for (VCSFileDiff<?> diff : edits) {
            if (allowDiff(type, diff, fileFilter, diffFilter)) {
               lines++;
            }
         }
         return lines;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public static int getAddedLines(VCSFileDiff<?> diff) {
      VCSChange.Type type = diff.getType();
      if (type.isAdd()) {
         VCSFile file = diff.getNewResource();
         try {
            return countLines(file);
         } catch (IOException e) {
            throw new IllegalStateException("Can not open the file: "
                  + e.getMessage());
         }
      } else {
         int counter = 0;
         for (Edit e : diff.getEdits()) {
            counter += e.getLengthB();
         }
         return counter;
      }

   }

   public static int getRemovedLines(VCSFileDiff<?> diff) {
      VCSChange.Type type = diff.getType();
      if (type.isDelete()) {
         VCSFile file = diff.getOldResource();
         try {
            return countLines(file);
         } catch (IOException e) {
            throw new IllegalStateException("Can not open the file: "
                  + e.getMessage());
         }
      } else {
         int counter = 0;
         for (Edit e : diff.getEdits()) {
            counter += e.getLengthA();
         }
         return counter;
      }
   }

   public static int countLines(VCSFile file) throws IOException {
      LineIterator it = null;
      try {
         byte[] contents = file.getContents();
         if (contents == null || contents.length == 0) {
            return 0;
         }
         it = IOUtils.lineIterator(new ByteArrayInputStream(contents),
               (String) null);
         int counter = 0;
         while (it.hasNext()) {
            it.next();
            counter++;
         }
         return counter;
      } finally {
         if (it != null) {
            it.close();
         }
      }
   }

   public Collection<VCSFileDiff<?>> getFileChanges() {
      editsLock.readLock().lock();
      try {
         ArrayList<VCSFileDiff<?>> diffs = new ArrayList<VCSFileDiff<?>>();
         for (VCSFileDiff<?> diff : edits) {
            if (allowDiff(diff, fileFilter, diffFilter)) {
               diffs.add(diff);
            }
         }
         return diffs;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public Collection<VCSFile> getFilesWithChange(VCSChange.Type type) {
      editsLock.readLock().lock();
      try {
         Collection<VCSFile> files = new ArrayList<VCSFile>();
         for (VCSFileDiff<?> fd : edits) {
            if (allowDiff(type, fd, fileFilter, diffFilter)) {

               VCSFile file = null;
               if (type.isAdd() || type.isModify()) {
                  file = fd.getNewResource();
               } else {
                  file = fd.getOldResource();
               }
               if (file == null) {
                  throw new IllegalStateException("unspecified file resource");
               }
               files.add(file);
            }
         }
         return files;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public Collection<VCSFile> getFilesWithChange(VCSChange.Type type,
         VCSFilter<VCSFile> filter) {
      editsLock.readLock().lock();
      try {
         Collection<VCSFile> files = new ArrayList<VCSFile>();
         for (VCSFileDiff<?> fd : edits) {
            if (fd.getType().equals(type)) {
               VCSFile file = null;
               if (type.isAdd() || type.isModify()) {
                  file = fd.getNewResource();
               } else {
                  file = fd.getOldResource();
               }
               if (file == null) {
                  throw new IllegalStateException("unspecified file resource");
               }
               if (filter.include(file)) {
                  files.add(file);
               }
            }
         }
         return files;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public int getNumberOfFilesWithChange(VCSChange.Type type) {
      editsLock.readLock().lock();
      try {
         int counter = 0;
         for (VCSFileDiff<?> fd : edits) {
            if (allowDiff(type, fd, fileFilter, diffFilter)) {
               counter++;
            }
         }
         return counter;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public Collection<VCSFileDiff<?>> getChangesOf(VCSChange.Type type) {
      editsLock.readLock().lock();
      try {
         Collection<VCSFileDiff<?>> changes = new ArrayList<VCSFileDiff<?>>();
         for (VCSFileDiff<?> fd : edits) {
            if (allowDiff(type, fd, fileFilter, diffFilter)) {
               changes.add(fd);
            }
         }
         return changes;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public boolean isOldFileChanged(VCSFile file, VCSChange.Type type) {
      editsLock.readLock().lock();
      try {
         for (VCSFileDiff<?> d : edits) {
            if (type.equals(d.getType())) {
               if (type.isAdd()) {
                  return false;
               }
               VCSFile oldFile = d.getOldResource();
               if (oldFile == null) {
                  throw new IllegalStateException(
                        "old file is null while it must not be null");
               }

               if (oldFile.equals(file)) {
                  return true;
               }
            }
         }
         return false;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public boolean isNewFileChanged(VCSFile file, VCSChange.Type type) {
      editsLock.readLock().lock();
      try {
         for (VCSFileDiff<?> d : edits) {
            if (type.equals(d.getType())) {
               if (type.isDelete()) {
                  return false;
               }
               VCSFile newFile = d.getNewResource();
               if (newFile == null) {
                  throw new IllegalStateException(
                        "new file is null while it must not be null");
               }

               if (newFile.equals(file)) {
                  return true;
               }
            }
         }
         return false;
      } finally {
         editsLock.readLock().unlock();
      }
   }

   public void add(VCSFileDiff<?> e) {
      if (e == null) {
         throw new IllegalArgumentException("e must not be null");
      }

      boolean incorrect = true;
      if (e.getType().isDelete()) {
         incorrect = !e.getOldCommit().equals(oldC);
      } else if (e.getType().isAdd()) {
         incorrect = !e.getNewCommit().equals(newC);
      } else {
         incorrect = !(e.getNewCommit().equals(newC) && e.getOldCommit()
               .equals(oldC));
      }
      if (incorrect) {
         throw new IllegalArgumentException(
               "e doesn't seem to be a change from old to new commit");
      }
      editsLock.writeLock().lock();
      try {
         edits.add(e);
      } finally {
         editsLock.writeLock().unlock();
      }
   }

   public void add(Collection<VCSFileDiff<?>> eds) {
      editsLock.writeLock().lock();
      try {
         for (VCSFileDiff<?> d : eds) {
            this.add(d);
         }
      } finally {
         editsLock.writeLock().unlock();
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((newC == null) ? 0 : newC.hashCode());
      result = prime * result + ((oldC == null) ? 0 : oldC.hashCode());
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
      CommitEdits other = (CommitEdits) obj;
      if (newC == null) {
         if (other.newC != null)
            return false;
      } else if (!newC.equals(other.newC))
         return false;
      if (oldC == null) {
         if (other.oldC != null)
            return false;
      } else if (!oldC.equals(other.oldC))
         return false;
      return true;
   }

   @Override
   public int compareTo(CommitEdits other) {
      Date myDate = this.newC.getCommitDate();
      Date otherDate = other.newC.getCommitDate();
      return myDate.compareTo(otherDate);
   }
}