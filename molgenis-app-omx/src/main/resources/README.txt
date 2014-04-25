1) make sure there is a molgenis-server.properties in "[USER_HOME_DIR]/.molgenis/omx/molgenis-server.properties"
2) properties file should contain:
	- index.directory=/Users/charbonb/data/variants/referenced_index (the directory where the final index, used for MOLGENIS, is stored)
	- data.directory=/Users/charbonb/data/variants/data (the location of the VCF / TSV files)
3) run org.molgenis.variantbrowser.IndexFiles, this program takes an argument to specify where the intermediate index is stored:
	java org.molgenis.variantbrowser.IndexFiles -index /[PREFERRED_PATH]
4) run java org.molgenis.variantbrowser.VariantReferencer with the argument from step 3
	java org.molgenis.variantbrowser.VariantReferencer -index /[PREFERRED_PATH]
5) (re)start app-omx

notes:
- sorting does not work
- chr columns are renamed to CHROM for genomebrowser usage
- pos columns are coverted to uppercase for genomebrowser usage
- if no "ID" column is specified, one is generated based on the position and the chromosome for genomebrowser usage