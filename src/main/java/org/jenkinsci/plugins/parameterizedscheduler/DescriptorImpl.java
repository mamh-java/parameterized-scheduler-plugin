package org.jenkinsci.plugins.parameterizedscheduler;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;

@Extension
@Symbol("parameterizedCron")
public class DescriptorImpl extends TriggerDescriptor {
    private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

    /**
     * I don't like inner classes. Using the declaritive support here by calling super constructor with class.
     */
    public DescriptorImpl() {
        super(ParameterizedTimerTrigger.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isApplicable(Item item) {
        boolean result = false;

        Jenkins jenkins = Jenkins.getInstance();

        if (item instanceof AbstractProject) {
            result = ((AbstractProject) item).isParameterized();
        } else if (jenkins != null &&
                jenkins.getPlugin("workflow-job") != null &&
                item instanceof WorkflowJob) {
            result = ((WorkflowJob) item).isParameterized();
        }
        return result;
    }

    @Override
    public String getDisplayName() {
        return Messages.ParameterizedTimerTrigger_DisplayName();
    }

}
