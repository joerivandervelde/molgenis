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


<#list datasetRepository as row>
    <#if row.getString("RLV")??>
        <#assign rlvFields = row.getString("RLV")?split("|")>
        <#assign gene = rlvFields[rvcfMapping["gene"]]>

        ${gene} :

        <#list rlvFields[rvcfMapping["sampleStatus"]]?split("/") as sampleStatus>
            <#assign sampleName = sampleStatus?split(":")[0]>
            ${sampleName}
        </#list>


    </#if>
</#list>

