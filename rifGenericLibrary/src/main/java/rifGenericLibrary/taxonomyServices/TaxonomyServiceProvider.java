package rifGenericLibrary.taxonomyServices;

/**
 * A business class designed to hold information about a taxonomy service that can
 * be advertised to end-users.  Each provider has three attributes:
 * <ul>
 * <li><b>identifier</b>: a machine-readable identifier (eg: icd10)
 * <li><b>name</b>: the human-readable name of the service as it would appear to users 
 * in a list
 * (eg: "ICD 10 Code service")
 * <li><b>description</b>: a human readable description that can be used to provide
 * users with information about what the service does (eg: "Provides terms from 
 * the WHO's classification of diseases")
 * </ul>
 *
 * <p>
 * We envision that this class will be used to support a drop-down list of services in some
 * front-end component.  Users may choose to mouse over the name of a service and learn more
 * from the service description.  Once they have chosen the service they want, the identifier
 * will be used for the <code>taxonomyServiceID</code> parameter for methods called in 
 * {@link rifGenericLibrary.taxonomyServices.FederatedTaxonomyService}.
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

public class TaxonomyServiceProvider {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String identifier;
	private String name;
	private String description;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public TaxonomyServiceProvider() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	
	public String getDisplayName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}
	
	public String getDescription() {		
		return description;
	}
	
	public void setDescription(final String description) {

		this.description = description;
	}
	
	// ==========================================
	// Section Override
	// ==========================================
}
