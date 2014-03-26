/**
 * This package contains all the classes that the middleware needs to unpack data from
 * business class objects and create SQL queries. There are different groups of classes
 * worth noting:
 * <ul>
 * <li>
 * <b>manager classes</b> which have operations that manage operations relating to one or more
 * business concepts.  For example, {@link rifServices.dataStorageLayer.SQLHealthOutcomeManager} contains
 * operations relevant to {@link rifServices.businessConceptLayer.HealthCode}. These classes will contain
 * all the SQL queries in the code base.  Some manager classes manage more than one concept.  For example,
 * {@link rifServices.dataStorageLayer.SQLRIFContextManager} manages operations for 
 * {@link rifServices.businessConceptLayer.GeoLevelSelect},
 * {@link rifServices.businessConceptLayer.GeoLevelArea},
 * {@link rifServices.businessConceptLayer.GeoLevelView},
 * {@link rifServices.businessConceptLayer.GeoLevelToMap}
 * </li>
 * <li>
 * <b>query formatter classes</b> which are used to help construct common types of SQL queries.  The formatters
 * were created to help reduce repetitive coding work, and to provide a way of controlling the layout and case
 * of query features
 * </li>
 * <li>
 * <b>connection manager</b>.  {@link rifServices.dataStorageLayer.SQLConnectionManager} is the class which is
 * responsible for connection pooling and ensuring that users are registered for a session
 * </li>
 * 
 * <h2>Manager classes in detail</h2>
 * The main service class, {@link rifServices.ProductionRIFJobSubmissionService}, implements methods in the 
 * {@link rifServices.businessConceptLayer.RIFJobSubmissionAPI} interface.  However, it delegates most of these
 * methods to various manager classes.  
 * <p>
 * Methods for manager classes have similar code patterns.  A method will typically:
 * <ol>
 * <li>
 * call the "checkErrors(...)" methods in the parameter values, which are typically instances of business
 * classes
 * </li>
 * <li>
 * use a query formatter to construct the SQL query
 * </li>
 * <li>
 * create a Java PreparedStatement object, and use the parameter values to set fields in the query
 * </li>
 * <li>
 * iterate through a result set
 * </li>
 * <li>
 * return the results.
 * </li>
 * </ol>
 * <p>
 * Code which uses prepared statements to execute queries is contained within a try...catch...finally block.
 * Should an SQLException be generated, it is caught and audited in a log file.  An instance of 
 * {@link rifServices.system.RIFServiceException} is created with an appropriate {@link rifServices.system.RIFServiceError}
 * error code and a collection of error messages.  The messages are meant to provide information to end-users
 * which is in plain language and which does not inadvertently disclose sensitive data.
 * </p>
 * <p>
 * The finally part of the code block closes instances of <code>PreparedStatement</code> and <code>ResultSet</code>
 * which were used when the SQL query was executed.
 * </p>
 * 
  * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
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
package rifServices.dataStorageLayer;