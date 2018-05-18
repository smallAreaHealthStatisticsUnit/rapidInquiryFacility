package org.sahsu.rif.generic.system;

import java.text.Collator;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
	
	public static final String RIF_SERVICE_MESSAGES = "RIFServiceMessages";
	public static final String DATA_LOADER_TOOL_MESSAGES = "RIFDataLoaderToolMessages";
	private static final String RIF_GENERIC_LIBRARY_MESSAGES = "RIFGenericLibraryMessages";
	private static final String TAXONOMY_SERVICE_MESSAGES = "TaxonomyServiceMessages";
	
	private static Collection<ResourceBundle> bundles = new ArrayList<>();
	private static Collator collator;
	
	public static Messages serviceMessages() {
		
		return new Messages(ResourceBundle.getBundle(RIF_SERVICE_MESSAGES));
	}
	
	public static Messages genericMessages() {
		
		return new Messages(ResourceBundle.getBundle(RIF_GENERIC_LIBRARY_MESSAGES));
	}
	
	public static Messages dataLoaderMessages() {
		
		return new Messages(ResourceBundle.getBundle(DATA_LOADER_TOOL_MESSAGES));
	}
	
	public static Messages taxonomyMessages() {
		
		return new Messages(ResourceBundle.getBundle(TAXONOMY_SERVICE_MESSAGES));
	}
	
	public Messages(ResourceBundle rb) {
		
		if (!bundles.contains(rb)) {
			
			bundles.add(rb);
		}
	}
	
	private static final SimpleDateFormat TIMESTAMP_FORMAT =
					new SimpleDateFormat("dd-MMM-yyyy_HH_mm_ss");
	private static final SimpleDateFormat PHRASE_FORMAT =
					new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss:SSS");
	private static final SimpleDateFormat DATE_FORMAT =
					new SimpleDateFormat("dd-MMM-yyyy");
	
	// ==========================================
	// Section Accessors
	// ==========================================
	
	public String getTimeStampForFileName(final Date date) {

		return TIMESTAMP_FORMAT.format(date);
	}
	
	/**
	 * Gets the time phrase.
	 *
	 * @param date the date
	 * @return the time phrase
	 */
	public String getTimePhrase(final Date date) {
		
		return PHRASE_FORMAT.format(date);
	}
	
	
	/**
	 * Gets the date.
	 *
	 * @param datePhrase the date phrase
	 * @return the date
	 */
	public Date getDate(final String datePhrase) {
		
		Date result = null;
		try {
			result = DATE_FORMAT.parse(datePhrase);
		}
		catch(ParseException parseException) {
			//@TODO decide whether to handle this particular
			//exception or ignore it
		}
		return result;
	}
	
	/**
	 * Gets the time.
	 *
	 * @param timePhrase the time phrase
	 * @return the time
	 */
	public Date getTime(final String timePhrase) {
		
		Date result = null;
		try {
			result = PHRASE_FORMAT.parse(timePhrase);
		}
		catch(ParseException parseException) {
			//@TODO decide whether to handle this particular
			//exception or ignore it
		}
		
		return result;
	}
	
	/**
	 * Gets the date phrase.
	 *
	 * @param date the _date
	 * @return the date phrase
	 */
	public String getDatePhrase(final Date date) {
		
		return DATE_FORMAT.format(date);
	}
	
	
	
	/**
	 * Gets the locale.
	 *
	 * @return the locale
	 */
	public Locale getLocale() {
		
		return Locale.getDefault();
	}
	
	/**
	 * Gets the collator.
	 *
	 * @return the collator
	 */
	public Collator getCollator() {
		
		if (collator == null) {
			collator = Collator.getInstance();
		}
		
		return (Collator) collator.clone();
	}
	
	/**
	 * Gets the message.
	 *
	 * @param key the key
	 * @return the message
	 */
	public String getMessage(final String key) {
		
		for (ResourceBundle bundle : bundles) {
			if (bundle.containsKey(key)) {
				
				return (bundle.getString(key));
			}
		}
		return key;
	}
	
	/**
	 * Gets the message
	 *
	 * @param key the key value
	 * @param parameters any parameters for the message
	 * @return the formatted message
	 */
	public String getMessage(String key, String... parameters) {
		
		String messageWithBlanks = getMessage(key);
		MessageFormat messageFormat = new MessageFormat(messageWithBlanks);
		return messageFormat.format(parameters);
	}
	
	/**
	 * Sets the collator.
	 *
	 * @param coll the new collator
	 */
	public void setCollator(final Collator coll) {
		
		collator = coll;
	}
	
	/**
	 * Checks whether we know about the key.
	 * @param key the key to check
	 * @return true if the key is present
	 */
	public boolean containsKey(String key) {
		
		for (ResourceBundle bundle : bundles) {
			if (bundle.containsKey(key)) {
				return true;
			}
		}
		return false;
	}
	
}
