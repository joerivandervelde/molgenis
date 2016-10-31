package org.molgenis.data.vcf.utils;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by joeri on 10/31/16.
 */
public class FixVcfAlleleNotationTest {

    @Test
    public void test()
    {
        assertEquals(FixVcfAlleleNotation.trimRefAlt("AT", "ATT", "-"), "A-AT");
        assertEquals(FixVcfAlleleNotation.trimRefAlt("ATGTG", "ATG", "-"), "ATG-A");
        assertEquals(FixVcfAlleleNotation.trimRefAlt("ATGTG", "ATGTGTGTG", "-"), "A-ATGTG");
        assertEquals(FixVcfAlleleNotation.trimRefAlt("GATAT", "GAT", "-"), "GAT-G");
        assertEquals(FixVcfAlleleNotation.trimRefAlt("A", "T", "-"), "A-T");
        assertEquals(FixVcfAlleleNotation.trimRefAlt("AT", "TA", "-"), "AT-TA");
    }

}
