package hudson.plugins.brakeman;

import hudson.Launcher;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.AnnotationsAggregator;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;

/**
 *  Enable Brakeman in multi-config jobs.
 */
public class BrakemanAnnotationsAggregator extends AnnotationsAggregator {
    /**
     * Creates a new instance of {@link BrakemanAnnotationsAggregator}.
     *
     * @param build
     *            the matrix build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     */
    public BrakemanAnnotationsAggregator(final MatrixBuild build, final Launcher launcher,
            final BuildListener listener, final HealthDescriptor healthDescriptor, final String defaultEncoding,
            final boolean useStableBuildAsReference) {
        super(build, launcher, listener, healthDescriptor, defaultEncoding);
    }

    @Override
    protected Action createAction(final HealthDescriptor healthDescriptor, final String defaultEncoding, final ParserResult aggregatedResult) {
        return new BrakemanResultAction(build, healthDescriptor,
                new BrakemanResult(build, defaultEncoding, aggregatedResult));
    }

    @Override
    protected boolean hasResult(final MatrixRun run) {
        return getAction(run) != null;
    }

    @Override
    protected BrakemanResult getResult(final MatrixRun run) {
        return getAction(run).getResult();
    }

    private BrakemanResultAction getAction(final MatrixRun run) {
        return run.getAction(BrakemanResultAction.class);
    }
}

