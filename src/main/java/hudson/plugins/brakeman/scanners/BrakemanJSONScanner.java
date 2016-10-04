package hudson.plugins.brakeman.scanners;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.brakeman.Warning;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Java class that represents a JSON Scanner of the Brakeman Output file
 * This class includes a specific strategy for implementing the parsing process
 * of the output file.
 *
 * @author Thomas Baird
 */
public class BrakemanJSONScanner extends AbstractBrakemanScanner {
    /**
     * Creates a new instance of <code>BrakemanJSONScanner</code>
     */
    public BrakemanJSONScanner() {
    }

    public void scan(String content, ParserResult project) {
        try {
            JSONObject brakemanFile = new JSONObject(content);
            scanJSONResultSet(brakemanFile, project, "warnings");
            scanJSONResultSet(brakemanFile, project, "ignored_warnings");
        } catch (JSONException e) {
            // Need to figure out how to get the logger in the arguments of this function to use here.
            e.printStackTrace();
        }
    }

    private void scanJSONResultSet(JSONObject brakemanResultFile, ParserResult project, String filterType) {
        try {
            JSONArray rows = brakemanResultFile.getJSONArray(filterType);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String fileName = row.getString("file");
                int line = row.getInt("line");
                String type = row.getString("warning_type");
                String category;
                if (filterType == "ignored_warnings") {
                    category = "Ignored";
                } else {
                    category = "General";
                }
                StringBuilder message = new StringBuilder();
                message.
                        append(row.getString("message")).
                        append(": ").
                        append(row.getString("code"));
                String prio = row.getString("confidence");
                Priority priority = checkPriority(prio);

                project.addAnnotation(new Warning(fileName, getStart(line), getEnd(line), type, category, message.toString(), priority));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
