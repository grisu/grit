package grisu.tests.tests

public @interface TestDocumentation {

	String description() default "n/a"
	String setupDescription() default "n/a"
	String checkDescription() default "n/a"
	String tearDownDescription() default "n/a"
	String[] supportedParameters() default []
}
