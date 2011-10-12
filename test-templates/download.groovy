import grisu.tests.testRuns.*


DownloadTest {

	testrun = ParallelDownloadTestRun
	
	disable = false
	
	
	batches = 1
	// how many parallel uploads
	runs = 10
	// how many times the file gets downloaded in a row per run
	repeats = 10

	//the remote source file
	sourceFile = "gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt" 
}

