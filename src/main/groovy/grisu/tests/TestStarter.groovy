package grisu.tests

import grisu.frontend.control.login.LoginManager
import grisu.frontend.view.cli.CliHelpers
import grisu.tests.testRuns.TestRunFactory

static void main(args) {

	CliHelpers.enableProgressDisplay(false)

	def cli = new CliBuilder(usage: 'grit -b [backend] testfiles')

	cli.h(longOpt:'help', "Show usage information and quit")
	cli.b(longOpt:'backend', argName:'backend', args:1, required:true, 'Grisu backend to connect to, required')

	def opt = cli.parse(args)


	if (opt.h) {
		cli.usage()
		System.exit(0)
	}

	def backend = opt.b


	def testruns = []

	for (file in opt.arguments()) {
		def trs = TestRunFactory.createTestRuns(file)
		testruns.addAll(trs)
	}

	def si = LoginManager.loginCommandline(backend)


	for ( tr in testruns ) {
		tr.setServiceInterfaces([si])
		tr.kickOffAndWaitForTestsToFinish()
		tr.waitForCleanUp()
		tr.printResults()
	}

	System.exit(0)
}

