import grisu.tests.tests.*


// the name of this testrun
downloadTest {


	disable = false

	// how many parallel downloads (optional, default: 1)
	runs = 1

	test {

		type = DownloadTest

		// how many times the file gets downloaded in a row per run
		repetitions = 10

		//the remote source file
		sourceUrl = "gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt"
	}
}

