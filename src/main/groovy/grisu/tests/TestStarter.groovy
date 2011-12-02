package grisu.tests

import grisu.frontend.control.login.LoginManager
import grisu.jcommons.dependencies.BouncyCastleTool
import grisu.jcommons.utils.Version
import grisu.jcommons.view.cli.CliHelpers
import grisu.tests.testRuns.TestRunFactory

import org.slf4j.MDC

public class TestStarter {

	final List testruns = []
	final def si
	final def backend

	public TestStarter(String backend, List configFiles) {

		for (file in configFiles) {
			def trs = TestRunFactory.createTestRuns(file)
			testruns.addAll(trs)
		}

		this.backend = backend
		si = LoginManager.loginCommandline(backend)
	}


	public void start() {

		for ( tr in testruns ) {
			tr.setServiceInterfaces([si])
			tr.kickOffAndWaitForTestsToFinish()
			tr.waitForCleanUp()
			tr.getStatus()
		}
	}

	public List getTestRuns() {
		return testruns
	}


	static void main(args) {

		BouncyCastleTool.initBouncyCastle();

		MDC.put("session",
				System.getProperty("user.name") + "_" + new Date().getTime());

		// MDC.put("local_user", System.getProperty("user.name"));
		MDC.put("grit-version", Version.get("grit"));


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


		TestStarter ts = new TestStarter(backend, opt.arguments())

		ts.start()




		System.exit(0)
	}
}

