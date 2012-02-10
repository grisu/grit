grit
===

grit is a small grid/grisu test framework which kicks of bunches of tests at a time. The tests can either be written in either Groovy or Java and are configured using a groovy config file in order to make the whole thing more flexible.

Prerequisites
--------------------

In order to build Grit from the git sources, you need: 

- Java (version 6)
- [git](http://git-scm.com) 
- [Apache Maven](http://maven.apache.org) (version >=2)

Download
----------

A pre-build binary (as well as rpm & deb packages) of Grit can be found here:

http://code.ceres.auckland.ac.nz/stable-downloads/


Checking out sourcecode
-------------------------------------

 `git clone git://github.com/grisu/grit.git`

Building grit using Maven
------------------------------------------

To build one of the above modules, cd into the module root directory of the module to build and execute: 

    cd grit
    mvn clean install
    
This will give you an executable jar file called "grit-binary.jar" in the target directory. In order to run, there also needs to be the bouncy-castle library jar (http://code.ceres.auckland.ac.nz/webstart/bcprov-jdk16-145.jar -- rename to bcprov.jar) in the same folder as grit-binary.jar...

Running grit
--------------------

    java -jar grit-binary.jar -b bestgrid-test -c <credential-config-file> <test-config-files>`

alternatively, if you installed either the rpm or debian package:

    grit -b bestgrid-test -c <credential-config-file> <test-config-files>

Options:

 -b : option specifies the backend to connect to. Options at the moment are "bestgrid", "bestgrid-test", "dev", "local". defaults to "bestgrid"
 -c : the credential(s) to be used with this testrun. examples in the examples/creds subfolder
 
Examples for test-config files can be found in the examples/tests subfolder.

Tests
---------

You can either write your own tests (implement the [Test interface](https://github.com/grisu/grit/blob/develop/src/main/groovy/grisu/tests/tests/Test.groovy) -- better to just extend [AbstractTest] (https://github.com/grisu/grit/blob/develop/src/main/groovy/grisu/tests/tests/AbstractTest.groovy) though.

Also, already existing Tests can be found [here](https://github.com/grisu/grit/tree/develop/src/main/groovy/grisu/tests/testRuns) and [here](https://github.com/grisu/grit/tree/develop/src/main/groovy/grisu/tests/tests).

Test-Template config files can be found [here](https://github.com/grisu/grit/tree/develop/test-templates) and need to be valid groovy files (for more info on that: http://groovy.codehaus.org/ConfigSlurper)


