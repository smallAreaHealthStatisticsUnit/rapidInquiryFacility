package org.sahsu.rif.services.datastorage.common;


/**
 * The purpose of this service is to provide a secure implementation of 
 * {@link rifServices.dataStorageLayer.RIFStudyResultRetrievalAPI} which
 * is final - meaning that it cannot be overridden by the client.  
 * 
 * <p>
 * The main reason for having this class is to help 'close off' any protected 
 * method that might generate a security concern or might only serve a testing 
 * purpose.
 * </p>
 */

//TOUR_CONCURRENCY
/**
 * This class is the version of the retrieval service that is used to support
 * production rather than testing purposes.  It is a final class to ensure that
 * it cannot be sub-classed in ways that would create concurrency problems.
 */
//TOUR_SECURITY

import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;

/**
 * <p>
 * The class is declared final to prevent a malicious client application to submit a subclass
 * that overrides methods to produce harmful effects.  It should be visible only within this 
 * package.  It is made available through {@link rifServices.dataStorageLayer.MSSQLProductionRIFStudyServiceBundle},
 * but that class only retrieves it as an instance of {@link RIFStudyResultRetrievalAPI}.
 * Client code cannot cast the RIFStudyResultRetrievalAPI to a ProductionRIFStudySubmissionService because
 * this class is not visible outside the package.  Therefore, the client would not be able to use
 * casting as a way of getting access to more of a class's methods than are advertised through the 
 * interface it implements.
 * </p>
 *  
 */
public final class ProductionStudyRetrievalService extends StudyRetrievalService {

	public ProductionStudyRetrievalService() {

	}
}
