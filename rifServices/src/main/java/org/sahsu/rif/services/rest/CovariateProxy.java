package org.sahsu.rif.services.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="covariate")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder= {
	"name",
	"covariateType",
	"minimumValue",
	"maximumValue",
	"description"
	})
final public class CovariateProxy {

 	@XmlElement(required = true)
    private String covariateType;	
	@XmlElement(required = true)	
	private String maximumValue;
	@XmlElement(required = true)	
	private String minimumValue;
	@XmlElement(required = true)
	private String name;
	@XmlElement()
	private String description;
	
	CovariateProxy() {

	}

	public String getCovariateType() {
		return covariateType;
	}

	void setCovariateType(final String covariateType) {
		this.covariateType = covariateType;
	}

	public String getMaximumValue() {
		return maximumValue;
	}

	void setMaximumValue(final String maximumValue) {
		this.maximumValue = maximumValue;
	}

	public String getMinimumValue() {
		return minimumValue;
	}

	void setMinimumValue(final String minimumValue) {
		this.minimumValue = minimumValue;
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
}
