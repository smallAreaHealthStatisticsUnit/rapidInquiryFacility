/**
 * Contains classes which apply across the the middleware.
 * <ul>
 * <li>
 * {@link org.sahsu.rif.services.system.RIFServiceError}: is an enumerated type that describes error codes
 * for the kinds of errors we might expect the RIF to encounter.
 * </li>
 * <li>
 * {@link org.sahsu.rif.generic.system.RIFServiceException}: a checked exception for the RIF middleware
 * </li>
 * <li>
 * {@link rifServices.system.RIFServiceMessages}: a class that reads messages from a *.properties file
 * that has the same name.  These messages are used to create error messages or UI messages.
 * </li>
 * <li>
 * {@link rifServices.system.RIFServiceStartupOptions}: stores configuration data used to 
 * adjust the way a RIF service works
 * </li>
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * @version
 */
package org.sahsu.rif.services.system;
