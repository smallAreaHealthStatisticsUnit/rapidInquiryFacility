package org.sahsu.rif.generic.datastorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public class ConnectionQueue {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<Connection> allConnections = new ArrayList<Connection>();
	private BlockingQueue<Connection> pool = new ArrayBlockingQueue<Connection>(10);
	
	// ==========================================
	// Section Construction
	// ==========================================

	public ConnectionQueue() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void addConnection(final Connection connection) {
		allConnections.add(connection);
		pool.add(connection);
	}
	
	public Connection assignConnection() 
	throws InterruptedException {
		
		Connection connection
			= pool.poll(1000, TimeUnit.MILLISECONDS);
		return connection;
	}
	
	
	public void reclaimConnection(
		final Connection connection) 
		throws InterruptedException {
		
		pool.put(connection);
	}
	
	public void closeAllConnections()
			throws RIFServiceException {
		
		try {		
			for (Connection connection : allConnections) {
				connection.close();
			}
			allConnections.clear();
			pool.clear();		
		}
		catch(SQLException sqlException) {
			String errorMessage
				= GENERIC_MESSAGES.getMessage("sqlConnectionManager.error.unableCloseConnections");
			RIFServiceException rifServiceException
				 = new RIFServiceException(
					RIFGenericLibraryError.DB_UNABLE_TO_CLOSE_CONNECTIONS,
					errorMessage);
			throw rifServiceException;
		}		
		
		
	}
	
	public void clearConnections() {
	
		allConnections.clear();
		pool.clear();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
