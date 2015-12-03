package org.molgenis.data.annotation.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/** 
 * Annotator that adds the output of the CADD webservice to a VCF file.
 * TODO: rewrite into a proper annotator!
 *  
 * Many indels in your VCF are not present in the static CADD files for SNV, 1000G, etc.
 * The webservice can calculate CADD scores for these variants to get more complete data.
 * This is what you get back when you use the webservice of CADD:
 * 
 * ## CADD v1.3 (c) University of Washington and Hudson-Alpha Institute for Biotechnology 2013-2015. All rights reserved.
 * #CHROM	POS	REF	ALT	RawScore	PHRED
 * 1	3102852	G	A	0.458176	7.103
 * 1	3102888	G	T	-0.088829	1.815
 * 1	3103004	G	A	1.598097	13.84
 * 1	3319479	G	A	0.717654	8.942
 * 
 * This information can be added to a VCF based on chrom, pos, ref, alt.
 *
 */
public class CaddWebserviceOutputAnnotator
{

	File vcfToAnnotate;
	File caddWebserviceOutput;
	PrintWriter pw;
	
	public CaddWebserviceOutputAnnotator(File vcfToAnnotate, File caddWebserviceOutput, File outputFile) throws FileNotFoundException
	{
		if (!vcfToAnnotate.isFile())
		{
			throw new FileNotFoundException("VCF file " + vcfToAnnotate.getAbsolutePath()
					+ " does not exist or is directory");
		}
		if (!caddWebserviceOutput.isFile())
		{
			throw new FileNotFoundException("CADD webservice output file " + caddWebserviceOutput.getAbsolutePath()
					+ " does not exist or is directory");
		}
		if (outputFile.isFile())
		{
			System.out.println("Warning: output file " + outputFile.getAbsolutePath()
					+ " already exists, overwriting content!");
		}
		this.vcfToAnnotate = vcfToAnnotate;
		this.caddWebserviceOutput = caddWebserviceOutput;
		this.pw = new PrintWriter(outputFile);
	}
	
	public void annotate() throws Exception
	{
		Scanner caddOutputScanner = new Scanner(caddWebserviceOutput);
		Map<String,String> caddIndelScores = new HashMap<String,String>();

		while(caddOutputScanner.hasNextLine())
		{
			//e.g.  '2	47630249	GA	G	1.373305	10.52'
			String line = caddOutputScanner.nextLine();
	        if(line.startsWith("#"))
	        {
	            continue;
	        }
	        String[] split = line.split("\t");
            if(split.length != 6)
            {
                throw new Exception("Expecting exactly 6 columns when splitting CADD indel result file on tab. Line does not conform:\n" + line);
            }
            //so: "2	47630249	GA	G" to "1.373305	10.52"
            caddIndelScores.put(split[0]+"\t"+split[1]+"\t"+split[2]+"\t"+split[3], split[4]+"\t"+split[5]);
		}
		caddOutputScanner.close();
		
		//now annotate
		Scanner vcfOutputScanner = new Scanner(vcfToAnnotate);
		while(vcfOutputScanner.hasNextLine())
		{
			//e.g.  '2	47630249	GA	G	1.373305	10.52'
			String line = vcfOutputScanner.nextLine();
	        if(line.startsWith("#"))
	        {
	        	pw.println(line);
	            continue;
	        }
	        String[] split = line.split("\t", -1);
	        
	        //TODO: obviously, this will fail for multiple alternative alleles etc.
	        String chrPosRefAlt = split[0] + "\t" + split[1] + "\t" + split[3] + "\t" + split[4];
	        
	        if(caddIndelScores.containsKey(chrPosRefAlt))
	        {
	        	String[] caddScore = caddIndelScores.get(chrPosRefAlt).split("\t");
	        	String caddRaw = caddScore[0];
	        	String caddScaled = caddScore[1];
	        	
	        	StringBuffer lineWithCadd = new StringBuffer();
	        	for(int i = 0; i < split.length; i ++)
	        	{
	        		if(i == 7)
	        		{
	        			lineWithCadd.append("CADD=" + caddRaw + ";CADD_SCALED=" + caddScaled + ";" + split[i] + "\t");
	        		}
	        		else
	        		{
	        			lineWithCadd.append(split[i] + "\t");
	        		}
	        		
	        	}
	        	lineWithCadd.deleteCharAt(lineWithCadd.length()-1);
	        	pw.println(lineWithCadd.toString());
	        }
	        else
	        {
	        	pw.println(line);
	        }
	        
	        
	        pw.flush();
		}
		vcfOutputScanner.close();
		pw.close();
	}

	public static void main(String[] args) throws Exception
	{
		File vcfToAnnotate = new File(args[0]);
		File caddWebserviceOutput = new File(args[1]);
		File outputFile = new File(args[2]);
		CaddWebserviceOutputAnnotator cwoa = new CaddWebserviceOutputAnnotator(vcfToAnnotate, caddWebserviceOutput, outputFile);
		cwoa.annotate();
	}

}
