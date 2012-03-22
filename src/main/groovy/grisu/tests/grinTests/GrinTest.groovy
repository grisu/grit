package grisu.tests.grinTests

import grisu.control.ServiceInterface
import grisu.frontend.control.login.LoginManager
import grisu.frontend.model.job.JobObject
import grisu.grin.model.Grid
import grisu.jcommons.constants.Constants
import grisu.jcommons.interfaces.GrinformationManager
import grisu.jcommons.model.info.*


class GrinTest {


	static void main(args) throws Exception {

		LoginManager.initEnvironment()

		def successes = []
		def failures = []

		GrinformationManager im = new GrinformationManager('/home/markus/Workspaces/Goji/grin/src/main/resources/nesi.groovy')

		Grid grid = im.getGrid()

		LoginManager.myProxyHost = 'myproxy.arcs.org.au'
		ServiceInterface si = LoginManager.loginCommandline('local_ws_jetty')

		def validGroups = []
		def invalidGroups = []

		for (String fqan in si.getFqans().asSortedSet()) {

			def group = grid.getGroup(fqan);
			if ( group ) {
				validGroups.add(group)
			} else {
				invalidGroups.add(fqan)
			}
		}

		println 'Valid groups: '+ validGroups
		println ''
		println 'Invalid groups: '+ invalidGroups
		println ''

		def jobs = []
		for ( Group g : validGroups ) {

			println 'Testing group: ' + g
			def dirs = im.getDataLocationsForVO(g.toString())

			for ( def d : dirs ) {
				println 'Checking directory: '+d

				boolean exists = si.fileExists(d.getUrl())
				if ( exists ) {
					println '\texists'
					successes.add('Directory '+d.getUrl()+" exists")
				} else {
					println '\tdoes not exists'
					failures.add('Directory '+d.getUrl()+" does not exist")
				}

				boolean isFolder = si.isFolder(d.getUrl())
				if ( isFolder ) {
					println '\tis folder'
					successes.add('Directory '+d.getUrl()+" is folder")
				} else {
					println '\tdoes is no folder'
					failures.add('Directory '+d.getUrl()+" is no folder")
				}
			}

			for ( Queue q : im.getAllSubmissionLocationsForVO(g.toString()) ) {
				println 'Testing queue: '+q.toString()

				if ( ! validGroups.contains(g) ) {
					println 'No valid group for queue. Skipping...'
					continue
				}
				println 'Using group: '+ g.toString()
				JobObject job = new JobObject(si)
				job.setApplication(Constants.GENERIC_APPLICATION_NAME)
				job.setCommandline("echo queuetest: "+q+' -- '+g)
				job.setSubmissionLocation(q.toString())
				job.createJob(g.toString())

				job.submitJob(true)

				jobs.add(job)

				println 'Submitted: '+job.getJobname()
			}
		}


		println 'Checking jobs'

		jobs.each { job ->
			println 'Waiting for job: '+job.getJobname()
			job.waitForJobToFinish(10)
			println 'Job finished: '+job.getJobname()
			def stdout = job.getStdOutContent()
			println 'Stdout for job "'+job.getJobname()+'": '+stdout
			job.kill(true)
			println 'Job '+job.getJobname()+' killed.'
		}


		System.exit(0)
	}
}
