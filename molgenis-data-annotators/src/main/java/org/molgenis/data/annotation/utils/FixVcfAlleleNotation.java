package org.molgenis.data.annotation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import org.springframework.util.StringUtils;

public class FixVcfAlleleNotation
{
	/**
	 * Fix N notation in VCFs produced by Ensembl VEP web service.
	 * 
	 * Example:
	 * 11	47354445	MYBPC3:c.3407_3409delACT	NAGT	N
	 * 
	 * Get location for UCSC:
	 * http://genome.ucsc.edu/cgi-bin/das/hg19/dna?segment=chr11:47354445,47354445
	 * Returns:
	 * "<DNA length="1">a</DNA>"
	 * 
	 * We need:
	 * "a"
	 * 
	 * And use it to turn the VCF record into:
	 * 11	47354445	MYBPC3:c.3407_3409delACT	AAGT	A
	 * 
	 * Both deletions and insertions suffer from this, e.g.:
	 * 6	51612724	PKHD1:c.9689delA	NT	N
	 * 6	51824680	PKHD1:c.5895dupA	N	NT
	 * 
	 * Caveat:
	 * Some original HGVS/cDNA notation inplies an insertion/deletion event involving the same bases, for example "c.652delAinsTA"
	 * This gets turned into "NA/NTA" and subsequently fixed into "TA/TTA". Note that the "A" is unnecessary here!
	 * This is not nice and some tools get confused. For example, CADD webservice doesn't understand until you change it into "T/TT".
	 * So we want either ref or alt to be 1 basepair, which is the right way to express variants in VCF format.
	 * This is why we do a check and fix it here.
	 * 
	 * 
	 * 
	 */
	public static void main(String[] args) throws Exception
	{
		File in = new File(args[0]);
		File out = new File(args[1]);
		
		PrintWriter pw = new PrintWriter(out);
		Scanner s = new Scanner(in);
		String line;
		while(s.hasNextLine())
		{
			//write out header untouched
			line = s.nextLine();
			if(line.startsWith("#")){
				pw.println(line);
				continue;
			}
			String[] split = line.split("\t");
			
			String chr = split[0];
			String pos = split[1];
			String ref = split[3];
			String alt = split[4];
			
			//if not both start with N, we expect neither to start with N (see example)
			if(!(ref.startsWith("N") && alt.startsWith("N")))
			{
				System.out.println("no reason to adjust variant " + chr + ":pos " + ref + "/" + alt + " because there is no N");
				pw.println(line);
				continue;
			}
			else if(ref.startsWith("N") && alt.startsWith("N"))
			{
				System.out.println("need to adjust variant " + chr + ":pos " + ref + "/" + alt + " because there is an N");
				int refNOccurence = StringUtils.countOccurrencesOf(ref, "N");
				int altNOccurence = StringUtils.countOccurrencesOf(alt, "N");
				if(refNOccurence != 1 || altNOccurence != 1)
				{
					s.close();
					pw.close();
					throw new Exception("expecting 'N' occurence == 1 for " + ref + " and " + alt);
				}
			}
			//sanity check
			else
			{
				s.close();
				pw.close();
				throw new Exception("either ref "+ref+" or alt "+alt+" starts with N, not expected this");
			}
			
			//get replacement base for N from UCSC
			URL ucsc = new URL("http://genome.ucsc.edu/cgi-bin/das/hg19/dna?segment=chr"+chr+":"+pos+","+pos);
			BufferedReader getUrlContent = new BufferedReader(new InputStreamReader(ucsc.openStream()));
			String urlLine;
			String replacementRefBase = null;
			while ((urlLine = getUrlContent.readLine()) != null)
			{
				//the base ('g', 'c', 'a', 't') is on line of its own, so length == 1
				if(urlLine.length() == 1)
				{
					replacementRefBase = urlLine.toUpperCase();
					System.out.println("we found replacement base for N = " + replacementRefBase);
				}
			}
			getUrlContent.close();
			
			//wait a little bit not too stress out the server we're querying
			Thread.sleep(100);
			
			//trim ref/alt if needed
			String fixedRef = split[3].replace("N", replacementRefBase);
			String fixedAlt = split[4].replace("N", replacementRefBase);
			String[] trimmedRefAlt = CaddWebserviceOutputUtils.trimRefAlt(fixedRef, fixedAlt, "_").split("_");
			
			//print the fixed notation
			StringBuffer fixedLine = new StringBuffer();
			for(int i = 0; i < split.length; i ++)
			{
				if(i == 3)
				{
					fixedLine.append(trimmedRefAlt[0] + "\t");
				}
				else if(i == 4)
				{
					fixedLine.append(trimmedRefAlt[1] + "\t");
				}
				else
				{
					fixedLine.append(split[i] + "\t");
				}
			}
			
			//remove trailing \t
			fixedLine.deleteCharAt(fixedLine.length()-1);
			
			//print & flush
			pw.println(fixedLine);
			pw.flush();
			
		}
		pw.close();
		s.close();
		
		System.out.println("Done!");

	}

}
