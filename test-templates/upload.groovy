import grisu.tests.testRuns.*


UploadTest {

	testrun = ParallelUploadTestRun
	
	disable = false
	
	
	batches = 1
	// how many parallel uploads
	runs = 10
	// how many times the file gets uploaded in a row per run
	repeats = 10

	//the local source file
	sourceFile = "/home/markus/tmp/18mb.bin"
	// the target directory
	targetDir = "gsiftp://gram5.ceres.auckland.ac.nz/home/mbin029/testfiles"
}

