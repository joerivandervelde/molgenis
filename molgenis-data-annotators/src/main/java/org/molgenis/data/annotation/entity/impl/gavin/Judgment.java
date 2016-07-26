package org.molgenis.data.annotation.entity.impl.gavin;

/**
 * Judgment result of the gavin method
 */
public class Judgment
{
	public enum Classification
	{
		Benign, Pathogenic, VOUS
	}

	public enum Method{
		calibrated, genomewide
	}

	String reason;
	Classification classification;
	Method method;
	String gene;
	String source; //e.g. "LabVariants", "ClinVar", "GAVIN"
	String type; //e.g. "Reported pathogenic", "Predicted pathogenic"

	public Judgment(Classification classification, Method method, String gene, String reason)
	{
		super();
		this.reason = reason;
		this.gene = gene;
		this.classification = classification;
		this.method = method;
		this.gene = gene;
	}

	public String getSource() {
		return source != null ? source : "";
	}

	public Judgment setSource(String source) {
		this.source = source;
		return this;
	}

	public String getType() {
		return type != null ? type : "";
	}

	public Judgment setType(String type) {
		this.type = type;
		return this;
	}

	public String getReason()
	{
		return reason;
	}

	public Classification getClassification()
	{
		return classification;
	}

	public Method getConfidence()
	{
		return method;
	}

	public String getGene() {
		return gene;
	}

	@Override
	public String toString()
	{
		return "Judgment [reason=" + reason + ", classification=" + classification + "]";
	}
}
