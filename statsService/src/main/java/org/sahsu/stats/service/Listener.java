package org.sahsu.stats.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.sahsu.rif.generic.util.RIFLogger;

/**
 * Starts the R Engine when the server starts, and tries to stop it when the server stops.
 */
@WebListener
public class Listener implements ServletContextListener {

	private static final RIFLogger logger = RIFLogger.getLogger();

	private ScriptService service;

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		System.out.println("Statistics Service Listener starting");
		service = ScriptService.instance();
		service.start();
		logger.info(getClass(), "Statistics Service Context initialised");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

		logger.info(getClass(), "Statistics Service Context destroyed. Stopping R engine.");
		if (service.isRunning()) {

			service.stop();
		}
	}
}

