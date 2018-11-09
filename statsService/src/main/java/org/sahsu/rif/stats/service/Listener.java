package org.sahsu.rif.stats.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.sahsu.rif.generic.util.RIFLogger;

@WebListener
public class Listener implements ServletContextListener {

	private static final RIFLogger logger = RIFLogger.getLogger();

	private BridgeToR bridge;

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		bridge = BridgeToR.instance();
		bridge.start();
		logger.info(getClass(), "Statistics Service Context initialised");
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {

		logger.info(getClass(), "Statistics Service Context destroyed. Stopping R engine.");
		if (bridge.isRunning()) {

			bridge.stop();
		}
	}
}

