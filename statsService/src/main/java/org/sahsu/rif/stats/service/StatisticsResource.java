package org.sahsu.rif.stats.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.Parameters;

@Path("/statistics")
public class StatisticsResource {

	@GET
	@Path("/functions" + StatisticsFunctions.STUDY_ID_URI)
	@Produces(MediaType.APPLICATION_JSON)
	public StatisticsFunctions getResources(
			@PathParam(StatisticsFunctions.STUDY_ID) String studyId) {

		return StatisticsFunctions.getInstance(studyId);
	}

	@POST
	@Path(StatisticsFunctions.SMOOTHING_URI + StatisticsFunctions.STUDY_ID_URI)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runSmoothingFunctions(Parameters parameters) {

		return null;
	}

	@POST
	@Path(StatisticsFunctions.RISK_ANALYSIS_URI + StatisticsFunctions.STUDY_ID_URI)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response runRiskAnalysis(Parameters parameters) {

		return null;
	}

}
