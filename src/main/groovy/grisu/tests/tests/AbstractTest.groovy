package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.frontend.model.job.JobObject
import grisu.jcommons.constants.Constants
import grisu.model.FileManager
import grisu.model.GrisuRegistryManager
import grisu.model.info.dto.DtoStringList;
import grisu.model.status.StatusObject
import grisu.tests.testRuns.TestRun

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.List
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.apache.log4j.Logger

import com.google.common.collect.Lists
import com.google.common.collect.Maps

abstract class AbstractTest implements Test, PropertyChangeListener {

	public static String DEFAULT_TEST_JOB_NAME = "tests_job"

	Logger myLogger = Logger.getLogger(AbstractTest.class.getName())

	protected void addLog(String msg) {
		tr.addTestLog(this, msg)
	}

	protected void addRunLog(String msg) {
		tr.addRunLog(this.msg)
	}

	public void setTestRun(TestRun tr) {
		this.tr = tr
	}

	public TestRun getTestRun() {
		return this.tr
	}

	public static final JOB_CREATION_PARAMETERS = [
		'group',
		'application',
		'commandline',
		'queue',
		'inputfiles',
		'walltime'
	]

	public ServiceInterface si
	public FileManager fm
	private String cert_name
	private int parallel

	private TestRun tr

	protected Date startedTime
	protected Date finishedTime
	protected long duration = 0L

	protected boolean success = false
	protected boolean success_check = false

	protected String check_comment = 'n/a'

	public String getCheckComment() {
		return check_comment
	}

	Exception executeException = null
	Exception setupException = null
	Exception checkException = null
	Exception tearDownException = null

	public Exception getCheckException() {
		return checkException
	}

	public Exception getExecuteException() {
		return executeException;
	}


	protected List jobs = Collections.synchronizedList(Lists.newArrayList())
	protected List jobnames = Collections.synchronizedList(Lists.newArrayList())

	public jobname_prefix = DEFAULT_TEST_JOB_NAME

	public int repetitions = 1
	public String group = "/nz/nesi"
	public String application = "UnixCommands"
	public String commandline = "echo hello world"
	public String queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING;
	public List inputfiles = []

	public boolean wait_for_jobs_to_finish = true
	public int concurrent_submissions = 1
	public int concurrent_creations = 1

	public boolean reuse_existing_jobs = false

	public long memory
	public int walltime = 60

	public static void killAllJobsWithPrefix(TestRun tr, Map config) {

		def reuse = config.get("reuse_existing_jobs")
		if (reuse) {
			tr.addRunLog("Not killing existing jobs because 'reuse_existing_jobs' flag is set.")
			return
		}

		String prefix = config.get("jobname_prefix")
		if (! prefix) {
			prefix = DEFAULT_TEST_JOB_NAME
		}
		killAllJobsWithPrefix(tr, prefix)
	}

	public static void killAllJobsWithPrefix(TestRun tr, String prefix) {
		tr.getServiceInterfaces().each { name, si ->
			tr.addRunLog('Credential '+name+': ')
			killAllJobsWithPrefixPr(si, tr, prefix)
		}
	}

	private static void killAllJobsWithPrefixPr(ServiceInterface si, TestRun tr, String prefix) {

		tr.addRunLog("Checking whether there are jobs to kill...")


		def toKill = []
		for (def jobname in si.getAllJobnames(null).asSortedSet()) {
			if (jobname.startsWith(prefix)) {
				toKill.add(jobname)
			}
		}

		if ( toKill) {
			tr.addRunLog("Killing "+toKill.size()+" jobs...")
			String handle = si.killJobs(DtoStringList.fromStringColletion(toKill), true)
			tr.addRunLog("Waiting for killing of jobs...")
			StatusObject so = StatusObject.waitForActionToFinish(si, handle, 4, false)
			tr.addRunLog("Killing of jobs finished.")
			if ( so.getStatus().isFailed() ) {
				tr.addRunLog("Error when trying to kill jobs: "+so.getStatus().getErrorCause())
				throw new Exception(so.getStatus().getErrorCause())
			} else {
				tr.addRunLog("Jobs killed succesfully.")
			}
		} else {
			tr.addRunLog("No jobs to be killed...")
		}
	}

	public void killAllJobs() {
		if ( ! reuse_existing_jobs ) {
			killAllJobsWithPrefix(si, this.jobname_prefix)
		} else {
			addLog("Not killing existing jobs because 'reuse_existing_jobs' flag is set.")
		}
	}

	protected JobObject prepareNewJob(String jobname) {

		addLog("Preparing "+jobname+"...")
		final JobObject job = new JobObject(si)

		job.addPropertyChangeListener(this)

		job.setJobname(jobname)

		job.setApplication(application)
		job.setCommandline(commandline)

		job.setSubmissionLocation(queue)

		for ( def inputfile : inputfiles) {
			job.addInputFileUrl(inputfile)
		}

		job.setWalltimeInSeconds(walltime)
		jobs.add(job)
		addLog("Job "+jobname+" prepared.")
	}

	protected void createJobOnBackend(JobObject job) {
		addLog("Creating job: "+job.getJobname())
		try {
			String jobname = job.createJob(group)
			addLog("Job created, final jobname: "+jobname)
			jobnames.add(jobname)
		} catch (all) {
			addLog("Could not create job "+job.getJobname())+": "+all.getLocalizedMessage()
			throw all
		}

		addLog("Created job: "+job.getJobname()+".")
	}

	protected void submitJob(JobObject job) {

		addLog("Submitting job "+job.getJobname()+"...")
		try {
			job.submitJob(true)
		} catch (all) {
			addLog("Could not submit job "+job.getJobname()+": "+all.getLocalizedMessage())
			throw all
		}
		addLog("Job submitted: "+job.getJobname())
	}

	/**
	 * Used to prepare all JobObjects for this test.
	 *
	 * Only call this once!
	 */
	protected void prepareAllJobs() {

		for ( int index=0; index<repetitions; index++ ) {

			prepareNewJob(this.jobname_prefix+"_"+getCertName()+"_"+parallelId+"_j"+index)
		}
	}

	/**
	 * Convenience method to prepare, create and submit all jobs for this test using the "concurrent_job_submissions" property of the test.
	 */
	protected void prepareAndSubmitAllJobs(boolean wait) {

		if ( reuse_existing_jobs ) {

			for (def jobname in si.getAllJobnames(null).asSortedSet()) {
				if (jobname.startsWith(jobname_prefix+"_"+getCertName()+"_"+getParallelId())) {
					addLog("Found existing job: "+jobname+". Adding it to this testrun...")
					JobObject job = new JobObject(si, jobname)
					jobs.add(job)
					jobnames.add(job.getJobname())
				}
			}
		} else {

			prepareAllJobs()

			def pool = Executors.newFixedThreadPool(concurrent_submissions)

			Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

			for ( final JobObject tmp : jobs ) {

				final JobObject job = tmp

				Thread t= new Thread() {
							public void run() {

								try {
									createJobOnBackend(job)
									submitJob(job)
								} catch (all) {
									addLog("Error creating "+job.getJobname()+": "+all.getLocalizedMessage())
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
				addLog ("At least one job creation/submission failed: ")
				exceptions.each{ name, e ->
					addLog("\t"+name+": "+e.getLocalizedMessage())
					throw new Exception("Not all jobs could be created/submitted succesfully...")
				}
			}
		}

		if (wait) {
			waitForJobsToFinish()
		}
	}

	protected void waitForJobsToFinish() {
		jobs.each { job ->
			addLog("Waiting for job to finish: "+job.getJobname())
			job.waitForJobToFinish(4)
		}
	}

	protected void createAllJobs() {

		def pool = Executors.newFixedThreadPool(concurrent_creations)

		Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

		for ( final JobObject tmp : jobs ) {

			final JobObject job = tmp

			Thread t= new Thread() {
						public void run() {

							try {
								createJobOnBackend(job)
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
			addLog ("At least one job creation failed: ")
			exceptions.each{ name, e ->
				addLog("\t"+name+": "+e.getLocalizedMessage())
				throw new Exception("Not all jobs could be created succesfully...")
			}
		}
	}

	protected void submitAllJobs() {

		def pool = Executors.newFixedThreadPool(concurrent_submissions)

		Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

		for ( final JobObject tmp : jobs ) {

			final JobObject job = tmp

			Thread t= new Thread() {
						public void run() {

							try {
								submitJob(job)
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
			addLog ("At least one job submission failed: ")
			exceptions.each{ name, e ->
				addLog("\t"+name+": "+e.getLocalizedMessage())
				throw new Exception("Not all jobs could be submitted succesfully...")
			}
		}
	}

	public void setServiceInterface(ServiceInterface si) {
		this.si = si
		this.fm = GrisuRegistryManager.getDefault(si).getFileManager()
	}

	public void setCertName(String id) {
		this.cert_name = id
	}

	public void setParallelId(int id) {
		this.parallel = id
	}

	public int getParallelId() {
		return this.parallel
	}

	public String getCertName() {
		return this.cert_name
	}

	/**
	 * The execution of the test.
	 */
	abstract protected void execute()
	/**
	 * Checks whether the test was successful.
	 *
	 * Needs to the {@link #success} variable to "true" if it was and an exception if it wasn't...
	 */
	abstract protected void check()
	/**
	 * Tears down the test.
	 */
	abstract protected void tearDown()
	/**
	 * Sets up the test.
	 */
	abstract protected void setup()

	public boolean getSuccess() {
		return this.success
	}

	public String getName() {
		return getCertName()+" / "+parallel;
	}

	public ServiceInterface getServiceInterface() {
		return si
	}


	public void tearDownTest() {
		myLogger.debug("Tearing down test: "+getCertName()+" / "+parallel)
		try {
			addLog "Start tearDown..."
			tearDown()
			addLog "Finished tearDown."
			myLogger.debug("Tearing down test "+getCertName()+" / "+parallel+" successful.")
		} catch (all) {
			addLog "TearDown error: "+all.getLocalizedMessage()
			myLogger.debug("Tearing down test "+getCertName()+" / "+parallel+" error: "+all.getLocalizedMessage())
			tearDownException = all
		}
	}

	public void setupTest() {
		myLogger.debug("Setting up test: "+getCertName()+" / "+parallel)
		addLog "Start setup..."
		try {
			setup()
			addLog "Finished setup."
			myLogger.debug("Setting up test "+getCertName()+" / "+parallel+" successful.")
		} catch (all) {
			addLog "Setup error: "+all.getLocalizedMessage()
			myLogger.debug("Setting up test "+getCertName()+" / "+parallel+" error: "+all.getLocalizedMessage())
			setupException = all
		}
	}


	void checkTest() {
		myLogger.debug("Checking test: "+getCertName()+" / "+parallel)

		if (setupException || executeException ) {
			myLogger.debug("Not checking test: "+getCertName()+" /"+parallel)
			addLog("Not checking test since setup or execution exception exist...")
			return
		}

		if (!success) {
			myLogger.debug("Not checking test: "+getCertName()+" /"+parallel)
			addLog("Not checking test since it didn't run successfully...")
			return
		}

		addLog "Start check..."
		try {
			check()
			addLog "Finished check."
			if ( success ) {
				myLogger.debug("Checking test "+getCertName()+" / "+parallel+" successful.")
				success_check = true
				tr.testChecked(this, true)
			} else {
				myLogger.debug("Checking test "+getCertName()+" / "+parallel+" showed error.")
				success_check = false
				tr.testChecked(this, false)
			}
		} catch (all) {
			addLog "Check error: "+all.getLocalizedMessage()
			myLogger.debug("Checking test "+getCertName()+" / "+parallel+" error: "+all.getLocalizedMessage())
			checkException = all
			success_check = false
			tr.testChecked(this, false)
		}
	}

	void executeTest() {

		if ( setupException ) {
			addLog("Not starting test, setup failed...")
			return
		}

		myLogger.debug("Starting test: "+getCertName()+" / "+parallel)

		try {

			startedTime = new Date()
			addLog "Start test..."

			execute()
			addLog "Finished test."
			finishedTime = new Date()
			this.success = true
			myLogger.debug("Finished test successfully: "+getCertName()+" / "+parallel)
			duration = finishedTime.getTime()-startedTime.getTime()
			addLog("Duration of test execution: "+duration+" ms")
			tr.testExecuted(this, true)
		} catch(all) {
			addLog "Test error: "+all.getLocalizedMessage()
			finishedTime = new Date()
			executeException = all
			myLogger.debug("Finished test "+getCertName()+" / "+parallel+ " with error: "+all.getLocalizedMessage())
			duration = finishedTime.getTime()-startedTime.getTime()
			addLog("Duration of test execution: "+duration+" ms")
			tr.testExecuted(this, false)
		} finally {
		}
	}

	public String getResult() {
		String r = "("+ getCertName() + " / "+parallel+"):\n"
		r += "\tSuccess: "+ wasSuccessful() + "\n"
		r += "\tDuration: "+duration+" ms" + "\n"
		r += "\tLog:" + "\n"
		for ( l in getLog() ) {
			r += "\t\t"+l.key +": "+l.value +"\n"
		}
		return r
	}

	public String getErrorMessage() {
		if ( executeException ) {
			return executeException.getLocalizedMessage()
		} else {
			return ""
		}
	}

	boolean isFinished() {
		return finishedTime != null
	}

	boolean isStarted() {
		return startedTime != null
	}

	boolean wasSuccessful() {
		return ( this.success && this.success_check )
	}

	void propertyChange(PropertyChangeEvent e) {

		if (!e.getPropertyName().equals("submissionLog") && !e.getPropertyName().equals("status") ) {
			addLog("Submission property "+e.getPropertyName()+" changed to "+e.getNewValue())
		}
	}
}
