/**
 * Contains all the classes used for reading and writing data from the 
 * business classes to file formats.  The formats may also include streams of 
 * JSON data that are not associated with business classes.
 * <p>
 * There are three main types of file formats which are supported:
 * <ul>
 * <li><b>html files</b>, which can present the content of a {@link rifServices.businessConceptLayer.RIFJobSubmissionAPI}
 * as web pages.</li>
 * <li>
 * <b>*.rifq files</b>, which describe the content of a <code>RIFJobSubmissionAPI</code> as an XML file that can be
 * used by other software clients
 * </li>
 * <li>
 * <b>*.rifz files</b>, which are ZIP format files that contain the original RIF job submission, the results and an audit
 * trail of how the submission was executed.
 * </li>
 * </ul>
 * 
 * <p>
 * Classes in this package are in three groups:
 * <ul>
 * <li>file filter classes</b>: are convenience classes that a Java client application can use to 
 * cause <code>JFileChooser</code> instances to show only one particular type of file format 
 * (eg: *.rifq, *.rifz).  For example {@link rifServices.dataStorageLayer.RIFZFileFilter} will cause a 
 * file chooser dialog to only show directories and files of the type *.rifz
 * </li>
 * <li>
 * <b>content handlers</b>: contain the read and write methods for serialising business objects as 
 * HTML, or XML.  In future they may also generate text fragments written using JSON.  
 * </li>
 * <li>
 * <b>readers and writers</b>are the classes which will either read or write files that describe a
 * <code>RIFJobSubmission</code> object.
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * The content handler classes make use of utility classes to produce HTML and XML files.  These
 * classes are in the <code>rifServices.util</code> package and are:
 * <ul>
 * <li>{@link org.sahsu.rif.generic.presentation.HTMLUtility}</li>
 * <li>{@link org.sahsu.rif.generic.fileformats.XMLUtility}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The utility classes are designed to write data either to an output stream or to a String.  The former
 * helps the RIF service produce documents and the latter helps create text for web services 
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
 * <h2>Coding Conventions</h2>
 * <table>
 * <tr valign="top">
 * <td>
 * <b>Convention</b>
 * </td>
 * <td>
 * <b>Meaning</b>
 * </td>
 * <td>
 * <b>Example</b>
 * </td>
 * </tr>
 * 
 * 
 * <tr valign="top">
 * <td>
 * <code>*ContentHandler</code>
 * </td>
 * <td>
 * indicates class that can write HTML and both read and write XML.  Note that the content
 * handler uses the SAX parser to parse XML.
 * </td>
 * <td>
 * <code>HealthCodeContentHandler</code>
 * </td>
 * </tr>
 * 
 * <tr valign="top">
 * <td>
 * <code>*Writer</code>
 * </td>
 * <td>
 * indicates class that is designed to write a complete document.  Theses classes are meant to 
 * hide references to ContentHandler classes from the rest of the application.  
 * </td>
 * <td>
 * <code>HealthCodeContentHandler</code>
 * </td>
 * </tr>
 * 
 * <tr valign="top">
 * <td>
 * <code>*Reader</code>
 * </td>
 * <td>
 * indicates class that is designed to read a complete document.  Like the <code>Writer</code> files, 
 * theses classes are meant to hide references to ContentHandler classes from the rest of the application.  
 * </td>
 * <td>
 * <code>RIFJobSubmissionXMLReader</code>
 * </td>
 * </tr>
 * 
 * <tr valign="top">
 * <td>
 * <code>*FileFilter</code>
 * </td>
 * <td>
 * a class which is used to filter listings of files in a given directory.  For example, the 
 * <code>XMLFileFilter</code> will cause files ending in <code>.xml</code> to appear in a list of 
 * files shown with a <code>JFileChooser</code>.
 * </td>
 * <td>
 * <code>RIFJobSubmissionXMLReader</code>
 * </td>
 * </tr>
 * 
 * </table>
 *
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
package org.sahsu.rif.services.fileformats;
