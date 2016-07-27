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
!selectedSampleValues??
<#elseif selectedSampleValues?seq_contains(sampleName)>
selectedSampleValues?seq_contains(sampleName)
</#if>


<div class="row">
    <div class="col-md-10 col-md-offset-1 well">
        <div class="modal-body" style="background-color: #FFFFFF; ">


            <button type="button" class="btn btn-secondary dropdown-toggle" id="toggleSampleSelect">Select samples</button>

            <div id="sampleSelect" style = "display:none">
                <#assign sampleListForURL = ""/>
                <#list uniqSampleNames as sampleName>
                    <input class="checkbox" id="one" type="checkbox" <#if !selectedSampleValues??>checked<#elseif selectedSampleValues?seq_contains(sampleName)>checked</#if> value="${sampleName}">${sampleName}<br />
                    <#assign sampleListForURL = sampleListForURL + sampleName + ",">
            </#list>
            </div>


            <#---a id="updateSampleAnchor" href="?entity=${entity}&mod=entitiesreport&selectedSamples=${sampleListForURL}"><button type="button" class="btn btn-secondary dropdown-toggle"><font color="black">Update</font></button></a-->
            <a id="updateSampleAnchor" href="?entity=${entity}&mod=entitiesreport&selectedSamples="><button type="button" class="btn btn-secondary dropdown-toggle"><font color="black">Update</font></button></a>



            <h3>GENE REPORT</h3>

            <table>
                <tr style="vertical-align: top;">
                    <td style="background-color: teal;padding: 5px;">
                        &nbsp;
                    </td>
                    <td style="padding: 5px;">
                        <h4><font color="teal">GENES WITH LESS THAN 0.1% FALSE DISCOVERY RATE</font></h4>
                        The most relevant genes to look at.</td>
                    </td>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="background-color: teal;padding: 5px;">
                        &nbsp;
                    </td>
                    <@printGeneList lowFdrGeneToVariant />
                </tr>
            </table>

            <hr/>

            <table>
                <tr style="vertical-align: top;">
                    <td style="background-color: green;padding: 5px;">
                        &nbsp;
                    </td>
                    <td style="padding: 5px;">
                        <h4><font color="green">GENES WITH LESS THAN 1% FALSE DISCOVERY RATE</font></h4>
                        Depending on your assumptions, these genes may be worth investigating.</td>
                    </td>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="background-color: green;padding: 5px;">
                        &nbsp;
                    </td>
                    <@printGeneList mediumFdrGeneToVariant />
                </tr>
            </table>

            <hr/>

            <table>
                <tr style="vertical-align: top;">
                    <td style="background-color: DarkGoldenRod;padding: 5px;">
                        &nbsp;
                    </td>
                    <td style="padding: 5px;">
                        <h4><font color="DarkGoldenRod">GENES WITH GREATER THAN 1% FALSE DISCOVERY RATE</font></h4>
                        Any other genes.</td>
                    </td>
                </tr>
                <tr style="vertical-align: top;">
                    <td style="background-color: DarkGoldenRod;padding: 5px;">
                        &nbsp;
                    </td>
                    <@printGeneList highFdrGeneToVariant />
                </tr>
            </table>

        </div>
    </div>
</div>


<#macro printGeneList genes>

<td style="padding: 5px;">

    <table>
    <#list genes?keys as gene>
        <tr>
            <td>
                <b>${gene}</b>
            </td>
        </tr>
        <tr>
            <td>
            <#list genes[gene] as row>
                <#assign rlvFields = row.getString("RLV")?split("|")>

                <i></i>
                ${rlvFields[rvcfMapping["transcript"]]}
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

                ${cNot} <#if pNot?? && pNot != "">${pNot}<#else>-</#if> ${type?replace("_", " ")} ${impact}

                <br><br>
                <#list rlvFields[rvcfMapping["sampleGenotype"]]?split("/") as sampleGeno>

                    <#assign sampleName = sampleGeno?split(":")[0]>
                    <#assign sampleGT = sampleGeno?split(":")[1]?replace("s", "/")?replace("p", "|")>
                    <#assign sampleStatus = sampleGeno?split(":")[1]?replace("s", "/")?replace("p", "|")>


                ${sampleName} - ${sampleGT},
                </#list>
                <br><br>
            </#list>
            </td>
        </tr>

    </#list>
    </table>
</td>

</#macro>