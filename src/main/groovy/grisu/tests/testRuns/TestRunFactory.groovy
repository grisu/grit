package grisu.tests.testRuns

import grisu.frontend.control.login.LoginManager




class TestRunFactory {

	static List createTestRuns(String pathToConfigFile) {

		def testrun = new ConfigSlurper().parse(new File(pathToConfigFile).toURL())

		def result = []

		for ( def name in testrun.keySet() ) {

			def configMap = [:]
			ConfigObject config = testrun.getProperty(name)

			for ( def key in config.keySet()) {
				configMap.put(key, config.get(key))
			}

			def disable = configMap.get("disable")
			if ( disable ) {
				continue
			} else {
				configMap.remove("disable")
			}

			def testclass = configMap.get("test")
			configMap.remove("test")

			def batches = configMap.get("batches")
			configMap.remove("batches")
			def runs = configMap.get("runs")
			configMap.remove("runs")
			def skipSetup = configMap.get("skip_setup")
			configMap.remove("skip_setup")
			def skipTearDown = configMap.get("skip_teardown")
			configMap.remove("skip_teardown")

			TestRun tr = new TestRun()
			tr.setName(name)
			tr.setTestClass(testclass)
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

			tr.setConfigMap(configMap)

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
