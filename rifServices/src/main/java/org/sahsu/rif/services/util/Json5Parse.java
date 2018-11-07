package org.sahsu.rif.services.util;

import java.io.BufferedReader;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;

/**
 * Parses JSON5; removing C style comments
 */
public class Json5Parse {

	private final String jsonText;
	private static String lineSeparator = System.getProperty("line.separator");
	private static JSONObject json;
	
	public Json5Parse(final BufferedReader reader) 
			throws JSONException, IOException {
		// This regex can cause stack overflows!!!!

		StringBuffer sb = new StringBuffer();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line.replaceAll("//.*", "") + lineSeparator); // Remove single line comments
		} // This could theoretically cause a stack overflow; but it is very, very unlikely...

		reader.close();
		String jsonText=jsonText=sb.toString();
		jsonText=removeCstyleComments(jsonText);					// Remove C style comments
		
		jsonText=jsonText.replace(lineSeparator, "");				// Remove line separators
		
		this.jsonText = jsonText;
		this.json = new JSONObject(jsonText);	
	}
	
	public String toString() {
		return jsonText;
	}	
	public JSONObject toJson() {
		return json;
	}
	
	/**
	 * Remove C style comments (this comment) from JSON text string (pre parse)
	 *
	 * @param: JSONObject json
	 */	
	private String removeCstyleComments(String jsonText) {
			
        String text = jsonText;
		String comment = "";
        int index = 0;
        while( index != -1) {
            comment = text.substring(text.indexOf("/*"),text.indexOf("*/")+2);
            text = text.replace(comment, "");

            index = text.indexOf("/*");
        }
        return text;
	}
}
