package grisu.tests.tests

import grisu.control.ServiceInterface
import grisu.frontend.model.job.JobObject
import grisu.tests.testRuns.TestRun

import com.google.common.collect.Maps

/**
 * Submits a bunch of jobs in serial, waits for them to finish and checks whether they they execute with status 0.
 * @author Markus Binsteiner
 *
 */
class GenericJobEndToEndTest extends AbstractTest implements Test {

	public static void setupTestRun(TestRun tr, Map config) {

		killAllJobsWithPrefix(tr, config.get("jobname_prefix"))
	}


	public static void teardownTestRun(TestRun tr, Map config) {

		killAllJobsWithPrefix(tr, config.get("jobname_prefix"))
	}


	public String stdout_matcher_string;


	@Override
	protected void execute() {

		Map exceptions = Collections.synchronizedMap(Maps.newHashMap())

		for ( final int i=1; i<=repetitions; i++ ) {

			addLog("Creating new job...")
			JobObject job = new JobObject(si)

			job.addPropertyChangeListener(this)

			job.setJobname(this.jobname_prefix+"_"+getCertName()+"_"+getParallelId()+"_j"+i)

			job.setApplication(application)
			job.setCommandline(commandline)

			if ( memory ) {
				job.setMemory(memory)
			}

			job.setSubmissionLocation(queue)

			for ( def inputfile : inputfiles) {
				job.addInputFileUrl(inputfile)
			}

			job.setWalltimeInSeconds(walltime)

			jobs.add(job)


			addLog("Creating job on backend...")
			job.createJob(group)

			addLog("Submitting job: "+job.getJobname())
			job.submitJob(true)
			addLog("Job submitted.")
			addLog("Waiting for job "+job.getJobname()+" to finish.")
			job.waitForJobToFinish(8)
			addLog("Job finished.")


			String output = job.getStdOutContent();
			if ( ! output.contains(stdout_matcher_string) ) {
				throw new RuntimeException("Job didn't produce expected result, couldn't find string: "+stdout_matcher_string)
			} else {
				addLog("Job executed successfully, found matcher string: "+stdout_matcher_string)
			}

			String jobDir = job.getJobDirectoryUrl()
			addLog("Killing job...")
			job.kill(true)
			addLog("Job killed, checking whether jobdirectory is gone...")
			if ( fm.fileExists(jobDir) ) {
				addLog("Job dir still exists...")
				throw new RuntimeException("Job directory still exists after cleaning.")
			} else {
				addLog("Job directory deleted successfully.")
			}
		}
	}

	@Override
	protected void check() {
	}

	@Override
	protected void tearDown() {
	}

	@Override
	protected void setup() {
	}
}
