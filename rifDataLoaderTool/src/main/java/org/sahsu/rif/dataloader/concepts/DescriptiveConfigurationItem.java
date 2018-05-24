package org.sahsu.rif.dataloader.concepts;

import org.sahsu.rif.generic.presentation.DisplayableListItemInterface;

/**
 * An interface that was designed to render items in a table with 
 * "display name" and "description" columns rather than a list with
 * just a "display name" value.  For convenience, it extends the interface
 * that is already used to generally describe things in a list.  
 * 
 * <p>
 * The interface was originally created to help support the data loader tool's
 * configuration hints feature.  Normally, the name of a 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} or a 
 * {@link DataSetFieldConfiguration} is
 * sufficient to let a viewer pick an item they want from a list.  
 * </p>
 *  
 * <p>
 * When these classes are used to support hints, their display names are usually
 * cryptic-looking regular expression patterns whose meaning or purpose may not
 * be immediately obvious.  In order to help users identify a hint by the meaning
 * rather than the syntax of the hint expression, we needed to have a table view
 * that could show both the expression and a description for it.
 * </p>
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

public interface DescriptiveConfigurationItem extends
		DisplayableListItemInterface {

	public String getDescription();
}


