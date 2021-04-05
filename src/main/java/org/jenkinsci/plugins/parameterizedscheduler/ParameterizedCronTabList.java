package org.jenkinsci.plugins.parameterizedscheduler;

import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import antlr.ANTLRException;

/**
 * mostly a copy of {@link CronTabList}
 * 
 * @author jameswilson
 *
 */
public class ParameterizedCronTabList {

	private final List<ParameterizedCronTab> cronTabs;

	public ParameterizedCronTabList(List<ParameterizedCronTab> cronTabs) {
		this.cronTabs = cronTabs;
	}

	public static ParameterizedCronTabList create(String cronTabSpecification) throws ANTLRException {
		return create(cronTabSpecification, ParameterParser.PARAMETER_SEPARATOR, ParameterParser.PAIR_SEPARATOR, null);
	}
	public static ParameterizedCronTabList create(String cronTabSpecification, String firstDelimiter, String parameterDelimiter) throws ANTLRException {
		return create(cronTabSpecification, firstDelimiter, parameterDelimiter, null);
	}

	public static ParameterizedCronTabList create(String cronTabSpecification, String firstDelimiter, String parameterDelimiter, Hash hash) throws ANTLRException {
		List<ParameterizedCronTab> result = new ArrayList<>();
		int lineNumber = 0;
		String timezone = null;
		for (String line : cronTabSpecification.split("\\r?\\n")) {
			line = line.trim();
			if(line.length() > 0 && !line.startsWith("#")) {
				lineNumber++;
				if(lineNumber == 1 && line.startsWith("TZ=")) {
					timezone = CronTabList.getValidTimezone(line.replace("TZ=", ""));
					if (timezone == null) {
						throw new ANTLRException("Invalid or unsupported timezone '" + line + "'");
					}
				} else {
					try {
						ParameterizedCronTab cronTab = ParameterizedCronTab.create(line, lineNumber, firstDelimiter, parameterDelimiter, hash, timezone);
						result.add(cronTab);
					} catch (ANTLRException e) {
						throw new ANTLRException(String.format("Invalid input: \"%s\": %s", line, e.toString()), e);
					}
				}
			}
		}
		return new ParameterizedCronTabList(result);
	}

	public List<ParameterizedCronTab> check(Calendar calendar) {
		return cronTabs.stream().filter(tab -> tab.check(calendar)).collect(Collectors.toList());
	}

	public String checkSanity() {
		for (ParameterizedCronTab tab : cronTabs) {
			String s = tab.checkSanity();
			if (s != null)
				return s;
		}
		return null;
	}
}
