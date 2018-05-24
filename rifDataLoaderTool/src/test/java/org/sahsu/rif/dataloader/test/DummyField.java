package org.sahsu.rif.dataloader.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 *
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

public class DummyField {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final String BLANK_VALUE = "";
	// ==========================================
	// Section Properties
	// ==========================================
	private String fieldName;
	
	
	private double commonValueThreshold;
	private double uncommonValueThreshold;
	private double blankValueThreshold;
	
	private Random randomIndexGenerator;
	private ArrayList<String> commonValues;
	private HashSet<String> usedCommonValues;
	
	private ArrayList<String> uncommonValues;
	private HashSet<String> usedUncommonValues;
	
	private ArrayList<String> errorValues;
	private HashSet<String> usedErrorValues;
	
	private boolean trackCoverage;
	// ==========================================
	// Section Construction
	// ==========================================

	private DummyField(final String fieldName) {
		this.fieldName = fieldName;
		
		commonValueThreshold = 0.60;
		uncommonValueThreshold = 0.70;
		blankValueThreshold = 0.80;
		
		commonValues = new ArrayList<String>();
		usedCommonValues = new HashSet<String>();		
		uncommonValues = new ArrayList<String>();
		usedUncommonValues = new HashSet<String>();
		
		errorValues = new ArrayList<String>();
		usedErrorValues = new HashSet<String>();
		
		randomIndexGenerator = new Random();
		
	}

	public static DummyField newInstance(final String fieldName) {
		DummyField field = new DummyField(fieldName);
		return field;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public String getFieldName() {
		return fieldName;
	}
	
	/*
	 * eg:
	 * 0.60 common value
	 * 0.10 uncommon value
	 * 0.10 blank value
	 * 0.20 (implied 20% of getting an error)
	 * 
	 * would translate into these ranges:
	 * [0, 0.60) = generate a common value
	 * [0.60, 0.70) = generate an uncommon value
	 * [0.70, 0.80) = generate a blank value
	 * [0.80, 1.0] = generate an error
	 */
	public void setOutcomeLikelihoods(
		final double commonValueLikelihood,
		final double uncommonValueLikelihood,
		final double blankValueLikelihood) {
			
		//0 to commonValueLikelihood range
		commonValueThreshold = commonValueLikelihood;
		uncommonValueThreshold = commonValueLikelihood + uncommonValueLikelihood;
		blankValueThreshold = uncommonValueThreshold + blankValueLikelihood;
		//errorThreshold is always 1
	}
	
	public String generateValue() {
		
		Random random = new Random();
		double decisionValue = random.nextDouble();
		
		if (decisionValue < commonValueThreshold) {
			return generateCommonValue();
		}
		else if (decisionValue < uncommonValueThreshold) {
			return generateUncommonValue();
		}
		else if (decisionValue < blankValueThreshold) {
			return generateBlankValue();
		}
		else {
			return generateErrorValue();
		}
	}

	private String generateCommonValue() {
		if (trackCoverage) {
			return getRandomValue(commonValues, usedCommonValues);
		}
		else {
			return getRandomValue(commonValues);			
		}
	}
	
	private String generateUncommonValue() {
		
		if (trackCoverage) {
			return getRandomValue(uncommonValues, usedUncommonValues);
		}
		else {
			return getRandomValue(uncommonValues);
		}
	}
	
	private String generateBlankValue() {
		return BLANK_VALUE;
	}
	
	private String generateErrorValue() {
		if (trackCoverage) {
			return getRandomValue(errorValues, usedErrorValues);
		}
		else {
			return getRandomValue(errorValues);
		}
	}
	
	private String getRandomValue(final ArrayList<String> list) {
		int numberOfItems = list.size();
		int randomIndex 
			= randomIndexGenerator.nextInt(numberOfItems);
		String result
			= list.get(randomIndex);
		return result;		
	}
	
	private String getRandomValue(
		final ArrayList<String> list,
		final HashSet<String> usedItemTrackingSet) {
		int numberOfItems = list.size();
		int randomIndex 
			= randomIndexGenerator.nextInt(numberOfItems);
		String result
			= list.get(randomIndex);		
		usedItemTrackingSet.add(result);
		return result;		
	}
		
	public void addCommonFieldValue(
		final String commonFieldValue) {
		
		commonValues.add(commonFieldValue);
	}
	
	public void addUncommonFieldValue(
		final String uncommonFieldValue) {

		uncommonValues.add(uncommonFieldValue);
		
	}
	
	public void addErrorFieldValue(
		final String errorFieldValue) {
		
		errorValues.add(errorFieldValue);
	}
	
	public boolean fullCommonValueCasesAchieved() {
		if (commonValues.size() == usedCommonValues.size()) {
			return true;
		}
		return false;
	}
	
	public boolean fullUncommonValueCasesAchieved() {
		if (commonValues.size() == usedUncommonValues.size()) {
			return true;
		}
		return false;		
	}
	
	public boolean fullErrorValueCasesAchieved() {
		if (errorValues.size() == usedErrorValues.size()) {
			return true;
		}
		return false;		
	}
	
	public boolean fullCoverageAchieved() {
		if (fullCommonValueCasesAchieved() &&
			fullUncommonValueCasesAchieved() &&
			fullErrorValueCasesAchieved()) {
			
			return true;
		}
		
		return false;
	}
	
	public void clearUsedFieldValues() {
		usedCommonValues.clear();
		usedUncommonValues.clear();
		usedErrorValues.clear();
	}
	
	public void setTrackCoverage(final boolean trackCoverage) {
		this.trackCoverage = trackCoverage;
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


