package org.sahsu.rif.stats.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.generic.util.TaxonomyLogger;

@WebListener
public class Listener implements ServletContextListener {

	private static final RIFLogger logger = RIFLogger.getLogger();

	private Service theService;

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		theService = Service.instance();
		theService.start();
		logger.info(getClass(), "Statistics Service Context initialised");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

		logger.info(getClass(), "Context destroyed. Stopping R engine.");
		if (theService.isRunning()) {

			theService.stop();
		}
	}
}

