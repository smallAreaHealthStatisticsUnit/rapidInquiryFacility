/**
 * Contains code for two main themes:
 * <ul>
 * <li>
 * all the code used to make services that are used by the study submission and study result
 * retrieval tools
 * </li>
 * <li>
 * other code that supports RIF-specific concepts across multiple sub-projects (eg: 
 * rifStudySubmissionTool, rifDataLoaderTool, rifWebServices, etc.)
 * </li>
 * </ul>
 * 
 * <p>
 * Please see <href="RIFCodingConventions.html">general coding conventions</a> used in the RIF
 * code base.
 * 
 * <h2>Code Road Map</h2>
 * All the code which describes properties of RIF domain concepts is contained in the 
 * {@link rifServices.businessConceptLayer} package.  This is where you should begin
 * your investigation about what this sub-package does.  All the code used to support
 * RIF services is contained in {@link rifServices.dataStorageLayer}.  The package will contain
 * all the code that is used to construct and execute SQL queries.  Here is where you will also
 * find any vendor-specific code that would be used to support PostgreSQL and SQLServer back ends.
 * The most important files to look at first are the definitions of services, which all end in 
 * "API":
 * <ul>
 * <li>{link rifServices.datastorage.RIFStudyServiceAPI}</li>
 * <li>{link rifServices.datastorage.RIFStudySubmissionAPI}</li>
 * <li>{link rifServices.datastorage.RIFStudyResultRetrievalAPI}</li>
 * </ul>
 * 
 * <p>
 * These service interfaces, as well as the definitions of business classes, should tell you all
 * you need to know about how client applications would make use of this sub-package.
 * 
 * <p>
 * {@link rifServices.restfulWebServices} contains the code that specifically relates to 
 * supporting web services.  The main purpose of this package is to isolate code that has 
 * dependencies on software libraries used to support web services.  We hope this will make it
 * easy to one day substitute Jersey for some other web service technology, in a way that minimises
 * the impact on the rest of the code base.
 * </p>
 * 
 * <p>
 * The {@link rifServices.fileFormats} package 
 * contains classes that are used to generate XML and HTML fragments that correspond to 
 * business objects described in {@link rifServices.businessConceptLayer}.  Support for XML will
 * be used to let end users save and load fragments of a study specification.  It would also be
 * used to include a machine-readable description of the submission query in a zip file that would
 * also contain the study results.
 * </p>
 * 
 * <p>
 * The {@link rifServices.taxonomyServices} package contains code used to allow the RIF to 
 * query one or more taxonomy services which would provide health codes.  For example, an ICD10
 * code provider would allow users to specify investigations that were tagged with ICD-10 codes.
 * There are different types of health codes, each of which may be maintained by a different
 * standards body and come with different conditions of use and distribution.  The package is
 * designed to show minimal knowledge of RIF-based concepts, other than a 
 * {@link rifServices.businessConceptLayer.HealthCode}.
 * </p>
 * 
 * <p>
 * The {@link rifServices.system} package contains classes that would be used across the
 * rif services sub-package, as well as in other sub-projects.  For example, the package
 * contains {@link org.sahsu.rif.generic.system.RIFServiceException}, which is a kind of exception that
 * is used in the <code>rifServices</code>, <code>rifDataLoaderTool</code> and 
 * <code>rifStudySubmissionTool</code> applications. 
 * </p>
 * 
 * <p>
 * The goal of developing the {@link rifServices.util} package is to isolate code which is used
 * across multiple projects, but which does not depend on RIF-specific concepts (eg: investigation,
 * covariate, study area etc).  Classes in this package are good candidates to be used in other
 * software projects.
 * </p> 
 * 
 * <h2>Code File Naming Conventions</h2>
 * 
 * 
 * 
 * 
 * 
 * 
 * Contains the implementation of rif services, which are classes that advertise features defined
 * in {@link rifServices.businessConceptLayer.RIFJobSubmissionAPI}.  Currently the main class
 * is {@link rifServices.ProductionRIFJobSubmissionService}, which is a service object that can be
 * deployed within client applications written in Java.  In future, this package will expand to include
 * classes which wrap this class as REST and SOAP web services.
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
package org.sahsu.rif.services.graphics;
