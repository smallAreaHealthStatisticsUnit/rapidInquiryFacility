package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;

import rifGenericLibrary.presentationLayer.DisplayableListItemInterface;

/**
 * An interface for RIF data types. The purpose of this interface is to encourage
 * the rest of calling code to ignore implementation details of each specific
 * sub-class of {@link rifDataLoaderTool.businessConceptLayer.rifDataTypes.AbstractRIFDataType}.  
 * However, there are cases when the SQL code generators need to know which kind of 
 * data type is being processed.  For example, it has to know if a field has
 * a {@link rifDataLoaderTool.businessConceptLayer.rifDataTypes.DateRIFDataType} so it knows
 * to call a database function instead of making a comparison using regular expressions.
 *  
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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


public interface RIFDataTypeInterface extends DisplayableListItemInterface {
	public String getName();
	public String getDescription();
	
	public RIFFieldValidationPolicy getFieldValidationPolicy();
	public String getMainValidationExpression();
	public ArrayList<String> getValidationExpressions();
	public String getValidationFunctionName();
	public String getValidationFunctionParameterValues();	
	public RIFFieldCleaningPolicy getFieldCleaningPolicy();
	public ArrayList<CleaningRule> getCleaningRules();
	public void setCleaningRules(ArrayList<CleaningRule> cleaningRules);
	public String getCleaningFunctionParameterValues();
	public RIFDataTypeInterface createCopy();
	public String getCleaningFunctionName();
	
}
