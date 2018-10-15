package org.sahsu.rif.services.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="healthCode")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder= {
	"code",
	"description",
	"nameSpace",
	"isTopLevelTerm",
	"numberOfSubTerms"
	})
final public class CalculationMethodProxy {

 	@XmlElement(required = true)
	private String codeRoutineName;

 	@XmlElement(required = true)
	private String prior;
	
 	@XmlElement(required = true)		
	private String description;
	
 	@XmlElement(required = true)		
	private List<ParameterProxy> parameterProxies;

 	CalculationMethodProxy() {

	}

	/*
	 * IntelliJ flags this as not used, but it is needed for Jackson, which
	 * calls it implicitly.
	 */
	public String getCodeRoutineName() {
		return codeRoutineName;
	}


	void setCodeRoutineName(final String codeRoutineName) {
		this.codeRoutineName = codeRoutineName;
	}


	public String getPrior() {
		return prior;
	}


	public void setPrior(final String prior) {
		this.prior = prior;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(final String description) {
		this.description = description;
	}

	/*
	 * IntelliJ flags this as not used, but it is needed for Jackson, which
	 * calls it implicitly.
	 */
	public List<ParameterProxy> getParameterProxies() {
		return parameterProxies;
	}


	void setParameterProxies(final List<ParameterProxy> parameterProxies) {
		this.parameterProxies = parameterProxies;
	}
}
