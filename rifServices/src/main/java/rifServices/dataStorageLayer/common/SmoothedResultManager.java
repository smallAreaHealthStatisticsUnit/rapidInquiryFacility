package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.RIFResultTable;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.Sex;

public interface SmoothedResultManager extends SQLManager {
	
	ArrayList<Sex> getSexes(
			Connection connection,
			String studyID)
					throws RIFServiceException;
	
	ArrayList<Integer> getYears(
			Connection connection,
			String studyID)
									throws RIFServiceException;
	
	String[] getColumnNameDescriptions(String[] columnNames);
	
	String[] getGeographyAndLevelForStudy(
			Connection connection,
			String studyID)
													throws RIFServiceException;
	
	String[] getDetailsForProcessedStudy(
			Connection connection,
			String studyID)
																	throws RIFServiceException;
	
	//TODO: to SQLserver
	String[] getHealthCodesForProcessedStudy(
			Connection connection,
			String studyID)
					throws RIFServiceException;
	
	RIFResultTable getStudyTableForProcessedStudy(
			Connection connection,
			String studyID,
			String type,
			String stt,
			String stp)
									throws RIFServiceException;
	
	RIFResultTable getSmoothedResults(
			Connection connection,
			String studyID,
			Sex sex)
													throws RIFServiceException;
	
	RIFResultTable getSmoothedResultsForAttributes(
			Connection connection,
			ArrayList<String> smoothedAttributesToInclude,
			String studyID,
			Sex sex)
																	throws RIFServiceException;
	
	RIFResultTable getPopulationPyramidData(
			Connection connection,
			String studyID,
			Integer year)
																					throws RIFServiceException;
}
