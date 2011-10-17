package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.model.FileManager
import grisu.model.dto.DtoStringList
import grisu.model.status.StatusObject

import com.google.common.collect.Lists

/**
 * Submits a bunch of jobs in serial, waits for them to finish and checks whether they they execute with status 0.
 * @author Markus Binsteiner
 *
 */
class CleanJobsTest extends AbstractTest implements Test {

	public boolean kill_jobs_seperately = true

	def failedTests = []

	protected List jobdirs = Collections.synchronizedList(Lists.newArrayList())


	public static void setupTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config)
	}


	public static void teardownTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config)
	}


	protected void setup() {

		prepareAndSubmitAllJobs(false)

		addLog("Getting all jobdirectories...")

		jobs.each { job ->
			String jobdir = job.getJobDirectoryUrl()
			jobdirs.add(jobdir)
			addLog("Adding jobdir: "+jobdir)
		}

		addLog("All jobdirectories added.")
	}

	protected void tearDown() {
	}

	@Override
	protected void execute() {

		if ( kill_jobs_seperately ) {
			jobs.each { job ->
				addLog("Killing job: "+job.getJobname())
				job.kill(true)
			}
		} else {

			def jobnames = []
			jobs.each { job ->
				jobnames.add(job.getJobname())
			}
			def list = DtoStringList.fromStringColletion(jobnames)
			addLog("Killing all jobs...")
			String handle = si.killJobs(list, true)
			addLog("Waiting for killing of jobs to finish...")
			StatusObject so = StatusObject.waitForActionToFinish(si, handle, 4, false, false)
			addLog("Killing of jobs finished.")

			if ( so.getStatus().isFailed() ) {
				throw new Exception(so.getStatus().getErrorCause())
			}
		}
	}

	@Override
	protected void check() {


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
}
