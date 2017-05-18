package org.molgenis.data.vcf.utils;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by joeri on 10/31/16.
 */
public class FixVcfAlleleNotationTest {

    @Test
    public void testBackTrim()
    {
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("AT", "ATT", "-"), "A-AT");
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("ATGTG", "ATG", "-"), "ATG-A");
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("ATGTG", "ATGTGTGTG", "-"), "A-ATGTG");
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("GATAT", "GAT", "-"), "GAT-G");
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("A", "T", "-"), "A-T");
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("AT", "TA", "-"), "AT-TA");
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("CT", "CT", "-"), "C-C");

        //this one can be front or backtrimmed! but its back first (retains the pos), so thats why its here
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("CT", "CTCT", "-"), "C-CTC");

        //substitution, cannot shorten
        assertEquals(FixVcfAlleleNotation.backTrimRefAlt("CATCCAGCCTGCTCTCCAC", "CTG", "-"), "CATCCAGCCTGCTCTCCAC-CTG");
    }

    @Test void testFrontTrim()
    {
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("C", "CTA", "-"), "C-CTA");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CC", "CCTA", "-"), "C-CTA");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CT", "CTCA", "-"), "T-TCA");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CT", "CT", "-"), "T-T");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CT", "C", "-"), "CT-C");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CTA", "CT", "-"), "TA-T");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("GAGAGT", "GAGC", "-"), "GAGT-GC");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("AAACGCTCATAGAGTAACTGGTTGTGCAGTAAAAGCAACTGGTCTC", "AAACGCTCATAGAGTAACTGGTTGTGCAGTAAAAGCAACTGGTCTCAAACGCTCAT", "-"), "C-CAAACGCTCAT");
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CTTTTAAATTTGATTTTATTGAGCACTTTCCTCT", "CTTTTAAATTTGATTTTATTGAGCACTTTCCTCTCTTTTAAATTTGATTTTATTGA", "-"), "T-TCTTTTAAATTTGATTTTATTGA");


        //substitution, cannot shorten
        assertEquals(FixVcfAlleleNotation.frontTrimRefAlt("CATCCAGCCTGCTCTCCAC", "CTG", "-"), "CATCCAGCCTGCTCTCCAC-CTG");
    }

}
