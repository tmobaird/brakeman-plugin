package hudson.plugins.brakeman.scanners;

import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
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
    public BrakemanJSONScanner() {}

    public boolean scan(String content, ParserResult project, PluginLogger logger) {
        boolean result = true;
        try {
            JSONObject brakemanFile = new JSONObject(content);
            scanJSONResultSet(brakemanFile, project, "warnings");
            scanJSONResultSet(brakemanFile, project, "ignored_warnings");
        } catch (JSONException e) {
            result = false;
            logger.log(String.format("%s", e.getMessage()));
        }
        return result;
    }

    private void scanJSONResultSet(JSONObject brakemanResultFile, ParserResult project, String filterType) throws JSONException {
        JSONArray rows = brakemanResultFile.getJSONArray(filterType);
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            String fileName = row.getString("file");
            int line = row.getInt("line");
            String type = row.getString("warning_type");
            String category = getCategory(filterType);
            StringBuilder message = new StringBuilder();
            message.
                append(row.getString("message")).
                append(": ").
                append(row.getString("code"));
            Priority priority = checkPriority(row.getString("confidence"));

            project.addAnnotation(new Warning(fileName, getStart(line), getEnd(line), type, category, message.toString(), priority));
        }
    }

    private String getCategory(String filterType) {
        String category;
        if (filterType == "ignored_warnings") {
            category = "Ignored";
        } else {
            category = "General";
        }
        return category;
    }
}
