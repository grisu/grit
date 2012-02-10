import grisu.jcommons.constants.Constants
import grisu.tests.tests.*


// the name of this testrun
deleteBigFolder {

	// whether to disable this testrun
	disable = false
	// how many big folders to delete in parallel
	runs = 1

	test {
		// the class of test
		type = DeleteBigFolderTest
		// the prefix of the jobnames to be created (optional, default: "test_job")
		jobname_prefix = 'big_folder_delete_test'
		// how many big folders to create per test (optional, default: 1)
		repetitions = 1

		// the group using to submit jobs to create the big folders
		group = '/nz/nesi'
		// the queue to submit the big-folder-create jobs to
		queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING

		// how many folders to create in jobdirectory (optional, default: 10)
		no_of_folders = 10
		// how many files put into each of those folders (optional, default: 10)
		no_of_files = 10
	}

}