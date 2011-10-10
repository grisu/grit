import grisu.jcommons.constants.Constants
import grisu.tests.testRuns.*

deleteBigFolder {

	testrun = DeleteBigFoldersTestRun

	disable = false
	
	batches = 1
	runs = 1

	group = '/nz/nesi'
	
	queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING

	no_of_folders = 10
	no_of_files = 10
	

}