import grisu.tests.tests.*


// the name of this testrun
UploadTest {


	// whether to disable this test
	disable = false

	// how many parallel uploads
	runs = 1

	test {
		// the class of test to run
		type = UploadTest
		// how many times the file gets uploaded in serial per run
		repetitions = 10

		//the local source file
		sourceFile = "/home/markus/test/24mb.bin"
		// the target directory
		targetDir = "gsiftp://gram5.ceres.auckland.ac.nz/home/mbin029/testfiles"

	}
}

