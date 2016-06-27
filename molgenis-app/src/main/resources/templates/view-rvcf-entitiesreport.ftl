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
"transcript"                        :3,
"phenotype"                         :4,
"phenotypeInheritance"              :5,
"phenotypeOnset"                    :6,
"phenotypeDetails"                  :7,
"phenotypeGroup"                    :8,
"sampleStatus"                      :9,
"samplePhenotype"                   :10,
"sampleGroup"                       :11,
"variantSignificance"               :12,
"variantSignificanceSource"         :13,
"variantSignificanceJustification"  :14,
"variantCompoundHet"                :15,
"variantGroup"                      :16
}>

<#-- ############################ -->
<#-- initialize dynamic variables -->
<#-- ############################ -->

<#-- if no sample was selected, take the first one from the list and select it-->
<#if selectedSampleName??><#else>
    <#list datasetRepository as row>
        <#list row.getEntities("SAMPLES_ENTITIES") as sample>
            <#assign selectedSampleName = sample.get("NAME")>
            <#break>
        </#list>
        <#break>
    </#list>
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
                    <#list row.getEntities("SAMPLES_ENTITIES") as sample>
                        <#-- the original name of the sample is prefixed with some stuff, we must strip this off here for nice display... -->
                        <#assign originalSampleName = "">
                        <#assign sampleName = sample.get("NAME")>
                        <#list sampleName?split("_") as sampleNameSplit>
                            <#if sampleNameSplit?index == 0 || sampleNameSplit?index == 1><#else><#if sampleNameSplit?hasNext><#assign originalSampleName = originalSampleName + sampleNameSplit + "_"><#else><#assign originalSampleName = originalSampleName + sampleNameSplit></#if></#if>
                        </#list>
                        <#-- create dropdown items, with original name as label and the prefixed name as a key-->
                        <a class="dropdown-item"
                           href="?entity=${entity}&mod=entitiesreport&selectedSampleName=${sampleName}&selectedAlleleFreq=${selectedAlleleFreq}&selectedOnsetExclude=${selectedOnsetExclude}&selectedMinimalImpact=${selectedMinimalImpact}<#if geneFilter??>&geneFilter=${geneFilter}</#if>">${originalSampleName}</a><br>
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

            <#-- gene list filter, original code from http://stackoverflow.com/questions/7097573/is-it-possible-to-update-a-url-link-based-on-user-text-input
                <a id="reflectedlink" href="http://www.google.com/search">http://www.google.com/search</a>
                <input id="searchterm"/>
                <script type="text/javascript">
                    var link= document.getElementById('reflectedlink');
                    var input= document.getElementById('searchterm');
                    input.onchange=input.onkeyup= function() {
                        link.search= '?q='+encodeURIComponent(input.value);
                        link.firstChild.data= link.href;
                    };
                </script>
            -->


            <a id="reflectedlink" href=""><button type="button" class="btn btn-secondary dropdown-toggle"><font color="black">Filter by genes:</font></button></a>
            <input id="searchterm" value="<#if geneFilter??>${geneFilter}</#if>"/>
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

        <#-- from the selected sample name, reconstruct the original name -->
        <#assign selectedOriginalSampleName = "">
        <#list selectedSampleName?split("_") as selectedSampleNameSplit>
            <#if selectedSampleNameSplit?index == 0 || selectedSampleNameSplit?index == 1><#else><#if selectedSampleNameSplit?hasNext><#assign selectedOriginalSampleName = selectedOriginalSampleName + selectedSampleNameSplit + "_"><#else><#assign selectedOriginalSampleName = selectedOriginalSampleName + selectedSampleNameSplit></#if></#if>
        </#list>

            <div class="modal-header">
                <h1>Report for ${selectedOriginalSampleName}</h1>
            </div>

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
                                ${selectedOriginalSampleName}<br>
                                    98765<br>
                                    ZXY4562<br>
                                    Doe, Jane<br>
                                    NICU
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
                                    Sequencing of this individual’s genome was performed and covered 95.7% of all
                                    positions at 8X coverage or higher, resulting in over 5.4 million variants compared
                                    to a reference genome. These data were analyzed to identify previously reported
                                    variants of potential clinical relevance as well as novel variants that could
                                    reasonably be assumed to cause disease (see methodology below). All results are
                                    summarized on page 1 with further details on subsequent pages.

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

                                    At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis
                                    praesentium voluptatum deleniti atque corrupti quos dolores.

                                    <h5><font color="DarkRed ">BLOOD GROUP GENOTYPING</font></h5>
                                    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                                    incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.

                                    <h5><font color="DarkRed ">PHARMACOGENOMIC ASSOCIATIONS</font></h5>
                                    Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium
                                    doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore
                                    veritatis et quasi architecto beatae vitae dicta sunt explicabo.

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
                                    Et harum quidem rerum facilis est et expedita distinctio.
                                    <h5><font color="indigo">RISK ALLELES FOR COMPLEX DISORDERS</font></h5>
                                    Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo
                                    minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis
                                    dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum
                                    necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non
                                    recusandae.
                                    <h5><font color="indigo">DISEASE SUSCEPTIBILITY ALLELES</font></h5>
                                    Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus
                                    maiores alias consequatur aut perferendis doloribus asperiores repellat.
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

                                    The initial sequencing component of this test was performed by x and the alignment,
                                    variant calling, data filtering, Sanger confirmation and interpretation were
                                    performed by the y at the z.

                                    <h5><font color="black">DNA SEQUENCING</font></h5>

                                    Genomic sequencing is performed using next generation sequencing on the Illumina
                                    HiSeq platform. Genomes are sequenced to at least 30X mean coverage and a minimum of
                                    95% of bases are sequenced to at least 8X coverage.

                                    <h5><font color="black">ALIGNMENT AND VARIANT CALLING</font></h5>

                                    Paired-end 100bp reads are aligned to the NCBI reference sequence (GRCh37) using the
                                    Burrows-Wheeler Aligner (BWA), and variant calls are made using the Genomic Analysis
                                    Tool Kit (GATK).

                                    <h5><font color="black">VARIANT ANNOTATION AND INTERPRETATION</font></h5>

                                    Variants are subsequently filtered to identify: (1) variants classified as disease
                                    causing in public databases; (2) nonsense, frameshift, and +/-1,2 splice-site
                                    variants that are novel or have a minor allele frequency <1% in European American or
                                    African American chromosomes from the NHLBI Exome Sequencing Project
                                    (http://evs.gs.washington.edu/EVS/); and (3) rs11212617 (C11orf65; metformin),
                                    rs12248560 (CYP2C19; clopidogrel), rs4244285 (CYP2C19; clopidogrel), rs4986893
                                    (CYP2C19; clopidogrel), rs28399504 (CYP2C19; clopidogrel), rs41291556 (CYP2C19;
                                    clopidogrel), rs72552267 (CYP2C19; clopidogrel), rs72558186 (CYP2C19; clopidogrel),
                                    rs56337013 (CYP2C19; clopidogrel), rs1057910 (CYP2C9; warfarin), rs1799853 (CYP2C9;
                                    warfarin), rs7900194 (CYP2C9; warfarin), rs9332131 (CYP2C9; warfarin), rs28371685
                                    (CYP2C9; warfarin), rs28371686 (CYP2C9; warfarin), rs9923231 (VKORC1; warfarin),
                                    rs4149056 (SLCO1B1; simvastatin), and rs1045642 (ABCB1; digoxin). The evidence for
                                    phenotype-causality is then evaluated for each variant resulting from the filtering
                                    strategies above and variants are classified according to q criteria. Only those
                                    variants with evidence for causing highly penetrant disease or contributing to
                                    disease in a recessive manner are reported. Before reporting, all variants are
                                    confirmed via Sanger sequencing or another orthogonal technology.

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
        Genotype<br>
        Depth
    </th>
    <th>
        Consequence<br>
        Impact<br>
        Frequency
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
            <#list row.getEntities("SAMPLES_ENTITIES") as sample>
                <#assign key = row.getString("POS") + "_" + row.getString("ALT") + "_" + selectedOriginalSampleName>
                <#if sample.get("NAME") == key>
                    ${sample.get("GT")}<br>
                    ${sample.get("AD")}<br>
                    <#break>
                </#if>
            </#list>
            ${rlvFields[rvcfMapping["alleleFreq"]]}<br>
        </td>
        <td>
            ${rlvFields[rvcfMapping["phenotype"]]}<br>
            ${rlvFields[rvcfMapping["phenotypeInheritance"]]}<br>
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
            <#if rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedOriginalSampleName + ":${sampleStatus}") && rlvFields[rvcfMapping["variantSignificance"]]?startsWith("${pathogenicStatus}") >
                <@printRow row rlvFields />
            </#if>
        </#if>
    </#list>
    <#if rowsPrinted>
        </tbody>
        </table>
        <#assign rowsPrinted = false>
    </#if>
</#macro>