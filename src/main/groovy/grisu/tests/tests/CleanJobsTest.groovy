package grisu.tests.tests

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
class CleanJobsTest extends AbstractTest implements Test, PropertyChangeListener {

	List jobs = Collections.synchronizedList(Lists.newArrayList())
	List jobdirs = Collections.synchronizedList(Lists.newArrayList())
	private final jobname_prefix

	private final int amount_jobs
	final String group
	final String application
	final String commandline
	final String queue
	final List inputfiles

	int concurrentJobSubmissions = 1

	final int walltime

	final FileManager fm

	def failedTests = []



	public CleanJobsTest(ServiceInterface si, int batch, int id, int amount_jobs_in_serial, String group, String application, String commandline, int walltime, String queue,
	List inputFiles, int concurrentJobSubmissions) {
		super(si, batch, id)
		fm = GrisuRegistryManager.getDefault(si).getFileManager()

		this.amount_jobs = amount_jobs_in_serial
		this.inputfiles = inputFiles
		this.group = group
		this.jobname_prefix = 'deleteJobsTest_'+batch+'_'+id
		this.walltime = walltime
		this.concurrentJobSubmissions = concurrentJobSubmissions
	}

	@Override
	protected void execute() {

		jobs.each { job ->
			addLog("Killing job: "+job.getJobname())
			job.kill(true)
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

	@Override
	protected void tearDown() {

		if (!success) {
			tests.each { job ->
				addLog("Killing job "+job.getJobname())
				try {
					job.kill(true)
					addLog("Job killed.")
				} catch (all) {
					addLog("Couldn't kill job: "+all.getLocalizedMessage())
				}
			}
		}

		failedTests.each { dir ->
			addLog("Trying to delete remaining jobdir: "+dir)
			try {
				fm.deleteFile(dir)
				addLog("Remaining dir deleted.")
			} catch (all) {
				addLog("Deleting dir failed: "+all.getLocalizedMessage())
			}
		}
	}

	@Override
	protected void setup() {

		addLog("Killing potentially existing jobs...")
		def toKill = []
		for (def jobname in si.getAllJobnames(null).asSortedSet()) {
			if (jobname.startsWith(this.jobname_prefix)) {
				toKill.add(jobname)
			}
		}

		if ( toKill) {
			String handle = si.killJobs(DtoStringList.fromStringColletion(toKill), true)
			addLog("Waiting for killing of jobs...")
			StatusObject so = StatusObject.waitForActionToFinish(si, handle, 4, false, false)
			addLog("Killing of jobs finished.")
		}

		def pool = Executors.newFixedThreadPool(concurrentJobSubmissions)

		Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

		for ( int index=0; index<amount_jobs; index++ ) {

			final JobObject job = new JobObject(si)

			job.addPropertyChangeListener(this)

			job.setJobname(this.jobname_prefix+"_j"+index)
			jobs.add(job)

			Thread t= new Thread() {
						public void run() {

							try {
								job.setApplication(application)
								job.setCommandline('ls -la')

								for ( def inputfile : inputfiles) {
									job.addInputFileUrl(inputfile)
								}

								job.setWalltimeInSeconds(walltime)
								addLog("Creating job: "+job.getJobname())
								job.createJob(group)

								addLog("Submitting job...")
								job.submitJob(true)
								addLog("Job submitted.")
								jobdirs.add(job.getJobDirectoryUrl())
							} catch (all) {
								exceptions.put(job.getJobname(), all)
								pool.shutdownNow()
							}
						}
					}
			pool.execute(t);
		}

		pool.shutdown();
		pool.awaitTermination(10, TimeUnit.HOURS)

		if ( exceptions ) {
			addLog ("At least one jobsubmission failed: ")
			exceptions.each{ name, e ->
				addLog("\t"+name+": "+e.getLocalizedMessage())
				throw new Exception("Not all jobs could be submitted succesfully...")
			}
		}

		jobs.each { job ->
			addLog("Waiting for job to finish: "+job.getJobname())
			job.waitForJobToFinish(4)
		}
	}

	void propertyChange(PropertyChangeEvent e) {

		if (!e.getPropertyName().equals("submissionLog") && !e.getPropertyName().equals("status") ) {
			addLog("Submission property "+e.getPropertyName()+" changed to "+e.getNewValue())
		}
	}
}
