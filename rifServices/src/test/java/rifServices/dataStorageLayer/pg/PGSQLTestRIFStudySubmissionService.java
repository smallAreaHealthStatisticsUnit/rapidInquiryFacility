package rifServices.dataStorageLayer.pg;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.util.FieldValidationUtility;
import rifServices.businessConceptLayer.AbstractCovariate;
import rifServices.businessConceptLayer.AdjustableCovariate;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.ExposureCovariate;
import rifServices.businessConceptLayer.GeoLevelToMap;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.RIFStudySubmissionAPI;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.common.CovariateManager;
import rifServices.dataStorageLayer.common.DiseaseMappingStudyManager;
import rifServices.dataStorageLayer.common.HealthOutcomeManager;
import rifServices.dataStorageLayer.common.SQLManager;
import rifServices.dataStorageLayer.common.SubmissionManager;
import rifServices.ontologyServices.HealthCodeProviderInterface;

public final class PGSQLTestRIFStudySubmissionService extends PGSQLAbstractRIFStudySubmissionService
		implements RIFStudySubmissionAPI {

	public PGSQLTestRIFStudySubmissionService() {

	}

}

