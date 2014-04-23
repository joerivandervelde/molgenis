package org.molgenis.omx.das;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.molgenis.omx.das.impl.MolgenisDasTarget;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;

public abstract class RangeHandlingDataSource implements RangeHandlingAnnotationDataSource
{
    public static final String MUTATION_STOP_POSITION = "stop_nucleotide";
    public static final String MUTATION_LINK = "linkout";
    public static final String MUTATION_NAME = "__identifier";
    public static final String MUTATION_DESCRIPTION = "INFO";
    public static final String MUTATION_START_POSITION = "POS";
    public static final String MUTATION_ID = "ID";
    public static final String MUTATION_CHROMOSOME = "CHROM";
    public static final String PATIENT_ID = "patient_id";

    protected DasFeature createDasFeature(Integer start, Integer stop, String identifier, String name,
                                          String description, String link, DasType type, DasMethod method, String dataSet, String patient) throws DataSourceException
    {
        if (stop == null) stop = start;// no stop? assume length of 1;

        // create description based on available information
        String featureDescription = "";
        if (StringUtils.isNotEmpty(description))
        {
            featureDescription = StringUtils.isNotEmpty(name) ? name + "," + description : description;
        }
        else
        {
            featureDescription = identifier;
        }

        List<String> notes = new ArrayList<String>();
        notes.add("track:"+dataSet);
        notes.add("source:MOLGENIS");
        if(StringUtils.isNotEmpty(patient)){
            notes.add("patient:"+patient);
        }

        Map<URL, String> linkout = new HashMap<URL, String>();
        try
        {
            linkout.put(new URL(link), "Link");
        }
        catch (MalformedURLException e)
        {
        }

        List<DasTarget> dasTarget = new ArrayList<DasTarget>();
        dasTarget.add(new MolgenisDasTarget(identifier, start, stop, featureDescription));
        List<String> parents = new ArrayList<String>();
        DasFeature feature = new DasFeature(identifier, featureDescription, type, method, start, stop,
                new Double(0), DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes,
                linkout, dasTarget, parents, null);
        return feature;
    }

    // unimplemented functions
    @Override
    public void destroy()
    {
        // Mandatory to override this function, but no destroy needed.
    }

    @Override
    public DasAnnotatedSegment getFeatures(String arg0, Integer arg1) throws BadReferenceObjectException,
            DataSourceException
    {
        throw new BadReferenceObjectException(arg0, "The handling of this request is not supported");
    }

    @Override
    public Collection<DasAnnotatedSegment> getFeatures(Collection<String> arg0, Integer arg1, Range arg2)
            throws UnimplementedFeatureException, DataSourceException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public URL getLinkURL(String arg0, String arg1) throws UnimplementedFeatureException, DataSourceException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public DasAnnotatedSegment getFeatures(String arg0, int arg1, int arg2, Integer arg3, Range arg4)
            throws BadReferenceObjectException, CoordinateErrorException, DataSourceException,
            UnimplementedFeatureException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public DasAnnotatedSegment getFeatures(String arg0, Integer arg1, Range arg2) throws BadReferenceObjectException,
            DataSourceException, UnimplementedFeatureException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public Collection<DasAnnotatedSegment> getFeatures(Collection<String> arg0, Integer arg1)
            throws UnimplementedFeatureException, DataSourceException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }

    @Override
    public Collection<DasEntryPoint> getEntryPoints(Integer segmentId, Integer notUsed)
            throws UnimplementedFeatureException, DataSourceException
    {
        throw new UnimplementedFeatureException("Not implemented");
    }
}
