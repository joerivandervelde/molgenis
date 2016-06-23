

<div class="row">
    <div class="col-md-10 col-md-offset-1 well">

<div class="modal-body" style="background-color: #FFFFFF; ">


    <div class="btn-group">
        <button type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            Select patient
        </button>
        <div class="dropdown-menu" style="width: 500px">
        <#list datasetRepository as row>
            <#list row.getEntities("SAMPLES_ENTITIES") as sample>

                <#-- the original name of the sample is prefixed with some stuff, we must strip this off here... -->
                <#assign original_name = "">
                <#list sample.get("NAME")?split("_") as spl>
                    <#if spl?index == 0 || spl?index == 1><#else><#assign original_name = original_name + spl></#if>
                </#list>


                <a class="dropdown-item" href="?entity=rvcf_output&mod=entitiesreport&patient=${sample.get("NAME")}">${original_name}</a><br>
            </#list>
            <#break>
        </#list>
        </div>
    </div>

    <div class="modal-header">
        <h1>Report for ${original_name} <#if datasetRepository.name??>${datasetRepository.name}</#if></h1>
    </div>


    <table>
        <tr>
            <td>

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
            ${original_name}<br>
            98765<br>
            ZXY4562<br>
            Doe, Jane<br>
            NICU
        </td>
    </tr>
</table>
                <hr />

<h3>GENOME REPORT</h3>

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: teal;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="teal">STRONG CAUSAL SUSPECTS FOR MONOGENIC DISORDER</font></h4>
            Sequencing of this individual’s genome was performed and covered 95.7% of all positions at 8X coverage or higher, resulting in over 5.4 million variants compared to a reference genome. These data were analyzed to identify previously reported variants of potential clinical relevance as well as novel variants that could reasonably be assumed to cause disease (see methodology below). All results are summarized on page 1 with further details on subsequent pages.

            <h5><font color="teal">CAT. I: KNOWN PATHOGENIC VARIANT, IN CLINICAL GENE, AFFECTED STATUS</font></h5>


            <table>
                <tr style="vertical-align: top; background-color: lightgrey;">
                    <th style="padding: 5px">
                        Gene<br>Transcript
                    </th>
                    <th style="padding: 5px">
                        Disease<br>Inheritance
                    </th>
                    <th style="padding: 5px">
                        Variant<br>Zygosity
                    </th>
                    <th style="padding: 5px">
                        Source<br>Classification
                    </th>
                    <th style="padding: 5px">
                        Allele frequency
                    </th>
                    <th style="padding: 5px">
                        Justification
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


            <h5><font color="teal">CAT. II: PREDICTED PATHOGENIC VARIANT, IN CLINICAL GENE, AFFECTED STATUS</h5>


            <h5><font color="teal">CAT. III: KNOWN PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HOMOZYGOUS GENOTYPE</h5>

            <h5><font color="teal">CAT. IV: PREDICTED PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HOMOZYGOUS GENOTYPE</h5>

        </td>

    </tr>
</table>

<hr />

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: green;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="green">WEAK CAUSAL SUSPECTS FOR MONOGENIC DISORDER</font></h4>


            <h5><font color="green">CAT. V: KNOWN PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HETEROZYGOUS GENOTYPE</h5>

            <h5><font color="green">CAT. VI: PREDICTED PATHOGENIC VARIANT, IN UNCHARACTERIZED GENE, HETEROZYGOUS GENOTYPE</h5>

            <h5><font color="green">CAT. VII: VUS, IN CLINICAL GENE, AFFECTED STATUS</h5>

            <h5><font color="green">CAT. VIII: VUS, IN UNCHARACTERIZED GENE, HOMOZYGOUS GENOTYPE</h5>
        </td>

    </tr>
</table>

                <hr />

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: DarkGoldenRod ;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="DarkGoldenRod ">CARRIER STATUS OF MONOGENIC DISORDER</font></h4>

            <h5><font color="DarkGoldenRod">CAT. V: KNOWN PATHOGENIC VARIANT, IN CLINICAL GENE, CARRIER GENOTYPE</h5>

            <h5><font color="DarkGoldenRod">CAT. VI: PREDICTED PATHOGENIC VARIANT, IN CLINICAL GENE, CARRIER GENOTYPE</h5>

        </td>

    </tr>
</table>

                <hr />

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: DarkRed ;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="DarkRed ">BLOOD GROUPS AND PHARMACOGENOMICS</font></h4>

            At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores.

            <h5><font color="DarkRed ">BLOOD GROUP GENOTYPING</font></h5>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam.


            <h5><font color="DarkRed ">PHARMACOGENOMIC ASSOCIATIONS</font></h5>
            Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.


        </td>

    </tr>
</table>

                <hr />

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: indigo;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="indigo">RISK AND SUSCEPTIBILITY ALLELES</font></h4>
            Et harum quidem rerum facilis est et expedita distinctio.
            <h5><font color="indigo">RISK ALLELES FOR COMPLEX DISORDERS</font></h5>
            Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae.
            <h5><font color="indigo">DISEASE SUSCEPTIBILITY ALLELES</font></h5>
            Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.
        </td>

    </tr>
</table>

                <hr />

<table>
    <tr style="vertical-align: top;">
        <td style="background-color: black;padding: 5px;">
            &nbsp;
        </td>

        <td style="padding: 5px;">

            <h4><font color="black">METHODOLOGY</font></h4>

            The initial sequencing component of this test was performed by x and the alignment, variant calling, data filtering, Sanger confirmation and interpretation were performed by the y at the z.

            <h5><font color="black">DNA SEQUENCING</font></h5>

            Genomic sequencing is performed using next generation sequencing on the Illumina HiSeq platform. Genomes are sequenced to at least 30X mean coverage and a minimum of 95% of bases are sequenced to at least 8X coverage.

            <h5><font color="black">ALIGNMENT AND VARIANT CALLING</font></h5>

            Paired-end 100bp reads are aligned to the NCBI reference sequence (GRCh37) using the Burrows-Wheeler Aligner (BWA), and variant calls are made using the Genomic Analysis Tool Kit (GATK).

            <h5><font color="black">VARIANT ANNOTATION AND INTERPRETATION</font></h5>

            Variants are subsequently filtered to identify: (1) variants classified as disease causing in public databases; (2) nonsense, frameshift, and +/-1,2 splice-site variants that are novel or have a minor allele frequency <1% in European American or African American chromosomes from the NHLBI Exome Sequencing Project (http://evs.gs.washington.edu/EVS/); and (3) rs11212617 (C11orf65; metformin), rs12248560 (CYP2C19; clopidogrel), rs4244285 (CYP2C19; clopidogrel), rs4986893 (CYP2C19; clopidogrel), rs28399504 (CYP2C19; clopidogrel), rs41291556 (CYP2C19; clopidogrel), rs72552267 (CYP2C19; clopidogrel), rs72558186 (CYP2C19; clopidogrel), rs56337013 (CYP2C19; clopidogrel), rs1057910 (CYP2C9; warfarin), rs1799853 (CYP2C9; warfarin), rs7900194 (CYP2C9; warfarin), rs9332131 (CYP2C9; warfarin), rs28371685 (CYP2C9; warfarin), rs28371686 (CYP2C9; warfarin), rs9923231 (VKORC1; warfarin), rs4149056 (SLCO1B1; simvastatin), and rs1045642 (ABCB1; digoxin). The evidence for phenotype-causality is then evaluated for each variant resulting from the filtering strategies above and variants are classified according to q criteria. Only those variants with evidence for causing highly penetrant disease or contributing to disease in a recessive manner are reported. Before reporting, all variants are confirmed via Sanger sequencing or another orthogonal technology.


        </td>

    </tr>
</table>


            </td></tr></table>
</div>
    </div>
</div>
