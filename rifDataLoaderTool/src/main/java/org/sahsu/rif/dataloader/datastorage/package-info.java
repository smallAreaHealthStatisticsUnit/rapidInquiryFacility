

/**
 * This package contains all the code that is used to manipulate imported data sets
 * using an underlying temporary database for transformation work. It is divided into
 * two sub-packages that almost mirror one another.  One is written to support 
 * PostgreSQL and the other is meant to support Microsoft SQL Server. 
 * 
 * <p>
 * Porting database code so that it can work equally well with multiple underlying
 * database brands can be difficult.  One of the most difficult tasks is anticipating
 * how much code can be shared and how much would have to be written to support the
 * needs of each database vendor. Although most vendors will implement most of the
 * SQL standard, some use different syntax or have different built-in functions.
 * </p>
 * 
 * <p>
 * Here we will go through a tour of what will likely continue to appear in both
 * the PostgreSQL (<code>pg</code>) package and the Microsoft SQL Server package 
 * (<code>ms</code>).  Wherever you see a "*" in the code names, treat it as a 
 * wildcard that will be filled in by various types of task names or the <code>ms</code>
 * or <code>pg</code> phrases that indicate which database type it uses.   
 * </p>
 * 
 * <h2>Understanding the work flow</h2>
 * The classes all service a work flow with the following steps:
 * <ol>
 * <li>
 * <b>extract</b>: initially load data from files into the database
 * </li>
 * <li>
 * <b>clean (search and replace)</b>: replace poor values with good values.
 * </li>
 * <li>
 * <b>clean (validate)</b>: verify whether values now reflect valid data.  If
 * they don't, mark it with <code>rif_error</code> instead.
 * </li>
 * <li>
 * <b>clean (cast text data to other types)</b>: cast cleaned text fields to
 * other data types, use null values for cases where errors have occurred.
 * </li>
 * <li>
 * <b>convert</b>: map the table to a specific part of the production schema.
 * For example, map parts of a cleaned CSV file to the numerator table section of
 * the RIF production schema.
 * </li>
 * <li>
 * <b>combine/split</b>: splits one file into many or merges many files into one.
 * This is only applicable for 
 * {@link rifDataLoaderTool.businessConceptLayer.BranchedWorkflow} cases.  Not
 * likely to be implemented in the early or middle part of RIF development.
 * </li>
 * <li>
 * <b>optimise</b>: add indices for certain tables to help foster efficient 
 * query operations later on.
 * </li>
 * <li>
 * <b>check</b>: add data quality checks to fields.  For example, identify the 
 * number of blank values in a column.
 * </li>
 * <li>
 * <b>publish</b>: indicate that the table is ready to be used.
 * </li>
 * </ol>
 * 
 * 
 * <h2>Where to start looking: service classes and manager classes</h2>
 * In each of the main two <code>datastorage</code> packages of the data loader
 * tool, <code>*Service</code> classes implement methods that are defined in the 
 * {@link rifDataLoaderTool.businessConceptLayer.DataLoaderServiceAPI} interface.
 * These service classes will typically perform some actions for an API method, but
 * delegate most of the work to a <code>*SQL*Manager</code> class that has a 
 * corresponding method with the same name.
 *
 * <p>
 * The delegation pattern between <code>*Service</code> and 
 * <code>*SQL*Manager</code> classes is designed to prevent <code>*Service</code> classes
 * from becoming too large.  The pattern also helps make a separation of concerns.
 * 
 * <p>
 * Service classes:
 * <ul>
 * <li>
 * Safe copy method parameters to local variables in order to create immutable versions
 * that guarantee their values will not be changed by clients calling the service.
 * Safe-copying is a common technique that helps minimise the likelihood that code will
 * encounter concurrency issues of multiple threads that may try to use it.
 * </li>
 * <li>
 * validate those parameters to make sure they're not null or would otherwise cause
 * problems in further code
 * </li>
 * <li>
 * log attempt to execute service method
 * </li>
 * <li>
 * Obtain a database connection from a pool of connections.
 * </li>
 * <li>
 * call a corresponding method of a <code>*SQL*Manager</code> class.
 * </li>
 * <li>
 * relinquish the database connection and add it back to the pool.
 * </li>
 * <li>
 * return any results
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * The <code>*SQL*Manager</code> classes:
 * <ul>
 * <li>
 * assume parameter values are correct.
 * </li>
 * <li>
 * use a set of naming conventions to determine the name of the next temporary
 * table that will reflect the changes that go with their processing step.
 * </li>
 * <li>
 * Initialise database resources like <code>java.sql.PreparedStatement</code> and
 * <code>java.sql.ResultSet</code>
 * </li>
 * <li>
 * use the parameter values to assemble SQL queries
 * </li>
 * <li>
 * log the attempt to execute the assembled query
 * </li>
 * <li>
 * execute the SQL queries and extract any result set data into business objects.
 * </li>
 * <li>
 * if any exceptions occurred, log the original SQL exception, construct a 
 * human-readable message and throw a {@link org.sahsu.rif.generic.system.RIFServiceException}.
 * and throw that.
 * </li>
 * <li>
 * regardless of whether an exception happens or not, it will clean up any open
 * database resources - it will close <code>java.sql.ResultSet</code> and 
 * <code>java.sql.PreparedStatement</code>.
 * </li>
 * </ul>
 * 
 * <p>
 * The service classes include an <code>Abstract*SQLDataLoaderService</code> that holds
 * most of the code, and two sub-classes <code>Production*DataLoaderService</code>
 * and <code>Test*DataLoaderService</code>.  These subclasses are not designed to 
 * hold much code, but the <code>Test*DataLoaderService</code> will contain methods
 * that help service testing.  For example, it has a method 
 * <code>clearAllDataSets()</code> that deletes all existing data sets in the database.
 * Although this is useful for ensuring the test environment resets between test 
 * cases, in a production setting, having such a method available would present a 
 * security risk.
 * </p>
 * 
 * <p>
 * The manager classes inherit a lot of common code from class
 * <code>Abstract*SQLDataLoaderStepManager</code>, which supports the following
 * tasks:
 * </p>
 * <ul>
 * <li>
 * convert a simple text table into a 
 * {@link org.sahsu.rif.generic.concepts.RIFResultTable} that can store
 * results
 * </li>
 * <li>
 * add comments to the schema
 * </li>
 * <li>
 * add primary keys to the temporary tables
 * </li>
 * <li>
 * export a table to a CSV file
 * </li>
 * </ul>
 * 
 */


/**
 * Contains all the SQL code generation classes and the main implementation of the RIF data loader
 * service.
 */
/**
 * @author kgarwood
 *
 */
package org.sahsu.rif.dataloader.datastorage;
