<script type="text/javascript">
    $(document).ready(function() {
        $('.checkbox').on("change", function() {
            var aHrefVals = [];

            $('.checkbox').filter(":checked").each(function() {
                aHrefVals.push($(this).val());
            });

            $("#updateSampleAnchor").attr("href", "?entity=${entity}&mod=entitiesreport&selectedSamples=" + aHrefVals.join(","));
        });
        $('.checkbox').trigger('change');
        $("#toggleSampleSelect").click(function(){
            $("#sampleSelect").toggle();
        });
    });
</script>

<#import "view-PatientReport-entitiesreport.ftl" as patientReport>

<#-- ######################## -->
<#-- passed variables         -->
<#-- ######################## -->
<#if selectedSamples??>
    <#assign selectedSampleValues = selectedSamples?split(",")>
</#if>

<#-- ######################## -->
<#-- static variables/helpers -->
<#-- ######################## -->
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


<#assign lowFdrGeneToVariant = {}>
<#assign mediumFdrGeneToVariant = {}>
<#assign highFdrGeneToVariant = {}>
<#assign uniqSampleNames = []>

<#list datasetRepository as row>
    <#if row.getString("RLV")??>
        <#assign rlvFields = row.getString("RLV")?split("|")>
        <#assign gene = rlvFields[rvcfMapping["gene"]]>
        <#assign fdrAffected = rlvFields[rvcfMapping["fdr"]]?split(",")[0]?number>

        <#if fdrAffected < 0.001>
            <#if lowFdrGeneToVariant[gene]??>
                <#assign lowFdrGeneToVariant = lowFdrGeneToVariant + { gene : lowFdrGeneToVariant[gene] + [row] }>
            <#else>
                <#assign lowFdrGeneToVariant = lowFdrGeneToVariant + { gene : [row] }>
            </#if>
        <#elseif fdrAffected < 0.01>
            <#if mediumFdrGeneToVariant[gene]??>
                <#assign mediumFdrGeneToVariant = mediumFdrGeneToVariant + { gene : mediumFdrGeneToVariant[gene] + [row] }>
            <#else>
                <#assign mediumFdrGeneToVariant = mediumFdrGeneToVariant + { gene : [row] }>
            </#if>
        <#else>
            <#if highFdrGeneToVariant[gene]??>
                <#assign highFdrGeneToVariant = highFdrGeneToVariant + { gene : highFdrGeneToVariant[gene] + [row] }>
            <#else>
                <#assign highFdrGeneToVariant = highFdrGeneToVariant + { gene : [row] }>
            </#if>
        </#if>


        <#list rlvFields[rvcfMapping["sampleGenotype"]]?split("/") as sampleGeno>
            <#assign sampleName = sampleGeno?split(":")[0]>
            <#if !uniqSampleNames?seq_contains(sampleName)>
                <#assign uniqSampleNames = uniqSampleNames + [ sampleName ]>
            </#if>
        </#list>

    </#if>
</#list>

<#if !selectedSampleValues??>
    <#assign selectedSampleValues = uniqSampleNames>
</#if>


<div class="row">
    <div class="col-md-10 col-md-offset-1 well">
        <div class="modal-body" style="background-color: #FFFFFF; ">


            <button type="button" class="btn btn-secondary dropdown-toggle" id="toggleSampleSelect">Select samples</button>

            <div id="sampleSelect" style = "display:none">
                <#assign sampleListForURL = ""/>
                <#list uniqSampleNames as sampleName>
                    <input class="checkbox" id="one" type="checkbox" <#if selectedSampleValues?seq_contains(sampleName)>checked</#if> value="${sampleName}">${sampleName}<br />
                    <#assign sampleListForURL = sampleListForURL + sampleName + ",">
            </#list>
            </div>


            <#---a id="updateSampleAnchor" href="?entity=${entity}&mod=entitiesreport&selectedSamples=${sampleListForURL}"><button type="button" class="btn btn-secondary dropdown-toggle"><font color="black">Update</font></button></a-->
            <a id="updateSampleAnchor" href="?entity=${entity}&mod=entitiesreport&selectedSamples="><button type="button" class="btn btn-secondary dropdown-toggle"><font color="black">Update</font></button></a>



            <h3>GENE REPORT FOR ${selectedSampleValues?size} SAMPLE<#if selectedSampleValues?size == 1 ><#else>S</#if></h3>

            <table>
                <tr style="vertical-align: top;">
                    <td style="background-color: black;padding: 5px;">
                        &nbsp;
                    </td>
                    <td style="padding: 5px;">
                        <h4><font color="black">GENES WITH LESS THAN 0.1% FALSE DISCOVERY RATE</font></h4>
                        The most relevant genes to look at.</td>
                    </td>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="background-color: black;padding: 5px;">
                        &nbsp;
                    </td>
                    <@printGeneList lowFdrGeneToVariant />
                </tr>
            </table>

            <hr/>

            <table>
                <tr style="vertical-align: top;">
                    <td style="background-color: #2f4f4f;padding: 5px;">
                        &nbsp;
                    </td>
                    <td style="padding: 5px;">
                        <h4><font color="#2f4f4f">GENES WITH LESS THAN 1% FALSE DISCOVERY RATE</font></h4>
                        Depending on your assumptions, these genes may be worth investigating.</td>
                    </td>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="background-color: #2f4f4f;padding: 5px;">
                        &nbsp;
                    </td>
                    <@printGeneList mediumFdrGeneToVariant />
                </tr>
            </table>

            <hr/>

            <table>
                <tr style="vertical-align: top;">
                    <td style="background-color: #808080;padding: 5px;">
                        &nbsp;
                    </td>
                    <td style="padding: 5px;">
                        <h4><font color="#808080">GENES WITH GREATER THAN 1% FALSE DISCOVERY RATE</font></h4>
                        Any other genes.</td>
                    </td>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="background-color: #808080;padding: 5px;">
                        &nbsp;
                    </td>
                    <@printGeneList highFdrGeneToVariant />
                </tr>
            </table>

        </div>
    </div>
</div>


<#macro printGeneList genes>

    <#assign catIvariantsForGene = {}>
    <#assign catIIvariantsForGene = {}>
    <#assign catIIIvariantsForGene = {}>
    <#assign catIVvariantsForGene = {}>

    <#assign catVvariantsForGene = {}>
    <#assign catVIvariantsForGene = {}>
    <#assign catVIIvariantsForGene = {}>
    <#assign catVIIIvariantsForGene = {}>

    <#assign catIXvariantsForGene = {}>
    <#assign catXvariantsForGene = {}>

    <#list genes?keys as gene>

        <#list genes[gene] as row>
            <#assign rlvFields = row.getString("RLV")?split("|")>

            <#assign hasHit = false>
            <#list selectedSampleValues as selectedSampleName>
                <#if rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":AFFECTED") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Reported pathogenic")>
                    <#if catIvariantsForGene[gene]??><#assign catIvariantsForGene = catIvariantsForGene + {gene : catIvariantsForGene[gene] + 1 }><#else><#assign catIvariantsForGene = catIvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":AFFECTED") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Predicted pathogenic")>
                    <#if catIIvariantsForGene[gene]??><#assign catIIvariantsForGene = catIIvariantsForGene + {gene : catIIvariantsForGene[gene] + 1 }><#else><#assign catIIvariantsForGene = catIIvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":HOMOZYGOUS") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Reported pathogenic")>
                    <#if catIIIvariantsForGene[gene]??><#assign catIIIvariantsForGene = catIIIvariantsForGene + {gene : catIIIvariantsForGene[gene] + 1 }><#else><#assign catIIIvariantsForGene = catIIIvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":HOMOZYGOUS") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Predicted pathogenic")>
                    <#if catIVvariantsForGene[gene]??><#assign catIVvariantsForGene = catIVvariantsForGene + {gene : catIVvariantsForGene[gene] + 1 }><#else><#assign catIVvariantsForGene = catIVvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>

                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":HETEROZYGOUS") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Reported pathogenic")>
                    <#if catVvariantsForGene[gene]??><#assign catVvariantsForGene = catVvariantsForGene + {gene : catVvariantsForGene[gene] + 1 }><#else><#assign catVvariantsForGene = catVvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":HETEROZYGOUS") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Predicted pathogenic")>
                    <#if catVIvariantsForGene[gene]??><#assign catVIvariantsForGene = catVIvariantsForGene + {gene : catVIvariantsForGene[gene] + 1 }><#else><#assign catVIvariantsForGene = catVIvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":VUS") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Reported pathogenic")>
                    <#if catVIIvariantsForGene[gene]??><#assign catVIIvariantsForGene = catVIIvariantsForGene + {gene : catVIIvariantsForGene[gene] + 1 }><#else><#assign catVIIvariantsForGene = catVIIvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":VUS") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Predicted pathogenic")>
                    <#if catVIIIvariantsForGene[gene]??><#assign catVIIIvariantsForGene = catVIIIvariantsForGene + {gene : catVIIIvariantsForGene[gene] + 1 }><#else><#assign catVIIIvariantsForGene = catVIIIvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>

                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":CARRIER") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Reported pathogenic")>
                    <#if catIXvariantsForGene[gene]??><#assign catIXvariantsForGene = catIXvariantsForGene + {gene : catIXvariantsForGene[gene] + 1 }><#else><#assign catIXvariantsForGene = catIXvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                <#elseif rlvFields[rvcfMapping["sampleStatus"]]?contains(selectedSampleName + ":CARRIER") && rlvFields[rvcfMapping["variantSignificance"]]?starts_with("Predicted pathogenic")>
                    <#if catXvariantsForGene[gene]??><#assign catXvariantsForGene = catXvariantsForGene + {gene : catXvariantsForGene[gene] + 1 }><#else><#assign catXvariantsForGene = catXvariantsForGene + {gene : 1 }></#if><#assign hasHit = true>
                </#if>
                <#--if-->
            </#list>

            <#if !hasHit>
                NOT MATCHED: ${row.getString("RLV")}<br><br>
            </#if>

        </#list>
    </#list>

    <#--list genes[gene] as row>
        <#assign rlvFields = row.getString("RLV")?split("|")>

    <i></i>
    ${rlvFields[rvcfMapping["transcript"]]}
        <#list row.getString("ANN")?split(",") as ann>
            <#assign annSplit = ann?split("|")>
            <#if annSplit[0] == rlvFields[0] && annSplit[3] == rlvFields[2]>
                <#assign type = annSplit[1]>
                <#assign impact = annSplit[2]>
                <#assign cNot = annSplit[9]>
                <#assign pNot = annSplit[10]>
                <#break>
            </#if>
        </#list>

    ${cNot} <#if pNot?? && pNot != "">${pNot}<#else>-</#if> ${type?replace("_", " ")} ${impact}

    <br><br>
        <#list rlvFields[rvcfMapping["sampleGenotype"]]?split("/") as sampleGeno>

            <#assign sampleName = sampleGeno?split(":")[0]>
            <#assign sampleGT = sampleGeno?split(":")[1]?replace("s", "/")?replace("p", "|")>
            <#assign sampleStatus = sampleGeno?split(":")[1]?replace("s", "/")?replace("p", "|")>


        ${sampleName} - ${sampleGT},
        </#list>
    <br><br>
    </#list-->


<td style="padding: 5px;">
<#assign style = "style=\"border: 1px solid darkgray; text-align: center; padding: 3px;\"">

    <table ${style}>
        <tr ${style}>
            <th ${style}>
                Gene
            </th>
            <th ${style}>
                <font color="teal">CAT. I</font>
            </th>
            <th ${style}>
                <font color="teal">CAT. II</font>
            </th>
            <th ${style}>
                <font color="teal">CAT. III</font>
            </th>
            <th ${style}>
                <font color="teal">CAT. IV</font>
            </th>
            <th ${style}>
                <font color="green">CAT. V</font>
            </th>
            <th ${style}>
                <font color="green">CAT. VI</font>
            </th>
            <th ${style}>
                <font color="green">CAT. VII</font>
            </th>
            <th ${style}>
                <font color="green">CAT. VII</font>
            </th>
            <th ${style}>
                <font color="DarkGoldenRod">CAT. IX</font>
            </th>
            <th ${style}>
                <font color="DarkGoldenRod">CAT. X</font>
            </th>
        </tr>
    <#list genes?keys as gene>
        <#if    catIvariantsForGene[gene]?? || catIIvariantsForGene[gene]?? || catIIIvariantsForGene[gene]?? || catIVvariantsForGene[gene]?? ||
                catVvariantsForGene[gene]?? || catVIvariantsForGene[gene]?? || catVIIvariantsForGene[gene]?? || catVIIIvariantsForGene[gene]?? ||
                catIXvariantsForGene[gene]?? || catXvariantsForGene[gene]??>
            <tr ${style}>
                <td ${style}>
                    ${gene}
                </td>
                <td ${style}>
                    <#if catIvariantsForGene[gene]??>${catIvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catIIvariantsForGene[gene]??>${catIIvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catIIIvariantsForGene[gene]??>${catIIIvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catIVvariantsForGene[gene]??>${catIVvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catVvariantsForGene[gene]??>${catVvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catVIvariantsForGene[gene]??>${catVIvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catVIIvariantsForGene[gene]??>${catVIIvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catVIIIvariantsForGene[gene]??>${catVIIIvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catIXvariantsForGene[gene]??>${catIXvariantsForGene[gene]}<#else></#if>
                </td>
                <td ${style}>
                    <#if catXvariantsForGene[gene]??>${catXvariantsForGene[gene]}<#else></#if>
                </td>
            </tr>
        </#if>
    </#list>
    </table>

</td>

</#macro>