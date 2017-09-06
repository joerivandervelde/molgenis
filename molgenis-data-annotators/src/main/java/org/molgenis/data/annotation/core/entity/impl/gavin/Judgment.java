package org.molgenis.data.annotation.core.entity.impl.gavin;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Judgment result of the gavin method
 */
@AutoValue
public abstract class Judgment
{
	public enum Classification
	{
		Benign, Pathogenic, VOUS
	}

	public enum Method
	{
		calibrated, genomewide
	}

	public abstract String getSource();

	public abstract String getType();

	public abstract String getReason();

	public abstract Classification getClassification();

	public abstract Method getConfidence();

	public abstract String getGene();

	public static Judgment create(String source, String type, Classification classification, Method confidence, String gene, String reason)
	{
		return new AutoValue_Judgment(source,type,reason,classification,confidence,gene);
	}

	public static Judgment create(Classification classification, Method confidence, String gene, String reason)
	{
		return new AutoValue_Judgment(null,null,reason,classification,confidence,gene);
	}

}