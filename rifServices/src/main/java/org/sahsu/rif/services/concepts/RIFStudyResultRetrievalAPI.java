package org.sahsu.rif.services.concepts;

import java.util.ArrayList;
import java.util.Locale;

import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;

public interface RIFStudyResultRetrievalAPI extends RIFStudyServiceAPI {

	RIFResultTable getTileMakerCentroids(
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
					throws RIFServiceException;
					
	String getMapBackground(
			final User user,
			final Geography geography)
					throws RIFServiceException;		
					
	String getHomogeneity(
			final User user,
			final String studyID)
					throws RIFServiceException;	
					
	String getCovariateLossReport(
			final User user,
			final String studyID)
					throws RIFServiceException;		
					
	String getRiskGraph(
			final User user,
			final String studyID)
					throws RIFServiceException;		
					
	String getSelectState(
			final User user,
			final String studyID)
					throws RIFServiceException;	
					
	String getPrintState(
			final User user,
			final String studyID)
					throws RIFServiceException;		
					
	String setPrintState(
			final User user,
			final String studyID,
			final String printStateText)
					throws RIFServiceException;		
					
	String getPostalCodes(
			final User user,
			final Geography geography,
			final String postcode,
			final Locale locale)
					throws RIFServiceException;		
					
	String getPostalCodeCapabilities(
			final User user,
			final Geography geography)
					throws RIFServiceException;

	String getTileMakerTiles(
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect,
			final Integer zoomlevel,
			final Integer x,
			final Integer y,
			final String tileType)
					throws RIFServiceException;

	String getTileMakerAttributes(
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect)
					throws RIFServiceException;
					
	ArrayList<Integer> getYearsForStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;

	ArrayList<Sex> getSexesForStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;

	String[] getGeographyAndLevelForStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;

	RIFResultTable getSmoothedResults(
			final User user,
			final String studyID,
			final String sex) 
					throws RIFServiceException;

	RIFResultTable getPopulationPyramidData(
			final User user,
			final String studyID,
			final String year)
					throws RIFServiceException;

	RIFResultTable getCurrentStatusAllStudies(
			final User user)
					throws RIFServiceException;

	String[] getDetailsForProcessedStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;
	
	RIFResultTable getStudyTableForProcessedStudy(
			final User user,
			final String studyID,
			final String type,
			final String stt,
			final String stp)
					throws RIFServiceException;
	
	String[] getHealthCodesForProcessedStudy(
			final User user,
			final String studyID)
					throws RIFServiceException;

	RIFServiceInformation getRIFServiceInformation(User user) throws RIFServiceException;
}
