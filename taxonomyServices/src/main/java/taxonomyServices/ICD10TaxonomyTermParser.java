package taxonomyServices;

import taxonomyServices.system.TaxonomyServiceError;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.taxonomyServices.TaxonomyTerm;
import rifGenericLibrary.taxonomyServices.TaxonomyTermManager;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * This class parses XML files that the WHO uses to publish the ICD 10 term collection.
 * Its main job is to extract a tree of terms from the file and then register the 
 * taxonomy terms with {@link rifGenericLibrary.taxonomyServices.TaxonomyTermManager},
 * which is provides a generic mechanism for managing the terms in-memory.  The 
 * <code>TaxonomyTermManager</code> is then made available so that the calling class
 * {@link taxonomyServices.ICDTaxonomyService}, which uses it to handle most of the
 * taxonomy service methods.
 * 
 * <p>
 * 
 * </p>
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class ICD10TaxonomyTermParser {
	
	private static String ICD10NameSpace ="icd10";
	private static int ICD10CODEWITHOUTCHILDREN_LENGTH =4;
	//private TaxonomyTerm icd10Root;
	
	private TaxonomyTermManager taxonomyTermManager;
	
	// Using HashMap or TreeMap for the taxonomy container depends on
	// user's requirements and the database design.
	//private HashMap<String, TaxonomyTerm> taxonomyContainer;
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ICD10TaxonomyTermParser() {
		taxonomyTermManager = TaxonomyTermManager.newInstance(ICD10NameSpace);
	}

	public void readFile(
		final File icd10File) 
		throws RIFServiceException {

		try {		
			parseICD10ClaMLFile(icd10File);		
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			String errorMessage 
				= "Something wrong happened when ICD10 taxonomy (ClaML) service was initialised";
			RIFServiceException rifServiceException
				= new RIFServiceException(
					TaxonomyServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage);
			throw rifServiceException;
		}

	}
	
	private void parseICD10ClaMLFile(final File ICD10XmlFile) 
		throws ParserConfigurationException, 
		SAXException, 
		IOException {
		
		DocumentBuilderFactory dbFactory 
			= DocumentBuilderFactory.newInstance();
		//How to handle three exceptions?
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();			
		Document icd10Source = dBuilder.parse(ICD10XmlFile);
		
		icd10Source.getDocumentElement().normalize();
				
		NodeList metaList = icd10Source.getElementsByTagName("Meta");
		Node chapterValues = metaList.item(0);
		Element chapterNames =(Element) chapterValues;
				
		String chapterNamesInString = chapterNames.getAttribute("value");
		String [] chapterNamesInArray = chapterNamesInString.split(" ");
		ArrayList<TaxonomyTerm> chapters = new ArrayList<TaxonomyTerm>();
		
		//Add children of root taxonomy. A child is a chapter.
		for(int i = 0; i < chapterNamesInArray.length; i++){
			TaxonomyTerm chapter = TaxonomyTerm.newInstance();
			chapter.setLabel(chapterNamesInArray[i]);
			//chapter.setParentTerm(icd10Root);
			chapter.setParentTerm(null);
			chapters.add(chapter);
			
			taxonomyTermManager.addTerm(chapter);
			//taxonomyContainer.put(chapterNamesInArray[i], chapter);
		}

		System.out.println("ICD10TaxonomyTermParser 2");
		
		NodeList taxonomyTermList 
			= icd10Source.getElementsByTagName("Class");
		
		//A pattern for parsing "Class" element which may have Subclass element
		String pattern ="\\d";
						 
		for(int i = 0; i < taxonomyTermList.getLength(); i++){
			Node node = taxonomyTermList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				//Element may be chapter, or block, or category
				Element element =(Element) node;
											
				//Value of elementID is an icd 10 code which may contain dot sign. If so, remove it!
				String elementID = deleteDotInHealthCode(element.getAttribute("code"));
				
				TaxonomyTerm taxonomy = taxonomyTermManager.getTerm(elementID);
				//TaxonomyTerm taxonomy = taxonomyContainer.get(elementID);
				
				//Element doesn't contain digits (like VIII) or its length isn't equal to FOUR means it has "Subclass" 
				if(! elementID.matches(pattern) || 
					elementID.length() != ICD10CODEWITHOUTCHILDREN_LENGTH){								

					NodeList children = element.getElementsByTagName("SubClass");
					ArrayList<TaxonomyTerm> childrenOfElement = new ArrayList<TaxonomyTerm>();

					for(int k = 0; k < children.getLength(); k++){
						Element childName = (Element) children.item(k);
						TaxonomyTerm childTaxonomy = TaxonomyTerm.newInstance();

						childTaxonomy.setNameSpace(ICD10NameSpace);
						childTaxonomy.setParentTerm(taxonomy);
                        //Remove "dot" sign if the code contains it.
						String childID = deleteDotInHealthCode(childName.getAttribute("code"));

						childTaxonomy.setLabel(childID);					
						childrenOfElement.add(childTaxonomy);
						
						taxonomyTermManager.addTerm(childTaxonomy);
						//taxonomyContainer.put(childID, childTaxonomy);
					}	
					taxonomy.addChildTerms(childrenOfElement);
				}

				NodeList descriptions = element.getElementsByTagName("Rubric");
				for(int k=0; k<descriptions.getLength(); k++){
					Element rubric =(Element) descriptions.item(k);
										
					if(rubric.getAttribute("kind").equals("preferred")){
						taxonomy.setDescription(rubric.getTextContent());
					}
					else if(rubric.getAttribute("kind").equals("inclusion")){
						//Do nothing at moment
					}
					else if(rubric.getAttribute("kind").equals("exclusion")){
						//Do nothing at moment
					}
				}
			}				
		}

		taxonomyTermManager.determineRootTerms();
		//Not include code to generate a sample icd10 xml file based on the whole WHO ICD10 code and those 
		//that a user want to distribute
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public TaxonomyTermManager getTaxonomyTermManager() {
		return taxonomyTermManager;
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
	
	/**
	 * Remove the dot sign in WHO ICD 10 healthcode in order to keep data format as used in RIF database
	 * 
	 * @param taxonomyTerm The code appears in WTO XML file that contain dot sign.
	 * 
	 * @return taxonomyTerm without the dot sign. (E.g., as used in sahsuland cancer table.)
	 */
	
	private static String deleteDotInHealthCode(String taxonomyTerm){
		if(! taxonomyTerm.contains(".")){
			return taxonomyTerm;
		}
		else{
		    StringBuilder taxonomyTermWithoutDot = new StringBuilder(taxonomyTerm);
		    return taxonomyTermWithoutDot.deleteCharAt(taxonomyTerm.indexOf(".")).toString();
		}
	}
	
}
