import grisu.jcommons.constants.Constants
import grisu.tests.testRuns.*
import grisu.tests.tests.GenericJobTest;

genericJob {

	test = GenericJobTest

	disable = false
	
	// the number of batches (i.e. serviceinterfaces/sessions)
	batches = 1
	// the number of parallel test executions
	runs = 1
	
	jobname_prefix = 'generic_job_test'

	// the group to use to submit the jobs
	group = '/nz/nesi'
	// the amount of jobs to submit in serial
	amount_jobs = 10
	// the application package name for the job
	application = "UnixCommands"
	// the commandline
	commandline = "ls -lah"
	// the walltime of the job
	walltime = 60
	// the queue to use to submit to
	queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
	// whether to wait for one job to finish executing before submitting the next job
	// or just submit all jobs one after another
	wait_for_job_to_finish_before_next_job_submit = false
	// whether the test is considered failed if the job execution (not submission) fails (true)
	// or not (false)
	require_job_success = false
	// a list of input files
	inputfiles = [
		'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt'
	]

}