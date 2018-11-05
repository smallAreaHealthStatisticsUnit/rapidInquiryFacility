package org.sahsu.rif.generic.taxonomyservices;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlRootElement
public class TermList {

	@XmlElementWrapper
	@XmlElementRef
	private final List<TaxonomyTerm> terms;

	public TermList() {

		// I don't want this to be called, but it's needed for the @XmlRootElement annotation.
		terms = Collections.emptyList();
	}

	public TermList(final List<TaxonomyTerm> terms) {

		this.terms = terms;
	}

	public List<TaxonomyTerm> getTerms() {

		return terms;
	}

	public String toString() {

		ToStringBuilder builder = new ToStringBuilder(this).append("Terms", terms);
		return builder.toString();
	}

	public boolean listIsUsable() {

		return (terms != null && !terms.isEmpty());
	}
}
