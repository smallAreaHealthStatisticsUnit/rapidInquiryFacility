package org.sahsu.stats.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.sahsu.rif.generic.util.StatisticsLogger;

/**
 * The root of the Statistics Service web service. The root path, statistics,
 * is provided by the name of the application's war file.
 */
@ApplicationPath("/service")
public class WebService extends Application {

	public WebService() {

		final StatisticsLogger logger = StatisticsLogger.getLogger();
		logger.info(getClass(), "!!!!!! Statistics WebService !!!!!!");
	}

	@Override
	public Set<Class<?>> getClasses() {
		
		Set<Class<?>> set = new HashSet<>();
		set.add(StatisticsResource.class);
		return set;
	}
}
