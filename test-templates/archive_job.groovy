import grisu.jcommons.constants.Constants
import grisu.tests.tests.*

// the name of this testrun
archiveJob {

	// the class of test to run
	test = ArchiveJobTest
	
	// ----------------------------------------------------
	// Common job properties
	
	// whether to disable this test
	disable = false

	// how many single test runs should run in parallel
	runs = 1
	// how many of the "per-run" jobs should be created & submitted in parallel (optional, default = 1)
	concurrent_submissions = 5
	// whether to wait for the submitted jobs to finish before the test is executed (optional, default = true)
	wait_for_jobs_to_finish = true

	// the prefix of the jobnames to be created (optional, default: "test_job")
	jobname_prefix = 'archive_test_job'
	// the amount of jobs that are submitted/archived per run (optional, default: 1)
	repetitions = 5
	// the group for the job to be submitted (optional, default: /nz/nesi)
	group = '/nz/nesi'
	// the application package for the job (optional, default: "UnixCommands"
	application = 'UnixCommands'
	// the commandline of the job to run (optional, default: "echo hello world"
	commandline = 'ls -lah'
	// the queue to submit the jobs to (optional, default: "Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING")
	queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING
	// the walltime of the jobs to submit
	walltime = 60
	// the inputfiles for the job (optional, default: empty list)
	inputfiles = [
		'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt'
	]

}