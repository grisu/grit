import grisu.tests.testRuns.*


UploadTest {

	testrun = ParallelUploadTestRun
	
	disable = false
	
	
	batches = 1
	runs = 10

	sourceFile = "/home/markus/tmp/18mb.bin"
	targetDir = "gsiftp://gram5.ceres.auckland.ac.nz/home/mbin029/testfiles"
}

