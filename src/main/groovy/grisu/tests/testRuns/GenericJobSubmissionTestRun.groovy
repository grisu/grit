package grisu.tests.testRuns

import grisu.control.ServiceInterface
import grisu.model.dto.DtoStringList
import grisu.model.status.StatusObject
import grisu.tests.tests.GenericJobTest
import grisu.tests.tests.Test

class GenericJobSubmissionTestRun extends AbstractTestRun implements TestRun {

	public static final String JOBNAME_PREFIX = "genericTestJob"

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

		killAllJobs()
	}

	private void killAllJobs() {
		for ( ServiceInterface si : this.serviceInterfaces ) {

			addLog("Killing potentially existing jobs...")
			def toKill = []
			for (def jobname in si.getAllJobnames(null).asSortedSet()) {
				if (jobname.startsWith(JOBNAME_PREFIX)) {
					toKill.add(jobname)
				}
			}

			if ( toKill) {
				addLog("Starting to kill "+toKill.size()+" jobs...")
				String handle = si.killJobs(DtoStringList.fromStringColletion(toKill), true)
				addLog("Waiting for jobs to be killed on backend...")
				StatusObject so = StatusObject.waitForActionToFinish(si, handle, 4, false, false)
				if ( so.getStatus().isFailed() ) {
					addLog("Error when trying to kill jobs: "+so.getStatus().getErrorCause())
				} else {
					addLog("Jobs killed succesfully.")
				}
			}
		}
	}

	public void tearDown() {

		killAllJobs()
	}

	public Test getTest(ServiceInterface si, int batch, int id) {
		return new GenericJobTest(si, batch, id, amount_jobs, group, application, commandline, walltime, queue, inputfiles, wait_for_job_to_finish_before_next_job_submit, require_job_success)
	}
}
