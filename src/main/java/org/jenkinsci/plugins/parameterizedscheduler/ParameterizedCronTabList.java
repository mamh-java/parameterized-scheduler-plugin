package org.jenkinsci.plugins.parameterizedscheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import antlr.ANTLRException;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;

/**
 * mostly a copy of {@link CronTabList}
 *
 * @author jameswilson
 */
public class ParameterizedCronTabList {

    private final List<ParameterizedCronTab> cronTabs;

    public ParameterizedCronTabList(List<ParameterizedCronTab> cronTabs) {
        this.cronTabs = cronTabs;
    }

    public static ParameterizedCronTabList create(String cronTabSpecification) throws ANTLRException {
        return create(cronTabSpecification, ParameterParser.PARAMETER_SEPARATOR, ParameterParser.PAIR_SEPARATOR, null);
    }

    public static ParameterizedCronTabList create(String cronTabSpecification, String firstSep, String paramSep) throws ANTLRException {
        return create(cronTabSpecification, firstSep, paramSep, null);
    }

    public static ParameterizedCronTabList create(String cronTabSpecification, String firstSep, String paramSep, Hash hash) throws ANTLRException {
        List<ParameterizedCronTab> result = new ArrayList<ParameterizedCronTab>();
        int lineNumber = 0;
        for (String line : cronTabSpecification.split("\\r?\\n")) {
            lineNumber++;
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#"))
                continue; // ignorable line
            try {
                ParameterizedCronTab tab = ParameterizedCronTab.create(line, firstSep, paramSep, lineNumber, hash);
                result.add(tab);
            } catch (ANTLRException e) {
                throw new ANTLRException(String.format("Invalid input: \"%s\": %s", line, e.toString()), e);
            }
        }
        return new ParameterizedCronTabList(result);
    }

    public ParameterizedCronTab check(Calendar calendar) {
        for (ParameterizedCronTab tab : cronTabs) {
            if (tab.check(calendar))
                return tab;
        }
        return null;
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
