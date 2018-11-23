package org.sahsu.stats.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.Parameters;
import org.sahsu.rif.generic.util.StatisticsLogger;

@Path("")
public class StatisticsResource {

	private static StatisticsLogger logger = StatisticsLogger.getLogger();

	@POST
	@Path(StatisticsFunctions.SCRIPT_URI)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runScript(Parameters parameters) {

		logger.info(getClass(), "In runScript, serving /statistics"
		                        + StatisticsFunctions.SCRIPT_URI);
		logger.info(getClass(), "Parameters object is "
		                        + (parameters == null ? "null" : "not null"));
		ScriptService r = ScriptService.instance();
		return r.runScript(parameters);
	}

	@GET
	@Path("/functions" + StatisticsFunctions.STUDY_ID_URI)
	@Produces(MediaType.APPLICATION_JSON)
	public StatisticsFunctions getResources(
			@PathParam(StatisticsFunctions.STUDY_ID) String studyId) {

		logger.info(getClass(), "Getting functions");
		return StatisticsFunctions.getInstance(studyId);
	}

	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public StatisticsFunctions test() {

		return StatisticsFunctions.getInstance("123");
	}

	// @POST
	// @Path(StatisticsFunctions.SMOOTHING_URI + StatisticsFunctions.STUDY_ID_URI)
	// @Consumes(MediaType.APPLICATION_JSON)
	// public Response runSmoothing(Parameters parameters) {
	//
	// 	return null;
	// }

	// @POST
	// @Path(StatisticsFunctions.RISK_ANALYSIS_URI + StatisticsFunctions.STUDY_ID_URI)
	// @Consumes(MediaType.APPLICATION_JSON)
	// public Response runRiskAnalysis(Parameters parameters) {
	//
	// 	return null;
	// }

}
