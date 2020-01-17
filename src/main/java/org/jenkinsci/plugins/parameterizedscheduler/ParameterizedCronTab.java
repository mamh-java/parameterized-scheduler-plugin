package org.jenkinsci.plugins.parameterizedscheduler;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.Maps;

import antlr.ANTLRException;

import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import org.apache.commons.lang.StringUtils;

/**
 * this is a copy of {@link CronTab} with added parameters map support
 *
 * @author jameswilson
 */
public class ParameterizedCronTab {
	private static final Logger LOGGER = Logger.getLogger(ParameterizedCronTab.class.getName());

	private final Map<String, String> parameterValues;
	private final CronTabList cronTabList;


	/**
	 * @param cronTab the crontab to use as a template
	 * @param parameters the parameters in name=value key pairings
	 */
	public ParameterizedCronTab(CronTab cronTab, Map<String, String> parameters) {
		cronTabList = new CronTabList(Collections.singleton(cronTab));
		parameterValues = parameters;

	}

	/**
	 * @param hash
	 *      Used to spread out token like "@daily". Null to preserve the legacy behaviour
	 *      of not spreading it out at all.
	 */
	public static ParameterizedCronTab create(String line, String firstSep, String paramSep,
											  int lineNumber, Hash hash) throws ANTLRException {
		if(StringUtils.isEmpty(firstSep)){
			firstSep = ParameterParser.PARAMETER_SEPARATOR;
		}
		if(StringUtils.isEmpty(paramSep)){
			paramSep = ParameterParser.PAIR_SEPARATOR;
		}
		String[] lineParts = line.split(firstSep);

		CronTab cronTab = new CronTab(lineParts[0].trim(), lineNumber, hash);

		Map<String, String> parameters = Maps.newHashMap();
		if (lineParts.length == 2) {
			parameters = new ParameterParser().parse(lineParts[1], paramSep);
		}
		return new ParameterizedCronTab(cronTab, parameters);
	}

	public static ParameterizedCronTab create(String line, int lineNumber, Hash hash) throws ANTLRException {
		return create(line, ParameterParser.PARAMETER_SEPARATOR, ParameterParser.PAIR_SEPARATOR, lineNumber, hash);
	}

	public Map<String, String> getParameterValues() {
		return parameterValues;
	}


	public boolean check(Calendar calendar) {
		return cronTabList.check(calendar);
	}

	public String checkSanity() {
		return cronTabList.checkSanity();
	}
}
