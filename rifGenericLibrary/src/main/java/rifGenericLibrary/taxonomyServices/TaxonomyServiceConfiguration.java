package rifGenericLibrary.taxonomyServices;

import rifGenericLibrary.businessConceptLayer.Parameter;

import java.util.ArrayList;

/**
 * The main business class that is used to hold data from the 
 * <code>TaxonomyServicesConfiguration.xml</code> file found in the resources
 * directory.
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

public class TaxonomyServiceConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String serviceIdentifier;
	private String name;
	private String description;
	private String version;
	private String ontologyServiceClassName;
	
	private ArrayList<Parameter> parameters;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private TaxonomyServiceConfiguration() {
		parameters = new ArrayList<Parameter>();
		
	}
	
	public static TaxonomyServiceConfiguration newInstance() {
		TaxonomyServiceConfiguration taxonomyServiceConfiguration
			= new TaxonomyServiceConfiguration();
		
		return taxonomyServiceConfiguration;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getServiceIdentifier() {
		return serviceIdentifier;
	}
	public void setServiceIdentifier(final String serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(final String description) {
		this.description = description;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(final String version) {
		this.version = version;
	}
	public String getOntologyServiceClassName() {
		return ontologyServiceClassName;
	}
	public void setOntologyServiceClassName(final String ontologyServiceClassName) {
		this.ontologyServiceClassName = ontologyServiceClassName;
	}
	public ArrayList<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(final ArrayList<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(final Parameter parameter) {
		parameters.add(parameter);
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
