package rifServices.dataStorageLayer.ms;

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

public final class MSSQLProductionRIFStudyServiceBundle 
	extends MSSQLAbstractStudyServiceBundle {

	// ==========================================
	// Section Constants
	// ==========================================
	
	//TOUR_CONCURRENCY
	/*
	 * <p>
	 * The Singleton pattern is about managing a single instance of a class.  We don't use
	 * it often in the RIF code base, but this is an example of it. All the RIF services will
	 * rely on the same collection of SQL manager classes that execute database queries. We've
	 * bundled the manager objects together into the "StudyServiceBundle", but we only want to
	 * produce one instance of it.
	 * <p> 
	 * 
	 * <p>
	 * Typically, Singleton code patterns are implemented to support "eager instantiation" or 
	 * "lazy instantiation".  Lazy instantiation instantiates the single instance only when it
	 * is first needed.  If the instance requires a lot of computational resources but is never
	 * used, the lazy instantiation will save the middleware from creating the object.
	 * </p>
	 * 
	 * <p>
	 * With lazy instantiation, the code typically declares the single instance as a static 
	 * variable and then does an "if obj != null" check before trying to initialise it once.
	 * </p>
	 * 
	 * <p>
	 * Versions of the lazy instantiation singleton pattern are often prone to concurrency
	 * problems when two threads race to initialise the same instance.  One thread sees that the object
	 * is null and attempts to set it.  The second thread may also see a null object and also try to
	 * instantiate it.  
	 * </p> 
	 * 
	 * <p>
	 * There are a number of ways to make lazy instantiation thread-safe.  However, here we have 
	 * avoided it by using "eager instantiation".  In this example, we declare a static variable that
	 * is set only once, when the Java class loader loads the definition of the class.
	 * </p>
	 * 
	 * <p>
	 * It is simple, but has its own shortcomings which we may remedy later.  Its main weakness is that
	 * 
	 * To avoid this problem,
	 * we have used the "eager instantiation" approach, where we declare and initialise
	 * a static variable when the class is first loaded.
	 * <p>
	 * I believe this solution is thread-safe, but it has its own main weakness of not handling
	 * Exceptions.  In future, we may switch to thread-safe implementations that use
	 * lazy instantiation.  See:
	 * http://www.oracle.com/technetwork/articles/javase/bloch-effective-08-qa-140880.html
	 * 
	 */
	private static final MSSQLProductionRIFStudyServiceBundle rifStudyServiceBundle
		= new MSSQLProductionRIFStudyServiceBundle();

		// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public MSSQLProductionRIFStudyServiceBundle() {

		MSSQLProductionRIFStudySubmissionService rifStudySubmissionService
			= new MSSQLProductionRIFStudySubmissionService();
		setRIFStudySubmissionService(rifStudySubmissionService);

		MSSQLProductionRIFStudyRetrievalService rifStudyRetrievalService
			= new MSSQLProductionRIFStudyRetrievalService();
		setRIFStudyRetrievalService(rifStudyRetrievalService);
		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public static MSSQLProductionRIFStudyServiceBundle getRIFServiceBundle() {
		return rifStudyServiceBundle;
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
}
