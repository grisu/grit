import grisu.tests.testRuns.*

cleanJob {

	testrun = CleanJobTestRun

	disable = false
	
	batches = 1
	runs = 1

	group = '/nz/nesi'

	//inputfiles = ['gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/4gbfile.tst']
	//	inputfiles = [
	//		'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/4gbfile.tst',
	//		'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt'
	//	]
	inputfiles = [
		'gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/simpleTestFile.txt'
	]

}