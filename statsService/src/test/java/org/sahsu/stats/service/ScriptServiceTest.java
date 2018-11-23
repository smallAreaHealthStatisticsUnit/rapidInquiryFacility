package org.sahsu.stats.service;

import java.util.Arrays;

import org.junit.Test;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.Parameters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScriptServiceTest {

	@Test
	public void when_studyTypeIsRiskAnalysis_returnsTrue() {

		Parameter p1 = Parameter.newInstance("this", "that");
		Parameter p2 = Parameter.newInstance("the", "other");
		Parameter p3 = Parameter.newInstance("studyType", "riskAnalysis");
		Parameter p4 = Parameter.newInstance("andSomething", "else");

		Parameters parms = new Parameters(Arrays.asList(p1, p2, p3, p4));
		ScriptService underTest = ScriptService.instance();
		assertTrue(underTest.isRiskAnalysis(parms));
	}

	@Test
	public void when_studyTypeIsDiseaseMapping_returnsFalse() {

		Parameter p1 = Parameter.newInstance("this", "that");
		Parameter p2 = Parameter.newInstance("the", "other");
		Parameter p3 = Parameter.newInstance("studyType", "diseaseMapping");
		Parameter p4 = Parameter.newInstance("andSomething", "else");

		Parameters parms = new Parameters(Arrays.asList(p1, p2, p3, p4));
		ScriptService underTest = ScriptService.instance();
		assertFalse(underTest.isRiskAnalysis(parms));
	}

	@Test
	public void when_noStudyType_returnsFalse() {

		Parameter p1 = Parameter.newInstance("this", "that");
		Parameter p2 = Parameter.newInstance("the", "other");
		Parameter p4 = Parameter.newInstance("andSomething", "else");

		Parameters parms = new Parameters(Arrays.asList(p1, p2, p4));
		ScriptService underTest = ScriptService.instance();
		assertFalse(underTest.isRiskAnalysis(parms));
	}

}
