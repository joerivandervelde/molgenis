package org.molgenis.omicsconnect;


import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.DatabaseFactory;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.core.CustomClass;
import org.molgenis.omx.core.DataItem;
import org.molgenis.omx.core.DataSet;
import org.molgenis.omx.core.EntityClass;
import org.molgenis.omx.core.Feature;
import org.molgenis.omx.core.FlexCol;
import org.molgenis.omx.core.FlexColValue;
import org.molgenis.omx.core.Observation;
import org.molgenis.omx.core.ObservedValue;
import org.molgenis.omx.core.Protocol;
import org.molgenis.omx.values.StringValue;
import org.molgenis.omx.values.TextValue;
import org.molgenis.omx.values.XrefValue;
import org.molgenis.omx.xgap.Gene;

public class DataModelV3Demo
{

	public DataModelV3Demo() throws DatabaseException
	{
		Database db = DatabaseFactory.get();
		
		/**
		 * case 1: flexible columns
		 */
		
		// add some Gene
		Gene myGene = new Gene();
		myGene.setIdentifier("WB003043");
		
		// add Feature 'humanOrthologue' with value type String
		Feature humanOrthologue = new Feature();
		EntityClass stringValue = db.find(EntityClass.class, new QueryRule(EntityClass.ENTITYNAME, Operator.EQUALS, StringValue.class.getName())).get(0);
		humanOrthologue.setDataType(stringValue);
		
		// add a FlexCol that combines the Gene class with the 'humanOrthologue' Feature (=column)
		FlexCol fc = new FlexCol();
		EntityClass geneClass = db.find(EntityClass.class, new QueryRule(EntityClass.ENTITYNAME, Operator.EQUALS, Gene.class.getName())).get(0);
		fc.setEntityClass(geneClass);
		fc.setFeature(humanOrthologue);
		
		// finally, add a value: for a specific Gene, add a Value for the 'humanOrthologue' column
		FlexColValue fcv = new FlexColValue();
		fcv.setFlexCol(fc);
		fcv.setTarget(myGene);
		TextValue textval = new TextValue();
		textval.setValue("NC_2340056");
		fcv.setValue(textval);
		
		/**
		 * case 2: runtime class extensions
		 */
		
		CustomClass humanGene = new CustomClass();
		EntityClass existingGeneClass = db.find(EntityClass.class, new QueryRule(EntityClass.ENTITYNAME, Operator.EQUALS, Gene.class.getName())).get(0);
		humanGene.setExtendsClass(existingGeneClass);
		humanGene.setEntityName("HumanGene");
		
		/**
		 * case 3: using XREF of existing class as a value
		 */
		Feature geneRef = new Feature();
		EntityClass geneValue = db.find(EntityClass.class, new QueryRule(EntityClass.ENTITYNAME, Operator.EQUALS, Gene.class.getName())).get(0);
		geneRef.setDataType(geneValue);
		Gene myOtherGene = new Gene();
		// ---> a decorator gets the valueType of the Feature (Gene) and only suggests/allows references to Gene! e.g.
		XrefValue xval = new XrefValue();
		xval.setValue(myOtherGene);
		
		/**
		 * case 4: using XREF to custom class as a value
		 * --> uses CustomClass created during case 2
		 */
		Feature humanGeneRef = new Feature();
		CustomClass humanGeneValue = db.find(CustomClass.class, new QueryRule(CustomClass.ENTITYNAME, Operator.EQUALS, "HumanGene")).get(0);
		humanGeneRef.setDataType(humanGeneValue);
		Gene myHumanGene = new Gene();
		myHumanGene.setCustomClass(humanGeneValue); //specialize this Gene into HumanGene
		// ---> a decorator gets the valueType of the Feature (HumanGene) and only suggests/allows references to HumanGene! e.g.
		XrefValue xval2 = new XrefValue();
		xval2.setValue(myHumanGene);
		
		/**
		 *  case 5: create dataset
		 */
		Protocol p = new Protocol();
		p.setFeatures(geneRef);
		DataSet ds = new DataSet();
		Observation o = new Observation();
		o.setPartOfDataSet(ds);
		DataItem di = new DataItem();
		di.setDataSet(ds);
		ObservedValue ov = new ObservedValue();
		ov.setDataItem(di);
		ov.setObservation(o);
		XrefValue xval3 = new XrefValue();
		xval3.setValue(myOtherGene);
		ov.setValue(xval3);
		
		/**
		 * 
		 */
		
		
	}
	
	/**
	 * @param args
	 * @throws DatabaseException 
	 */
	public static void main(String[] args) throws DatabaseException
	{
		new DataModelV3Demo();

	}

}
