package grisu.tests

import grisu.frontend.control.login.LoginManager
import grisu.jcommons.dependencies.BouncyCastleTool
import grisu.jcommons.utils.Version
import grisu.tests.testRuns.TestRunFactory
import grisu.tests.util.CredentialLoader

import org.slf4j.MDC

public class TestStarter {

	final List testruns = []
	final def backend
	final Map sis = [:]

	public TestStarter(String backend, Map credentials, List configFiles) {

		for (file in configFiles) {
			def trs = TestRunFactory.createTestRuns(file)
			testruns.addAll(trs)
		}

		this.backend = backend
		
		credentials.each { name, credential ->
			def si = LoginManager.login(credential, backend, false)
			sis.put(name, si)
		}
		

	}


	public void start() {

		for ( tr in testruns ) {
			tr.setServiceInterfaces(sis)
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
		LoginManager.initEnvironment();

		MDC.put("session",
				System.getProperty("user.name") + "_" + new Date().getTime());

		// MDC.put("local_user", System.getProperty("user.name"));
		MDC.put("grit-version", Version.get("grit"));


		//CliHelpers.

		def cli = new CliBuilder(usage: 'grit -b [backend] testfiles')

		cli.h(longOpt:'help', "Show usage information and quit")
		cli.b(longOpt:'backend', argName:'backend', args:1, required:true, 'Grisu backend to connect to, required')
		cli.o(longOpt:'output', argName:'output', args:1, required:false, 'Directory for output (logs etc...)')
		cli.c(longOpt:'credentials', argName:'credentials', args:1, required:true, 'Path to credential config file')
		
		def opt = cli.parse(args)

		if ( ! opt ) {
			System.exit(1)
		}
		
		if (opt.h) {
			cli.usage()
			System.exit(0)
		}

		def backend = opt.b
		
		def cred_config_path = opt.c
		def credentials = CredentialLoader.loadCredentials(cred_config_path)

		if (opt.o) {
			File f = new File(opt.o)
			f.mkdirs()
			if ( !f.exists() ) {
				print "Can't create output dir: "+f.getAbsolutePath()
				System.exit(1)
			}
			TestRunFactory.logDir = f
		}

		TestStarter ts = new TestStarter(backend, credentials, opt.arguments())

		ts.start()




		System.exit(0)
	}
}

