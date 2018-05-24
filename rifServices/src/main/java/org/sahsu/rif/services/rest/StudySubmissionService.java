package org.sahsu.rif.services.rest;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


import java.util.Set;
import java.util.HashSet;

import org.sahsu.rif.generic.util.RIFLogger;

//TOUR_WEB_SERVICES-1
/*
 * This describes a service that will be accessible by client applications.
 * For example, the web-based study submission written in JavaScript will
 * make Ajax calls using a url like this:
 * 
 * http://localhost:8080/rifServices/studySubmission/getGeographies?userID=kgarwood
 * 
 * The "studySubmission" part of the URL comes from the ApplicationPath annotation
 * shown below.
 */

@ApplicationPath("/studySubmission")
public class StudySubmissionService extends Application {

	public StudySubmissionService() {
		final RIFLogger rifLogger = RIFLogger.getLogger();
		rifLogger.info(getClass(), "!!!!!!!!!!!!!!!!!!!!! "
		               + getClass().getSimpleName() + " !!!!!!");
	}

	@Override
	public Set<Class<?>> getClasses() {
		
		Set<Class<?>> set = new HashSet<>();
		set.add(StudySubmissionServiceResource.class);
		return set;
	}
	
}
