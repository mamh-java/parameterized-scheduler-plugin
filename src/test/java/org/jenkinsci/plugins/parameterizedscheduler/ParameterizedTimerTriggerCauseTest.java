package org.jenkinsci.plugins.parameterizedscheduler;

import hudson.triggers.TimerTrigger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParameterizedTimerTriggerCauseTest {

	@Test
	void happyPath() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("o", "v");
		ParameterizedTimerTriggerCause testObject = new ParameterizedTimerTriggerCause(parameters);

		assertEquals(Messages.ParameterizedTimerTrigger_TimerTriggerCause_ShortDescription("{o=v}"),
				testObject.getShortDescription());
	}

	@Test
	void isTimerTrigger() {
		assertThat(new ParameterizedTimerTriggerCause(Collections.singletonMap("a", "b")),
				instanceOf(TimerTrigger.TimerTriggerCause.class));
	}
}
