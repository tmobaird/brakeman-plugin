package hudson.plugins.brakeman.scanners;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.brakeman.Warning;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Java class that representats a JSON Scanner of the Brakeman Output file
 * This class includes a specific strategy for implementing the parsing process
 * of the output file.
 *
 * @author Thomas Baird
 */
public class BrakemanTabsScanner implements AbstractBrakemanScanner {

    private static Pattern pattern = Pattern.compile("^([^\t]+?)\t(\\d+)\t([\\w\\s]+?)\t(\\w+)\t([^\t]+?)\t(High|Medium|Weak)", Pattern.MULTILINE);
    /**
     * Creates a new instance of <code>BrakemanTabsScanner</code>
     */
    public BrakemanTabsScanner() {}

    public void scan(String content, ParserResult project) {
        Matcher m = pattern.matcher(content);

        this.scanWarnings(m, project);
    }

    private void scanWarnings(Matcher m, ParserResult project) {
        while(m.find()) {
            String fileName = m.group(1);
            int line = Integer.parseInt(m.group(2));
            String type = m.group(3);
            String category = m.group(4);
            String message = m.group(5);
            String prio = m.group(6);

            Priority priority = Priority.HIGH;
            if ("Medium".equalsIgnoreCase(prio)) {
                priority = Priority.NORMAL;
            } else if ("High".equalsIgnoreCase(prio)) {
                priority = Priority.HIGH;
            } else if ("Weak".equalsIgnoreCase(prio)) {
                priority = Priority.LOW;
            }

            int start = 0;
            int end = line + 1;

            if(line > 2)
                start = line - 1;


            project.addAnnotation(new Warning(fileName, start, end, type, category, message, priority));
        }
    }
}
