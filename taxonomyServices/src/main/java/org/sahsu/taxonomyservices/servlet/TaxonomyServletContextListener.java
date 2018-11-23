package org.sahsu.taxonomyservices.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.sahsu.rif.generic.util.TaxonomyLogger;
import org.sahsu.taxonomyservices.StartService;

@SuppressWarnings("unused")
@WebListener
public class TaxonomyServletContextListener implements ServletContextListener {

	private static final TaxonomyLogger logger = TaxonomyLogger.getLogger();

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		StartService.instance().start();
		logger.info(getClass(), "Taxonomy Services Context initialised");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

	}
}
