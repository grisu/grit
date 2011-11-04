package grisu.tests.testRuns

import grisu.frontend.control.login.LoginManager
import grisu.tests.log.DefaultTestLogger




class TestRunFactory {

	static List createTestRuns(String pathToConfigFile) {

		def testrun = new ConfigSlurper().parse(new File(pathToConfigFile).toURL())

		def result = []

		// create testrun for every root-level element in config file
		for ( def name in testrun.keySet() ) {


			ConfigObject config = testrun.getProperty(name)

			def disable = config.get("disable", false)
			if ( disable ) {
				continue
			}

			def batches = config.get("batches", 1)
			def runs = config.get("runs", 1)

			def skipSetup = config.get("skip_setup", false)
			def skipTearDown = config.get("skip_teardown", false)

			def testclass = config.get('test').get('type')
			if ( ! testclass ) {
				throw new RuntimeException("Could not create testrun, no test type specified...");
			}

			TestRun tr = new TestRun()
			tr.setName(name)
			tr.setTestClass(testclass)

			def loggers = config.get("logger", new DefaultTestLogger(tr))

			tr.setTestLoggers(loggers)

			if (batches) {
				tr.setBatches(batches)
			}
			if (runs) {
				tr.setRuns(runs)
			}
			if (skipSetup) {
				tr.setSkipSetup(skipSetup)
			}
			if (skipTearDown) {
				tr.setSkipTearDown(skipTearDown)
			}



			Map testConfigMap = config.get('test')
			testConfigMap.remove('type')
			tr.setConfigMap(testConfigMap)

			result.add(tr)
		}

		return result
	}

	static void main(args) {


		def trs = createTestRuns("/home/markus/src/tests/test-templates/archive_job.groovy")

		def si = LoginManager.loginCommandline('dev')

		for (TestRun tr in trs) {
			tr.setServiceInterfaces([si])
			tr.kickOffAndWaitForTestsToFinish()
			tr.waitForCleanUp()
			tr.printResults()
		}
	}
}
