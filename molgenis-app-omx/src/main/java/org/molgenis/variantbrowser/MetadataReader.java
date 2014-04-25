package org.molgenis.variantbrowser;

    import java.io.BufferedReader;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Collections;
    import java.util.List;

    import org.apache.commons.io.FilenameUtils;
    import org.apache.commons.lang3.StringUtils;

    import au.com.bytecode.opencsv.CSVReader;

    public class MetadataReader {

        public static String getMetadataString(File file) throws IOException
        {
            // System.out.println(file.getName());
            // System.out.println("indexing " + file.getPath());
            FileInputStream fis;
            try
            {
                fis = new FileInputStream(file);
            }
            catch (FileNotFoundException fnfe)
            {
                // at least on windows, some temporary files raise this exception with an "access denied" message
                // checking if the file can be read doesn't help
                throw new RuntimeException("FILE NOT FOUND! "+file.getName());
            }

            try
            {
                CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(fis, "UTF-8")), '\t');
                try {
                    String[] tokens, headers = null;
                    while ((tokens = csvReader.readNext()) != null) {
                        if (tokens[0].startsWith("##")) continue;
                        else if (tokens[0].startsWith("#")) {
                            headers = new String[tokens.length];
                            System.arraycopy(tokens, 0, headers, 0, tokens.length);
                            headers[0] = headers[0].substring(1);
                            boolean containsID = false;
                            for(int i = 0; i < headers.length; i++)
                            {
                                if("ID".equals(headers[i])){
                                   containsID = true;
                                }
                                if("chr".equals(headers[i])){
                                    headers[i] = IndexFiles.CHROMOSOME;
                                }
                                if("pos".equals(headers[i])){
                                    headers[i] = IndexFiles.POSITION;
                                }
                                if("INFO".equals(headers[i])){
                                    ArrayList<String> list = new ArrayList<String>();
                                    Collections.addAll(list,headers);
                                    list.add(IndexFiles.REFERENCE);
                                    list.add(IndexFiles.HETROZYGOTE_ALT);
                                    list.add(IndexFiles.HOMEZYGOTE_ALT);
                                    headers = Arrays.copyOf(list.toArray(), list.toArray().length, String[].class);
                                }
                            }
                            if(!containsID){
                                ArrayList<String> list = new ArrayList<String>();
                                Collections.addAll(list,headers);
                                list.add("ID");
                                headers = Arrays.copyOf(list.toArray(), list.toArray().length, String[].class);
                            }
                            return FilenameUtils.removeExtension(file.getName())+"\r\n"+StringUtils.join(headers, ',')+"\r\n";
                        } else if (headers == null) {
                            headers = new String[tokens.length];
                            System.arraycopy(tokens, 0, headers, 0, tokens.length);
                            boolean containsID = false;
                            for(int i = 0; i < headers.length; i++)
                            {
                                if("ID".equals(headers[i])){
                                    containsID = true;
                                }
                                if("chr".equals(headers[i])){
                                    headers[i] = IndexFiles.CHROMOSOME;
                                }
                                if("pos".equals(headers[i])){
                                    headers[i] = IndexFiles.POSITION;
                                }
                                if("INFO".equals(headers[i])){
                                    ArrayList<String> list = new ArrayList<String>();
                                    Collections.addAll(list,headers);
                                    list.add(IndexFiles.REFERENCE);
                                    list.add(IndexFiles.HETROZYGOTE_ALT);
                                    list.add(IndexFiles.HOMEZYGOTE_ALT);
                                    headers = Arrays.copyOf(list.toArray(), list.toArray().length, String[].class);
                                }
                            }
                            if(!containsID){
                                ArrayList<String> list = new ArrayList<String>();
                                Collections.addAll(list,headers);
                                list.add("ID");
                                headers = Arrays.copyOf(list.toArray(), list.toArray().length, String[].class);
                            }
                            return FilenameUtils.removeExtension(file.getName())+"\r\n"+StringUtils.join(headers, ',')+"\r\n";
                        }
                    }
                }catch(Exception e){}
            } catch(Exception e){}
            return FilenameUtils.removeExtension(file.getName())+"\r\nNO HEADERS\r\n";
         }
}
