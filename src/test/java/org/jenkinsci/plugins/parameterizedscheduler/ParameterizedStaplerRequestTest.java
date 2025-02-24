package org.jenkinsci.plugins.parameterizedscheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ParameterizedStaplerRequestTest {

	@Test
	void getParameter() {
		String testValue = "testvalue";
		ParameterizedStaplerRequest testObject = new ParameterizedStaplerRequest(testValue);
		assertSame(testValue, testObject.getParameter(null));
	}

	@Test
	void getParameterValues() {
		String testValue = "testvalue";
		ParameterizedStaplerRequest testObject = new ParameterizedStaplerRequest(testValue);
		assertArrayEquals(new String[] { testValue }, testObject.getParameterValues(null));
	}

}
