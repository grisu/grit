package grisu.tests;

import grisu.control.ServiceInterface;
import grisu.frontend.control.login.LoginException;
import grisu.frontend.control.login.LoginManager;
import grisu.tests.testRuns.TestRun;
import grisu.tests.testRuns.TestRunFactory;

import java.util.List;

import org.python.google.common.collect.Lists;

public class TestStarterJava {

	public static void main(String[] args) throws LoginException {

		String backend = args[0];

		ServiceInterface si = LoginManager.loginCommandline(backend);
		List<ServiceInterface> sis = Lists.newArrayList();
		sis.add(si);


		List<TestRun> trs = TestRunFactory.createTestRuns(args[1]);

		for (TestRun tr : trs) {
			tr.setServiceInterfaces(sis);
			tr.kickOffAndWaitForTestsToFinish();
			tr.waitForCleanUp();
			tr.printResults();
		}
		System.exit(0);
	}
}