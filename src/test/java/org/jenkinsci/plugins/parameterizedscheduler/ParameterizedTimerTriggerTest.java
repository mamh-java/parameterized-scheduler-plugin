package org.jenkinsci.plugins.parameterizedscheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ParameterizedTimerTriggerTest {

	@Test
	void ctor() {
		String parameterizedSpecification = "* * * * *%foo=bar";
		ParameterizedTimerTrigger testObject = new ParameterizedTimerTrigger(parameterizedSpecification);

		assertSame(parameterizedSpecification, testObject.getParameterizedSpecification());
	}
}
