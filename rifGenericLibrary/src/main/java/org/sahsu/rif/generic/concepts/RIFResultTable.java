package org.sahsu.rif.generic.concepts;


import java.util.Arrays;


/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
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

public class RIFResultTable {

	// ==========================================
	// Section Constants
	// ==========================================
	
	public static enum ColumnDataType {TEXT, NUMERIC, JSON};
	
	
	// ==========================================
	// Section Properties
	// ==========================================
	private String[] columnNameDescriptions;	
	private String[] columnNames;
	private ColumnDataType[] columnDataTypes;
	
	private String[][] data;
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFResultTable() {
	}

	public static RIFResultTable newInstance() {
		RIFResultTable rifResultTable = new RIFResultTable();
		return rifResultTable;
	}
	
	public static RIFResultTable createCopy(
		final RIFResultTable originalRIFResultTable) {

		RIFResultTable cloneRIFResultTable
			= new RIFResultTable();
		
		String[] originalColumnNames = originalRIFResultTable.getColumnNames();
		String[] originalColumnDescriptions = originalRIFResultTable.getColumnNameDescriptions();
		ColumnDataType[] originalColumnDataTypes = originalRIFResultTable.getColumnDataTypes();
		if (originalColumnNames == null) {
			//assume that the other columns will be null as well
			cloneRIFResultTable.setColumnProperties(null, null, null);
		}
		else {
			String[] cloneColumnNames
				= Arrays.copyOf(originalColumnNames, originalColumnNames.length);
			String[] cloneColumnDescriptions
				= Arrays.copyOf(originalColumnNames, originalColumnDescriptions.length);

			ColumnDataType[] cloneColumnDataTypes
				= Arrays.copyOf(originalColumnDataTypes, originalColumnDataTypes.length);
			
			cloneRIFResultTable.setColumnProperties(
				cloneColumnNames, 
				cloneColumnDescriptions, 
				cloneColumnDataTypes);	
			
		}
		String[][] originalData
			= originalRIFResultTable.getData();
		if (originalData == null) {
			cloneRIFResultTable.setData(null);
		}
		else {
			String[][] clonedData = new String[originalData.length][];
			for (int i = 0; i < originalData.length; i++) {
				clonedData[i] = Arrays.copyOf(originalData[i], originalData.length);
			}
			cloneRIFResultTable.setData(clonedData);
		}
		
		return cloneRIFResultTable;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void setColumnProperties(
		final String[] columnNames,
		final ColumnDataType[] columnDataTypes) {

		this.columnNames = columnNames;
		columnNameDescriptions = new String[columnNames.length];
		for (int i = 0; i < columnNameDescriptions.length; i++) {
			columnNameDescriptions[i] = "";
		}
		this.columnDataTypes = columnDataTypes;
	}
	
	public void setColumnProperties(
		final String[] columnNames,
		final String[] columnNameDescriptions,
		final ColumnDataType[] columnDataTypes) {
		
		this.columnNames = columnNames;
		this.columnNameDescriptions = columnNameDescriptions;
		this.columnDataTypes = columnDataTypes;
	}
	
	public void setColumnProperties(
		final String[] fieldNames) {
		
		this.columnNames = fieldNames;
		columnNameDescriptions = new String[fieldNames.length];
		for (int i = 0; i < columnNameDescriptions.length; i++) {
			columnNameDescriptions[i] = "";
		}
		
		columnDataTypes = new ColumnDataType[fieldNames.length];
		for (int i = 0; i < columnDataTypes.length; i++) {
			columnDataTypes[i] = ColumnDataType.TEXT;
		}
	}	
	
	public String[] getColumnNameDescriptions() {
		return columnNameDescriptions;
	}
	
	public ColumnDataType[] getColumnDataTypes() {
		return columnDataTypes;
	}
	
	public String[] getColumnNames() {
		return columnNames;
	}
	
	public void setData(final String[][] data) {
		this.data = data;
	}
	
	public String[][] getData() {
		return data;
	}
	
	
	public void print() {
		print(null);		
	}
	
	// http://javadox.com/org.codehaus.jettison/jettison/1.1/org/codehaus/jettison/json/JSONObject.html
	public static String quote(String string) {
         if (string == null || string.length() == 0) {
             return "\"\"";
         }

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         String       t;

         sb.append('"');
         for (i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
                 sb.append('\\');
                 sb.append(c);
                 break;
             case '/':
 //                if (b == '<') {
                     sb.append('\\');
 //                }
                 sb.append(c);
                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\r");
                break;
             default:
                 if (c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         sb.append('"');
         return sb.toString();
     }
	 
	public void print(Integer numberOfLines) {

		System.out.println("Table has dimensions of rows="+data.length + "==columns="+columnNames.length+"==");

		//Print the header: column names
		StringBuilder tableColumnNameLine = new StringBuilder();
		for (int i = 0 ; i < columnNames.length; i++) {
			if (i != 0) {
				tableColumnNameLine.append("\t");
			}
			tableColumnNameLine.append(columnNames[i]);
		}
		System.out.println(tableColumnNameLine.toString());
		
		//Print the header: column data types
		StringBuilder tableColumnTypeLine = new StringBuilder();
		for (int i = 0 ; i < columnDataTypes.length; i++) {
			if (i != 0) {
				tableColumnTypeLine.append("\t");
			}
			if (columnDataTypes[i] == RIFResultTable.ColumnDataType.NUMERIC) {
				tableColumnTypeLine.append("N");				
			}
			else {
				tableColumnTypeLine.append("T");			
			}
		}
		System.out.println(tableColumnTypeLine.toString());
		
		//Print the header: column name descriptions
		
		
		
		if (numberOfLines == null) {
			
			//print all of the lines
			for (int ithRow = 0; ithRow < data.length; ithRow++) {
				printLine(data[ithRow]);
			}
		}
		else {
			//print only some of the lines
			for (int ithRow = 0; ithRow < numberOfLines; ithRow++) {
				printLine(data[ithRow]);
			}
		}		
	}
	
	private void printLine(final String... rowData) {
		StringBuilder row = new StringBuilder();
		for (int i = 0; i < rowData.length; i++) {
			if (i != 0) {
				row.append("\t");
			}
			row.append(rowData[i]);
		}
		System.out.println(row.toString());
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
