package rifServices.ontologyServices;

import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.taxonomyServices.TaxonomyTerm;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public final class ICD10ClaMLTaxonomyProvider implements HealthCodeProviderInterface {
	private static String ICD10NameSpace ="icd10";
	private static int ICD10CODEWITHOUTCHILDREN_LENGTH =4;
	private HealthCodeTaxonomy healthCodeTaxonomy;
	private File ICD10XmlFile;
	private TaxonomyTerm icd10Root;
	
	// Using HashMap or TreeMap for the taxonomy container depends on
	// user's requirements and the database design.
	private HashMap<String, TaxonomyTerm> taxonomyContainer;
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ICD10ClaMLTaxonomyProvider() {
		healthCodeTaxonomy = HealthCodeTaxonomy.newInstance(
			                   "ICD 10 Taxonomy", 
			                    "codes for ICD10", 
			                    ICD10NameSpace, 
				                "1.0");	
		icd10Root =TaxonomyTerm.newInstance();
		taxonomyContainer =new HashMap<String, TaxonomyTerm>();
	}

	public void initialise(ArrayList<Parameter> parameters) throws RIFServiceException {
		try {						
			Parameter icd10FileParameter
				= getParameter(
					"icd10_ClaML_file",
					parameters);
			if (icd10FileParameter == null) {
				//ERROR: initialisation parameters are missing a required parameter value				
				String errorMessage
					= "ICD10 taxonomy service is missing a parameter for \"icd10_ClaML_file\"";
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
						errorMessage);
				throw rifServiceException;
				
			}
			
			String filePath = icd10FileParameter.getValue();		
			ICD10XmlFile = new File(filePath);
		
			parseICD10ClaMLFile(ICD10XmlFile);		
		}
		catch(Exception exception) {
			String errorMessage 
				= "Something wrong happened when ICD10 taxonomy (ClaML) service was initialised";
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFServiceError.HEALTH_CODE_TAXONOMY_SERVICE_ERROR,
					errorMessage);
			throw rifServiceException;
		}

	}
	
	private void parseICD10ClaMLFile(File ICD10XmlFile) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//How to handle three exceptions?
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();			
		Document icd10Source = dBuilder.parse(ICD10XmlFile);
		
		icd10Source.getDocumentElement().normalize();
			 
		//Build root taxonomy which contains all chapters, for the usage of getToplevel() method

		icd10Root.setLabel("ICD10");
		icd10Root.setNameSpace(ICD10NameSpace);
		icd10Root.setDescription("International Statistical Classification of Diseases and Related Health Problems 10th Revision");
		icd10Root.setParentTerm(null);
				
		//Container containing all taxonomy. Either using HashMap or Treemap is based on database design.
		//The key is health code so the type is String.
				
		NodeList metaList = icd10Source.getElementsByTagName("Meta");
		Node chapterValues = metaList.item(0);
		Element chapterNames =(Element) chapterValues;
				
		//Element chapterNamesMeta =(Element) metaList.item(1);
				
		String chapterNamesInString = chapterNames.getAttribute("value");
		String [] chapterNamesInArray =chapterNamesInString.split(" ");
		ArrayList<TaxonomyTerm> chapters =new ArrayList<TaxonomyTerm>();
		
		//Add children of root taxonomy. A child is a chapter.
		for(int i=0; i< chapterNamesInArray.length; i++){
			TaxonomyTerm chapter =TaxonomyTerm.newInstance();
			chapter.setLabel(chapterNamesInArray[i]);
			chapter.setNameSpace(ICD10NameSpace);
			chapter.setParentTerm(null);
			chapters.add(chapter);
			taxonomyContainer.put(chapterNamesInArray[i], chapter);
		}
		icd10Root.addChildTerms(chapters);
		
		//add root taxonomy (virtual one) into taxonomy repository
		taxonomyContainer.put("ICD10Root", icd10Root);
				
		NodeList healthCodeList = icd10Source.getElementsByTagName("Class");
		
		//A pattern for parsing "Class" element which may have Subclass element
		String pattern ="\\d";
						 
		for(int i=0; i<healthCodeList.getLength(); i++){
			Node node = healthCodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				//Element may be chapter, or block, or category
				Element element =(Element) node;
											
				//Value of elementID is an icd 10 code which may contain dot sign. If so, remove it!
				String elementID =deleteDotInHealthCode(element.getAttribute("code"));
				
				TaxonomyTerm taxonomy =taxonomyContainer.get(elementID);
				
				//Element doesn't contain digits (like VIII) or its length isn't equal to FOUR means it has "Subclass" 
				if(! elementID.matches(pattern) || elementID.length() != ICD10CODEWITHOUTCHILDREN_LENGTH){								
					NodeList children = element.getElementsByTagName("SubClass");
					ArrayList<TaxonomyTerm> childrenOfElement =new ArrayList<TaxonomyTerm>();

					for(int k=0; k <children.getLength(); k++){
						Element childName = (Element) children.item(k);
						TaxonomyTerm childTaxonomy =TaxonomyTerm.newInstance();

						childTaxonomy.setNameSpace(ICD10NameSpace);
						childTaxonomy.setParentTerm(taxonomy);
                        //Remove "dot" sign if the code contains it.
						String childID =deleteDotInHealthCode(childName.getAttribute("code"));

						childTaxonomy.setLabel(childID);					
						childrenOfElement.add(childTaxonomy);
						taxonomyContainer.put(childID, childTaxonomy);
					}				
					taxonomy.addChildTerms(childrenOfElement);
				}

				NodeList descriptions = element.getElementsByTagName("Rubric");
				for(int k=0; k<descriptions.getLength(); k++){
					Element rubric =(Element) descriptions.item(k);
					
					//The below code needs JRE 1.7 support
					/*switch(rubric.getAttribute("kind")){
						case "preferred":
							taxonomy.setDescription(rubric.getTextContent());
							break;
						
						//at this stage, we are not interested in "inclusion" and "exclusion" sections
						case "inclusion":
							break;
						
						case "exclusion":
							break;
						
						default :
							break;		    	 
					}*/
					
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
		//Not include code to generate a sample icd10 xml file based on the whole WHO ICD10 code and those 
		//that a user want to distribute 
	}

	public HealthCodeTaxonomy getHealthCodeTaxonomy() {
		return healthCodeTaxonomy;
	}
	

	public boolean supportsTaxonomy(HealthCodeTaxonomy healthCodeTaxonomy) {
		return matchesNameSpace(healthCodeTaxonomy.getNameSpace());
	}

	public boolean supportsTaxonomy(HealthCode healthCode) {
		return matchesNameSpace(healthCode.getNameSpace());
	}

	public ArrayList<HealthCode> getHealthCodes(String searchText, boolean isCaseSensitive) throws RIFServiceException {
        Pattern searchPattern;
		if(searchText ==null){
			return null;
		}
		if (isCaseSensitive) {
			searchPattern = Pattern.compile(".*"+searchText+".*");
		}
		else {
			searchPattern = Pattern.compile(".*"+searchText+".*", Pattern.CASE_INSENSITIVE);
		}
		
		ArrayList<HealthCode> results = new ArrayList<HealthCode>();
		for (TaxonomyTerm term : taxonomyContainer.values()) {
			Matcher patternCodeMatcher
				= searchPattern.matcher(term.getLabel());
			if (patternCodeMatcher.matches()) {
				results.add(transformTaxonomyTermIntoHealthCode(term));
			}
			else {			
				Matcher patternDescriptionMatcher
					= searchPattern.matcher(term.getDescription());
				if (patternDescriptionMatcher.matches()) {
					results.add(transformTaxonomyTermIntoHealthCode(term));
				}
			}
		}
		return results.size()>0? results: null;
	}

	public ArrayList<HealthCode> getTopLevelCodes() throws RIFServiceException {
		//The container for all of the top level HealthCode
		ArrayList<HealthCode> healthCodes =new ArrayList<HealthCode>();
		
		for(TaxonomyTerm term : icd10Root.getChildTerms()){
			healthCodes.add(transformTaxonomyTermIntoHealthCode(term));
		}
		return healthCodes;
	}

	public ArrayList<HealthCode> getImmediateSubterms(HealthCode parentHealthCode) throws RIFServiceException {
		if(parentHealthCode ==null){
			return null;
		}
		if(! matchesNameSpace(parentHealthCode.getNameSpace())){
			return null;
		}
		if(parentHealthCode.getNumberOfSubTerms()==0){
			//This code is a leaf which has no children
			return null;
		}
		//The container for the children of this parentHealthCode
		ArrayList<HealthCode> children =new ArrayList<HealthCode>(parentHealthCode.getNumberOfSubTerms());
		TaxonomyTerm parentTaxonomyTerm = taxonomyContainer.get(parentHealthCode.getCode());
		for(TaxonomyTerm term :  parentTaxonomyTerm.getChildTerms()){
			HealthCode healthCode =transformTaxonomyTermIntoHealthCode(term);
			children.add(healthCode);
		}
		return children.size()>0? children: null;
	}

	public HealthCode getParentHealthCode(HealthCode childHealthCode) throws RIFServiceException {
		if(childHealthCode ==null){
			return null;
		}
		if(! matchesNameSpace(childHealthCode.getNameSpace())){
			return null;
		}
		if(childHealthCode.isTopLevelTerm()){
			return null;
		}
		//How to handle if childHealthCode is wrong? E.g., not exist in the Health Taxonomy.
		TaxonomyTerm childTaxonomyTerm = taxonomyContainer.get(childHealthCode.getCode());
		if(childTaxonomyTerm ==null){
			return null;
		}
		TaxonomyTerm parentTaxonomyTerm =childTaxonomyTerm.getParentTerm();

	    return transformTaxonomyTermIntoHealthCode(parentTaxonomyTerm);

	}

	public HealthCode getHealthCode(String code, String nameSpace) throws RIFServiceException {
		if(!matchesNameSpace(nameSpace)){
			return null;
		}
		else{
			TaxonomyTerm taxonomyTerm = taxonomyContainer.get(code);
		    return transformTaxonomyTermIntoHealthCode(taxonomyTerm);
		}
	}

	public boolean healthCodeExists(HealthCode healthCode) throws RIFServiceException {
		if(matchesNameSpace(healthCode.getNameSpace())){
			return ! (taxonomyContainer.get(healthCode.getCode())==null);
		}
		return false;
	}
	
	public HashMap<String, TaxonomyTerm> getTaxonomyRepository(){
		return taxonomyContainer;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	
	private Parameter getParameter(
		final String parameterName,
		final ArrayList<Parameter> parameters) {
			
		Collator collator = RIFGenericLibraryMessages.getCollator();		
		for (Parameter parameter : parameters) {
			if (collator.equals(parameterName, parameter.getName())) {
				return parameter;
			}			
		}
			
		//parameter was not found
		return null;
	}
	
	/**
	 * Remove the dot sign in WHO ICD 10 healthcode in order to keep data format as used in RIF database
	 * 
	 * @param healthCode The code appears in WTO XML file that contain dot sign.
	 * 
	 * @return healthCode without the dot sign. (E.g., as used in sahsuland cancer table.)
	 */
	
	private static String deleteDotInHealthCode(String healthCode){
		if(! healthCode.contains(".")){
			return healthCode;
		}
		else{
		    StringBuilder healthCodeWithoutDot =new StringBuilder(healthCode);
		    return healthCodeWithoutDot.deleteCharAt(healthCode.indexOf(".")).toString();
		}
	}
	
	private boolean matchesNameSpace(String otherNameSpace){
		if(otherNameSpace ==null){
			return false;
		}
		String nameSpace = healthCodeTaxonomy.getNameSpace();
		Collator collator = RIFGenericLibraryMessages.getCollator();
		return collator.equals(nameSpace, otherNameSpace);
	}
	
	/**
	 * 
	 * @param term 
	 *        A valid {@link TaxonomyTerm}.
	 *        
	 * @return The corresponding {@link HealthCode} of this term.
	 */
	public HealthCode transformTaxonomyTermIntoHealthCode(TaxonomyTerm term){
		if(term ==null){
			return null;
		}
		HealthCode healthCode =HealthCode.newInstance();
		healthCode.setCode(term.getLabel());
		healthCode.setTopLevelTerm(term.getParentTerm() ==null);
		healthCode.setNameSpace(term.getNameSpace());
		healthCode.setDescription(term.getDescription());
		healthCode.setNumberOfSubTerms(term.getChildTerms().size());
		return healthCode;
	}
}
