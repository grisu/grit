import grisu.jcommons.constants.Constants
import grisu.tests.testRuns.*

cleanJobs {

	testrun = CleanJobsTestRun

	disable = false
	
	// the number of batches (i.e. serviceinterfaces/sessions)
	batches = 1
	// the number of parallel test executions
	runs = 1

	// whether to kill the jobs seperately or use the "multiple-kill-jobs"-method "killJobs()
	kill_jobs_seperately = false
	
	// whether to wait for all jobs to be finished before start killing them...
	wait_for_jobs_to_finish = false
	// the group to use to submit the jobs
	group = '/nz/nesi'
	// the amount of jobs to submit in serial
	amount_jobs = 10
	// how many jobs to submit concurrently (mostly so that test execution is quicker -- might fail in setup-phase already though)
	concurrent_submissions = 10
	// the application package name for the job
	application = "UnixCommands"
	// the commandline
	commandline = "ls -lah"
	// the walltime of the job
	walltime = 60
	// the queue to use to submit to
	queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING

	// a list of input files
	inputfiles = [
	//		'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt'
	]

}