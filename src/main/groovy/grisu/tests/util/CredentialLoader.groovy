package grisu.tests.util

import grisu.jcommons.utils.MyProxyServerParams;
import grith.jgrith.Credential
import grith.jgrith.CredentialFactory;
import grith.jgrith.plainProxy.LocalProxy;
import grith.jgrith.utils.CliLogin


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
				case Credential.Type.SLCS:
					Credential c = createSlcs(config)
					credentials.put(name, c)
					break
				case Credential.Type.MyProxy:
					Credential c = loadMyProxy(config)
					credentials.put(name, c)
					break
				case Credential.Type.Proxy:
					Credential c = loadLocalProxy(config)
					credentials.put(name, c)
					break
				default: 
					print 'default'
			}
		}
		return credentials
		
	}
	
	static Credential loadLocalProxy(ConfigObject co) {
		def path = co.get('path')
		if ( ! path ) {
			path = LocalProxy.PROXY_FILE
		}
		Credential c = new Credential(path)
		return c
	}
	
	static Credential loadMyProxy(ConfigObject co) {
		
		def username = co.get('username')
		def password = co.get('password')
		def myproxy = co.get('host')
		def port = co.get('port')
		def lifetime = co.get('lifetime')
		if ( ! lifetime ) {
			lifetime = 12
		}
		if ( ! myproxy ) {
			myproxy = MyProxyServerParams.DEFAULT_MYPROXY_SERVER
		}
		if ( ! port ) {
			port = MyProxyServerParams.DEFAULT_MYPROXY_PORT
		}
		
		Credential c = CredentialFactory.createFromMyProxy(username, password, myproxy, port, lifetime*3600)
		return c
	}
	
	static Credential createSlcs(ConfigObject co) {
		
		def idp = co.get('idp')
		def username = co.get('username')
		
		println "Using user '"+username+"' at '"+idp+"'..."
		
		char[] pw = CliLogin
				.askPassword("Please enter institution password")
				
		Credential c = CredentialFactory.createFromSlcs(null, idp, username, pw)
		
		return c
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