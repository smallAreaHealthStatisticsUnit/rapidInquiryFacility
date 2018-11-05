package org.sahsu.taxonomyservices;

import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.concepts.Parameter;

/**
 * The main business class that is used to hold data from the 
 * <code>TaxonomyServicesConfiguration.xml</code> file found in the resources
 * directory.
 *
 * @author kgarwood
 */

public class TaxonomyServiceConfiguration {

	private String serviceIdentifier;
	private String name;
	private String description;
	private String version;
	private String ontologyServiceClassName;
	
	private List<Parameter> parameters;

	private TaxonomyServiceConfiguration() {
		parameters = new ArrayList<>();
	}
	
	public static TaxonomyServiceConfiguration newInstance() {

		return new TaxonomyServiceConfiguration();
	}

	public String getServiceIdentifier() {
		return serviceIdentifier;
	}
	void setServiceIdentifier(final String serviceIdentifier) {
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
	String getOntologyServiceClassName() {
		return ontologyServiceClassName;
	}
	void setOntologyServiceClassName(final String ontologyServiceClassName) {
		this.ontologyServiceClassName = ontologyServiceClassName;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void setParameters(final List<Parameter> parameters) {
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
