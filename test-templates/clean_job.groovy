import grisu.jcommons.constants.Constants
import grisu.tests.testRuns.*

cleanJob {

	testrun = CleanJobTestRun

	disable = false
	
	batches = 1
	runs = 1

	group = '/nz/nesi'
	
	queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING

	// how many times the test input file should be copied inside the job. that determines how many files there are to be deleted in the jobdir
	no_of_files = 10

}