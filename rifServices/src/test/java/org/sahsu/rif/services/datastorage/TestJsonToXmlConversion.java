package org.sahsu.rif.services.datastorage;

import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;

/*
   Not a real unit test. I wrote this to confirm my understanding of what was happening when we
   convert a JSON array to XML; specifically the covariates array, when we added support for
   multiple covariates.

   -- Martin McCallion, 2019-03-07
 */
public class TestJsonToXmlConversion {

	private static final String JSON1 = "{\"things\":["
	                                    + "{\"thing\":\"one\"}, "
	                                    + "{\"thing\":\"two\"}]}";
	private static final String JSON2 = "{\"things\":{\"thinga\":"
	                                    + "{\"thing1\":\"one\"}, "
	                                    + "\"thingb\":{\"thing2\":\"two\"}}}";
	private static final String JSON3 = "{\"things\":{\"thing\":["
	                                    + "{\"thing1\":\"one\"}, "
	                                    + "{\"thing2\":\"two\"}]}}";

	private static final String COVARIATES = "{\"covariates\":{\"covariate\":[\n"
	                                         + "\t{\"adjustable_covariate\":\n"
	                                         + "\t\t{\"name\":\"AREATRI1KM\",\"minimum_value\":\"0"
	                                         + ".0\",\"maximum_value\":\"1.0\","
	                                         + "\"covariate_type\":\"INTEGER_SCORE\","
	                                         + "\"description\":\"area tri 1 km covariate\"}\n"
	                                         + "\t},\n"
	                                         + "\t{\"adjustable_covariate\":\n"
	                                         + "\t\t{\"name\":\"SES\",\"minimum_value\":\"1.0\","
	                                         + "\"maximum_value\":\"5.0\","
	                                         + "\"covariate_type\":\"INTEGER_SCORE\","
	                                         + "\"description\":\"socio-economic status\"}\n"
	                                         + "\t}\n"
	                                         + "]}}";

	@Test
	public void testConvert() {

		JSONObject jsonObject1 = new JSONObject(JSON1);
		String xmlString1 = XML.toString(jsonObject1);
		JSONObject jsonObject2 = new JSONObject(JSON2);
		String xmlString2 = XML.toString(jsonObject2);
		JSONObject jsonObject3 = new JSONObject(JSON3);
		String xmlString3 = XML.toString(jsonObject3);
		System.out.printf("JSON1=%s\nXML1=%s\nJSON2=%s\nXML2=%s\nJSON3=%s\nXML3=%s\n", JSON1,
		                  xmlString1, JSON2, xmlString2, JSON3, xmlString3);

		JSONObject cov = new JSONObject(COVARIATES);
		String xmlCov = XML.toString(cov);
		System.out.printf("Covariates JSON: %s\nCovariates XML : %s\n", cov, xmlCov);
	}

}
