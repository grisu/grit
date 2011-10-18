package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager
import grisu.model.status.StatusObject

import java.beans.PropertyChangeListener

import com.google.common.collect.Lists

@TestDocumentation(
description = 'This test archives a bunch of jobs and waits for the archiving task to be finished before archiving the next job within a run',
setupDescription = 'Creates the jobs according to the values specified in the config and keeps a list of all jobdirectories.',
tearDownDescription = 'Kills all jobs (just to be sure) and then deletes all archive directories.',
supportedParameters = []
)
class ArchiveJobTest extends AbstractTest implements Test, PropertyChangeListener {


	List archiveUrls = Collections.synchronizedList(Lists.newArrayList())
	List jobdirs = Collections.synchronizedList(Lists.newArrayList())
	def failedTests = []

	private final static Map ifiles = [:]

	public static void setupTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config)

		List files = config.get("inputfiles")

		ServiceInterface si = sis[0]

		addRunLog("Getting filesizes of input files...")
		for ( def tmp : files ) {
			def file = FileManager.getFilename(tmp)
			long size = si.getFileSize(tmp)
			ifiles[file] = size
			addRunLog("Filesize for "+file+": "+size)
		}
	}


	@Override
	protected void execute() {

		for ( JobObject job : jobs ) {

			addLog("Kicking off archiving of job...")
			def archiveUrl = job.archive()

			archiveUrls.add(archiveUrl)

			addLog("Waiting for archiving of job to finish.")
			StatusObject so =StatusObject.waitForActionToFinish(si, archiveUrl, 5, true, false)

			if (so.getStatus().isFailed()) {
				def ec = so.getStatus().getErrorCause()
				if ( ! ec ) {
					ec = "Archiving of job failed: unknown reason"
				} else {
					ec = "Archiving of job failed: "+ec
				}
				addLog(ec)
				throw new Exception(ec)
			} else {
				addLog("Archiving of job finished. Archive url: "+archiveUrl)
			}
		}
	}


	@Override
	protected void check() {

		for ( JobObject job : jobs ) {
			addLog ('Checking job: '+job.getJobname())
			addLog ('Checking whether all input files are listed in stdout...')
			for ( file in ifiles.keySet() ) {

				addLog("Checking filesize for file: "+file)
				def filename = FileManager.getFilename(file)
				long newSize = fm.getFileSize(job.getJobDirectoryUrl()+"/"+filename)
				if ( newSize != ifiles.get(file)) {
					throw new Exception("Filesize differs for source/jobinput file: "+filename)
				} else {
					addLog("Filesizes match.")
				}
			}

			addLog ("All files exist, test successful so far.")
			success = true
		}
		addLog("Checking whether old jobdirs are deleted...")
		jobdirs.each{ dir ->

			addLog("Checking whether jobdir still exists: "+dir)
			if ( fm.fileExists(dir) ) {
				failedTests.add(dir)
				addLog("Job dir still exists: "+dir)
			} else {
				addLog("Job dir deleted.")
			}
		}

		if ( failedTests ) {
			success = false
			addLog("Test not successful, jobdirectories left:")
			addLog("\t"+failedTests.join(" ,"))
		} else {
			success = true
			addLog("No jobdirectories left. Test successful.")
		}
	}

	@Override
	protected void tearDown() {

		killAllJobs()

		for ( String url : archiveUrls ) {

			addLog("Deleting: "+url)
			try {
				fm.deleteFile(url)
				addLog("Deleted: "+url)
			} catch (all) {
				addLog("Could not delete "+url+": "+all.getLocalizedMessage())
			}
		}
	}

	@Override
	protected void setup() {

		prepareAndSubmitAllJobs(true)

		jobs.each { job ->
			String jobdir = job.getJobDirectoryUrl()
			jobdirs.add(jobdir)
			addLog("Adding jobdir: "+jobdir)
		}

		addLog("All jobdirectories added.")
	}
}
