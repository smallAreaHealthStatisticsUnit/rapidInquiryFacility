
package org.sahsu.rif.generic.presentation;

import java.io.*;
import java.util.ArrayList;


/**
 * <p>
 * A convenience utility to generate snippets of HTML code.  There may be better
 * utility classes that are part of established Java libraries.  However, for now
 * this class at least hides much of the actual HTML formatting code from
 * other Java classes.
 * </p>
 * <p>
 * In the future, having this class may make it easier to include default properties
 * that are extended to any HTML snippets it produces, such as font, colour, or
 * border width of tables
 * 
 * </p>
 *
 * <hr>
 * Copyright 2012 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */


public final class HTMLUtility {
	
// ==========================================
// Section Constants
// ==========================================
	/** The Constant BORDER_LINE_WIDTH. */
	private static final int BORDER_LINE_WIDTH = 1;
	
// ==========================================
// Section Properties
// ==========================================
		
	/** The output stream. */
	private OutputStream outputStream;
	
	/** The print stream. */
	private PrintStream printStream;
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new HTML utility.
     */
	public HTMLUtility() {

    }
    
	/**
	 * Initialise.
	 *
	 * @param outputStream the output stream
	 * @param characterSet the character set
	 */
	public void initialise(
		final OutputStream outputStream, 
		final String characterSet) {

		this.outputStream = outputStream;
		try {
			if (characterSet == null) {
				printStream 
					= new PrintStream(outputStream, true, "UTF-8");				
			}
			else {				
				printStream 
					= new PrintStream(outputStream, true, characterSet);
			}
		}
		catch(UnsupportedEncodingException exception) {
			//TODO:
		}
	}
	
// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Gets the html.
	 *
	 * @return the html
	 */
	public String getHTML() {
		
		return outputStream.toString();
	}
	
	/**
	 * Begin document.
	 */
	public void beginDocument() {

		printStream.print("<html>");
		printStream.print("<head>");
		printStream.print("</head>");
		printStream.flush();
	}
	
	
	/**
	 * Begin font.
	 *
	 * @param fontFace the font face
	 * @param fontSize the font size
	 * @param fontColour the font colour
	 */
	public void beginFont(
		final String fontFace,
		final String fontSize,
		final String fontColour) {
		
		printStream.print("<font");
		if (fontFace != null) {
			printStream.print(" face=\"");
			printStream.print(fontFace);
			printStream.print("\"");
		}
		if (fontSize != null) {
			printStream.print(" size=\"");
			printStream.print(fontSize);
			printStream.print("\"");
		}
		
		if (fontColour != null) {
			printStream.print(" color=\"");
			printStream.print(fontColour);
			printStream.print("\"");			
		}
		printStream.println(">");
	}
	
	/**
	 * End font.
	 */
	public void endFont() {

		printStream.println("</font>");
	}

	/**
	 * Begin italic.
	 */
	public void beginItalic() {

		printStream.println("<i>");
	}
	
	/**
	 * End italic.
	 */
	public void endItalic() {

		printStream.println("</i>");
	}
	
	/**
	 * Begin body.
	 */
	public void beginBody() {

		printStream.print("<body>");	
		printStream.flush();
	}
	
	/**
	 * End body.
	 */
	public void endBody() {

		printStream.print("</body>");
		printStream.flush();
	}
	
	/**
	 * End document.
	 */
	public void endDocument() {

		printStream.print("</html>");		
		printStream.flush();
	}
	
	/**
	 * Write header.
	 *
	 * @param headerLevel the header level
	 * @param title the title
	 */
	public void writeHeader(
		final int headerLevel, 
		final String title) {

		if (headerLevel == 1) {
			printStream.print("<h1>");
		}
		else if (headerLevel == 2) {
			printStream.print("<h2>");			
		}
		else if (headerLevel == 3) {
			printStream.print("<h3>");			
		}
		else {
			printStream.print("<h4>");
		}
		
		printStream.print(title);
		
		if (headerLevel == 1) {
			printStream.print("</h1>");
		}
		else if (headerLevel == 2) {
			printStream.print("</h2>");			
		}
		else if (headerLevel == 3) {
			printStream.print("</h3>");			
		}
		else {
			printStream.print("</h4>");
		}

		printStream.flush();
	}
	
	/**
	 * Insert line space.
	 */
	public void insertLineSpace() {

		printStream.print("<p>");
		printStream.print("</p>");				
		printStream.flush();
	}
	
	/**
	 * Insert line space.
	 */
	public void insertLineBreak() {

		printStream.print("<br>");
		printStream.flush();
	}
		
	
	/**
	 * Write paragraph.
	 *
	 * @param text the text
	 */
	public void writeParagraph(
		final String text) {

		printStream.print("<p>");
		printStream.print(text);
		printStream.print("</p>");		
		printStream.flush();
	}
	
	/**
	 * Write paragraph.
	 *
	 * @param text the text
	 */
	public void writeText(
		final String text) {

		printStream.print(text);
		printStream.flush();
	}
		
	
	/**
	 * Begin bulleted list.
	 */
	public void beginBulletedList() {

		printStream.print("<ul>");		
	}
	
	/**
	 * Write bulleted item.
	 *
	 * @param item the item
	 */
	public void writeBulletedItem(
		final String item) {

		printStream.print("<li>");
		printStream.print(item);
		printStream.print("</li>");
	}
	
	/**
	 * End bulleted list.
	 */
	public void endBulletedList() {

		printStream.print("</ul>");		
	}

	/**
	 * Begin invisible table.
	 */
	public void beginInvisibleTable() {
		
		printStream.print("<table border=\"");
		printStream.print(String.valueOf(0));
		printStream.print("\">");
		printStream.flush();
	}
	
	/**
	 * Begin table.
	 */
	public void beginTable() {

		printStream.print("<table border=\"");
		printStream.print(String.valueOf(BORDER_LINE_WIDTH));
		printStream.print("\">");
		printStream.flush();
	}

	/**
	 * End table.
	 */
	public void endTable() {

		printStream.print("</table>");
		printStream.flush();
	}
	
	/**
	 * Begin row.
	 */
	public void beginRow() {

		printStream.print("<tr>");
		printStream.flush();
	}
	
	/**
	 * End row.
	 */
	public void endRow() {

		printStream.print("</tr>");
		printStream.flush();
	}
	
	/**
	 * Begin column value.
	 */
	public void beginColumnValue() {

		printStream.print("<td>");
	}
	
	/**
	 * End column value.
	 */
	public void endColumnValue() {
		printStream.print("</td>");
	}
	
	/**
	 * Write column value.
	 *
	 * @param columnValue the column value
	 */
	public void writeColumnValue(
		final String columnValue) {

		printStream.print("<td>");
		if (columnValue != null) {
			printStream.print(columnValue);			
		}
		printStream.print("</td>");
		printStream.flush();
	}
	
	/**
	 * Write bold column value.
	 *
	 * @param columnValue the column value
	 */
	public void writeBoldColumnValue(
		final String columnValue) {

		printStream.print("<td>");
		printStream.print("<b>");
		if (columnValue != null) {
			printStream.print(columnValue);			
		}
		printStream.print("</b>");
		printStream.print("</td>");		
		printStream.flush();
	}
	
	/**
	 * Write bulleted list.
	 *
	 * @param listItems the list items
	 */
	public void writeBulletedList(
		final ArrayList<String> listItems) {

		printStream.print("<ol>");
		for (String listItem : listItems) {
			printStream.print("<li>");
			printStream.print(listItem);
			printStream.print("</li>");
		}
		printStream.print("</ol>");
		printStream.flush();
	}
   
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

}
