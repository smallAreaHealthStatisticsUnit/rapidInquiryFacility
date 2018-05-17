package rifServices.restfulWebServices.pg;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.Set;
import java.util.HashSet;

import rifGenericLibrary.util.RIFLogger;
import rifServices.restfulWebServices.StudyResultRetrievalServiceResource;

@ApplicationPath("/studyResultRetrieval/pg")
public class PGSQLRIFStudyResultRetrievalWebServiceApplication extends Application {

	public PGSQLRIFStudyResultRetrievalWebServiceApplication() {
		final RIFLogger rifLogger = RIFLogger.getLogger();
		rifLogger.info(this.getClass(), "!!!!!!!!!!!!!!!!!!!!! "
		                                + getClass().getSimpleName() + " !!!!!!");
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = new HashSet<>();
		set.add(StudyResultRetrievalServiceResource.class);
		return set;
	}
	
}
