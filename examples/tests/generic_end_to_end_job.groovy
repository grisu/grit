import grisu.jcommons.constants.Constants
import grisu.tests.tests.*;

// the name of the testrun
genericEndToEndJob {


		// whether to disable this test
		disable = false

		// the number of parallel test executions
		runs = 1

		test {
				// the class of test to run
				type = GenericJobEndToEndTest

				jobname_prefix = 'generic_end_to_end_job_test'

				// the group to use to submit the jobs
				group = '/nz/grid-dev'
				// the amount of jobs to submit in serial
				repetitions = 10
				// the application package name for the job
				application = "UnixCommands"
				// the commandline
				commandline = "cat inputfile.txt"
				// the walltime of the job
				walltime = 60
				// the queue to use to submit to
				queue = Constants.NO_SUBMISSION_LOCATION_INDICATOR_STRING

				// a list of input files
				inputfiles = [
					   '/home/markus/Desktop/grit/inputfile.txt'
				]

		stdout_matcher_string =	'markus'
		}

}
