package org.sahsu.rif.services.rest;

import java.util.ArrayList;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.AgeBand;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.Investigation;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Sex;

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

final public class InvestigationProxyConverter
	extends XmlAdapter<InvestigationProxy, Investigation> {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public InvestigationProxyConverter() {

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
	
	public InvestigationProxy marshal(final Investigation investigation) {
		InvestigationProxy investigationProxy
			= new InvestigationProxy();
		
		investigationProxy.setTitle(investigation.getTitle());
		investigationProxy.setHealthTheme(investigation.getHealthTheme().getName());
		investigationProxy.setSex(investigation.getSex().getName());
		
		ArrayList<HealthCode> healthCodes
			= investigation.getHealthCodes();
		HealthCodeProxyConverter healthCodeProxyConverter
			= new HealthCodeProxyConverter();
		ArrayList<HealthCodeProxy> healthCodeProxies
			= new ArrayList<HealthCodeProxy>();		
		for (HealthCode healthCode : healthCodes) {
			healthCodeProxies.add(healthCodeProxyConverter.marshal(healthCode));			
		}
		
		ArrayList<AbstractCovariate> covariates
			= investigation.getCovariates();
		/**
		 * KLG TODO - Perhaps need to change API so that ExposureCovariates are
		 * always returned
		 */
		CovariateProxyConverter covariateProxyConverter
			= new CovariateProxyConverter();
		ArrayList<CovariateProxy> covariateProxies
			= new ArrayList<CovariateProxy>();
		for (AbstractCovariate covariate : covariates) {
			AdjustableCovariate adjustableCovariate
				= (AdjustableCovariate) covariate;
			covariateProxies.add(covariateProxyConverter.marshal(adjustableCovariate));
		}
		investigationProxy.setCovariates(covariateProxies);
		
		NumeratorDenominatorPairProxyConverter ndPairProxyConverter
			= new NumeratorDenominatorPairProxyConverter();
		NumeratorDenominatorPair ndPair
			= investigation.getNdPair();
		investigationProxy.setNdPair(ndPairProxyConverter.marshal(ndPair));
		
		AgeBandProxyConverter ageBandProxyConverter
			= new AgeBandProxyConverter();
		ArrayList<AgeBand> ageBands = investigation.getAgeBands();
		ArrayList<AgeBandProxy> ageBandProxies = new ArrayList<AgeBandProxy>();
		for (AgeBand ageBand : ageBands) {
			ageBandProxies.add(ageBandProxyConverter.marshal(ageBand));
		}
		investigationProxy.setAgeBands(ageBandProxies);
		
		return investigationProxy;	
	}
	
	public Investigation unmarshal(final InvestigationProxy investigationProxy) {
		Investigation investigation = Investigation.newInstance();
		
		investigation.setTitle(investigationProxy.getTitle());
		HealthTheme healthTheme
			= HealthTheme.newInstance(investigationProxy.getHealthTheme());
		investigation.setHealthTheme(healthTheme);
		
		Sex sex
			= Sex.getSexFromName(investigationProxy.getHealthTheme());
		investigation.setSex(sex);
		
		HealthCodeProxyConverter healthCodeProxyConverter
			= new HealthCodeProxyConverter();
		ArrayList<HealthCodeProxy> healthCodeProxies
			= investigationProxy.getHealthCodes();
		ArrayList<HealthCode> healthCodes = new ArrayList<HealthCode>();
		for (HealthCodeProxy healthCodeProxy : healthCodeProxies) {
			healthCodes.add(healthCodeProxyConverter.unmarshal(healthCodeProxy));
		}
		investigation.setHealthCodes(healthCodes);
		
		
		ArrayList<CovariateProxy> covariateProxies
			= investigationProxy.getCovariates();
		ArrayList<AbstractCovariate> covariates
			= new ArrayList<AbstractCovariate>();
		CovariateProxyConverter covariateProxyConverter
			= new CovariateProxyConverter();		
		for (CovariateProxy covariateProxy : covariateProxies) {
			covariates.add(covariateProxyConverter.unmarshal(covariateProxy));
		}		
		investigation.setCovariates(covariates);
		
		NumeratorDenominatorPairProxy ndPairProxy
			= investigationProxy.getNdPair();		
		NumeratorDenominatorPairProxyConverter ndPairProxyConverter
			= new NumeratorDenominatorPairProxyConverter();
		investigation.setNdPair(ndPairProxyConverter.unmarshal(ndPairProxy));
		
		AgeBandProxyConverter ageBandProxyConverter
			= new AgeBandProxyConverter();
		ArrayList<AgeBandProxy> ageBandProxies 
			= new ArrayList<AgeBandProxy>();
		ArrayList<AgeBand> ageBands
			= new ArrayList<AgeBand>();
		for (AgeBandProxy ageBandProxy : ageBandProxies) {
			ageBands.add(ageBandProxyConverter.unmarshal(ageBandProxy));
		}
		investigation.setAgeBands(ageBands);
		
		return investigation;
	}
}
