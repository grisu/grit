package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.CleanJobsTest
import grisu.tests.tests.Test

class CleanJobsTestRun extends AbstractTestRun implements TestRun {

	public int amount_jobs
	public String group
	public List inputfiles
	public String application
	public String commandline
	public String queue
	public boolean wait_for_job_to_finish_before_next_job_submit
	public int walltime
	public boolean require_job_success
	public int concurrent_submissions

	public void setup() {
	}

	public void tearDown() {
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new CleanJobsTest(si, batch, id, amount_jobs, group, application, commandline, walltime, queue, inputfiles, concurrent_submissions)
	}
}
