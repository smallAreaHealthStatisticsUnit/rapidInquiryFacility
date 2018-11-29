package org.sahsu.rif.services.system;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.sahsu.rif.generic.util.RIFLogger;

/**
 * Starts the RIF when the server starts, and tries to stop it when the server stops.
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
@WebListener
public class Listener implements ServletContextListener {

	private static final RIFLogger logger = RIFLogger.getLogger();

	private RIFService service;

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		System.out.println("RIF Middleware Service Listener starting");
		service = RIFService.instance();
		service.start();
		String serverDetails =
				String.format("Context name: %s; context path: %s; server info: %s",
				              servletContextEvent.getServletContext().getServletContextName(),
				              servletContextEvent.getServletContext().getContextPath(),
				              servletContextEvent.getServletContext().getServerInfo());
		logger.info(getClass(), "RIF Middleware Context initialised. " + serverDetails);
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

		logger.info(getClass(), "RIF Middleware Context destroyed.");
		if (service.isRunning()) {

			service.stop();
		}
	}
}

