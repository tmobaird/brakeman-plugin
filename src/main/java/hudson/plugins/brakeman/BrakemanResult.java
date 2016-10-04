package hudson.plugins.brakeman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.Iterator;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.Result;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.Thresholds;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.BuildResultEvaluator;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.model.FileAnnotation;
import org.apache.commons.lang.StringUtils;


import org.kohsuke.stapler.DataBoundConstructor;

import hudson.plugins.analysis.util.PluginLogger;

import com.thoughtworks.xstream.XStream;


/**
 * Represents the results of the warning analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Maximilian Odendahl
 */
public class BrakemanResult extends BuildResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -137460587767210579L;

    private transient boolean useDeltaValues;
    private transient Thresholds thresholds = new Thresholds();
    /** The build history for the results of this plug-in. */
    private transient BuildHistory history;
    private String reason;

    /**
     * Creates a new instance of {@link BrakemanResult}
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param usePreviousBuildAsReference
     *            compare with previous build
     * @param useStableBuildAsReference
     *            compare with only stable builds
     */
    public BrakemanResult(final Run<?, ?> build, final String defaultEncoding, final ParserResult result,
                          final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        this(build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference,
                BrakemanResultAction.class);
    }

    protected BrakemanResult(final Run<?, ?> build, final String defaultEncoding, final ParserResult result,
                               final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
                               final Class<? extends ResultAction<BrakemanResult>> actionType) {
        this(build, new BuildHistory(build, actionType, usePreviousBuildAsReference, useStableBuildAsReference),
                result, defaultEncoding, true);
    }

    private BrakemanResult(final Run<?, ?> build, final BuildHistory history,
                     final ParserResult result, final String defaultEncoding, final boolean canSerialize) {
        super(build, history, result, defaultEncoding);

        if (canSerialize) {
            serializeAnnotations(result.getAnnotations());
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /** {@inheritDoc} */
    @Override
    protected String createDeltaMessage() {
        return ResultSummary.createDeltaMessage(this);
    }

    /** {@inheritDoc} */
    @Override
    protected String getSerializationFileName() {
        return "compiler-Brakeman.xml";
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Brakeman_ProjectAction_Name();
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @SuppressWarnings("hiding")
    @Override
    public void evaluateStatus(final Thresholds thresholds, final boolean useDeltaValues, final PluginLogger logger, final String url) {
        evaluateStatus(thresholds, useDeltaValues, true, logger, url);
    }

    /**
     * Updates the build status, i.e. sets this plug-in result status field to
     * the corresponding {@link Result}. Additionally, the {@link Result} of the
     * build that owns this instance of {@link BuildResult} will be also
     * changed.
     *
     * This has been overridden to add a different evaluate strategy that
     * evaluates thresholds against non-ignored warnings/annotations.
     *
     * @param thresholds
     *            the failure thresholds
     * @param useDeltaValues
     *            the use delta values when computing the differences
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     * @param logger
     *            the logger
     * @param url
     *            the URL of the results
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("hiding")
    @Override
    public void evaluateStatus(final Thresholds thresholds, final boolean useDeltaValues, final boolean canComputeNew,
                               final PluginLogger logger, final String url) {
        // CHECKSTYLE:ON
        this.thresholds = thresholds;
        this.useDeltaValues = useDeltaValues;

        BuildResultEvaluator resultEvaluator = new BuildResultEvaluator(url);
        Result buildResult;
        StringBuilder messages = new StringBuilder();
        Set<FileAnnotation> annotations = getNonIgnoredAnnotations();
        if (getHistory().isEmpty() || !canComputeNew) {
            logger.log("Ignore new warnings since this is the first valid build");
            buildResult = resultEvaluator.evaluateBuildResult(messages, thresholds, annotations);
        }
        else if (useDeltaValues) {
            buildResult = resultEvaluator.evaluateBuildResult(messages, thresholds, annotations,
                    getDelta(), getHighDelta(), getNormalDelta(), getLowDelta());
        } else {
            buildResult = resultEvaluator.evaluateBuildResult(messages, thresholds,
                    annotations, getNewWarnings());
        }
        String myReason = messages.toString();
        setReason(myReason);

        setResult(buildResult);

        logger.log(String.format("%s %s - %s", Messages.Brakeman_ResultAction_Status(), buildResult.color.getDescription(), getReason()));
    }

    public Set<FileAnnotation> getIgnoredAnnotations() {
        Set<FileAnnotation> myAnnotations = getAnnotations();
        Iterator<FileAnnotation> itr = myAnnotations.iterator();
        Set<FileAnnotation> annotations = new HashSet<FileAnnotation>();
        while(itr.hasNext()) {
            FileAnnotation a = itr.next();
            if(a.getCategory() == "Ignored") {
                annotations.add(a);
            }
        }

        return annotations;
    }

    public Set<FileAnnotation> getNonIgnoredAnnotations() {
        Set<FileAnnotation> myAnnotations = getAnnotations();
        Iterator<FileAnnotation> itr = myAnnotations.iterator();
        Set<FileAnnotation> annotations = new HashSet<FileAnnotation>();
        while(itr.hasNext()){
            FileAnnotation a = itr.next();
            if(a.getCategory() != "Ignored") {
                annotations.add(a);
            }
        }

        return annotations;
    }

    public int getNumberOfIgnoredAnnotations() {
        Set<FileAnnotation> annotations = getIgnoredAnnotations();
        int result = annotations.size();

        return result;
    }

    public int getNumberOfNonIgnoredAnnotations() {
        Set<FileAnnotation> annotations = getNonIgnoredAnnotations();
        int result = annotations.size();

        return result;
    }


    /** {@inheritDoc} */
    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return BrakemanResultAction.class;
    }
}
