package grisu.tests.util

import grith.jgrith.Credential


class CredentialLoader {
	
	static Map loadCredentials(String pathToCredentialConfigFile) {
		
		def credConfig = new ConfigSlurper().parse(new File(pathToCredentialConfigFile).toURL())
		def credentials = [:]
		for ( def name in credConfig.keySet() ) {
			
			ConfigObject config = credConfig.getProperty(name)
			def type = config.get('type')
			
			switch (type) {
				case Credential.Type.Local: 
					Credential c = loadLocal(config)
					credentials.put(name, c)
					break
				default: 
					print 'default'
			}
		}
		return credentials
		
	}
	
	static Credential loadLocal(ConfigObject co) {
		
		def cert = co.get('certificate')
		def key = co.get('key')
		def passphrase = co.get('passphrase')
		
		def lifetime = co.get('lifetime')
		
		Credential c = new Credential(cert, key, passphrase.toCharArray(), lifetime)
	
		return c
	}

	
	static void main(args) {
		
		def creds = loadCredentials('/home/markus/src/jgrith/src/main/resources/exampleCredConfig.groovy')
		
		for ( c in creds ) {
			print c.getDn()
			println '\t'+c.getRemainingLifetime()
		}
		
	}
	
}
