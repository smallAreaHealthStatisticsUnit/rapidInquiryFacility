package org.sahsu.taxonomyservices;

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
 * {@code FederatedTaxonomyService}.
 */
public class TaxonomyServiceProvider implements Comparable<TaxonomyServiceProvider> {

	private String identifier;
	private String name;
	private String description;
	
	TaxonomyServiceProvider() {

	}

	public void setName(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	String getIdentifier() {
		return identifier;
	}
	
	void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}
	
	public String getDescription() {		
		return description;
	}
	
	public void setDescription(final String description) {

		this.description = description;
	}

	@Override
	public int compareTo(final TaxonomyServiceProvider o) {

		return getIdentifier().compareTo(o.getIdentifier());
	}
}
