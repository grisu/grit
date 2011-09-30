package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.CleanJobsTest
import grisu.tests.tests.Test

class CleanJobsTestRun extends AbstractTestRun implements TestRun {

	public boolean kill_jobs_seperately
	public boolean wait_for_jobs_to_finish
	public int amount_jobs
	public String group
	public List inputfiles
	public String application
	public String commandline
	public String queue
	public int walltime
	public int concurrent_submissions

	public void setup() {
	}

	public void tearDown() {
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new CleanJobsTest(si, batch, id, amount_jobs, group, application, commandline, walltime, queue, inputfiles, concurrent_submissions, kill_jobs_seperately, wait_for_jobs_to_finish)
	}
}
