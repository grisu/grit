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

			def constructor = configMap.get("testrun").getConstructor()
			configMap.remove("testrun")

			TestRun tr = constructor.newInstance()
			tr.setName(name)

			for ( def key : configMap.keySet() ) {
				def field = tr.class.getField(key)
				def value = configMap.get(key)
				field.set(tr, value)
			}
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
