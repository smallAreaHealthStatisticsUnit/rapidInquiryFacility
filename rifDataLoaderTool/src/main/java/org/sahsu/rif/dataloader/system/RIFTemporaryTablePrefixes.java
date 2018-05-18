package org.sahsu.rif.dataloader.system;

/**
 * Defines the prefixes of all the temporary and permanent tables that will be produced
 * as a data source is processed by the RIF.  The prefixes will be combined with the 
 * <code>coreTableName</code> field of the {@link rifDataLoaderTool.businessConceptLayer.DataSet}.
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

public enum RIFTemporaryTablePrefixes {

	EXTRACT("extract_"),
	CLEAN_SEARCH_REPLACE("cln_srch_"), //search and replace poor values
	CLEAN_VALIDATION("cln_val_"),
	CLEAN_CASTING("cln_cast_"), //cast cleaned fields to correct data types
	CLEAN_AUDIT_WITH("tp1_"),
	CLEAN_AUDIT("audit_clean_"),
	CLEAN_FINAL("clean_"),
	CONVERT("convert_"),
	COMBINE("combine_"),
	OPTIMISE("optimise_"),
	CHECK("check_"),
	PUBLISH("publish_"),
	EMPTY_CHECK("dq_empty_"),
	EMPTY_PER_YEAR_CHECK("dq_empty_yr_"),
	AUD_CHANGES("aud_chg_"),
	AUD_FAILED_VALIDATION("aud_val_");
	
	private String tablePrefix;
	
	private RIFTemporaryTablePrefixes(
		final String tablePrefix) {
		
		this.tablePrefix = tablePrefix;
	}
	
	public String getTablePrefix() {

		return tablePrefix;
	}
	
	public String getTableName(
		final String coreTableName) {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(tablePrefix);
		buffer.append(coreTableName);
		return buffer.toString();
		
	}
}


