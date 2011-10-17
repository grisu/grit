package grisu.tests.tests

import grisu.control.JobConstants
import grisu.control.ServiceInterface
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import com.google.common.collect.Lists
import com.google.common.collect.Maps

/**
 * Submits a bunch of jobs in serial, waits for them to finish and checks whether they they execute with status 0.
 * @author Markus Binsteiner
 *
 */
class GenericJobTest extends AbstractTest implements Test, PropertyChangeListener {

	public static void setupTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config.get("jobname_prefix"))
	}


	public static void teardownTestRun(List<ServiceInterface> sis, Map config) {

		killAllJobsWithPrefix(sis, config.get("jobname_prefix"))
	}


	List jobs = Collections.synchronizedList(Lists.newLinkedList())

	public  String jobname_prefix

	public int amount_jobs
	public String group
	public String application
	public String commandline
	public String queue
	public List inputfiles

	public boolean wait_for_job_to_finish_before_next_job_submit
	public boolean require_job_success

	public int walltime

	final FileManager fm


	@Override
	protected void execute() {

		for (def job : jobs) {
			addLog("Creating job...")
			job.createJob(group)

			addLog("Submitting job: "+job.getJobname())
			job.submitJob(true)
			addLog("Job submitted.")
			if ( wait_for_job_to_finish_before_next_job_submit ) {
				addLog("Waiting for job "+job.getJobname()+" to finish.")
				job.waitForJobToFinish(8)
				addLog("Job finished.")
			}
		}
	}

	@Override
	protected void check() {


		def failedJobs = []

		jobs.each{ job ->
			addLog("Waiting for job "+job.getJobname()+" to finish...")
			job.waitForJobToFinish(8)
			addLog("Getting status for job "+job.getJobname())
			int status = job.getStatus(false)
			addLog("Status for job "+job.getJobname()+": "+JobConstants.translateStatus(status))

			if ( status != JobConstants.DONE ) {
				failedJobs.add(job)
			}
		}


		if (require_job_success && failedJobs) {
			success = false
			addLog("Test failed: "+failedJobs.size())
		} else {
			success = true
			addLog("Test successful.")
		}
	}

	@Override
	protected void tearDown() {
	}

	@Override
	protected void setup() {


		Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

		for ( final int i=1; i<=amount_jobs; i++ ) {


			try {
				JobObject job = new JobObject(si)

				job.addPropertyChangeListener(this)

				job.setJobname(this.jobname_prefix+"_j"+i)

				job.setApplication(application)
				job.setCommandline('ls -la')

				for ( def inputfile : inputfiles) {
					job.addInputFileUrl(inputfile)
				}

				job.setWalltimeInSeconds(walltime)

				jobs.add(job)
			} catch (all) {
				exceptions.add(all)
			}
		}


		if ( exceptions ) {
			addLog ("At least one job creation failed: ")
			exceptions.each{ name, e ->
				addLog("\t"+name+": "+e.getLocalizedMessage())
				throw new Exception("Not all jobs could be created succesfully...")
			}
		}
	}

	void propertyChange(PropertyChangeEvent e) {

		if (!e.getPropertyName().equals("submissionLog") && !e.getPropertyName().equals("status") ) {
			addLog("Submission property "+e.getPropertyName()+" changed to "+e.getNewValue())
		}
	}
}
