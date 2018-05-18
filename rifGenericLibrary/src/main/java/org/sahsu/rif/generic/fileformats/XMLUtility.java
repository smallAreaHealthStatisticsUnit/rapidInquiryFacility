
package org.sahsu.rif.generic.fileformats;

import java.io.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.sahsu.rif.generic.util.FieldValidationUtility;


/**
 *
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


public final class XMLUtility {
	

// ==========================================
// Section Constants
// ==========================================
	private static int counter = 0;
	
// ==========================================
// Section Properties
// ==========================================
	/** The print stream. */
	private PrintStream printStream;
	
	/** The character set. */
	private String characterSet;
	
	/** The field validation utility. */
	private FieldValidationUtility fieldValidationUtility;
	
	/** The comment injector. */
	private XMLCommentInjector commentInjector;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new XML utility.
     */
	public XMLUtility() {

    	commentInjector = new XMLCommentInjector();
		fieldValidationUtility = new FieldValidationUtility();
		
		counter++;
	}

// ==========================================
// Section Accessors and Mutators
// ==========================================
	/**
	 * Initialise.
	 *
	 * @param commentInjector the comment injector
	 * @param outputStream the output stream
	 * @param characterSet the character set
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void initialise(
		final XMLCommentInjector commentInjector,
		final OutputStream outputStream, 
		final String characterSet) 
		throws UnsupportedEncodingException {
		
		this.commentInjector = commentInjector;
		this.characterSet = characterSet;
		printStream 
			= new PrintStream(outputStream, true, characterSet);
	}
	
	/**
	 * Write start xml.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeStartXML() throws IOException  {

	   printStream.print("<?xml version=\"1.0\" ");
	   printStream.print("encoding=\"");
	   printStream.print(characterSet);
	   printStream.print("\" ?>");
	   printStream.flush();
	}
   	
	/**
	 * Write record list start tag.
	 *
	 * @param recordName the record name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeRecordListStartTag(
		final String recordName) 
		throws IOException {

		writeRecordStartTag(recordName);
	}
	
	/**
	 * Write record list end tag.
	 *
	 * @param recordName the record name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeRecordListEndTag(
		final String recordName) 
		throws IOException {

		writeRecordEndTag(recordName);
	}

	/**
	 * Write record start tag.
	 *
	 * @param recordName the record name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeRecordStartTag(
		final String recordName) 
		throws IOException {

		writeRecordStartTag(recordName, null, null);
	}
	
	/**
	 * Write record start tag.
	 *
	 * @param recordName the record name
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeRecordStartTag(
		final String recordName, 
		final String attributeName,
		final String attributeValue) 
		throws IOException {

		printStream.print("<");
	   	printStream.print(StringEscapeUtils.escapeXml10(recordName));

		if ((fieldValidationUtility.isEmpty(attributeName) == false) &&
			(fieldValidationUtility.isEmpty(attributeValue) == false)) {
			printStream.print(" ");
			printStream.print(attributeName);
			printStream.print("=\"");
			printStream.print(attributeValue);
			printStream.print("\"");
		}
		
		printStream.print(">");
		
		String recordComment = commentInjector.getRecordComment(recordName);

		if (recordComment != null) {			
			if (commentInjector.isFirstRecordOccurrence(recordName) == true) {	
				String comment = commentInjector.getRecordComment(recordName);
				commentInjector.setFirstRecordOccurrence(recordName);
				writeComment(comment);
			}

		}
			
		printStream.flush();		
	}
	
	/**
	 * Write record end tag.
	 *
	 * @param recordName the record name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeRecordEndTag(
		final String recordName) 
		throws IOException {

		printStream.print("</");
	    printStream.print(StringEscapeUtils.escapeXml10(recordName));
		printStream.print(">");
		printStream.flush();		
	}
	
	/**
	 * Write field start tag.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeFieldStartTag(
		final String recordName, 
		final String fieldName) 
		throws IOException  {

		printStream.print("<");
	   	printStream.print(StringEscapeUtils.escapeXml10(fieldName));
		printStream.print(">");
		
		String fieldComment 
			= commentInjector.getFieldComment(recordName, fieldName);	
		if ( (fieldComment != null) && 
			 (commentInjector.isFirstFieldOccurrence(recordName, fieldName) == true)) {

			String comment 
				= commentInjector.getFieldComment(recordName, fieldName);
			commentInjector.setFirstFieldOccurrence(recordName, fieldName);
			writeComment(comment);
		}		
				
		printStream.flush();
	}
   
	/**
	 * Write field end tag.
	 *
	 * @param fieldName the field name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeFieldEndTag(
		final String fieldName) 
		throws IOException  {

		printStream.print("</");
	    printStream.print(StringEscapeUtils.escapeXml10(fieldName));
		printStream.print(">");
		printStream.flush();
	}
   
	/**
	 * Write comment.
	 *
	 * @param comment the comment
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeComment(
		final String comment) 
		throws IOException  {

		printStream.print("<!-- ");
		printStream.print(StringEscapeUtils.escapeXml10(comment));
		printStream.print("-->");
		printStream.flush();
	}
      
	/**
	 * Write field.
	 *
	 * @param recordName the record name
	 * @param fieldName the field name
	 * @param value the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeField(
		final String recordName,
		final String fieldName, 
		final String value) 
		throws IOException  {

		if (StringUtils.isEmpty(value) == true) {
			return;
		}
		
		writeFieldStartTag(recordName, fieldName);
		printStream.print(StringEscapeUtils.escapeXml10(value));
				
		writeFieldEndTag(fieldName);
	}
	
	/**
	 * Write value.
	 *
	 * @param value the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeValue(
		final String value) 
		throws IOException {
		
		printStream.print(StringEscapeUtils.escapeXml10(value));
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
