package org.sahsu.rif.stats.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.sahsu.rif.generic.util.RIFLogger;

/**
 * Ensures that the R Engine is started when the server starts, and tries to stop it when the server
 * stops.
 */
@WebListener
public class Listener implements ServletContextListener {

	private static final RIFLogger logger = RIFLogger.getLogger();

	private LinkToR link;

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		link = LinkToR.instance();
		link.start();
		logger.info(getClass(), "Statistics Service Context initialised");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

		logger.info(getClass(), "Statistics Service Context destroyed. Stopping R engine.");
		if (link.isRunning()) {

			link.stop();
		}
	}
}

