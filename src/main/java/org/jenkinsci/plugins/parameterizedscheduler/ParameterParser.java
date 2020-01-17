package org.jenkinsci.plugins.parameterizedscheduler;

import hudson.model.ParametersDefinitionProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.parameterizedscheduler.Messages;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

public class ParameterParser {
	/**
	 * if ever changed, documentation and messages will need to be updated as well
	 */
	protected static final String PARAMETER_SEPARATOR = "%";
	protected static final String NAME_VALUE_SEPARATOR = "=";
	protected static final String PAIR_SEPARATOR = ";";

	/**
	 *
	 * @param nameValuePairFormattedString of name=value;other=value name value pairs
	 * @return
	 */
	public Map<String, String> parse(String nameValuePairFormattedString) {
		if (StringUtils.isBlank(nameValuePairFormattedString)) {
			return Maps.<String, String> newHashMap();
		}
		String clean = nameValuePairFormattedString.trim();
		if (nameValuePairFormattedString.endsWith(PAIR_SEPARATOR)) {
			//the default splitter message in this scenario is not user friendly, so snip a trailing semicolon
			clean = clean.substring(0, clean.length() - 1);
		}
		return Splitter.on(PAIR_SEPARATOR).trimResults().withKeyValueSeparator(NAME_VALUE_SEPARATOR).split(clean);
	}

	public Map<String, String> parse(String nameValuePairFormattedString, String paramSep) {
		if(StringUtils.isEmpty(paramSep)){
			return parse(nameValuePairFormattedString);
		}

		if (StringUtils.isBlank(nameValuePairFormattedString)) {//
			return Maps.<String, String> newHashMap();
		}
		String clean = nameValuePairFormattedString.trim();
		if (nameValuePairFormattedString.endsWith(paramSep)) {
			clean = clean.substring(0, clean.length() - 1);
		}
		return Splitter.on(paramSep).trimResults().withKeyValueSeparator(NAME_VALUE_SEPARATOR).split(clean);
	}

	public String checkSanity(String cronTabSpec, ParametersDefinitionProperty parametersDefinitionProperty) {
		String[] cronTabLines = cronTabSpec.split("\\r?\\n");
		for (int i = 0; i < cronTabLines.length; i++) {
			String[] split = cronTabLines[i].split(PARAMETER_SEPARATOR);
			if (split.length > 2) {
				return Messages.ParameterizedTimerTrigger_MoreThanOnePercent();
			}
			if (split.length == 2) {
				try {
					Map<String, String> parsedParameters = parse(split[1]);
					List<String> parameterDefinitionNames = parametersDefinitionProperty.getParameterDefinitionNames();
					List<String> parsedKeySet = new ArrayList<String>(parsedParameters.keySet());
					parsedKeySet.removeAll(parameterDefinitionNames);
					if (!parsedKeySet.isEmpty()) {
						return Messages.ParameterizedTimerTrigger_UndefinedParameter(parsedKeySet, parameterDefinitionNames);
					}
				} catch (IllegalArgumentException e) {
					return e.getMessage();
				}
			}
		}
		return null;
	}
}
