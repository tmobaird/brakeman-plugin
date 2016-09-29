package hudson.plugins.brakeman.scanners;

import hudson.plugins.analysis.core.ParserResult;

/**
 * A Java class that representats a JSON Scanner of the Brakeman Output file
 * This class includes a specific strategy for implementing the parsing process
 * of the output file.
 *
 * @author Thomas Baird
 */
public interface AbstractBrakemanScanner {

    public void scan(String content, ParserResult project);

}