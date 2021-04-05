package org.jenkinsci.plugins.parameterizedscheduler;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import hudson.model.ParametersDefinitionProperty;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterParser {
	/**
	 * if ever changed, documentation and messages will need to be updated as well
	 */
	protected static final String PARAMETER_SEPARATOR = "%";
	protected static final String NAME_VALUE_SEPARATOR = "=";
	protected static final String PAIR_SEPARATOR = ";";

	/**
	 * Parses a string with key value pairs
	 * @param nameValuePairFormattedString of name=value;other=value name value pairs
	 * @return Map of key-value pairs parsed from provided string
	 */
	public Map<String, String> parse(String nameValuePairFormattedString) {
		if (StringUtils.isBlank(nameValuePairFormattedString)) {
			return Collections.emptyMap();
		}
		String clean = nameValuePairFormattedString.trim();
		if (nameValuePairFormattedString.endsWith(PAIR_SEPARATOR)) {
			//the default splitter message in this scenario is not user friendly, so snip a trailing semicolon
			clean = clean.substring(0, clean.length() - 1);
		}
		return Splitter.on(PAIR_SEPARATOR).trimResults().withKeyValueSeparator(Splitter.on(NAME_VALUE_SEPARATOR).limit(2)).split(clean);
	}

	public Map<String, String> parse(String nameValuePairFormattedString, String paramdelimiter) {
		if(StringUtils.isEmpty(paramdelimiter)){
			return parse(nameValuePairFormattedString);
		}

		if (StringUtils.isBlank(nameValuePairFormattedString)) {//
			return Maps.<String, String> newHashMap();
		}
		String clean = nameValuePairFormattedString.trim();
		if (nameValuePairFormattedString.endsWith(paramdelimiter)) {
			clean = clean.substring(0, clean.length() - 1);
		}
		return Splitter.on(paramdelimiter).trimResults().withKeyValueSeparator(NAME_VALUE_SEPARATOR).split(clean);
	}

	@CheckForNull
	public String checkSanity(String cronTabSpec, ParametersDefinitionProperty parametersDefinitionProperty) {
		String[] cronTabLines = cronTabSpec.split("\\r?\\n");
		for (String cronTabLine : cronTabLines) {
			int idx = cronTabLine.indexOf(PARAMETER_SEPARATOR);
			if (idx != -1 && idx + 1 < cronTabLine.length()) {
				String split = cronTabLine.substring(idx + 1);
				try {
					Map<String, String> parsedParameters = parse(split);
					List<String> parameterDefinitionNames = parametersDefinitionProperty != null
							? parametersDefinitionProperty.getParameterDefinitionNames() : Collections.emptyList();
					List<String> parsedKeySet = parsedParameters.keySet().stream().filter(s -> !parameterDefinitionNames.contains(s)).collect(Collectors.toList());
					if (!parsedKeySet.isEmpty()) {
						return Messages.ParameterizedTimerTrigger_UndefinedParameter(parsedKeySet, parameterDefinitionNames);
					}
					List<String> emptyParameters = parsedParameters.keySet().stream().filter(k -> parsedParameters.get(k).isEmpty()).collect(Collectors.toList());
					if (!emptyParameters.isEmpty()) {
						return Messages.ParameterizedTimerTrigger_EmptyParameter(emptyParameters);
					}
				} catch (IllegalArgumentException e) {
					return e.getMessage();
				}
			}
		}
		return null;
	}
}
