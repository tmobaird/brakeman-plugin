package hudson.plugins.brakeman.workflow;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;

/**
 * Created by apaulin on 7/1/16.
 */
public class PublishBrakemanStep extends AbstractStepImpl {

    private final String outputFile;

    @DataBoundConstructor
    public PublishBrakemanStep(@CheckForNull String outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(PublishBrakemanStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "publishBrakeman";
        }


        @Override
        public String getDisplayName() {
            return "Publish Brakeman reports";
        }
    }
}
