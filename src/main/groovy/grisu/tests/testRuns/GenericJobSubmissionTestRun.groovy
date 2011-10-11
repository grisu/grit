package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.tests.tests.GenericJobTest
import grisu.tests.tests.Test

class GenericJobSubmissionTestRun extends AbstractTestRun implements TestRun {

	public int amount_jobs
	public String group
	public List inputfiles
	public String application
	public String commandline
	public String queue
	public boolean wait_for_job_to_finish_before_next_job_submit
	public int walltime
	public boolean require_job_success

	public void setup() {
	}

	public void tearDown() {
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new GenericJobTest(si, batch, id, amount_jobs, group, application, commandline, walltime, queue, inputfiles, wait_for_job_to_finish_before_next_job_submit, require_job_success)
	}
}
