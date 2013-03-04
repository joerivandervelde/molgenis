package org.molgenis.omx;

import static org.molgenis.util.DetectOS.getLineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.omx.core.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EMeasureFeatureWriterTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEMeasure() throws IOException
	{
		new EMeasureFeatureWriter(null).close();
	}

	@Test
	public void testConvert() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		EMeasureFeatureWriter featureWriter = new EMeasureFeatureWriter(bos);
		try
		{
			List<Feature> Features = new ArrayList<Feature>();
			Feature Feature1 = new Feature();
			Feature1.setIdentifier("feature1");
			Feature1.setName("this is feature1");
			Feature1.setDataType_EntityClassName("boolean");
			Feature Feature2 = new Feature();
			Feature2.setIdentifier("feature2");
			Feature2.setName("this is feature2");
			Feature2.setDataType_EntityClassName("string");
			Features.add(Feature1);
			Features.add(Feature2);

			featureWriter.writeFeatures(Features);
		}
		finally
		{
			featureWriter.close();
		}
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><QualityMeasureDocument xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" classCode=\"CONTAINER\" moodCode=\"DEF\" xsi:schemaLocation=\"urn:hl7-org:v3 multicacheschemas/REPC_MT000100UV01.xsd\" xsi:type=\"REPC_MT000100UV01.Organizer\">	<subjectOf>"
				+ getLineSeparator()
				+ "		<measureAttribute><code code=\"feature1\" codeSystem=\"TBD\" displayName=\"this is feature1\"/><value code=\"dunno\" codeSystem=\"TBD\" displayName=\"This should be the mappingsname\" xsi:type=\"boolean\"/>		</measureAttribute>"
				+ getLineSeparator()
				+ "	</subjectOf>	<subjectOf>"
				+ getLineSeparator()
				+ "		<measureAttribute><code code=\"feature2\" codeSystem=\"TBD\" displayName=\"this is feature2\"/><value code=\"dunno\" codeSystem=\"TBD\" displayName=\"This should be the mappingsname\" xsi:type=\"string\"/>		</measureAttribute>"
				+ getLineSeparator() + "	</subjectOf></QualityMeasureDocument>";

		Assert.assertEquals(bos.toString("UTF-8"), expected);
	}
}
