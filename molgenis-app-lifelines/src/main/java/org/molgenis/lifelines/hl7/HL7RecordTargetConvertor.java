package org.molgenis.lifelines.hl7;

import org.molgenis.hl7.COCTMT050000UV01Patient;
import org.molgenis.hl7.II;
import org.molgenis.hl7.REPCMT000100UV01RecordTarget;
import org.molgenis.omx.core.Feature;
import org.molgenis.omx.core.Observation;
import org.molgenis.omx.core.ObservedValue;
import org.molgenis.omx.core.Value;
import org.molgenis.omx.values.TextValue;

public class HL7RecordTargetConvertor
{
	private static final String FEATURE_NAME = "patient";

	private HL7RecordTargetConvertor()
	{
	}

	public static Feature toObservableFeature(REPCMT000100UV01RecordTarget recordTarget)
	{
		COCTMT050000UV01Patient patient = recordTarget.getPatient().getValue();
		II id = patient.getId().iterator().next();

		Feature feature = new Feature();
		feature.setIdentifier(id.getRoot());
		feature.setName(FEATURE_NAME);
		return feature;
	}

	public static String toObservableFeatureIdentifier(REPCMT000100UV01RecordTarget recordTarget)
	{
		COCTMT050000UV01Patient patient = recordTarget.getPatient().getValue();
		II id = patient.getId().iterator().next();
		return id.getRoot();
	}

	public static ObservedValue toObservedValue(REPCMT000100UV01RecordTarget recordTarget, Feature feature,
			Observation observationSet)
	{
		COCTMT050000UV01Patient patient = recordTarget.getPatient().getValue();
		II id = patient.getId().iterator().next();

		ObservedValue value = new ObservedValue();
		value.setFeature(feature);
		value.setObservation(observationSet);
		//FIXME: support other types than string!
		TextValue sv = new TextValue();
		sv.setValue(id.getExtension());
		//FIXME: add refenced value to database here?!?
		value.setValue(sv);
		return value;
	}
}
