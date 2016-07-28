package org.molgenis.data.vcf.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by joeri on 7/28/16.
 *
 * Quick tool to split up MVL table into 2 tables with MREF structure.
 *
 */
public class VKGL {

    public static void main(String[] args) throws FileNotFoundException {

        File in = new File("/Users/joeri/Desktop/Projects/VKGL/gekregen_variant_lijsten/VKGL_MVL_combined_incl_UMCU_EMX_KJV_v2.tsv");
        PrintWriter outChrPos = new PrintWriter(new File("/Users/joeri/Desktop/Projects/VKGL/gekregen_variant_lijsten/VKGL_MVL_combined_incl_UMCU_EMX_KJV_PositionTable.tsv"));
        PrintWriter outClsf = new PrintWriter(new File("/Users/joeri/Desktop/Projects/VKGL/gekregen_variant_lijsten/VKGL_MVL_combined_incl_UMCU_EMX_KJV_ClassificationTable.tsv"));

        HashMap<String, List<String>> chrPosToLine = new HashMap<>();

        Scanner s = new Scanner(in);

        //header
        s.nextLine();

        while(s.hasNextLine())
        {
            String line = s.nextLine();
            String[] split = line.split("\t", -1);

            String chrPos = split[1] + "_" + split[2];

            if(chrPosToLine.containsKey(chrPos)){
                chrPosToLine.get(chrPos).add(line);
            }
            else
            {
                ArrayList<String> lines = new ArrayList<String>();
                lines.add(line);
                chrPosToLine.put(chrPos, lines);
            }
        }


        int idCounter1 = 1;
        int idCounter2 = 1;
        outChrPos.println("ID" + "\t" + "#CHROM" + "\t" + "POS" + "\t" + "Classifications");
        outClsf.println("ID\toldID\t#CHROM\tPOS\tGene\tLab\tTranscript\tcDNA\tType\tLocation\tExon\tEffect\tProtein\tClassification");
        for(String key : chrPosToLine.keySet())
        {
            String mrefKeys = null;
            for(String line : chrPosToLine.get(key))
            {
                String[] split = line.split("\t", -1);
                String mrefKey = split[4] + "_" + split[3] + "_" + split[12] + "_" + idCounter2;
                mrefKeys = mrefKeys == null ? mrefKey : (mrefKeys += "," + mrefKey);
                outClsf.println(mrefKey + "\t" + line);
                idCounter2++;
            }

            outChrPos.println(idCounter1 + "\t" + key.replace("_", "\t") + "\t" + mrefKeys);
            idCounter1++;
        }

        outChrPos.flush();
        outChrPos.close();
        outClsf.flush();
        outClsf.close();

    }

}
