package grisu.tests.tests

import grisu.control.JobConstants
import grisu.control.ServiceInterface
import grisu.frontend.model.job.JobObject
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.model.dto.DtoStringList
import grisu.model.status.StatusObject

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import com.google.common.collect.Lists
import com.google.common.collect.Maps

/**
 * Submits a bunch of jobs in serial, waits for them to finish and checks whether they they execute with status 0.
 * @author Markus Binsteiner
 *
 */
class GenericJobTest extends AbstractTest implements Test, PropertyChangeListener {

	List jobs = Collections.synchronizedList(Lists.newLinkedList())
	private final jobname_prefix

	private final int amount_jobs
	final String group
	final String application
	final String commandline
	final String queue
	final List inputfiles

	final boolean wait_for_job_to_finish_before_next_job_submit
	final boolean require_job_success
	final int concurrent_job_creations

	final int walltime

	final FileManager fm

	public GenericJobTest(ServiceInterface si, int batch, int id, int amount_jobs_in_serial, String group, String application, String commandline, int walltime, String queue,
	List inputFiles, boolean wait_for_job_to_finish_before_next_job_submit, boolean require_job_success, int concurrent_job_creations) {
		super(si, batch, id)
		fm = GrisuRegistryManager.getDefault(si).getFileManager()

		this.amount_jobs = amount_jobs_in_serial
		this.inputfiles = inputFiles
		this.group = group
		this.jobname_prefix = 'genericTestJob_'+batch+'_'+id
		this.walltime = walltime
		this.wait_for_job_to_finish_before_next_job_submit = wait_for_job_to_finish_before_next_job_submit
		this.require_job_success = require_job_success
		this.concurrent_job_creations = concurrent_job_creations
	}

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

		addLog("Killing jobs...")
		def toKill = []
		jobs.each{ job ->
			try {
				job.kill(true)
			} catch (all) {
				addLog("Failed to kill job "+job.getJobname()+": "+all.getLocalizedMessage())
			}
		}

		addLog("All jobs killed")
	}

	@Override
	protected void setup() {

		addLog("Killing potentially existing jobs...")
		def toKill = []
		for (def jobname in si.getAllJobnames(null).asSortedSet()) {
			if (jobname.startsWith(jobname_prefix)) {
				toKill.add(jobname)
			}
		}

		if ( toKill) {
			String handle = si.killJobs(DtoStringList.fromStringColletion(toKill), true)
			StatusObject so = StatusObject.waitForActionToFinish(si, handle, 4, false, false)
		}

		def pool = Executors.newFixedThreadPool(concurrent_job_creations)

		Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

		for ( i in 0..<amount_jobs ) {

			Thread t = new Thread() {
						public void run() {

							try {
								JobObject job = new JobObject(si)

								job.addPropertyChangeListener(GenericJobTest.this)

								job.setJobname(GenericJobTest.this.jobname_prefix+"_j"+i)

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
					}

			pool.execute(t)
		}

		pool.shutdown();
		pool.awaitTermination(10, TimeUnit.HOURS)

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
