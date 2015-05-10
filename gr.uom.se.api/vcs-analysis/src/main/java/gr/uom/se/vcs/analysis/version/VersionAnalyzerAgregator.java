/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.analysis.Analyzer;
import gr.uom.se.vcs.analysis.CommitAnalyzer;
import gr.uom.se.vcs.analysis.CounterProcessor;
import gr.uom.se.vcs.analysis.util.CommitEdits;
import gr.uom.se.vcs.analysis.version.provider.ConnectedVersionProvider;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Map;
import java.util.Set;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VersionAnalyzerAgregator {

   private AuthorVersionProcessor authors;
   private AuthorVersionProcessor commiters;
   private CommitVersionCounterProcessor versionCounter;
   private VersionChangeProcessor changes;
   private CounterProcessor<VCSCommit> commitCounter;

   private CommitAnalyzer analyzer;
   private VCSCommitFilter commitFilter;
   private VCSResourceFilter<VCSResource> resourceFilter;
   private VCSResourceFilter<VCSResource> resourceFilterForChanges;
   private VCSFilter<VCSFileDiff<?>> changeFilter;
   private int threads = 4;
   private int queueSize = 1000;
   private boolean collectAuthors;
   private boolean collectCommitters;
   private boolean analyzeVersionCommits;
   private boolean analyzeIntermediateCommits;
   private boolean countCommits;
   private boolean countCommitsPerVersion;
   private ConnectedVersionProvider versionProvider;
   
   public VersionAnalyzerAgregator(ConnectedVersionProvider versionProvider) {
      this.versionProvider = versionProvider;
   }

   public synchronized void collectAuthors(boolean authors) {
      this.collectAuthors = authors;
   }

   public synchronized void collectCommitters(boolean committers) {
      this.collectCommitters = committers;
   }

   public synchronized void analyzeVersionCommits(boolean versions) {
      this.analyzeVersionCommits = versions;
   }

   public synchronized void analyzeIntermediateCommits(boolean indermediate) {
      this.analyzeIntermediateCommits = indermediate;
   }

   public synchronized void setThreads(int threads) {
      this.threads = threads;
   }

   public synchronized void setQueueSize(int size) {
      this.queueSize = size;
   }

   public synchronized void setCommitFilter(
         VCSCommitFilter commitFilter) {
      this.commitFilter = commitFilter;
   }

   public synchronized void setResourceFilterForCommits(
         VCSResourceFilter<VCSResource> resourceFilter) {
      this.resourceFilter = resourceFilter;
   }

   public synchronized void setChangeFilter(
         VCSFilter<VCSFileDiff<?>> changeFilter) {
      this.changeFilter = changeFilter;
   }

   public synchronized void countCommits(boolean countCommits) {
      this.countCommits = countCommits;
   }

   public synchronized void countCommitsPerVersion(
         boolean countCommitsPerVersion) {
      this.countCommitsPerVersion = countCommitsPerVersion;
   }

   public synchronized void setResourceFilterForModifications(
         VCSResourceFilter<VCSResource> resourceFilter) {
      this.resourceFilterForChanges = resourceFilter;
   }

   public synchronized void run(VCSRepository repo)
         throws VCSRepositoryException, InterruptedException {

      if (analyzer == null) {
         
         // Create an analyzer to visit all commits and to run all processors
         this.analyzer = Analyzer.<VCSCommit> builder().setThreads(threads)
               .setTaskQueueSize(queueSize).build(CommitAnalyzer.class);
         analyzer.setResourceFilter(resourceFilter);
         analyzer.setCommitFilter(commitFilter);

         if (this.countCommitsPerVersion) {
            // Create a commit processor to count commits per version
            this.versionCounter = new CommitVersionCounterProcessor(versionProvider,
                  null);
            this.analyzer.addParallel(versionCounter);
         }

         if (this.collectAuthors) {
            // Create a commit processor to collect authors per version
            this.authors = new AuthorVersionProcessor(versionProvider, null, true);
            this.analyzer.addParallel(authors);
         }
         if (this.collectCommitters) {
            // Create a commit processor to collect commiters per version (the
            // boolean value must be false)
            this.commiters = new AuthorVersionProcessor(versionProvider, null, false);
            this.analyzer.addParallel(commiters);
         }

         // Create a commit processor to collect the edits for each version
         changes = new VersionChangeProcessor(versionProvider, null,
               this.changeFilter, this.resourceFilterForChanges,
               this.analyzeIntermediateCommits, this.analyzeVersionCommits,
               (VCSChange.Type[]) null);
         this.analyzer.addParallel(changes);

         if (this.countCommits) {
            // Create a counter processor to count all commits that will visit
            this.commitCounter = new CounterProcessor<VCSCommit>();
            
            this.analyzer.addParallel(this.commitCounter);
         }
         
         VCSCommit MASTER_HEAD = repo.getHead();
         analyzer.start();
         try {
            MASTER_HEAD.walkCommits(analyzer, true);
         } finally {

            try {
               analyzer.stop();
            } finally {
               analyzer.shutDown();
            }
         }
      }
   }

   public synchronized Map<String, Set<String>> authorsPerVersion() {
      if (authors != null) {
         return authors.getResult();
      }
      return null;
   }

   public synchronized Map<String, Set<String>> committersPerVersion() {
      if (this.commiters != null) {
         return this.commiters.getResult();
      }
      return null;
   }

   public synchronized Map<String, Integer> numberOfCommitsPerVersion() {
      if (this.versionCounter != null) {
         return this.versionCounter.getResult();
      }
      return null;
   }

   public synchronized int numberOfCommits() {
      if (this.commitCounter != null) {
         return this.commitCounter.getResult();
      }
      return 0;
   }

   public synchronized Map<String, VCSCommit> getVersions() {
      if (this.versionProvider != null) {
         return versionProvider.getVersions();
      }
      return null;
   }

   public synchronized Map<String, CommitEdits> getVersionChanges() {
      if (this.changes != null) {
         return this.changes.getVersionsChanges();
      }
      return null;
   }

   public synchronized Map<String, Set<CommitEdits>> getIntermediateChanges() {
      if (changes != null) {
         return changes.getResult();
      }
      return null;
   }
}
