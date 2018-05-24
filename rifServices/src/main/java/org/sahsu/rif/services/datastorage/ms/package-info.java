/**
 * 
 * This package contains all the classes which use business objects to construct and execute
 * SQL queries.  Whereas the <code>concepts</code> package is the most important
 * package for understanding domain knowledge, this package is the most important for understanding
 * how the middleware interacts with the database.
 * 
 * <p>
 * The package contains the following class themes, which are mentioned here in roughly the
 * order they might be used:
 * <ul>
 * <li><b>service interfaces</b>: all of these end in <code>*API</code> and are the best place
 * to understand how the features of the RIF are advertised to client applications.</li>
 * <li>
 * <b>production and test service implementations</b>: these are classes which implement the
 * service APIs and are adjusted to meet the needs of test vs production environments.  Test 
 * services will typically need more methods to ensure that the state of the database is reset
 * to a known state.  For example, {@link rifServices.dataStorageLayer.MSSQLTestRIFStudySubmissionService}
 * contains a method for clearing a user's studies, which would not be offered in a production
 * environment.  All test services are prefixed with "Test" and all production services are prefixed
 * with "Production".
 * </li>
 * <li>
 * <b>abstract service implementations</b>: centralise common code that would be used in both
 * test and production services.  The study submission and study result retrieval RIF services
 * actually share a lot of common code, but they are offered to potentially different user audiences.
 * </li>
 * <li>
 * <b>service resource classes</b>: in particular, {@link rifServices.dataStorageLayer.MSSQLRIFServiceResources}
 * help ensure that all the RIF services share the same instances of manager classes which execute
 * queries.  Implementations of {@link rifServices.dataStorageLayer.MSSQLAbstractStudyServiceBundle} are 
 * used to ensure that these manager classes are instantiated once in a way that will not encourage
 * potential synchronisation problems.
 * </li>
 * <li>
 * <b> manager classes </b>: implementations of RIF service classes delegate the task of executing
 * SQL queries to manager classes, which roughly correspond with key concepts in the business concept
 * layer.  For example, {@link rifServices.dataStorageLayer.MSSQLInvestigationManager} manages
 * database operations associated with {@link rifServices.businessConceptLayer.Investigation}.  All
 * manager classes end in <code>*Manager</code> and almost all of them begin with <code>SQL</code>.
 * </li>
 * <li>
 * <b>query formatters</b>:  classes meant to minimise the amount of repetitive code used to 
 * construct SQL queries such as SELECT, INSERT, UPDATE statements.  These classes have evolved
 * to reduce the potential problems of concatenating fragments of SQL queries.  The formatters are
 * intended to help make SQL queries more readable in log files.  As well, they can adjust
 * case sensitivity of key words depending on whether the queries are executed in PostgreSQL or
 * SQL Server environments.
 * </li>
 * </ul>
 * 
 * <p>
 * Many of these kinds of classes are reflected in the following table of naming conventions for
 * classes in this package:
 * <table>
 * <tr>
 * <td><b>Convention</b></td>
 * <td><b>Meaning</b></td>
 * </tr>
 * <tr>
 * <td><b>Example</b></td>

 * 
 * <tr>
 * <td>SQL*</td>
 * <td>File contains SQL code</td>
 * </tr>
 * <tr>
 * <td><code>*API</code>
 * 
 * </table>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * This package contains all the classes that the middleware needs to unpack data from
 * business class objects and create SQL queries. There are different groups of classes
 * worth noting:
 * <ul>
 * <li>
 * <b>manager classes</b> which have operations that manage operations relating to one or more
 * business concepts.  For example, {@link rifServices.dataStorageLayer.MSSQLHealthOutcomeManager} contains
 * operations relevant to {@link rifServices.businessConceptLayer.HealthCode}. These classes will contain
 * all the SQL queries in the code base.  Some manager classes manage more than one concept.  For example,
 * {@link rifServices.dataStorageLayer.MSSQLRIFContextManager} manages operations for 
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
 * <b>connection manager</b>.  {@link rifServices.dataStorageLayer.MSSQLConnectionManager} is the class which is
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
 * {@link org.sahsu.rif.generic.system.RIFServiceException} is created with an appropriate {@link rifServices.system.RIFServiceError}
 * error code and a collection of error messages.  The messages are meant to provide information to end-users
 * which is in plain language and which does not inadvertently disclose sensitive data.
 * </p>
 * <p>
 * The finally part of the code block closes instances of <code>PreparedStatement</code> and <code>ResultSet</code>
 * which were used when the SQL query was executed.
 * </p>
 * 
 * <h2>Query formatter classes in detail</h2>
 * Early in development, the SQL* classes had a lot of repetitive code for common SQL queries.  As well,
 * it became clear that as the project made a greater commitment to supporting both PostgreSQL and
 * SQLServer, that there may be issues about whether certain key words were case sensitive.  Therefore
 * 
 * 
 * 
 * 
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
package org.sahsu.rif.services.datastorage.ms;
