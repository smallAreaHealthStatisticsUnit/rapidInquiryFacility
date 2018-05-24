package org.sahsu.rif.dataloader.test;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public class UKHospitalEpisodeStatisticsGenerator 
	extends CSVInterRecordLevelFileGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public UKHospitalEpisodeStatisticsGenerator(
		final boolean trackCoverage,
		final int maximumRecordsToCreate) {
		
		super(
			trackCoverage, 
			maximumRecordsToCreate);
		
	}

	private void foo() {
		addField("ACPPLAN_N");
		addCommonFieldValue("ACPPLAN_N", "1", "2");
		addUncommonFieldValue("ACPPLAN_N", "9");
		addErrorFieldValue("ACPPLAN_N", "No");
		
		addField("ACPSEQ");
		addCommonFieldValue("ACPSEQ", "1", "2", "3");
		addErrorFieldValue("ACPSEQ", "99");
		
		addField("ACPSOUR_N");
		addCommonFieldValue("ACPSOUR_N", "01", "02", "03", "04", "05");
		addCommonFieldValue("ACPSOUR_N", "06", "07", "08", "09", "10");
		addUncommonFieldValue("ACPSOUR_N", "99");
		addErrorFieldValue("ACPSOUR_N", "12");
		
		addField("ACPSPEF_N");
		addCommonFieldValue("ACPSPEF_N", "100", "101", "110", "120", "130");
		addCommonFieldValue("ACPSPEF_N", "140", "141", "142", "143");
		//the fields below are available from 2004-2005
		addCommonFieldValue("ACPSPEF_N", "145", "146", "147", "148", "149"); 
		addCommonFieldValue("ACPSPEF_N", "150", "160", "170", "171", "180", "190");
		addCommonFieldValue("ACPSPEF_N", "180", "190");
		//the field below are available from 1998-1999 and 2003-2004
		addCommonFieldValue("ACPSPEF_N", "191");
		//the field below are available from 2004-2005
		addCommonFieldValue("ACPSPEF_N", "192");
		addCommonFieldValue("ACPSPEF_N", "300", "301", "302", "303", "304", "305");
		addCommonFieldValue("ACPSPEF_N", "310", "311");
		//the field below are available from 1990 - 1991		
		addCommonFieldValue("ACPSPEF_N", "312");
		//the fields below are available from 1991 - 1992		
		addCommonFieldValue("ACPSPEF_N", "313", "314");
		addCommonFieldValue("ACPSPEF_N", "315", "320");
		//the field below are available from 2004-2005		
		addCommonFieldValue("ACPSPEF_N", "321");
		addCommonFieldValue("ACPSPEF_N", "330", "340", "350");
		//the field below are available from 2004-2005		
		addCommonFieldValue("ACPSPEF_N", "352");
		addCommonFieldValue("ACPSPEF_N", "360", "361", "370", "371", "400");
		addCommonFieldValue("ACPSPEF_N", "401", "410", "420", "421", "430");
		//available from 1990-1991
		addCommonFieldValue("ACPSPEF_N", "450");
		//available from 1993-1994
		addCommonFieldValue("ACPSPEF_N", "450");
		//available from 1993-1994
		addCommonFieldValue("ACPSPEF_N", "460");
		addCommonFieldValue("ACPSPEF_N", "499");
		//available prior to 2004-2005
		addCommonFieldValue("ACPSPEF_N", "501");
		addCommonFieldValue("ACPSPEF_N", "502");
		//available from October 1995
		addCommonFieldValue("ACPSPEF_N", "560");		
		addCommonFieldValue("ACPSPEF_N", "600", "601");
		//available to 2003-2004
		addCommonFieldValue("ACPSPEF_N", "610", "620");
		addCommonFieldValue("ACPSPEF_N", "700", "710", "711", "712", "713");
		addCommonFieldValue("ACPSPEF_N", "715", "800", "810", "820", "821");
		addCommonFieldValue("ACPSPEF_N", "822", "823", "824", "830", "831");
		//available to 2003-2004
		addCommonFieldValue("ACPSPEF_N", "832");
		addCommonFieldValue("ACPSPEF_N", "900", "901");
		//available from 2004-2005
		addCommonFieldValue("ACPSPEF_N", "902", "903", "904");
		//available from 2002-2003
		addCommonFieldValue("ACPSPEF_N", "950");
		//available from 2006-2007
		addCommonFieldValue("ACPSPEF_N", "960");
		addUncommonFieldValue("ACPSPEF_N", "null");
		addUncommonFieldValue("ACPSPEF_N", "&");

		//available 1997-1998 to 2005-2006
		addField("ACPSTAR_N");
		addCommonFieldValue("ACPSTAR_N", "23/04/2002");
		addErrorFieldValue("ACPSTAR_N", "04/23/2002");
		addUncommonFieldValue("ACPSTAR_N", "null");
		
		
		addField("ACSCFLAG");
		addCommonFieldValue("ACSCFLAG", "1");
		addCommonFieldValue("ACSCFLAG", "0");
		addErrorFieldValue("ACSCFLAG", "3");
		addErrorFieldValue("ACSCFLAG", "11");
		
		addField("ACTIVAGE");
		addCommonFieldValue("ACTIVAGE", "24");
		addUncommonFieldValue("ACTIVAGE", "0");
		addUncommonFieldValue("ACTIVAGE", "130");
		addUncommonFieldValue("ACTIVAGE", "999");
		addErrorFieldValue("ACTIVAGE", "8569");

		addField("ADM_CFL");
		addCommonFieldValue("ADM_CFL", "0");
		addUncommonFieldValue("ADM_CFL", "1");
		addCommonFieldValue("ADM_CFL", "2");
		addErrorFieldValue("ADM_CFL", "3");
		addErrorFieldValue("ADM_CFL", "22");

		addField("ADMIAGE");
		addCommonFieldValue("ADMIAGE", "18");
		addUncommonFieldValue("ADMIAGE", "0");
		addUncommonFieldValue("ADMIAGE", "180"); //schema says this is OK
		addUncommonFieldValue("ADMIAGE", "999");
		addErrorFieldValue("ADMIAGE", "5647");
		
		addField("ADMIDATE");
		addCommonFieldValue("ADMIDATE", "12/11/2006");
		addUncommonFieldValue("ADMIDATE", "01/01/1800"); //develop cleaning rules!
		addUncommonFieldValue("ADMIDATE", "01/01/1801");
		addUncommonFieldValue("ADMIDATE", "01/01/1600");
		addUncommonFieldValue("ADMIDATE", "15/10/1582");
		addErrorFieldValue("ADMIDATE", "11/13/2006");

		addField("ADMIFLAG");
		addCommonFieldValue("ADMIFLAG", "Y");
		addCommonFieldValue("ADMIFLAG", "N");
		addErrorFieldValue("ADMIFLAG", "X");
		
		addField("ADMIMETH");
		addCommonFieldValue("ADMIMETH", "11", "12", "13", "21", "22");
		addCommonFieldValue("ADMIMETH", "23", "24");
		//available from 2013-2014
		addCommonFieldValue("ADMIMETH", "25");
		//available from 2013-2014		
		addCommonFieldValue("ADMIMETH", "2A", "2B", "2C", "2D");
		addCommonFieldValue("ADMIMETH", "28", "31", "32", "82", "83");
		addUncommonFieldValue("ADMIMETH", "98", "99");
		addErrorFieldValue("ADMIMETH", "2F");
		addErrorFieldValue("ADMIMETH", "14");
		addErrorFieldValue("ADMIMETH", "232");
		addErrorFieldValue("ADMIMETH", "14E");

		//available from 2000-2001 onwards
		addField("ADMINCAT");
		
		addCommonFieldValue("ADMINCAT", "01", "02", "03", "04");
		addUncommonFieldValue("ADMINCAT", "98", "99");
		addErrorFieldValue("ADMINCAT", "0");
		addErrorFieldValue("ADMINCAT", "1");
		addErrorFieldValue("ADMINCAT", "07");

		//available 2007-2008 onwards
		//marked: data not cleaned
		addField("ADMINCATST");
		addCommonFieldValue("ADMINCATST", "01", "02", "03", "04");
		addUncommonFieldValue("ADMINCATST", "98", "99");
		addErrorFieldValue("ADMINCATST", "06");
		addErrorFieldValue("ADMINCATST", "101"); 

		
		addField("ADMISORC");
		addCommonFieldValue("ADMISORC", "19", "29");
		
		//available 1999-00 to 2006-2007
		addCommonFieldValue("ADMISORC", "30", "37", "38");

		//excluded from 1999-2000
		addCommonFieldValue("ADMISORC", "39");
		
		addCommonFieldValue("ADMISORC", "49");
		//available 1999-00 to 2006-2007
		addCommonFieldValue("ADMISORC", "48", "50");
		
		addCommonFieldValue("ADMISORC", "51", "52", "53", "54");

		//from 1996-1997
		addCommonFieldValue("ADMISORC", "65", "66");
		
		//from 1989-90 to 1995-96
		addCommonFieldValue("ADMISORC", "69");

		//available from 1996-1997
		addCommonFieldValue("ADMISORC", "85", "86");

		addCommonFieldValue("ADMISORC", "79", "87", "88");
		
		//available from 1989-90 to 1995-96
		addCommonFieldValue("ADMISORC", "89");
		
		addUncommonFieldValue("ADMISORC", "98", "99");
		addErrorFieldValue("ADMISORC", "999");
		addErrorFieldValue("ADMISORC", "72");

		//available 1996-1997 onwards
		addField("ADMISTAT");
		addCommonFieldValue("ADMISTAT", "0", "1", "2");
		addUncommonFieldValue("ADMISTAT", "8", "9");
		addErrorFieldValue("ADMISTAT", "6");
		addErrorFieldValue("ADMISTAT", "32");
		addErrorFieldValue("ADMISTAT", "Unk");
		addErrorFieldValue("ADMISTAT", "n.a.");

		
		//Marked as "Data not Cleaned"
		addField("AEKEY");
		addCommonFieldValue("AEKEY", "12345678912345");
		addErrorFieldValue("AEKEY", "453636");

		addField("ALCDIAG");
		addCommonFieldValue("ALCDIAG", "C185", "486");
		//this is a legitimate code in ICD10 (malignant carcinoid tumours of appendix)
		//but does not match the annnna or annnnn formats expected
		//by field
		addErrorFieldValue("ALCDIAG", "C7A020");
		addErrorFieldValue("ALCDIAG", "X");

		//not sure
		addField("ALCDIAG_4");
		addCommonFieldValue("ALCDIAG_4", "C185", "486");

		addField("ALCFRAC");
		addCommonFieldValue("ALCFRAC", "4.56", "3.21", "1.2");
		addErrorFieldValue("ALCFRAC", "none");

		
		addField("ANAGEST");
		addUncommonFieldValue("ANAGEST", "38");
		addUncommonFieldValue("ANAGEST", "7");
		addUncommonFieldValue("ANAGEST", "null");
		addErrorFieldValue("ANAGEST", "77");

		addField("ANASDATE");
		addCommonFieldValue("ANASDATE", "12/11/2006");
		addUncommonFieldValue("ANASDATE", "01/01/1800"); 
		addUncommonFieldValue("ANASDATE", "01/01/1801");
		addErrorFieldValue("ANASDATE", "11/13/2006");
		
		
		addField("ANTEDUR");
		addCommonFieldValue("ANTEDUR", "10");
		addUncommonFieldValue("ANTEDUR", "270");
		addUncommonFieldValue("ANTEDUR", "null");
		addErrorFieldValue("ANTEDUR", "271");
		addErrorFieldValue("ANTEDUR", "3000");
	
		addField("AT_GP_PRACTICE");
		addCommonFieldValue("AT_GP_PRACTICE", "4XX");
		addUncommonFieldValue("AT_GP_PRACTICE", "Y");
		addErrorFieldValue("AT_GP_PRACTICE", "9X2");
		addErrorFieldValue("AT_GP_PRACTICE", "Y2345");
		
		addField("AT_RESIDENCE");
		addCommonFieldValue("AT_RESIDENCE", "S10", "U11", "W12");
		//available from 1990/1991
		addCommonFieldValue("AT_RESIDENCE", "X13");
		addUncommonFieldValue("AT_RESIDENCE", "Y");		
		addErrorFieldValue("AT_RESIDENCE", "B15");
		
		addField("AT_TREATMENT");
		addCommonFieldValue("AT_TREATMENT", "B4", "C14");
		addUncommonFieldValue("AT_TREATMENT", "Y");
		addErrorFieldValue("AT_TREATMENT", "AB2");
		addErrorFieldValue("AT_TREATMENT", "A231");
		
		addField("BABYSEQ");
		addCommonFieldValue("BABYSEQ", "1");
		addUncommonFieldValue("BABYSEQ", "7");
		addErrorFieldValue("BABYSEQ", "0");
		addErrorFieldValue("BABYSEQ", "10");
		
		addField("BEDYEAR");
		addCommonFieldValue("BEDYEAR", "0", "5", "10", "17");
		addUncommonFieldValue("BEDYEAR", "366", "null");
		addErrorFieldValue("BEDYEAR", "367");

		addField("BIRESUS_N");
		addCommonFieldValue("BIRESUS_N", "1", "2", "3", "4");
		addUncommonFieldValue("BIRESUS_N", "8", "9");
		addErrorFieldValue("BIRESUS_N", "11", "a");
		
		addField("BIRORDER_N");
		addCommonFieldValue("BIRORDER_N", "1", "2", "3", "4");
		addUncommonFieldValue("BIRORDER_N", "8", "9", "X");
		addErrorFieldValue("BIRORDER_N", "11", "a");
				
		addField("BIRSTAT_N");
		addCommonFieldValue("BIRSTAT_N", "1", "2", "3", "4");
		addUncommonFieldValue("BIRSTAT_N", "9");
		addErrorFieldValue("BIRSTAT_N", "5", "12");
			
		addField("BIRWEIT_N");
		addCommonFieldValue("BIRWEIT_N", "3350");
		
		addUncommonFieldValue("BIRWEIT_N", "0001");
		addUncommonFieldValue("BIRWEIT_N", "6999");
		addUncommonFieldValue("BIRWEIT_N", "9999");
		addErrorFieldValue("BIRWEIT_N", "0000");
		addErrorFieldValue("BIRWEIT_N", "8000");
		
		//available 2007-2008 onwards
		addField("BOOKREFNO");
		addCommonFieldValue("BOOKREFNO", "123456789123");
		addErrorFieldValue("BOOKREFNO", "123456789123567");
		
		//available 2008-09 onwards		
		addField("CANNET");
		addCommonFieldValue("CANNET", "N01", "N98", "Z99");
		addUncommonFieldValue("CANNET", "Null");
		addErrorFieldValue("CANNET", "M03", "Q345");
		
		//available 2008-09 onwards		
		addField("CANREG");
		addCommonFieldValue("CANREG", "Y0201", "Y1500", "Y1701", "Z9999");
		addUncommonFieldValue("CANREG", "Null");
		addErrorFieldValue("CANREG", "Y0100", "Y1702");
		
		//available from 1997-98
		addField("CARERSI");
		addCommonFieldValue("CARERSI", "01", "02");
		addUncommonFieldValue("CARERSI", "99");
		addErrorFieldValue("CARERSI", "1", "2", "3", "9");
		
		//available from 1989-90 to 2001-02
		addField("CATEGORY");
		addCommonFieldValue("CATEGORY", "10", "11", "12", "13", "20");
		addCommonFieldValue("CATEGORY", "21", "22", "23", "30", "31", "32", "33");
		addUncommonFieldValue("CATEGORY", "null");
		addErrorFieldValue("CATEGORY", "14", "34", "57", "99");
	
		addField("CAUSE");
		addCommonFieldValue("CAUSE", "V01", "V0109XA", "Y98");
		addUncommonFieldValue("CAUSE", "null");

		addField("CAUSE_3");
		addCommonFieldValue("CAUSE_3", "V01", "V0109XA", "Y98");
		addUncommonFieldValue("CAUSE_3", "null");
	
		addField("CAUSE_4");
		addCommonFieldValue("CAUSE_4", "V01", "V0109XA", "Y98");
		addUncommonFieldValue("CAUSE_4", "null");
		
		//Available from 2012-2014 onwards
		addField("CCG_GP_PRACTICE");
		addCommonFieldValue("CCG_GP_PRACTICE", "56A");
		addUncommonFieldValue("CCG_GP_PRACTICE", "59999");
		addErrorFieldValue("CCG_GP_PRACTICE", "60000");
		addErrorFieldValue("CCG_GP_PRACTICE", "X12");
		
		//Available 2013-14 onwards
		addField("CCG_RESIDENCE");
		addCommonFieldValue("CCG_RESIDENCE", "56A");
		addUncommonFieldValue("CCG_RESIDENCE", "59999");
		addErrorFieldValue("CCG_RESIDENCE", "60000");
		addErrorFieldValue("CCG_RESIDENCE", "X12");		
		
		//Available 2013-14 onwards
		addField("CCG_RESPONSIBLITY");
		addCommonFieldValue("CCG_RESPONSIBLITY", "56A");
		addUncommonFieldValue("CCG_RESPONSIBLITY", "59999");
		addErrorFieldValue("CCG_RESPONSIBLITY", "60000");
		addErrorFieldValue("CCG_RESPONSIBLITY", "X12");			
		
		//Available 2013-14 onwards
		addField("CCG_RESPONSIBILITY_ORIGIN");
		addCommonFieldValue("CCG_RESPONSIBILITY_ORIGIN", "1", "2", "3", "4");
		addUncommonFieldValue("CCG_RESPONSIBILITY_ORIGIN", "9");
		addErrorFieldValue("CCG_RESPONSIBILITY_ORIGIN", "6", "8", "99");
		
		addField("CCG_TREATMENT");
		addCommonFieldValue("CCG_TREATMENT", "56A");
		addUncommonFieldValue("CCG_TREATMENT", "59999");
		addErrorFieldValue("CCG_TREATMENT", "60000");
		addErrorFieldValue("CCG_TREATMENT", "X12");		
		
		//Available 2013-14 onwards
		addField("CCG_TREATMENT_ORIGIN");
		addCommonFieldValue("CCG_TREATMENT_ORIGIN", "1", "2");
		addUncommonFieldValue("CCG_TREATMENT_ORIGIN", "9");
		addErrorFieldValue("CCG_TREATMENT_ORIGIN", "3", "99");
		
		//Available 2007-2008 onwards
		addField("CDSEXTDATE");
		addCommonFieldValue("CDSEXTDATE", "04/04/2010");
		addUncommonFieldValue("CDSEXTDATE", "");
		addErrorFieldValue("CDSEXTDATE", "04/04/2002", "12/14/2009");
		
		//Available 2007-2008 onwards
		addField("CDSUNIQUEID");
		addCommonFieldValue("CDSUNIQUEID", "abcdefghijklmnopqrstuvwxyz123456789");
		addErrorFieldValue("CDSUNIQUEID", "abcdefghijklmnopqrstuvwxyz12345678988");

		//Available 2007-2008 onwards
		//Marked as Data not cleaned
		addField("CDSVERPROTID");
		addCommonFieldValue("CDSVERPROTID", "4AX");
		addErrorFieldValue("CDSVERPROTID", "554A");
		
		addField("CDSVERSION");
		addCommonFieldValue("CDSVERSION", "567AB4");
		addErrorFieldValue("CDSVERSION", "567AB47868");
		
		addField("CENDUR");
		addCommonFieldValue("CENDUR", "0", "50");
		addUncommonFieldValue("CENDUR", "29200", "null");
		addErrorFieldValue("CENDUR", "292007");

		addField("CENSAGE");
		addCommonFieldValue("CENSAGE", "40", "23", "64");
		addUncommonFieldValue("CENSAGE", "1", "119", "null");
		addErrorFieldValue("CENSAGE", "");

		addField("CENSTAT");
		addCommonFieldValue("CENSTAT", "1", "2", "3");
		
		//1990-91 to 1995-96
		addUncommonFieldValue("CENSTAT", "null");
		addErrorFieldValue("CENSTAT", "");

		addField("CENWARD");
		addCommonFieldValue("CENWARD", "518116");	
		/**
		 * 33=maternity, associated with maternity wards [AA]
		 * 3=elderly [B]
		 * 1=male [C]
		 * 1=NHS hospital provider [D]
		 * 5=number of days in a week that ward is open at night [F]
		 */		
		addErrorFieldValue("CENWARD", "333115");

		addField("CHAPTER");
		addCommonFieldValue("CHAPTER", "I", "XXII");
		addErrorFieldValue("CHAPTER", "VII");

		addField("CLASSPAT");
		addCommonFieldValue("CLASSPAT", "1", "2", "3", "4", "5");
		addUncommonFieldValue("CLASSPAT", "8", "9");
		addErrorFieldValue("CLASSPAT", "6", "10");

		addField("CONSULT");
		addCommonFieldValue("CONSULT", "C1234567", "D1234567", "CD123456");
		addUncommonFieldValue("CONSULT", "H9999998", "M9999998", "N9999998");
		addUncommonFieldValue("CONSULT", "C9999998", "D9999998", "CD999998");
		addUncommonFieldValue("CONSULT", "&", "99");
		addErrorFieldValue("CONSULT", "C111222333444", "E8888888");

		//Available from 2013-2014 onwards
		addField("CR_GP_PRACTICE");
		addCommonFieldValue("CR_GP_PRACTICE", "B13");
		addUncommonFieldValue("CR_GP_PRACTICE", "Y");
		addErrorFieldValue("CR_GP_PRACTICE", "X456X");

		addField("CR_RESIDENCE");
		addCommonFieldValue("CR_RESIDENCE", "B13");
		addUncommonFieldValue("CR_RESIDENCE", "Y");
		addErrorFieldValue("CR_RESIDENCE", "X456X");		
		
		addField("CR_TREATMENT");
		addCommonFieldValue("CR_TREATMENT", "B13");
		addUncommonFieldValue("CR_TREATMENT", "Y");
		addErrorFieldValue("CR_TREATMENT", "X456X");		
		
		//Available from 2000-01
		//Marked as Data not cleaned
		addField("CSNUM");
		addCommonFieldValue("CSNUM", "6A5B4C");
		addErrorFieldValue("CSNUM", "66666BBBB");		
				
		addField("CURRWARD");
		addCommonFieldValue("CURRWARD", "AB");
		addErrorFieldValue("CURRWARD", "ABC");
		
		//Available 2011-12 onwards
		addField("CURRWARD_ONS");
		addCommonFieldValue("CURRWARD_ONS", "E12345678", "W12345678");
		addCommonFieldValue("CURRWARD_ONS", "S12345678", "95B 24");
		addUncommonFieldValue("CURRWARD_ONS", "Y");
		addUncommonFieldValue("CURRWARD_ONS", "E99999999", "W99999999");		
		addUncommonFieldValue("CURRWARD_ONS", "E99999999", "W99999999");		
		addUncommonFieldValue("CURRWARD_ONS", "S99999999", "L99999999");		
		addUncommonFieldValue("CURRWARD_ONS", "M99999999");		
		addErrorFieldValue("CURRWARD_ONS", "N", "R12345678", "L11223344556677");
		
		addField("DELCHANG");
		addCommonFieldValue("DELCHANG", "1", "2", "3", "4", "5", "6", "7");
		addUncommonFieldValue("DELCHANG", "8", "9", "null");
		addErrorFieldValue("DELCHANG", "0", "10");	
		
		addField("DELINTEN");
		addCommonFieldValue("DELINTEN", "0", "1", "2", "3", "4", "5", "6");
		addCommonFieldValue("DELINTEN", "7");
		addUncommonFieldValue("DELINTEN", "8", "9");
		addErrorFieldValue("DELINTEN", "11");		
		
		addField("DELMETH_D");
		addCommonFieldValue("DELMETH_D", "01", "02", "03", "04", "05", "06", "07");
		addCommonFieldValue("DELMETH_D", "08", "09", "10");
		addUncommonFieldValue("DELMETH_D", "X");
		addErrorFieldValue("DELMETH_D", "4", "11");		
		
		addField("DELMETH_N");
		addCommonFieldValue("DELMETH_N", "0", "1", "2", "3", "4", "5", "6", "7");
		addCommonFieldValue("DELMETH_N", "8");
		addUncommonFieldValue("DELMETH_N", "9", "X");
		addErrorFieldValue("DELMETH_N", "Y", "10");		
		
		addField("DELONSET");
		addCommonFieldValue("DELONSET", "1", "2", "3", "4", "5");
		//from 1996-1997 onwards
		addUncommonFieldValue("DELONSET", "8");
		
		addUncommonFieldValue("DELONSET", "9", "null");
		
		addField("DELPLAC_N");
		addCommonFieldValue("DELPLAC_N", "0", "1", "2", "3", "4", "5", "6", "7");
		addUncommonFieldValue("DELPLAC_N", "8", "9");
		addErrorFieldValue("DELPLAC_N", "X", "10");		
		
		addField("DELPOSAN");
		addCommonFieldValue("DELPOSAN", "1", "2", "3", "4", "5", "6");
		addUncommonFieldValue("DELPOSAN", "7", "8", "9");
		//from 1990-91 to 1994-95
		addUncommonFieldValue("DELPOSAN", "null");
		addErrorFieldValue("DELPOSAN", "0", "12");
		
		addField("DELPREAN");
		addCommonFieldValue("DELPREAN", "1", "2", "3", "4", "5", "6");
		addUncommonFieldValue("DELPREAN", "7", "8", "9");
		addErrorFieldValue("DELPREAN", "");		
		addUncommonFieldValue("DELPREAN", "null");
		addErrorFieldValue("DELPREAN", "0", "12");		
		
		addField("DELSTAT_N");
		addCommonFieldValue("DELSTAT_N", "1", "2", "3");
		addUncommonFieldValue("DELSTAT_N", "8", "9");
		addErrorFieldValue("DELSTAT_N", "4", "10");		
		
		//Available 1997-98 to 2005-06
		addField("DEPDAYS");
		addCommonFieldValue("DEPDAYS", "000", "10");
		addUncommonFieldValue("DEPDAYS", "9998", "9999");
		addErrorFieldValue("DEPDAYS", "10000");		
		
		
		addField("DET_CFL");
		addCommonFieldValue("DET_CFL", "0", "1", "2");
		addErrorFieldValue("DET_CFL", "9", "99");		
		
		addField("DETDUR");
		addCommonFieldValue("DETDUR", "0", "5", "10");
		addUncommonFieldValue("DETDUR", "29200", "null");
		addErrorFieldValue("DETDUR", "29201");		
		
		addField("DETNCAT");
		addCommonFieldValue("DETNCAT", "0", "1", "2", "3");
		addErrorFieldValue("DETNCAT", "4");		
		
		addField("DETNDATE");
		addCommonFieldValue("DETNDATE", "");
		addUncommonFieldValue("DETNDATE", "01/01/1800");
		//Available from 1989-90 to 2011-12
		addUncommonFieldValue("DETNDATE", "01/01/1801");
		
		addUncommonFieldValue("DETNDATE", "01/01/1600");
		addUncommonFieldValue("DETNDATE", "01/01/1582");
		
		addErrorFieldValue("DETNDATE", "15/15/2010");		
		
		addField("DIAG_3_CONCAT");
		addCommonFieldValue("DIAG_3_CONCAT", "J45,J46");
		addErrorFieldValue("DIAG_3_CONCAT", "J633,J634");		
		
		addField("DIAG_3_NN");
		addCommonFieldValue("DIAG_3_NN", "J45");
		addUncommonFieldValue("DIAG_3_NN", "null");
		addErrorFieldValue("DIAG_3_NN", "J633");		
		
		addField("");
		addCommonFieldValue("", "");
		addUncommonFieldValue("", "");
		addErrorFieldValue("", "");		
		
		addField("");
		addCommonFieldValue("", "");
		addUncommonFieldValue("", "");
		addErrorFieldValue("", "");		
		
		addField("");
		addCommonFieldValue("", "");
		addUncommonFieldValue("", "");
		addErrorFieldValue("", "");		
		
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

}


