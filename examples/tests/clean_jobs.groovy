import grisu.jcommons.constants.Constants
import grisu.tests.log.StdOutTestLogger
import grisu.tests.testRuns.*
import grisu.tests.tests.CleanJobsTest

// the name of this testrun
cleanJobs {

	// testrun specific config

	// which logger(s) to use for this testrun
	logger = StdOutTestLogger

	// whether to disable this testrun
	disable = false

	// the number of batches (i.e. serviceinterfaces/sessions)
	batches = 1
	// the number of parallel test executions
	runs = 1

	test {
		// the class of test to run
		type = CleanJobsTest
		
		// whether to kill the jobs seperately or use the "multiple-kill-jobs"-method "killJobs() (optional, default: true)
		kill_jobs_seperately = false

		// whether to wait for all jobs to be finished before start killing them...
		wait_for_jobs_to_finish = false
		// the group to use to submit the jobs
		group = '/nz/nesi'
		// the amount of jobs to submit per run
		repetitions = 10
		// how many jobs to submit concurrently (mostly so that test execution is quicker -- might fail in setup-phase already though)
		concurrent_submissions = 10
		// the application package name for the job
		application = "UnixCommands"
		// the commandline
		commandline = "sh createFiles.sh 10 10 test0.txt"
		// the walltime of the job
		walltime = 60
		// the queue to use to submit to
		queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
		// a list of input files
		inputfiles = [
			'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt'
		]
	}

}