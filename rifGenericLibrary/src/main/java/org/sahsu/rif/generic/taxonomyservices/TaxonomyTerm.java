package org.sahsu.rif.generic.taxonomyservices;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Describes the concept of a taxonomy term.  It comprises the following fields:
 * <ul>
 * <li>label</li>
 * <ll>description</li>
 * <li>parentTerm</li>
 * <li>childTerms</li>
 * </ul>
 *
 *<p>
 * There are two aspects of design I considered in making the class.  Initially 
 * I wanted to add a machine-readable <code>identifier</code> field to complement
 * the human-readable <code>label</code> field.  To show the difference, consider
 * a term like "testosterone", which could mean a hormone or a steroid.  If you wanted
 * to have both contexts preserved in a taxonomy, you'd have two terms which had
 * different identifiers, the same human-readable labels, and different descriptions.
 * </p>
 * 
 * <p>
 * However, most of the taxonomies the RIF uses expose human users to the codes that would
 * seem better reserved for machines.  For example, "J45" is a label and "asthma" would
 * be a description.  Some users would actually look up and remember ICD codes.
 * </p>
 * 
 * <p>
 * Note that in the ICD codes, the different contexts of "asthma" are
 * reflected in different ICD codes (eg: J45.20 is "uncomplicated mild intermittent asthma" and
 * J45.30 is "uncomplicated mild persistent asthma").
 * </p>
 * 
 * <p>
 * We could have made "J45" as an identifier, "asthma" as a label, but then there may not be
 * a description.  To simplify the taxonomy services for the RIF, I decided to go with just
 * having label and description.
 * </p>
 * 
 * <p>
 * A taxonomy term should also have a name space, but rather than storing this information with
 * each term, I've made it a property of the taxonomy service.  For example, "icd10" could be
 * used to set a name space field in each term.  But instead, the name space is provided in the
 * <code>getIdentifier()</code> method of 
 * {@code TaxonomyServiceAPI}.
 */
@XmlRootElement(name="healthCode")
// @XmlAccessorType(XmlAccessType.FIELD)
// @XmlType(propOrder= {
// 		"identifier",
// 		"label",
// 		"description",
// 		"isTopLevelTerm"
// })
final public class TaxonomyTerm {

	/** The label. */
	private String label;
	
	/** The name space. */
	private String nameSpace;
	
	/** The description. */
	private String description;
	
	/** The parent term. */
	private TaxonomyTerm parentTerm;
	
	/** The sub terms. */
	private ArrayList<TaxonomyTerm> childTerms;

	/**
	 * Instantiates a new taxonomy term.
	 */
	public TaxonomyTerm() {
		
		childTerms = new ArrayList<>();
		parentTerm = null;
	}

	/**
	 * New instance.
	 *
	 * @return the taxonomy term
	 */
	public static TaxonomyTerm newInstance() {

		return new TaxonomyTerm();
	}

	public boolean hasMatchingLabel(final String targetLabel) {
		return label.equals(targetLabel);		
	}
	
	public static boolean hasTermMatchingLabel(
		final List<TaxonomyTerm> taxonomyTerms,
		final String targetLabel) {
		
		for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
			if (taxonomyTerm.getLabel().equals(targetLabel)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Adds the sub terms.
	 *
	 * @param childTerms the sub terms
	 */
	public void addChildTerms(final ArrayList<TaxonomyTerm> childTerms) {

		this.childTerms.addAll(childTerms);
	}
	
	/**
	 * Gets the sub terms.
	 *
	 * @return the sub terms
	 */
	public ArrayList<TaxonomyTerm> getChildTerms() {
		
		return childTerms;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	@XmlElement(required = true)
	public String getLabel() {
		
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(
		final String label) {
		
		this.label = label;
	}

	/**
	 * Gets the name space.
	 *
	 * @return the name space
	 */
	public String getNameSpace() {

		return nameSpace;
	}

	/**
	 * Sets the name space.
	 *
	 * @param nameSpace the new name space
	 */
	public void setNameSpace(
		final String nameSpace) {

		this.nameSpace = nameSpace != null ? nameSpace.trim() : null;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@XmlElement(required = true)
	public String getDescription() {
		
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(final String description) {
		
		this.description = description;
	}
	
	/**
	 * Gets the parent term.
	 *
	 * @return the parent term
	 */
	public TaxonomyTerm getParentTerm() {
		return parentTerm;
	}
	
	/**
	 * Sets the parent term.
	 *
	 * @param parentTerm the new parent term
	 */
	public void setParentTerm(final TaxonomyTerm parentTerm) {
		
		this.parentTerm = parentTerm;
	}

	@XmlElement(required = true)
	public String getIdentifier() {

		return label + "-" + nameSpace;
	}

	public String toString() {

		ToStringBuilder builder = new ToStringBuilder(this)
				                          .append(label).append(nameSpace).append(description);
		return builder.toString();
	}
}
