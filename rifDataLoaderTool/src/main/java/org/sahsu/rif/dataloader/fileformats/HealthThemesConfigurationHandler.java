package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Class responsible for serialising and deserialising instances of 
 * {@rifDataLoaderTool.businessConceptLayer.DLHealthTheme} with XML.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

final class HealthThemesConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private boolean isSerialisingHints;
	
	private ArrayList<HealthTheme> healthThemes;
	private HealthTheme currentHealthTheme;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public HealthThemesConfigurationHandler() {
		isSerialisingHints = false;
		healthThemes = new ArrayList<HealthTheme>();
		
		setPluralRecordName("health_themes");		
		setSingularRecordName("health_theme");
	}

	@Override
	public void initialise(
		final OutputStream outputStream,
		final XMLCommentInjector commentInjector) 
		throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public HealthTheme getHealthTheme(final String targetHealthThemeName) {
		for (HealthTheme healthTheme : healthThemes) {
			String currentHealthThemeName = healthTheme.getName();
			if (currentHealthThemeName.equals(targetHealthThemeName)) {
				return healthTheme;
			}			
		}
		
		return null;
	}
	
	public void setIsSerialisingHints(final boolean isSerialisingHints) {
		this.isSerialisingHints = isSerialisingHints;
	}
	
	public ArrayList<HealthTheme> getHealthThemes() {
		return healthThemes;
	}
	
	public String getHTML(
		final HealthTheme healthTheme) {

		return "";
	}
	
	public void writeXML(
		final ArrayList<HealthTheme> healthThemes)
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());

		for (HealthTheme healthTheme : healthThemes) {
			
			xmlUtility.writeRecordStartTag(getSingularRecordName());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"name", 
				healthTheme.getName());

			xmlUtility.writeField(
				getSingularRecordName(), 
				"description", 
				healthTheme.getDescription());			
			
			xmlUtility.writeField(
				getSingularRecordName(), 
				"last_modified", 
				getLastModifiedTimeStampPhrase(healthTheme.getLastModifiedTime()));	
			
			xmlUtility.writeRecordEndTag(getSingularRecordName());			
		}
				
		xmlUtility.writeRecordEndTag(getPluralRecordName());		
	}

	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {
		
		if (isPluralRecordName(qualifiedName)) {
			activate();
			healthThemes.clear();
		}
		else if (isSingularRecordName(qualifiedName) == true) {			
			currentHealthTheme
				= HealthTheme.newInstance();
		}		
		else {
			assert false;
		}			
	}
	
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {


		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			healthThemes.add(currentHealthTheme);
		}		
		else if (equalsFieldName("name", qualifiedName)) {
			currentHealthTheme.setName(
				getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName)) {
			currentHealthTheme.setDescription(
				getCurrentFieldValue());
		}		
		else if (equalsFieldName("last_modified", qualifiedName)) {
			String timeStampPhrase = getCurrentFieldValue();
			currentHealthTheme.setLastModifiedTime(
				getLastModifiedTimeStamp(timeStampPhrase));
		}
		
		else {
			assert false;
		}		
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
