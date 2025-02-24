package org.jenkinsci.plugins.parameterizedscheduler;

import hudson.model.ParametersDefinitionProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParameterParserTest {

	@Mock
	private ParametersDefinitionProperty mockParametersDefinitionProperty;

	@Test
	void test_nullReturns_emptyMap() {
		ParameterParser testObject = new ParameterParser();
		assertEquals(Collections.emptyMap(), testObject.parse(null));
	}

	@Test
	void test_EmptyStringReturns_emptyMap() {
		ParameterParser testObject = new ParameterParser();

		assertEquals(Collections.emptyMap(), testObject.parse(""));
		assertEquals(Collections.emptyMap(), testObject.parse("     "));
	}

	@Test
	void test_Malformed_NoEquals_StringThrows_Exception() {
		ParameterParser testObject = new ParameterParser();
		assertThrows(IllegalArgumentException.class, () -> testObject.parse("namevalue"));
	}

	@Test
	void test_Malformed_ExtraSemicolon_StringThrows_Exception() {
		ParameterParser testObject = new ParameterParser();
		assertThrows(IllegalArgumentException.class, () -> testObject.parse("name=value;;"));
	}

	@Test
	void test_OneParamStringReturns_emptyMap() {
		ParameterParser testObject = new ParameterParser();

		HashMap<String, String> expected = new HashMap<>();
		expected.put("name", "value");
		assertEquals(expected, testObject.parse("name=value"));
	}

	@Test
	void test_MalFormed_TrimsSpacesStringThrows_Exception() {
		ParameterParser testObject = new ParameterParser();
		assertThrows(IllegalArgumentException.class, () -> testObject.parse(" name = value; ;;"));
	}

	@Test
	void test_TwoParamsStringReturns_emptyMap() {
		ParameterParser testObject = new ParameterParser();

		HashMap<String, String> expected = new HashMap<>();
		expected.put("name", "value");
		expected.put("name2", "value2");
		assertEquals(expected, testObject.parse("name2=value2;name=value"));
	}

	@Test
	void test_TwoParamsStringWithSpaceReturns_emptyMap() {
		ParameterParser testObject = new ParameterParser();

		HashMap<String, String> expected = new HashMap<>();
		expected.put("name", "value");
		expected.put("name2", "value2");
		assertEquals(expected, testObject.parse("name2=value2; name=value"));
	}

	@Test
	void test_ValueContainsEquals_emptyMap() {
		ParameterParser testObject = new ParameterParser();
		assertEquals(Collections.singletonMap("name", "value=contains=equals"), testObject.parse("name=value=contains=equals"));
	}

	@Test
	void checkSanity_HappyPath() {
		ParameterParser testObject = new ParameterParser();

		when(mockParametersDefinitionProperty.getParameterDefinitionNames()).thenReturn(Collections.singletonList("name"));
		assertNull(testObject.checkSanity("* * * * *%name=value", mockParametersDefinitionProperty));
	}

	@Test
	void checkSanity_NotDefined_ProjectParameter() {
		ParameterParser testObject = new ParameterParser();

		List<String> list = Collections.singletonList("not name");
		when(mockParametersDefinitionProperty.getParameterDefinitionNames()).thenReturn(list);
		assertEquals(Messages.ParameterizedTimerTrigger_UndefinedParameter("[name]", list.toString()),
				testObject.checkSanity("* * * * *%name=value", mockParametersDefinitionProperty));
	}

	@Test
	void checkSanity_TrailingSemiColon_IsTrimmed() {
		ParameterParser testObject = new ParameterParser();

		when(mockParametersDefinitionProperty.getParameterDefinitionNames()).thenReturn(
				Arrays.asList("env", "freckled"));
		assertNull(testObject.checkSanity("* * * * *%env=eight;freckled=flase;", mockParametersDefinitionProperty));
	}

	@Test
	void checkSanity_NoParametersIsNoBigDeal() {
		ParameterParser testObject = new ParameterParser();

		assertNull(testObject.checkSanity("* * * * *%", mockParametersDefinitionProperty));
		assertNull(testObject.checkSanity("* * * * *", mockParametersDefinitionProperty));
	}

	@Test
	void checkSanity_duplicateParamName() {
		ParameterParser testObject = new ParameterParser();
		assertTrue(testObject.checkSanity("* * * * *%name=value;name=value2", mockParametersDefinitionProperty).startsWith("Duplicate key"));
	}

	@Test
	void checkSanity_UnmatchedEquals() {
		ParameterParser testObject = new ParameterParser();
		when(mockParametersDefinitionProperty.getParameterDefinitionNames()).thenReturn(
				Arrays.asList("name", "name2"));
		assertEquals(Messages.ParameterizedTimerTrigger_EmptyParameter(Collections.singletonList("name2")),
				testObject.checkSanity("* * * * *%name=value;name2=", mockParametersDefinitionProperty));
	}

	@Test
	void checkSanity_NullParameters() {
		ParameterParser testObject = new ParameterParser();
		assertEquals(Messages.ParameterizedTimerTrigger_UndefinedParameter(Collections.singletonList("name"), Collections.emptyList()),
				testObject.checkSanity("* * * * *%name=value", null));
	}

	@Test
	void test_paramValue_with_percent() {
		ParameterParser testObject = new ParameterParser();
		HashMap<String, String> expected = new HashMap<>();
		expected.put("name", "value");
		expected.put("percent", "10%");
		assertEquals(expected, testObject.parse("name=value;percent=10%"));
	}

	@Test
	void checkSanity_paramValueWithPercent() {
		ParameterParser testObject = new ParameterParser();
		List<String> list = new ArrayList<>();
		list.add("percent");
		list.add("name");
		when(mockParametersDefinitionProperty.getParameterDefinitionNames()).thenReturn(list);
		assertNull(testObject.checkSanity("* * * * *%percent=10%;name=value", mockParametersDefinitionProperty));
	}

}
