package gr.uom.se.vcs.analysis;

import gr.uom.se.util.pattern.processor.AbstractProcessorQueue;
import gr.uom.se.util.pattern.processor.BlockingQueue;
import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.util.pattern.processor.SerialQueue;
import gr.uom.se.util.pattern.processor.ThreadQueueImp;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.analysis.util.CommitEdits;
import gr.uom.se.vcs.analysis.util.KeyValueProcessor;
import gr.uom.se.vcs.analysis.version.AuthorVersionProcessor;
import gr.uom.se.vcs.analysis.version.CommitFileChangeCounter;
import gr.uom.se.vcs.analysis.version.CommitVersionCounterProcessor;
import gr.uom.se.vcs.analysis.version.FirstAndSecondChangeCounter;
import gr.uom.se.vcs.analysis.version.TagVersionProvider;
import gr.uom.se.vcs.analysis.version.VersionChangeProcessor;
import gr.uom.se.vcs.analysis.version.VersionFileChangeCounter;
import gr.uom.se.vcs.analysis.version.VersionLinesCounterProcessor;
import gr.uom.se.vcs.analysis.version.VersionProvider;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.VCSRepositoryImp;
import gr.uom.se.vcs.walker.filter.VCSAndFilter;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.VCSNotFilter;
import gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsUseCase {

   /**
    * Path to test resources.
    * <p>
    */
   public static final String RESOURCES = "src/test/resources/";

   /**
    * Path to a small local git repository.
    * <p>
    */
   public static final String LOCAL_GIT_PATH = RESOURCES
         + "integration_rx_java";

   /**
    * Path to a small 'remote' git repository.
    * <p>
    */
   public static final String REMOTE_GIT_PATH = "https://github.com/Netflix/RxJava";

   /**
    * The repository under test.
    * <p>
    */
   public static VCSRepository repo = null;

   public MetricsUseCase() throws VCSRepositoryException, InterruptedException {
   }

   public static void main(String[] arg) throws VCSRepositoryException,
         InterruptedException, IOException {
      
      setUp();
      // These two use cases compute the same thing, but in a different way.
      // Test 1 uses available processors to compute a part of its data
      // and then to compute lines/files added/remove and so on, it does
      // this manually by the data that VersionChangeProcessor return.
      test1();
      // Test 2 compute all metrics using only processors, he avoids computing
      // directly its metrics from the data retained from VersionChangeProcessor
      // but uses some 'special' processors that can be inserted into version
      // change processor. Thus it will perform only one pass for all metrics
      test2();
   }

   public static void test1() throws InterruptedException,
         VCSRepositoryException {
      // Get the versionProvider from repo, and create a map between tag's
      // commit and tag name
      TagVersionProvider versionProvider = new TagVersionProvider(repo);

      // Create a commit processor to count commits per version
      final CommitVersionCounterProcessor cCounterPerV = new CommitVersionCounterProcessor(
            versionProvider, null/* id */);

      // Create a commit processor to collect authors per version
      final AuthorVersionProcessor authorsPerV = new AuthorVersionProcessor(
            versionProvider, null/* id */, true /* true for authors */);

      // Create a commit processor to collect commiters per version (the boolean
      // value must be false)
      final AuthorVersionProcessor committersPerV = new AuthorVersionProcessor(
            versionProvider, null /* id */, false /* false for committers */);

      // Create a counter processor to count all commits that will visit
      final CounterProcessor<VCSCommit> counter = new CounterProcessor<VCSCommit>();

      // Create a commit processor to collect the changes for each version
      /*
       * This processor can calculate two types of changes: 1 - The changes
       * between the commits of two subsequent versions (this is called version
       * changes). Which perform a diff on commit c1 that is the last commit of
       * the version v1 with commit c2 that is the last commit of v2 (this
       * commit is called version commit). The changes are calculated from older
       * to newer. 2 - The changes for each commit within a version (these
       * commits are called intermediate commits). Intermediate commits are
       * those that are made from first commit of the change until the last one
       * (the version commit, but not including it). Each type of this change is
       * calculated by diffing the commit with its previous commit. Doing so, we
       * can measure the changes within a version and can build various metrics
       * that shows up the state within a version. For example if we assume that
       * for each commit that adds a .java file there should be a
       * change/addition on a .java file that is in a path of a test directory
       * (measure the testability of the version), then we can measure these by
       * filtering the files that are added/modified on each commit (this is the
       * special case analyzed below)
       */
      /*
       * Here we can pass a filter if we want to allow our change processor to
       * calculate changes only for the types of resources this filter allows.
       * For example ResourceFilterUtility.suffix(".java") will allow the
       * calculating only for .java files. We leave this null so we can
       * calculate changes for every resource. You can put a filter after the
       * results are calculated (see the special case below), however defining a
       * filter at this stage will help the processing time, because the
       * processor will not calculate changes for files that the filter doesn't
       * allow.
       */
      final VersionChangeProcessor linesPerV = new VersionChangeProcessor(
            versionProvider, null /* id */, null /* change filter */, null /*
                                                                            * Resource
                                                                            * filter
                                                                            */,
            true /* calculate intermediate changes in each version */,
            true /* calculate changes for two subsequent versions */,
            (VCSChange.Type[]) null /* calculate all types of change */);

      // Create an analyzer to visit all commits and to run all processors
      CommitAnalyzer analyzer = Analyzer.<VCSCommit> builder()
            .addSerial(counter).       // count total number of commits in serial
            addParallel(cCounterPerV). // count commits per version in parallel
            addParallel(authorsPerV).  // collect authors per version in parallel
            addParallel(committersPerV).// collect committers per version in
                                        // parallel
            addParallel(linesPerV).    // collect all changes between two versions
            setThreads(8).             // the number of threads (default is 4)
            setTaskQueueSize(1000).   // the number of tasks before blocking
            build(CommitAnalyzer.class); // create the analyzer

      // Start visiting all commits
      long start = System.currentTimeMillis();

      // WARNING: this should be called before
      // the first use of this provider because
      // it will collect all commits of each
      // version from the repository.
      // However if you want to process only the differences
      // of two or more versions (see VersionChangeProcessor)
      // you do not need all the other commits, but the
      // version commits which are already stored in the provider.
      versionProvider.collectVersionInfo();

      // Should start the analyzer before walking anything
      analyzer.start();

      try {
         // An analyzer can be used as a visitor in different
         // VCS API methods that require a visitor, however
         // in this case we are using a version provider that
         // will load each version and their respective commits
         // in memory so we can get from provider each version and
         // then pass them to the analyzer
         // The provider here gives us an iterator on commits
         // that each one is a version (think of as the commits of the tags)
         for (VCSCommit version : versionProvider) {

            // We should process the version commit
            // because getting all commits for a given
            // version will return all commits, but the version commit
            // itself
            analyzer.process(version);

            // Now for the given version we are processing
            // all commits
            for (VCSCommit commit : versionProvider.getCommits(version)) {
               analyzer.process(commit);
            }
         }
      } finally {
         // Wee need to run the stop in try finally because
         // if a processor had an exception while running it will
         // be thrown at this moment not while running. And if an
         // exception is thrown then we risk to terminate main thread
         // and leave other threads running!!
         try {
            // Always stop the analyzer before getting the results
            analyzer.stop();
         } finally {
            // ALWAYS call shutdown if analyzer is running any of the processors
            // in parallel. Otherwise you will let other threads running.
            analyzer.shutDown();
         }
      }

      // Get the results from all processors
      Map<String, Integer> commitsCounter = cCounterPerV.getResult();
      Map<String, Set<String>> authorsPerVersion = authorsPerV.getResult();
      Map<String, Set<String>> committersPerVersion = committersPerV
            .getResult();
      Map<String, Set<CommitEdits>> intermediateChanges = linesPerV.getResult();
      Map<String, CommitEdits> versionChanges = linesPerV.getVersionsChanges();
      int commitCounter = 0;

      // Use version provider to iterate over versions as it gives
      // a sorted view of versions
      for (String ver : versionProvider.getVersionNames()) {

         // Count the number of processed commits so far
         commitCounter += commitsCounter.get(ver);

         // Print the number of commits for the current version
         System.out.format("%1s has %2s commits\n", ver,
               commitsCounter.get(ver));

         // Print the authors of current version
         printAuthors(authorsPerVersion, ver);

         printCommitters(committersPerVersion, ver);
         // Counting the lines added or deleted is time consuming
         // especially if we have a lot of added/removed files
         // because it need to load each file from disk and
         // check its contents by counting the lines
         printVersionChanges(versionChanges, ver);

         // THIS IS A SPECIAL CASE OF ANALYSIS
         // HERE WE WILL COUNT FOR EACH CHANGE (A COMMIT) THAT IS PERFORMED
         // PRIOR TO A VERSION (RELEASE) FOR EVERY JAVA FILE THAT IS ADDED
         // OR CHANGED AND TEST (JAVA FILE)
         // IN OTHER WORDS HOW IS THE TESTING OF THIS PROJECT DOING, USUALLY
         // FOR EACH SOURCE FILE THERE WOULD BE A UNIT TEST THAT TESTS
         // THE WELL FUNCTIONING OF THIS FILE
         Set<CommitEdits> edits = intermediateChanges.get(ver);

         // Will count for each new .java file if
         // a .java file in a test directory is added or modified!!!
         int filesAddedWithTest = 0;
         // Will count for each new .java file there is not any .java file
         // under a test directory that is added or modified
         int filesAddedNoTest = 0;
         // Will count the frequency of .java test files added or modified
         int testFilesAddedOrModified = 0;
         // Keep in mind that each CommitEdits is an object that describes
         // the changes of a commit. So here we are counting things for
         // each commit in version ver.

         for (CommitEdits edit : edits) {

            // Get java files added that are not test files
            int javaFilesAdd = edit.getNumberOfFilesWithChange(
                  VCSChange.Type.ADDED, javaNotTestFilter, null);

            // Get java files added that are test files
            int javaTestFilesAdd = edit.getNumberOfFilesWithChange(
                  VCSChange.Type.ADDED, javaTestFilter, null);

            // Get java files modified that are test files
            int javaTestFilesModified = edit.getNumberOfFilesWithChange(
                  VCSChange.Type.MODIFIED, javaTestFilter, null);

            // If java files added (not test files)
            // is not empty, we have some added java file
            if (javaFilesAdd > 0) {
               // If we have modified test files (or added) we increase the
               // counter
               if (javaTestFilesAdd > 0 || javaTestFilesModified > 0) {

                  filesAddedWithTest++;

               } else {
                  // We have java file added but not a test added/modified (BAD)
                  filesAddedNoTest++;
               }
            }

            // We have test files that are added so increase the counter
            if (javaTestFilesAdd > 0 || javaTestFilesModified > 0) {
               testFilesAddedOrModified++;
            }
         }

         if (filesAddedNoTest > 0 || filesAddedWithTest > 0) {
            System.out.format("Commits that adds java files with tests: %1s\n",
                  filesAddedWithTest);
            System.out.format("Commits that adds java files no tests: %1s\n",
                  filesAddedNoTest);
         }

         if (testFilesAddedOrModified > 0) {
            System.out.format("Commits that adds/modify test files: %1s\n",
                  testFilesAddedOrModified);
         }
      }
      long end = System.currentTimeMillis();

      System.out.format("Time: %1s\n", (end - start));
      System.out.format("Commits counted: %1s\n", commitCounter);
      System.out.format("Releases: %1s\n", authorsPerVersion.size());
   }

   @SuppressWarnings("unchecked")
   public static void test2() throws InterruptedException,
         VCSRepositoryException {
      // Get the versionProvider from repo, and create a map between tag's
      // commit and tag name
      TagVersionProvider versionProvider = new TagVersionProvider(repo);

      // Create a commit processor to count commits per version
      final CommitVersionCounterProcessor cCounterPerV = new CommitVersionCounterProcessor(
            versionProvider, null/* id */);

      // Create a commit processor to collect authors per version
      final AuthorVersionProcessor authorsPerV = new AuthorVersionProcessor(
            versionProvider, null/* id */, true /* true for authors */);

      // Create a commit processor to collect commiters per version (the boolean
      // value must be false)
      final AuthorVersionProcessor committersPerV = new AuthorVersionProcessor(
            versionProvider, null /* id */, false /* false for committers */);

      // Create a counter processor to count all commits that will visit
      final CounterProcessor<VCSCommit> counter = new CounterProcessor<VCSCommit>();

      // Create a processor to count added files per version
      final KeyValueProcessor<CommitEdits, String, AtomicInteger> filesAddedPerVersion = getVersionFileChangeCounter(
            versionProvider, null, null, VCSChange.Type.ADDED);

      // Create a processor to count deleted files per version
      final KeyValueProcessor<CommitEdits, String, AtomicInteger> filesDeletedPerVersion = getVersionFileChangeCounter(
            versionProvider, null, null, VCSChange.Type.DELETED);

      // Create a processor to count modified files per version
      final KeyValueProcessor<CommitEdits, String, AtomicInteger> filesModifiedPerVersion = getVersionFileChangeCounter(
            versionProvider, null, null, VCSChange.Type.MODIFIED);

      // Create a processor to count the new lines per version
      final KeyValueProcessor<CommitEdits, String, Integer> newLinesPerVersion = getVersionLinesCounter(
            versionProvider, true, null, null, (VCSChange.Type[]) null);

      // Create a processor to count the deleted lines per version
      final KeyValueProcessor<CommitEdits, String, Integer> oldLinesPerVersion = getVersionLinesCounter(
            versionProvider, false, null, null, (VCSChange.Type[]) null);
      
      // Create a processor to count the commits within a version that
      // add/modify a .java test
      // file
      final KeyValueProcessor<CommitEdits, String, AtomicInteger> testFilesPerCommitsInVersion = getCommitFileChangeCounter(
            versionProvider, null, javaTestFilter, VCSChange.Type.MODIFIED,
            VCSChange.Type.ADDED);

      // Create a processor to count the commits that add a .java (not in test
      // dir) but not a corresponding
      // test file (.java)
      final KeyValueProcessor<CommitEdits, String, AtomicInteger> javaFilesAddedNoTestInVersion = getFirstAndSecondChangeCounter(
            versionProvider, false, EnumSet.of(VCSChange.Type.ADDED), null,
            javaNotTestFilter,
            EnumSet.of(VCSChange.Type.ADDED, VCSChange.Type.MODIFIED), null,
            javaTestFilter);

      // Create a processor to count the commits that add a .java (not in test
      // dir) and a corresponding test file (.java) too
      final KeyValueProcessor<CommitEdits, String, AtomicInteger> javaFilesAddedWithTestInVersion = getFirstAndSecondChangeCounter(
            versionProvider, true, EnumSet.of(VCSChange.Type.ADDED), null,
            javaNotTestFilter,
            EnumSet.of(VCSChange.Type.ADDED, VCSChange.Type.MODIFIED), null,
            javaTestFilter);
   
      // Create a serial processor to add all KeyValue processors (they will be
      // called
      // by a change processor, because each of them deals with a CommitEdits
      // object
      // This is the first part of processors, that do not require I/O operations.
      // They will be run in parallel (2 threads mostly)
      
      ThreadQueueImp<CommitEdits> nonIOOperations = getBlockingParallelProcessor(2, 1000,
            filesAddedPerVersion, 
            filesDeletedPerVersion,
            filesModifiedPerVersion 
            , testFilesPerCommitsInVersion,
            javaFilesAddedNoTestInVersion, javaFilesAddedWithTestInVersion
            );

      // Previously we did not entered the newLines and oldLines processor in the same
      // queue, because it was serial, and these two operators requires to read from disks
      // the files that are deleted/added in order to count their lines, and this would
      // block other processors to process. We are separating non I/O processors from
      // I/O processors.
      ThreadQueueImp<CommitEdits> iOOperations = getBlockingParallelProcessor(2, 1000,
            newLinesPerVersion, oldLinesPerVersion);
      // Now we have the serial processor to run these two blocks of processors
      // and pass it to the change processor.
      Processor<CommitEdits> editsProcessor = getSerialProcessor(nonIOOperations, iOOperations);
      // Create a commit processor to collect the changes for each version
      /*
       * This processor can calculate two types of changes: 1 - The changes
       * between the commits of two subsequent versions (this is called version
       * changes). Which perform a diff on commit c1 that is the last commit of
       * the version v1 with commit c2 that is the last commit of v2 (this
       * commit is called version commit). The changes are calculated from older
       * to newer. 2 - The changes for each commit within a version (these
       * commits are called intermediate commits). Intermediate commits are
       * those that are made from first commit of the change until the last one
       * (the version commit, but not including it). Each type of this change is
       * calculated by diffing the commit with its previous commit. Doing so, we
       * can measure the changes within a version and can build various metrics
       * that shows up the state within a version. For example if we assume that
       * for each commit that adds a .java file there should be a
       * change/addition on a .java file that is in a path of a test directory
       * (measure the testability of the version), then we can measure these by
       * filtering the files that are added/modified on each commit (this is the
       * special case analyzed below)
       */
      /*
       * Here we can pass a filter if we want to allow our change processor to
       * calculate changes only for the types of resources this filter allows.
       * For example ResourceFilterUtility.suffix(".java") will allow the
       * calculating only for .java files. We leave this null so we can
       * calculate changes for every resource. You can put a filter after the
       * results are calculated (see the special case below), however defining a
       * filter at this stage will help the processing time, because the
       * processor will not calculate changes for files that the filter doesn't
       * allow.
       */
      // We should add the edits processor (the serial that we created
      // previously)
      // in version change processor

      final VersionChangeProcessor linesPerV = new VersionChangeProcessor(
            versionProvider, null /* id */, editsProcessor, null /*
                                                                  * change
                                                                  * filter
                                                                  */, null /*
                                                                            * Resource
                                                                            * filter
                                                                            */,
            true /* calculate intermediate changes in each version */,
            true /* calculate changes for two subsequent versions */,
            (VCSChange.Type[]) null /* calculate all types of change */);

      // Create an analyzer to visit all commits and to run all processors
      CommitAnalyzer analyzer = Analyzer.<VCSCommit> builder()
            .addSerial(counter).       // count total number of commits in serial
            addParallel(cCounterPerV). // count commits per version in parallel
            addParallel(authorsPerV).  // collect authors per version in parallel
            addParallel(committersPerV).// collect committers per version in
                                        // parallel
            addParallel(linesPerV).    // collect all changes between two versions
            setThreads(4).             // the number of threads (default is 4)
            setTaskQueueSize(100).   // the number of tasks before blocking
            build(CommitAnalyzer.class); // create the analyzer

      // Start visiting all commits
      long start = System.currentTimeMillis();

      // WARNING: this should be called before
      // the first use of this provider because
      // it will collect all commits of each
      // version from the repository.
      // However if you want to process only the differences
      // of two or more versions (see VersionChangeProcessor)
      // you do not need all the other commits, but the
      // version commits which are already stored in the provider.
      versionProvider.collectVersionInfo();

      // Should start the analyzer before walking anything
      analyzer.start();

      try {
         // An analyzer can be used as a visitor in different
         // VCS API methods that require a visitor, however
         // in this case we are using a version provider that
         // will load each version and their respective commits
         // in memory so we can get from provider each version and
         // then pass them to the analyzer
         // The provider here gives us an iterator on commits
         // that each one is a version (think of as the commits of the tags)
         for (VCSCommit version : versionProvider) {

            // We should process the version commit
            // because getting all commits for a given
            // version will return all commits, but the version commit
            // itself
            analyzer.process(version);

            // Now for the given version we are processing
            // all commits
            for (VCSCommit commit : versionProvider.getCommits(version)) {
               analyzer.process(commit);
            }
         }
      } finally {
         // Wee need to run the stop in try finally because
         // if a processor had an exception while running it will
         // be thrown at this moment not while running. And if an
         // exception is thrown then we risk to terminate main thread
         // and leave other threads running!!
         try {
            // Always stop the analyzer before getting the results
            analyzer.stop();
         } finally {
            // ALWAYS call shutdown if analyzer is running any of the processors
            // in parallel. Otherwise you will let other threads running.
            analyzer.shutDown();
            nonIOOperations.shutdown();
            iOOperations.shutdown();
         }
      }

      // Get the results from all processors
      Map<String, Integer> commitsCounter = cCounterPerV.getResult();
      Map<String, Set<String>> authorsPerVersion = authorsPerV.getResult();
      Map<String, Set<String>> committersPerVersion = committersPerV
            .getResult();
      
      int commitCounter = 0;
      int offset = findLargestVersionName(versionProvider);
      printRepeat(" ", offset);
      printHead();
      // Use version provider to iterate over versions as it gives
      // a sorted view of versions
      for (String ver : versionProvider.getVersionNames()) {

         // Count the number of processed commits so far
         commitCounter += commitsCounter.get(ver);

         // Print the number of commits for the current version
         //System.out.format("%1s has %2s commits\n", ver,
         //      commitsCounter.get(ver));
         System.out.print(ver);
         printRepeat(" ", offset - ver.length());
         printValues(commitsCounter.get(ver),
               authorsPerVersion.get(ver).size(),
               committersPerVersion.get(ver).size(),
               filesAddedPerVersion.getValue(ver).get(),
               filesDeletedPerVersion.getValue(ver).get(),
               filesModifiedPerVersion.getValue(ver).get(),
               newLinesPerVersion.getValue(ver),
               oldLinesPerVersion.getValue(ver),
               javaFilesAddedWithTestInVersion.getValue(ver).get(),
               javaFilesAddedNoTestInVersion.getValue(ver).get(),
               testFilesPerCommitsInVersion.getValue(ver).get()
               );
         System.out.println();
      }

      long end = System.currentTimeMillis();

      System.out.format("Time: %1s\n", (end - start));
      System.out.format("Commits counted: %1s\n", commitCounter);
      System.out.format("Releases: %1s\n", authorsPerVersion.size());
   }

   static void printHead() {
      System.out.println(
            "\tCOMMITS\tAUTHORS\tCOMMITTERS\tADDED FILES\tDELETED FILES\tMODIFIED FILES\tADDED LINES\tDELETED LINES\tSOURCE TEST\tSOURCE NO TEST\tTESTS");
   }
   static void printValues(Number... vals) {
      System.out.format(
            "\t%7d\t%7d\t%10d\t%11d\t%13d\t%14d\t%11d\t%13d\t%11d\t%14d\t%5d", (Object[])vals);
   }
   static void printRepeat(String str, int num) {
      for(int i = 0; i < num; i++) {
         System.out.print(str);
      }
   }
   static int findLargestVersionName(VersionProvider provider) {
      int maxLen = 0;
      for(String name : provider.getVersionNames()) {
         int len = name.length();
         if(len > maxLen) {
            maxLen = len;
         }
      }
      return maxLen;
   }
   
   // THE FOLLOWING FILTERS WILL BE USED TO GATHER INFO ONLY FOR
   // .JAVA FILES AND IN A SPECIAL CASE COVERED IN TEST METHOD.
   // We need a .java filter
   // We use the utility for this because we have already a filter
   // for that job
   final static VCSResourceFilter<VCSFile> javaFilter = ResourceFilterUtility
         .suffix(".java");
   // We need a path filter so we can check if a file has in its path
   // a directory /test/. We do not have one implemented but will
   // implement it here
   final static VCSResourceFilter<VCSFile> testDirFilter = new VCSResourceFilter<VCSFile>() {

      @Override
      public boolean include(VCSFile entity) {
         VCSResource parent = entity.getParent();
         while (parent != null) {
            if (parent.getPath().toLowerCase().endsWith("/test")) {
               return true;
            }
            parent = parent.getParent();
         }
         return false;
      }

      @Override
      public boolean enter(VCSFile resource) {
         // nothing to do here
         return true;
      }
   };
   // Here we construct a mixed filter for .java files under some /test/
   // directories
   @SuppressWarnings("unchecked")
   final static VCSFilter<VCSFile> javaTestFilter = ResourceFilterUtility.and(
         javaFilter, testDirFilter);
   // Here we have a filter for .java files that are not under some /test/
   // directory. Because resource filter utility doesn't support NOT filter
   // we construct one on the fly
   
   @SuppressWarnings("unchecked")
   final static VCSFilter<VCSFile> javaNotTestFilter = new VCSAndFilter<VCSFile>(
         Arrays.asList(new VCSNotFilter<VCSFile>(testDirFilter), javaFilter));

   static void printAuthors(Map<String, Set<String>> authorsPerVersion,
         String ver) {
      Set<String> set = authorsPerVersion.get(ver);
      System.out.format("Authors: %1s\n", set.size());
      for (String author : set) {
         System.out.format(" - %1s\n", author);
      }
   }

   static void printCommitters(Map<String, Set<String>> committersPerVersion,
         String ver) {
      Set<String> set = committersPerVersion.get(ver);
      System.out.format("Committers: %1s\n", set.size());
      for (String committer : set) {
         System.out.format(" - %1s\n", committer);
      }
   }

   static void printVersionChanges(Map<String, CommitEdits> versionChanges,
         String ver) {
      // Here we are inspecting the changes of the current version from
      // previous version, which are calculated by diffing the current
      // version
      // commit with its previous version commit. All intermediate changes
      // that
      // each commit made starting from previous version until this version
      // are not printed here. However you can use changesBetweenVersions
      // (see up where all result variables are declared)

      System.out.println("Changes from previous version:");
      CommitEdits ce = versionChanges.get(ver);
      // Check here in case this is the first version and has no previous
      // version
      // to calculate changes
      if (ce != null) {
         System.out.format("ADDED FILES: %1s\n",
               ce.getNumberOfFilesWithChange(VCSChange.Type.ADDED));
         System.out.format("DELETED FILES: %1s\n",
               ce.getNumberOfFilesWithChange(VCSChange.Type.DELETED));
         System.out.format("MODIFIED FILES: %1s\n",
               ce.getNumberOfFilesWithChange(VCSChange.Type.MODIFIED));

         // Now we will print the new and old lines
         System.out.format("Added Lines: %1s\n", ce.getNoNewLines());
         System.out.format("Deleted lines: %1s\n", ce.getNoOldLines());
      }
   }

   static KeyValueProcessor<CommitEdits, String, AtomicInteger> getVersionFileChangeCounter(
         VersionProvider provider, VCSFilter<VCSFileDiff<?>> changeFilter,
         VCSFilter<VCSFile> fileFilter, VCSChange.Type... types) {
      return new VersionFileChangeCounter(provider, null, changeFilter,
            fileFilter, types);
   }

   static KeyValueProcessor<CommitEdits, String, Integer> getVersionLinesCounter(
         VersionProvider provider, boolean newLines,
         VCSFilter<VCSFileDiff<?>> changeFilter,
         VCSFilter<VCSFile> resourceFilter, VCSChange.Type... types) {
      return new VersionLinesCounterProcessor(provider, null, newLines,
            changeFilter, resourceFilter, types);
   }

   static KeyValueProcessor<CommitEdits, String, AtomicInteger> getCommitFileChangeCounter(
         VersionProvider provider, VCSFilter<VCSFileDiff<?>> changeFilter,
         VCSFilter<VCSFile> resourceFilter, VCSChange.Type... types) {
      return new CommitFileChangeCounter(provider, null, changeFilter,
            resourceFilter, types);
   }

   static KeyValueProcessor<CommitEdits, String, AtomicInteger> getFirstAndSecondChangeCounter(
         VersionProvider provider, boolean changed, Set<VCSChange.Type> types1,
         VCSFilter<VCSFileDiff<?>> changeFilter1,
         VCSFilter<VCSFile> resourceFilter1, Set<VCSChange.Type> types2,
         VCSFilter<VCSFileDiff<?>> changeFilter2,
         VCSFilter<VCSFile> resourceFilter2) {

      return new FirstAndSecondChangeCounter(provider, null, changed, types1,
            changeFilter1, resourceFilter1, types2, changeFilter2,
            resourceFilter2);
   }

   static <T> AbstractProcessorQueue<T> getSerialProcessor(
         Processor<T>... processors) {
      SerialQueue<T> serial = new SerialQueue<T>();
      for (Processor<T> p : processors) {
         serial.add(p);
      }
      return serial;
   }

   static <T> ThreadQueueImp<T> getBlockingParallelProcessor(
         int threads, int tasks, Processor<T>... processors) {
      BlockingQueue<T> queue = new BlockingQueue<T>(threads, tasks, null);
      for (Processor<T> p : processors) {
         queue.add(p);
      }
      return queue;
   }

   public static void setUp() throws VCSRepositoryException {

      VCSRepository repo = new VCSRepositoryImp(LOCAL_GIT_PATH, REMOTE_GIT_PATH);
      File directory = new File(LOCAL_GIT_PATH);

      // Case there is not a .git directory in the given path
      if (!VCSRepositoryImp.containsGitDir(LOCAL_GIT_PATH)) {

         // Check directory path for access
         if (directory.exists()) {

            // All cases when there is a problem in cloning the new
            // repository, no test will perform if the directory can not
            // be cleaned, so we need to manually clean the directory
            if (!directory.isDirectory()) {

               throw new IllegalStateException("path at: " + LOCAL_GIT_PATH
                     + " is not a directory");

            } else if (directory.listFiles().length > 0) {

               throw new IllegalStateException("can not proceed to "
                     + LOCAL_GIT_PATH + ", directory is not empty");

            } else if (!(directory.canRead() || directory.canWrite())) {

               throw new IllegalStateException(
                     "can not access directory at path: " + LOCAL_GIT_PATH);
            }
         }
         // Try to download the repository
         System.out.format("Downloading repository from: %1s please wait...\n",
               REMOTE_GIT_PATH);
         long start = System.currentTimeMillis();
         repo.cloneRemote();
         System.out.format("Repository downloaded at %1s sec(s)\n",
               (int) (((System.currentTimeMillis() - start) / 1000) + 0.5));

         // A .git directory is present, however we should update the repository
      } else {

         // Try to update the repository
         System.out.format("Updating repository from: %1s please wait...\n",
               REMOTE_GIT_PATH);
         long start = System.currentTimeMillis();
         repo.update();
         System.out.format("Repository updated at %1s sec(s)\n",
               (int) (((System.currentTimeMillis() - start) / 1000) + 0.5));
      }

      MetricsUseCase.repo = repo;
   }
}
