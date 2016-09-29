package hudson.plugins.brakeman.scanners;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.brakeman.Warning;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Java class that representats a JSON Scanner of the Brakeman Output file
 * This class includes a specific strategy for implementing the parsing process
 * of the output file.
 *
 * @author Thomas Baird
 */
public class BrakemanJSONScanner implements AbstractBrakemanScanner {
    /**
     * Creates a new instance of <code>BrakemanJSONScanner</code>
     */
    public BrakemanJSONScanner() {}

    public void scan(String content, ParserResult project) {
        // Parses Warning From JSON
        // Probably should create BrakemanJSONParser Class
        // and BrakemanTabsParser Class so we can make this backwards compatible
        this.scanWarnings(content, project);
        this.scanIgnoredWarnings(content, project);

    }

    private void scanWarnings(String brakemanOutput, ParserResult project) {
        try {
            JSONObject brakemanFile = new JSONObject(brakemanOutput);
            JSONArray rows = brakemanFile.getJSONArray("warnings");
            for(int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String fileName = row.getString("file");
                int line = row.getInt("line");
                String type = row.getString("warning_type");
                String category = "General";
                String message = row.getString("message") + ": " + row.getString("code");
                String prio = row.getString("confidence");

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

                if (line > 2) {
                    start = line - 1;
                }

                project.addAnnotation(new Warning(fileName, start, end, type, category, message, priority));
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void scanIgnoredWarnings(String brakemanOutput, ParserResult project) {
        try {
            JSONObject brakemanFile = new JSONObject(brakemanOutput);
            JSONArray rows = brakemanFile.getJSONArray("ignored_warnings");
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String fileName = row.getString("file");
                int line = row.getInt("line");
                String type = row.getString("warning_type");
                String category = "Ignored";
                String message = row.getString("message") + ": " + row.getString("code");
                String prio = row.getString("confidence");

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

                if (line > 2) {
                    start = line - 1;
                }

                project.addAnnotation(new Warning(fileName, start, end, type, category, message, priority));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}