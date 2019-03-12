
package org.sahsu.rif.services.fileformats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.sahsu.rif.generic.fileformats.AbstractXMLContentHandler;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.ExposureCovariate;
import org.xml.sax.Attributes;

public final class CovariateContentHandler
	extends AbstractXMLContentHandler {

	/** The covariates. */
	private List<AbstractCovariate> covariates;
	
	/** The current covariate. */
	private AbstractCovariate currentCovariate;
	
	/** The Constant adjustableCovariateName. */
	private static final String adjustableCovariateName = "adjustable_covariate";
	
	/** The Constant exposureCovariateName. */
	private static final String exposureCovariateName = "exposure_covariate";

	/**
     * Instantiates a new covariate content handler.
     */
    CovariateContentHandler() {
		
    	setPluralRecordName("covariates");
    	setSingularRecordName("covariate");
		covariates = new ArrayList<>();
		
		ignoreXMLStartTag("name");
		ignoreXMLStartTag("minimum_value");
		ignoreXMLStartTag("maximum_value");
	    ignoreXMLStartTag("covariate_type");
    }

	/**
	 * Gets the covariates.
	 *
	 * @return the covariates
	 */
	public List<AbstractCovariate> getCovariates() {
		
		return covariates;
	}
	
	/**
	 * Write xml.
	 *
	 * @param covariates the covariates
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final List<AbstractCovariate> covariates) throws IOException {

		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordListStartTag(getPluralRecordName());
		for (AbstractCovariate covariate : covariates) {
			writeXML(covariate);
		}		
		xmlUtility.writeRecordListEndTag(getPluralRecordName());
	}
	
	/**
	 * Write xml.
	 *
	 * @param covariate the covariate
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(final AbstractCovariate covariate) throws IOException {

		XMLUtility xmlUtility = getXMLUtility();

		xmlUtility.writeRecordStartTag(getSingularRecordName());

		String covariateName =  covariate instanceof AdjustableCovariate
				                        ? adjustableCovariateName
				                        : exposureCovariateName;
		xmlUtility.writeRecordStartTag(adjustableCovariateName);
		xmlUtility.writeField(covariateName, "name", covariate.getName());
		xmlUtility.writeField(covariateName, "minimum_value", covariate.getMinimumValue());
		xmlUtility.writeField(covariateName, "maximum_value", covariate.getMaximumValue());
		xmlUtility.writeField(covariateName,"covariate_type",
		                      covariate.getType().toString());
		xmlUtility.writeRecordEndTag(covariateName);

		xmlUtility.writeRecordEndTag(getSingularRecordName());
	}

	@Override
	public void initialise(final OutputStream outputStream,
			final XMLCommentInjector commentInjector) throws UnsupportedEncodingException {

		super.initialise(outputStream, commentInjector);
		covariates.clear();
	}
	

	@Override
	public void startElement(
			final String nameSpaceURI, final String localName, final String qualifiedName,
			final Attributes attributes) {

		if (isPluralRecordName(qualifiedName)) {
			// covariates.clear();
			activate();
		}
		else if (equalsFieldName(qualifiedName, "adjustable_covariate")) {
			currentCovariate = AdjustableCovariate.newInstance();
		}
		else if (equalsFieldName(qualifiedName, "exposure_covariate")) {
			currentCovariate = ExposureCovariate.newInstance();
		}
		else
			assert isIgnoredStartTag(qualifiedName);
	}
	

	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) {

		if (isPluralRecordName(qualifiedName)) {
			deactivate();
		}
		else if (qualifiedName.equals("adjustable_covariate")) {
			covariates.add(currentCovariate);
		}
		else if (qualifiedName.equals("exposure_covariate")) {
			covariates.add(currentCovariate);
		}
		else if (equalsFieldName(qualifiedName, "name")) {
			currentCovariate.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "minimum_value")) {
			currentCovariate.setMinimumValue(getCurrentFieldValue());
		}
		else if (equalsFieldName(qualifiedName, "maximum_value")) {
			currentCovariate.setMaximumValue(getCurrentFieldValue());
		}
		else
			assert isIgnoredEndTag(qualifiedName);
	}
}
