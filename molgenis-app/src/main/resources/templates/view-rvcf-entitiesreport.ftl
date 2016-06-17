<div class="modal-header">
    <h1>Report for 20111499-8392854-DNA<#if datasetRepository.name??>${datasetRepository.name}</#if></h1>
</div>
<div class="modal-body" style="background-color: #FFFFFF; ">



<table style="font-weight: bold;white-space: nowrap;">
    <tr style="vertical-align: top;">
        <td style="background-color: blue;padding: 5px;">
            &nbsp;
        </td>
        <td style="color: grey;padding: 5px;">
            Name:<br>
            DOB:<br>
            Sex:<br>
            Ethnicity:<br>
            Indication of testing:<br>
            Test:
        </td>
        <td style="padding: 5px;">
            Doe, Jeffrey<br>
            12/34/5678<br>
            Male<br>
            Caucasian<br>
            5GPM<br>
            WES
        </td>
        <td style="color: grey;padding: 5px;">
            MRN:<br>
            Specimen:<br>
            Received:<br>
        </td>
        <td style="padding: 5px;">
            123456789<br>
            Blood, peripheral<br>
            12/34/5678
        </td>

        <td style="color: grey;padding: 5px;">
            Patient #:<br>
            DNA #:<br>
            Family #:<br>
            Referring physician:<br>
            Referring facility:
        </td>
        <td style="padding: 5px;">
        datasetRepository.getName()<br>
            98765<br>
            ZXY4562<br>
            Doe, Jane<br>
            NICU
        </td>
    </tr>
</table>

<h3>GENOME REPORT</h3>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: teal;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="teal">RESULT SUMMARY</font></h4>
            Sequencing of this individual’s genome was performed and covered 95.7% of all positions at 8X coverage or higher, resulting in over 5.4 million variants compared to a reference genome. These data were analyzed to identify previously reported variants of potential clinical relevance as well as novel variants that could reasonably be assumed to cause disease (see methodology below). All results are summarized on page 1 with further details on subsequent pages.

            <h5><font color="teal">A. MONOGENIC DISEASE RISK: 1 VARIANT IDENTIFIED</font></h5>


            <table>
                <tr style="vertical-align: top; background-color: lightgrey;">
                    <th style="padding: 5px">
                        Disease<br>Inheritance
                    </th>
                    <th style="padding: 5px">
                        Phenotype
                    </th>
                    <th style="padding: 5px">
                        Gene<br>Transcript
                    </th>
                    <th style="padding: 5px">
                        Zygosity<br>Variant
                    </th>
                    <th style="padding: 5px">
                        Classification
                    </th>
                </tr>

            <#list datasetRepository as row>

            <#--${row.getString("#CHROM")} ${row.getString("POS")} ${row.getString("REF")}-->

            <#--<#if row.getString("RLV")??>${row.getString("RLV")}</#if><br>-->

                <#if row.getString("RLV")??>
                    <#assign rlvFields = row.getString("RLV")?split("|")>
                    <#if rlvFields[5]?contains("20111499-8392854-DNA")>


                        <tr style="vertical-align: top;">


                            <td style="padding: 5px">



                            </td>
                            <td style="padding: 5px">
                                ${rlvFields[2]}
                            </td>
                            <td style="padding: 5px">
                                ddd
                            </td>
                            <td style="padding: 5px">
                                fff
                            </td>
                            <td style="padding: 5px">
                            ${rlvFields[10]}
                            </td>
                        </tr>

                    </#if>
                </#if>

            </#list>


            </table>


            <h5><font color="teal">CAT.I: PHENOTYPE MATCH AND PROTEIN AFFECTING: 1 VARIANT</h5>

            <h5><font color="teal">CAT.II: SOMETHING</h5>

            <h5><font color="teal">B. CARRIER RISK: 2 VARIANTS IDENTIFIED</h5>


        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: green;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="green">DETAILED VARIANT INFORMATION</font></h4>

        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: DarkGoldenRod ;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="DarkGoldenRod ">PHARMACOGENOMIC ASSOCIATIONS AND BLOOD GROUPS</font></h4>

        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: DarkRed ;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="DarkRed ">RISK ALLELES</font></h4>

        </td>

    </tr>
</table>

<br><br>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: indigo;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="indigo">METHODOLOGY</font></h4>

        </td>

    </tr>
</table>



</div>