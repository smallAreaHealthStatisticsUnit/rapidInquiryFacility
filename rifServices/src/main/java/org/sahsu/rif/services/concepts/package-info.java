/**
 * 
 * 
 * 
 * <p>
 * This is the most important package for new RIF developers.  It contains definitions of
 * all the main domain concepts used in the RIF, as well as the interface methods ({@link 
 * rifServices.businessConceptLayer.RIFJobSubmissionAPI} used by the main middleware
 * service.  
 * </p>
 * 
 * <p>
 * The business classes in the RIF are intended to represent domain concepts that could be understood
 * by an epidemiologist, a person using GIS in public health, or a statistician who specialises in health
 * data.
 * </p>
 * 
 * <p>
 * In general, the business classes share many common features:
 * <ul>
 * <li> 
 * set and get methods which allow objects to behave as Java bean data containers.
 * </li>
 * <li>
 * objects are created using a factory pattern.  All constructors are private, and objects are created using a call
 * to a static factory method.
 * </li>
 * <li>
 * a static method for copying an instance
 * </li>
 * <li>
 * a "checkSecurityViolations" method which will check text fields to ensure they don't contain malicious values which
 * could cause SQL injectsions or cross-scripting attacks.  The method is designed to fail quickly, and will generate
 * an exception as soon as it encounters the first malicious field value.  The method is recursively applied to any
 * class members which themselves may refer to other RIF business classes.
 * </li>
 * <li>
 * a "checkErrors" method will check empty required fields, invalid values of individual fields and invalid combinations
 * of values from multiple fields.  The intent of checkErrors is to identify as many errors as possible, and generate 
 * a {@link org.sahsu.rif.generic.system.RIFServiceException} if there are any.
 * </li>
 * <li>
 * 
 * </li>
 * </ul>
 * </p>
 * 
 * 
 * </p>
 * 
 * <h1>Overview of Main Business Concepts</h1>
 * <p>
 * A {@link org.sahsu.rif.services.concepts.RIFStudySubmission} describes a research
 * activity that will be executed by the RIF to produce extraction results.  A submission
 * comprises information about the question being put to the database and the kinds of 
 * answers which are expected back.  
 * 
 * 
 * <h2> RIFJobSubmission: provides the question and specifies how the answer should be returned</h2>
 * The RIF tool suite includes an end-user application which will allow a scientist to submit
 * a study to the RIF database via the middleware that this code base provides.  The result will be a
 * data extraction that may be configured to suit different ways of post-processing results or configured
 * to generate different types of reports.  The basic data structure for specifying the question and 
 * post-processing the answer is the {@link org.sahsu.rif.services.concepts.RIFStudySubmission}.  An object
 * of this class has the following major features:
 * <ul>
 * <li>an {@link org.sahsu.rif.services.concepts.AbstractStudy}, which describes the scientific question</li>
 * <li>
 * an optional set of {@link rifServices.businessConceptLayer.CalculationMethod} objects, which describes how 
 * results should be post-processed
 * </li>
 * <li>
 * a set of {@link rifServices.businessConceptLayer.RIFOutputOption} objects, which describes what kinds of results should be generated
 * </li>
 * </ul>
 * <p>
 * For example, the study may want to find incidents of asthma for men aged 20 - 40 who live in Lancashire.
 * The results may need to be post-processed by a calculation method that performs BYM bayesian smoothing.  The
 * job submission may specify that the results should contain both data and maps.
 * </p> 
 * 
 * <h2>Anatomy of a study</h2>
 * The study defines the scientific question that the RIF software is meant to answer.  The RIF will eventually 
 * support two studies: a {@link org.sahsu.rif.services.concepts.DiseaseMappingStudy} and a risk analysis study.
 * In the early phases of this project, development will focus on aspects of disease mapping studies.  Later on,
 * we will add a "RiskAnalysisStudy" class and whatever helper classes it may need.
 * 
 * <p>
 * Every study has the following basic features:
 * <ul>
 * <li>fields which describe what the study is about</li>
 * <li>a {@link org.sahsu.rif.services.concepts.Geography}, which describes the data source for geographic information</li>
 * <li>a study area - {@link org.sahsu.rif.services.concepts.DiseaseMappingStudyArea} or
 * in future a RiskAnalysisStudyArea.  The study area defines the area that a scientific activity will examine
 * </li>
 * <li>a {@link org.sahsu.rif.services.concepts.ComparisonArea}.  The incidence of disease found in the study area
 * will be compared to the incidence found in a population in a more general area.</li> 
 * <li>a collection of {@link org.sahsu.rif.services.concepts.Investigation} objects, which describe specific
 * research questions that relate to the theme of the study.</li>
 * </ul>
 * 
 * <p>
 * For example, a study could be about looking at incidence of asthma in the London borough of Barnet.  The geography
 * could come from "UK2001", which describes geographic information taken from the UK 2001 census.  The data set would
 * include boundaries of geographic areas that were current as of that year.  The disease mapping study area would be "Barnet",
 * and the comparison area could be Greater London, southwest England or all of England.  Epidemiologists would
 * exercise their judgements about what comparison area would be most appropriate to use for their study.  
 * </p>
 * 
 * <p>
 * The study could have two separate investigations.  The first could describe the number of health 
 * episodes of males aged 20-24, taken from health records from the years 2000-2010, which reference the ICD 10 code 
 * for asthma "J45".  The investigation could also be associated with a covariate that accounts for socio-economic 
 * status.  The second investigation might examine women from a different age group who are associated 
 * with the ICD 9 "493" code that describes asthma.
 * </p>
 * 
 * <p>
 * The properties of a study have been spread out in two main classes: 
 * <ul>
 * <li>
 * {@link org.sahsu.rif.services.concepts.AbstractStudy}
 * </li>
 * <li>
 * {@link org.sahsu.rif.services.concepts.DiseaseMappingStudy}
 * </li>
 * </ul>
 * We've made use of an abstract class because we're anticipating the need for classes that relate to risk analysis
 * studies.  If the needs of risk analysis studies become significantly different than those of disease mapping studies,
 * we would expect to see properties migrate out of AbstractStudy and into the DiseaseMappingStudy and RiskAnalysisStudy
 * subclasses.
 * </p>
 * 
 * <h2> Study areas and comparison areas</h2>
 * The concepts of study area and comparison area are virtually identical.  However, we're not sure whether this will
 * remain true when we later consider the needs of study areas used for risk analyses activities.  It may be the case
 * that different attributes are needed for the concepts of a comparison area, a study area for disease mapping and
 * a study area for risk analyses.  For example, in risk analyses, users often select their areas of interest as
 * concentric bands of proximity relative to some putative pollution source.  We're not sure how this could influence
 * the information about how areas would be defined in the study.
 * 
 * This uncertainty explains why we have developed these classes:
 * <ul>
 * <li>{@link rifServices.businessConceptLayer.AbstractGeographicalArea}</li>
 * <li>{@link rifServices.businessConceptLayer.AbstractStudyArea}</li>
 * <li>{@link org.sahsu.rif.services.concepts.DiseaseMappingStudyArea}</li>
 * </ul>
 * 
 * <p>
 * For now, AbsractStudyArea is simply a marker class.  When we've clarified requirements for risk analysis studies, the
 * class will either grow or be removed.
 * </p>
 * 
 * <p>
 * Each AbstractGeographicalArea comprises the following features:
 * <ul>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelSelect}: determines the type of area you'd like to select.  In the UK
 * this could be "super output area", "output area", "district", "region". 
 * </li>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelArea}: the values the RIF database has which match the GeoLevelSelect.
 * For example, if "region" is selected, one of the GeoLevelArea values could be "Southwest England".
 * </li>
 * <li>
 * {@link rifServices.businessConceptLayer.GeoLevelView}: the resolution used for interactive area selection maps
 * </li>
 * <li>
 * {@link org.sahsu.rif.services.concepts.GeoLevelToMap}: the resolution used for the maps that are produced from
 * the extraction results.
 * </li>
 * <li>a collection of {@link org.sahsu.rif.services.concepts.MapArea} objects: each map area is defined by an
 * identifier and a label. The identifier identifies the area in a way that the database will use, whereas the label
 * is what is advertised to end-users.
 * </ul>
 * 
 * <p>
 * A geographical area therefore comprises a collection of area identifiers and variables which help define the context
 * for what those areas mean.
 * </p>
 * 
 * <h2> Anatomy of an investigation</h2>
 * So far, we know that a RIFSubmission object contains a study object, which in turn is made of one or more
 * investigations.  An investigation describes the attributes of people we're trying to examine as part of the study,
 * as well as more context about the records that contain information about them.  The basic features of an investigation are:
 * <ul>
 * <li>{@link org.sahsu.rif.services.concepts.HealthTheme}: describes the theme of health data (eg: "cancer"). Each
 * theme is associated with one or more numerator tables</li>
 * <li>{@link rifServices.businessConceptLayer.NumeratorDenominatorPair}: describes the numerator and its associated
 * denominator table.  Numerator tables are where demographics of your study group is drawn from.  Denominator tables
 * describe the demographics of the population you use for comparison.  
 * </li>
 * <li>a set of {@link rifServices.businessConceptLayer.HealthCode} objects:  Each health code describes some kind of 
 * health condition (eg: lung cancer)</li>
 * <li>a set of {@link org.sahsu.rif.services.concepts.AgeBand} objects: An age band could be age 20 - 40.  Each age
 * band is in turn defined by a lower {@link rifServices.businessConceptLayer.AgeGroup} and an upper age group.
 * </li>
 * <li>a sex, which can be males, females or both</li>
 * <li>a {@link rifServices.businessConceptLayer.YearRange}: describes the years of health records to consider</li>
 * <li>an interval, which can be used to divide results into different {@link.rifServices.businessConceptLayer.YearInterval} 
 * intervals within the year range</li>
 * <li>
 * a collection of {@link org.sahsu.rif.services.concepts.AbstractCovariate} objects: describe the factors which
 * should be used to provide further context for results (eg: socio-economic level, smoking/non-smoking, education level)
 * </li> 
 * </ul>
 * <p>
 * For example, an investigation may have the health theme of "cancer", which helps determine the data tables which
 * can provide cancer data for people living in an area of interest, and data about cancer for the general population.
 * The investigation may focus on cases of lung cancer and use ICD 10 codes C33-C34 or the ICD 9 code 162.9.  Our
 * investigation could target older men with a lower age group of 60-64 and an upper group of 70-74.  This means our
 * age band would effectively be 60 - 74.  We may be interested in any health data from the years 1995-2009 and want
 * to break up the extraction results for this study in intervals of 5 years (1995-1999, 2000-2004, 2005-2009).
 * Finally, our investigation may want to use covariates such as the socio-economic status of people with lung cancer, 
 * and whether or not they smoke.
 * </p>
 *    
 * <h2> Calculation methods: the R plugins which post-process extraction results</h2>
 * A {@link rifServices.businessConceptLayer.CalculationMethod} describes properties of a plugin which is written
 * in R and registered in the RIF database.  The middleware advertises what R plugins are available so that 
 * scientists can specify what routines they would like to be applied to the extraction results of their RIF job
 * submission.  Each CalculationMethod has the following features:
 * <ul>
 * <li>name: which is used to help list the plugin in end-user applications</li>
 * <li>codeRoutineName: is the name of the R plugin that will be used by the RIF database when it identifies
 * the plugin.</li>
 * <li>
 * prior: an aspect of how the statistical plugin should operate.  A prior may be set to 
 * "standard deviation" or "precision".
 * </li>
 * <li>
 * description: provides information to end-users about what the R plugin does.
 * </li>
 * <li>
 * a collection of {@link org.sahsu.rif.generic.concepts.Parameter} objects: used to help set aspects of
 * the plugin's behaviour
 * </li>
 * </ul>
 * 
 * <h2>Output options</h2>
 * The RIF will be able to generate different kinds of results:
 * <ul>
 * <li>the extraction data</li>
 * <li>data files used to generate maps</li>
 * <li>ratios and rates</li>
 * <li>indicators of population holes</li>
 * </ul>
 * 
 * 
 * <h1>Areas of Future Development</h1>
 * The main area of the business classes which will change in the near future are those that would
 * be different between disease mapping studies and risk analysis studies.  Some of the marker abstract classes
 * which have been developed assume that more properties will be added to them.  However, if the two kinds of
 * studies are studied more and found to be more similar to one another, some of the classes may be collapsed
 * or simply disappear.
 * 
 * <p>
 * The other main area will be the development of {@link org.sahsu.rif.services.concepts.AdjustableCovariate} and
 * {@link org.sahsu.rif.services.concepts.ExposureCovariate}.  Currently, the RIF requires that all covariates are
 * banded rather than continuous variables.  We need to examine covariate requirements and determine how or whether
 * functionality of covariate classes needs to be extended.
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
package org.sahsu.rif.services.concepts;

