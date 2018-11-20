package org.sahsu.rif.generic.concepts;

import java.util.Collection;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A grouping or collection of {@code Parameter} objects, suitable for passing to services.
 */
@XmlRootElement
public class Parameters {

	private Collection<Parameter> parameters;

	@SuppressWarnings("unused")
	private Parameters() {
		// Needed for JAXB
	}

	public Parameters(final Collection<Parameter> parameters) {

		this.parameters = parameters;
	}

	public void add(Parameter p) {

		parameters.add(p);
	}

	public Collection<Parameter> getParameters() {

		return parameters;
	}

	// Needed for JAXB.
	public void setParameters(Collection<Parameter> parameters) {

		this.parameters = parameters;
	}

	public Stream<Parameter> stream() {

		return getParameters().stream();
	}
}
