<#-- ######################## -->
<#-- static variables/helpers -->
<#-- ######################## -->

<#assign lateOnsetGenes = ["AIP", "ALK", "APC", "AXIN2", "BAP1", "BMPR1A", "BRCA1", "CDH1", "CDK4", "CDKN2A", "CEBPA", "CHEK2", "CTHRC1", "CTNNA1", "DICER1", "EGFR", "FH", "FLCN", "GATA2", "KIT", "MAX", "MLH1", "MLH3", "MSH2", "MSH3", "MSH6", "MUTYH", "NF2", "PAX5", "PDGFRA", "PMS2", "PRKAR1A", "RAD51D", "STK11", "TMEM127", "TP53"]>

<#assign alleleFreqOptions = ["1", "0.1", "0.05", "0.02", "0.01", "0.002", "0.005", "0.001", "0.0001"]>

<#assign impactOptions = ["Modifier", "Low", "Moderate", "High"]>

<#assign rvcfMapping = {
"allele"                            :0,
"alleleFreq"                        :1,
"gene"                              :2,
"fdr"                               :3,
"transcript"                        :4,
"phenotype"                         :5,
"phenotypeInheritance"              :6,
"phenotypeOnset"                    :7,
"phenotypeDetails"                  :8,
"phenotypeGroup"                    :9,
"sampleStatus"                      :10,
"samplePhenotype"                   :11,
"sampleGenotype"                    :12,
"sampleGroup"                       :13,
"variantSignificance"               :14,
"variantSignificanceSource"         :15,
"variantSignificanceJustification"  :16,
"variantCompoundHet"                :17,
"variantGroup"                      :18
}>


<#assign samples = []>
<#list datasetRepository as row>
    <#if row.getString("RLV")??>
        <#assign rlvFields = row.getString("RLV")?split("|")>
        <#list rlvFields[rvcfMapping["sampleStatus"]]?split("/") as sampleStatus>
            <#assign sampleName = sampleStatus?split(":")[0]>
            <#if !samples?seqContains(sampleName)>
                <#assign samples = samples + [ sampleName ]>
            </#if>
        </#list>
    </#if>
</#list>



<#-- ############################ -->
<#-- initialize dynamic variables -->
<#-- ############################ -->

<#-- if no sample was selected, take the first one from the list and select it-->
<#if !selectedSampleName??>
    <#assign selectedSampleName = samples[0]>
</#if>

<#-- if no allele frequency was selected, set a default-->
<#if selectedAlleleFreq??><#else>
    <#assign selectedAlleleFreq = 0.02>
</#if>

<#-- if no allele frequency was selected, set a default-->
<#if selectedOnsetExclude??><#else>
    <#assign selectedOnsetExclude = "UMCG">
</#if>

<#-- if no minimum impact was selected, set a default-->
<#if selectedMinimalImpact??><#else>
    <#assign selectedMinimalImpact = "Moderate">
</#if>

<#if geneFilter??>
    <#assign geneFilterValues = geneFilter?split(",")>
</#if>

<#-- ############### -->
<#-- begin rendering -->
<#-- ############### -->

<div class="row">
    <div class="col-md-10 col-md-offset-1 well">
        <div class="modal-body" style="background-color: #FFFFFF; ">
            <div class="btn-group">
                <button type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown"
                        aria-haspopup="true" aria-expanded="false">
                    Select patient
                </button>
                <div class="dropdown-menu" style="width: 500px">
                <#list datasetRepository as row>
                    <#list samples as sampleName>
                    <#-- create dropdown items, with original name as label and the prefixed name as a key-->
                        <a class="dropdown-item"
                           href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${sampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=${selectedOnsetExclude}&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">${sampleName}</a><br>
                    </#list>
                    <#break>
                </#list>
                </div>
            </div>
            <div class="btn-group">
                <button type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Allele frequency: < ${selectedAlleleFreq?number*100}%
                </button>
                <div class="dropdown-menu" style="width: 500px">
                <#list alleleFreqOptions as afo>
                    <a class="dropdown-item" href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${afo}&selectedOnsetExclude=${selectedOnsetExclude}&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">< ${afo?number*100}%</a><br>
                </#list>
                </div>
            </div>

            <div class="btn-group">
                <button type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Late onset exclusion: ${selectedOnsetExclude}
                </button>
                <div class="dropdown-menu" style="width: 500px">
                    <a class="dropdown-item" href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=No&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">No exclusion</a><br>
                    <a class="dropdown-item" href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=UMCG&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">Exclude UMCG late onset</a><br>
                    <a class="dropdown-item" href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=CGD&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">Exclude CGD late onset</a><br>
                    <a class="dropdown-item" href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=UMCG_and_CGD&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">Exclude UMCG and CGD late onset</a><br>
                </div>
            </div>

            <div class="btn-group">
                <button type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Minimum variant impact: ${selectedMinimalImpact}
                </button>
                <div class="dropdown-menu" style="width: 500px">

                <#list impactOptions as io>
                    <a class="dropdown-item" href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=${selectedOnsetExclude}&selectedMinimalImpact=${io}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">${io}</a><br>
                </#list>
                </div>
            </div>
            <a id="reflectedlink" href=""><button type="button" class="btn btn-secondary dropdown-toggle"><font color="black">Filter by genes:</font></button></a>
            <input id="searchterm" size="4" value="<#if geneFilter??>${geneFilter}</#if>"/>
            <script type="text/javascript">
                var link= document.getElementById('reflectedlink');
                var input= document.getElementById('searchterm');
                input.onchange=input.onkeyup= function() {
                    link.search= '?entity=${entity}&mod=entitiesreport&selectedSampleName=${selectedSampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=${selectedOnsetExclude}&selectedMinimalImpact=${selectedMinimalImpact}&geneFilter='+input.value;
                };
            </script>
        <#-- ################# -->
        <#-- the actual report -->
        <#-- ################# -->


                <h3>PATIENT REPORT FOR ${selectedSampleName}</h3>


            <table>
                <tr>
                    <td>

                        <table style="font-weight: bold; white-space: nowrap;">
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
                                    n/a<br>
                                    n/a<br>
                                    n/a<br>
                                    n/a<br>
                                    n/a<br>
                                    n/a
                                </td>
                                <td style="color: grey;padding: 5px;">
                                    MRN:<br>
                                    Specimen:<br>
                                    Received:<br>
                                </td>
                                <td style="padding: 5px;">
                                    n/a<br>
                                    n/a<br>
                                    n/a
                                </td>

                                <td style="color: grey;padding: 5px;">
                                    Patient #:<br>
                                    DNA #:<br>
                                    Family #:<br>
                                    Referring physician:<br>
                                    Referring facility:
                                </td>
                                <td style="padding: 5px;">
                                ${selectedSampleName}<br>
                                    n/a<br>
                                    n/a<br>
                                    n/a<br>
                                    n/a
                                </td>
                            </tr>
                        </table>
                        <hr/>

                        <h3>GENOME REPORT</h3>

                        <table>
                            <tr style="vertical-align: top;">
                                <td style="background-color: teal;padding: 5px;">
                                    &nbsp;
                                </td>

                                <td style="padding: 5px;">

                                    <h4><font color="teal">STRONG CAUSAL SUSPECTS FOR MONOGENIC DISORDER</font></h4>
                                    Variants are ranked from most relevant (known clinical genes, known pathogenic) towards lesser relevance (uncharacterized genes, predicted pathogenic).
                                    <h5><font color="teal">CAT. I: KNOWN PATHOGENIC VARIANT, IN CLINICAL GENE, AFFECTED STATUS</font></h5>
                                    <@printTable "AFFECTED" "Reported pathogenic"/>

                                    <h5><font color="teal">CAT. II: PREDICTED PATHOGENIC VARIANT, IN CLINICAL GENE, AFFECTED STATUS</font></h5>
                                    <@printTable "AFFECTED" "Predicted pathogenic"/>

                                    <h5><font color="teal">CAT. III: KNOWN PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HOMOZYGOUS GENOTYPE</font></h5>
                                    <@printTable "HOMOZYGOUS" "Reported pathogenic"/>

                                    <h5><font color="teal">CAT. IV: PREDICTED PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HOMOZYGOUS GENOTYPE</font></h5>
                                    <@printTable "HOMOZYGOUS" "Predicted pathogenic"/>

                                </td>

                            </tr>
                        </table>

                        <hr/>

                        <table>
                            <tr style="vertical-align: top;">
                                <td style="background-color: green;padding: 5px;">
                                    &nbsp;
                                </td>

                                <td style="padding: 5px;">

                                    <h4><font color="green">WEAK CAUSAL SUSPECTS FOR MONOGENIC DISORDER</font></h4>

                                    <h5><font color="green">CAT. V: KNOWN PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HETEROZYGOUS GENOTYPE</font></h5>
                                    <@printTable "HETEROZYGOUS" "Reported pathogenic"/>

                                    <h5><font color="green">CAT. VI: PREDICTED PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HETEROZYGOUS GENOTYPE</font></h5>
                                    <@printTable "HETEROZYGOUS" "Predicted pathogenic"/>

                                    <h5><font color="green">CAT. VII: VUS, IN CLINICAL GENE, AFFECTED STATUS</font></h5>
                                    <@printTable "VUS" "Reported pathogenic"/>

                                    <h5><font color="green">CAT. VIII: VUS, IN UNCHARACTERIZED GENE, HOMOZYGOUS GENOTYPE</font></h5>
                                    <@printTable "VUS" "Reported pathogenic"/>

                                </td>
                            </tr>
                        </table>

                        <hr/>

                        <table>
                            <tr style="vertical-align: top;">
                                <td style="background-color: DarkGoldenRod ;padding: 5px;">
                                    &nbsp;
                                </td>
                                <td style="padding: 5px;">

                                    <h4><font color="DarkGoldenRod ">CARRIER STATUS OF MONOGENIC DISORDER</font></h4>

                                    <h5><font color="DarkGoldenRod">CAT. V: KNOWN PATHOGENIC VARIANT, IN CLINICAL GENE, CARRIER GENOTYPE</font></h5>
                                    <@printTable "CARRIER" "Reported pathogenic"/>

                                    <h5><font color="DarkGoldenRod">CAT. VI: PREDICTED PATHOGENIC VARIANT, IN CLINICAL GENE, CARRIER GENOTYPE</font></h5>
                                    <@printTable "CARRIER" "Predicted pathogenic"/>

                                </td>
                            </tr>
                        </table>

                        <hr/>

                        <table>
                            <tr style="vertical-align: top;">
                                <td style="background-color: DarkRed ;padding: 5px;">
                                    &nbsp;
                                </td>
                                <td style="padding: 5px;">

                                    <h4><font color="DarkRed ">BLOOD GROUPS AND PHARMACOGENOMICS</font></h4>
                                    This section does not contain any information yet, and may be future work.

                                    <h5><font color="DarkRed ">BLOOD GROUP GENOTYPING</font></h5>
                                    n/a

                                    <h5><font color="DarkRed ">PHARMACOGENOMIC ASSOCIATIONS</font></h5>
                                    n/a

                                </td>
                            </tr>
                        </table>

                        <hr/>

                        <table>
                            <tr style="vertical-align: top;">
                                <td style="background-color: indigo;padding: 5px;">
                                    &nbsp;
                                </td>
                                <td style="padding: 5px;">
                                    <h4><font color="indigo">RISK AND SUSCEPTIBILITY ALLELES</font></h4>
                                    This section does not contain any information yet, and may be future work.

                                    <h5><font color="indigo">RISK ALLELES FOR COMPLEX DISORDERS</font></h5>
                                    n/a

                                    <h5><font color="indigo">DISEASE SUSCEPTIBILITY ALLELES</font></h5>
                                    n/a
                                </td>

                            </tr>
                        </table>

                        <hr/>

                        <table>
                            <tr style="vertical-align: top;">
                                <td style="background-color: black;padding: 5px;">
                                    &nbsp;
                                </td>

                                <td style="padding: 5px;">

                                    <h4><font color="black">METHODOLOGY</font></h4>
                                    This section does not contain any information yet, and may be future work.

                                    <h5><font color="black">DNA SEQUENCING</font></h5>
                                    n/a

                                    <h5><font color="black">ALIGNMENT AND VARIANT CALLING</font></h5>
                                    n/a

                                    <h5><font color="black">VARIANT ANNOTATION AND INTERPRETATION</font></h5>
                                    n/a

                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>


<#macro printHeader>
<thead>
<tr style="background-color: lightgrey;">
    <th>
        Gene<br>
        Transcript<br>
        cDNA
    </th>
    <th>
        AA change<br>
        Consequence<br>
        Impact
    </th>
    <th>
        Genotype<br>
        Frequency<br>
        <nowrap>FDR_aff/carr</nowrap>
    </th>
    <th>
        Disorder<br>
        Inheritance<br>
        Onset
    </th>
    <th>
        Source<br>
        Justification
    </th>
</tr>
</thead>
</#macro>

<#macro printRow row rlvFields>

<#-- get some additional info from ANN field -->
    <#list row.getString("ANN")?split(",") as ann>
        <#assign annSplit = ann?split("|")>
    <#-- match allele and gene -->
        <#if annSplit[0] == rlvFields[0] && annSplit[3] == rlvFields[2]>
            <#assign type = annSplit[1]>
            <#assign impact = annSplit[2]>
            <#assign cNot = annSplit[9]>
            <#assign pNot = annSplit[10]>
            <#break>
        </#if>
    </#list>

<#-- apply filter -->
    <#if
    rlvFields[rvcfMapping["alleleFreq"]]?number <= selectedAlleleFreq?number &&
    !((selectedOnsetExclude=="UMCG" || selectedOnsetExclude=="UMCG_and_CGD") && lateOnsetGenes?seqContains(rlvFields[rvcfMapping["gene"]])) &&
    !((selectedOnsetExclude=="CGD" || selectedOnsetExclude=="UMCG_and_CGD") && rlvFields[rvcfMapping["phenotypeOnset"]] == "Adult") &&
    (
    (selectedMinimalImpact == "Modifier" && (impact == "MODIFIER" || impact == "LOW" || impact == "MODERATE" || impact == "HIGH")) ||
    (selectedMinimalImpact == "Low" && (impact == "LOW" || impact == "MODERATE" || impact == "HIGH")) ||
    (selectedMinimalImpact == "Moderate" && (impact == "MODERATE" || impact == "HIGH")) ||
    (selectedMinimalImpact == "High" && (impact == "HIGH"))
    ) &&
    (!geneFilterValues?? || geneFilterValues[0]?length == 0 || geneFilterValues?seqContains(rlvFields[rvcfMapping["gene"]]))
    >

        <#if !rowsPrinted>
        <table class="table table-striped table-condensed table-bordered">
            <@printHeader/>
        <tbody>
            <#assign rowsPrinted = true>
        </#if>

    <tr>
        <td>
        ${rlvFields[rvcfMapping["gene"]]}<br>
        ${rlvFields[rvcfMapping["transcript"]]}<br>
        ${cNot}<br>
        </td>
        <td>
            <#if pNot?? && pNot != "">${pNot}<#else>-</#if><br>
        ${type?replace("_", " ")}<br>
        ${impact}<br>
        </td>
        <td>
            <#if row.getString("RLV")??>
                <#assign rlvFields = row.getString("RLV")?split("|")>
                <#list rlvFields[rvcfMapping["sampleGenotype"]]?split("/") as sampleGeno>
                    <#assign sampleName = sampleGeno?split(":")[0]>
                    <#assign sampleGT = sampleGeno?split(":")[1]?replace("s", "/")?replace("p", "|")>
                    <#if sampleName == selectedSampleName>
                        ${sampleGT}<br>
                        <#break>
                    </#if>
                </#list>
            </#if>

        ${rlvFields[rvcfMapping["alleleFreq"]]}<br>

        <#assign fdrAffected = rlvFields[rvcfMapping["fdr"]]?split(",")[0]>
        <#assign fdrCarr = rlvFields[rvcfMapping["fdr"]]?split(",")[1]>
        ${(fdrAffected?number * 100)?round}% / ${(fdrCarr?number * 100)?round}%

        </td>
        <td>
        ${rlvFields[rvcfMapping["phenotype"]]}<br>
        ${rlvFields[rvcfMapping["phenotypeInheritance"]]} <#if rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":AFFECTED_COMPOUNDHET") || rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":HOMOZYGOUS_COMPOUNDHET")>Compound het.</#if><br>
        ${rlvFields[rvcfMapping["phenotypeOnset"]]}
        </td>
        <td>
        ${rlvFields[rvcfMapping["variantSignificanceSource"]]}<br>
        ${rlvFields[rvcfMapping["variantSignificanceJustification"]]}
        </td>
    </tr>
    <#else>
    </#if>

</#macro>

<#macro printTable sampleStatus pathogenicStatus>
    <#assign rowsPrinted = false>
    <#list datasetRepository as row>
        <#if row.getString("RLV")??>
            <#assign rlvFields = row.getString("RLV")?split("|")>
            <#if rlvFields[rvcfMapping["sampleStatus"]]?startsWith(selectedSampleName + ":${sampleStatus}") && rlvFields[rvcfMapping["variantSignificance"]]?startsWith("${pathogenicStatus}") >
                <@printRow row rlvFields />
            </#if>
        </#if>
    </#list>
    <#if rowsPrinted>
    </tbody>
    </table>
        <#assign rowsPrinted = false>
    <#else>
    No variants found.
    </#if>
</#macro>