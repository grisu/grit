import grisu.tests.tests.*

// the name of this testrun
CopyTest {

	// whether to disable the test
	disable = false
	// how many single test runs should run in parallel
	runs = 5


	test {

		// the class of test to run
		type = CopyTest

		// how often the file transfer should be repeated (optional, default: 1)
		repetitions = 10
		// the source file to copy (required)
		sourceUrl = "gsiftp://ng2.auckland.ac.nz/home/mbin029/pom.xml"
		// the target directory (required)
		targetDir = "gsiftp://gram5.ceres.auckland.ac.nz/home/mbin029/testfiles"
	}
}

