package org.jenkinsci.plugins.parameterizedscheduler;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.triggers.Trigger;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.StaplerRequest2;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@WithJenkins
class ParameterizedSchedulerTest {

	@Test
	void freestyle(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty(new StringParameterDefinition("foo", "lol")));
        assertThat(p.getLastCompletedBuild(), is(nullValue()));
        Trigger<Job> t = new ParameterizedTimerTrigger("* * * * *%foo=bar\n*/1 * * * *%foo=boo");
        t.start(p, true);
        p.addTrigger(t);
        new Cron().doRun();
        assertThat(p.isInQueue(), is(true));
        r.waitUntilNoActivity();
        assertThat(p.getLastCompletedBuild(), is(notNullValue()));
        List<String> values = new ArrayList<>();
        for (int i = 1; i < 3; ++i) {
            values.add((String) p.getBuildByNumber(i).getAction(ParametersAction.class).getParameter("foo").getValue());
        }
        assertThat(values, containsInAnyOrder("bar", "boo"));
    }

	@Test
	void pipeline(JenkinsRule r) throws Exception {
        WorkflowJob p = r.createProject(WorkflowJob.class);
        p.setDefinition(new CpsFlowDefinition("", true));
        WorkflowRun wfr = p.scheduleBuild2(0).get();
        p.addProperty(new ParametersDefinitionProperty(new StringParameterDefinition("foo", "lol")));
        Trigger<Job> t = new ParameterizedTimerTrigger("* * * * *%foo=bar\n*/1 * * * *%foo=boo");
        t.start(p, true);
        p.addTrigger(t);
        new Cron().doRun();
        r.waitUntilNoActivity();
        assertThat(p.getLastCompletedBuild(), is(not(wfr)));
        List<String> values = new ArrayList<>();
        for (int i = 2; i < 4; ++i) {
            values.add((String) p.getBuildByNumber(i).getAction(ParametersAction.class).getParameter("foo").getValue());
        }
        assertThat(values, containsInAnyOrder("bar", "boo"));
    }

	@Test
	void scripted(JenkinsRule r) throws Exception {
        WorkflowJob p = r.createProject(WorkflowJob.class);
        p.setDefinition(new CpsFlowDefinition("""
                properties([
                  parameters([
                    string(name: 'foo', defaultValue: 'lol')
                  ]),
                  pipelineTriggers([
                    parameterizedCron('* * * * *%foo=bar\\n*/1 * * * *%foo=boo')
                  ])
                ])""", true));
        WorkflowRun wfr = r.buildAndAssertSuccess(p);
        new Cron().doRun();
        r.waitUntilNoActivity();
        assertThat(p.getLastCompletedBuild(), is(not(wfr)));
        List<String> values = new ArrayList<>();
        for (int i = 2; i < 4; ++i) {
            values.add((String) p.getBuildByNumber(i).getAction(ParametersAction.class).getParameter("foo").getValue());
        }
        assertThat(values, containsInAnyOrder("bar", "boo"));
    }

	@Test
	void declarative(JenkinsRule r) throws Exception {
        WorkflowJob p = r.createProject(WorkflowJob.class);
        p.setDefinition(new CpsFlowDefinition("""
                pipeline {
                    agent any
                    parameters {
                      string(name: 'foo', defaultValue: 'lol')
                    }
                    triggers {
                        parameterizedCron('* * * * *%foo=bar\\n*/1 * * * *%foo=boo')
                    }
                    stages {
                        stage('Test') {
                            steps {
                                echo 'test'
                            }
                        }
                    }
                }""", true));
        WorkflowRun wfr = r.buildAndAssertSuccess(p);
        new Cron().doRun();
        r.waitUntilNoActivity();
        assertThat(p.getLastCompletedBuild(), is(not(wfr)));
        List<String> values = new ArrayList<>();
        for (int i = 2; i < 4; ++i) {
            values.add((String) p.getBuildByNumber(i).getAction(ParametersAction.class).getParameter("foo").getValue());
        }
        assertThat(values, containsInAnyOrder("bar", "boo"));
    }

	@Test
	@Issue("JENKINS-49372")
	void nullValueCreated(JenkinsRule r) throws Exception {
        FreeStyleProject p = r.createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty(new NullParameterDefinition("foo")));
        assertThat(p.getLastCompletedBuild(), is(nullValue()));
        Trigger<Job> t = new ParameterizedTimerTrigger("* * * * *%foo=test");
        t.start(p, true);
        p.addTrigger(t);
        new Cron().doRun();
        assertThat(p.isInQueue(), is(true));
        r.waitUntilNoActivity();
        // Build should complete successfully but will not have any value
        assertThat(p.getLastCompletedBuild(), is(notNullValue()));
    }

    private static class NullParameterDefinition extends ParameterDefinition {

        public NullParameterDefinition(@NonNull String name) {
            super(name);
        }

        @CheckForNull
        @Override
        public ParameterValue createValue(StaplerRequest2 staplerRequest, JSONObject jsonObject) {
            return null;
        }

        @CheckForNull
        @Override
        public ParameterValue createValue(StaplerRequest2 staplerRequest) {
            return null;
        }
    }
}
