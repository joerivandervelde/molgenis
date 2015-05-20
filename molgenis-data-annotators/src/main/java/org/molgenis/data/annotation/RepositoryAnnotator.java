package org.molgenis.data.annotation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

/**
 * interface for annotators. annotators take an iterator and return an iterator with some information added or updated
 */

public interface RepositoryAnnotator
{
	final static String ANNOTATOR_PREFIX = "molgenis_annotated_";

	Iterator<Entity> annotate(Iterable<Entity> source);

	/**
	 * returns an entityMetaData containing the attributes the annotator will add
	 * 
	 * @return ouputMetadata
	 */
	EntityMetaData getOutputMetaData();

	/**
	 * Returns a entityMetaData containing the attributes needed for the annotator to work
	 * 
	 * @return inputMetaData;
	 */
	EntityMetaData getInputMetaData();

	/**
	 * Returns null if the annotator will work for the given metadata, a reason if not so
	 * 
	 * @param inputMetaData
	 * @return canAnnotate
	 */
	String canAnnotate(EntityMetaData inputMetaData);

	/**
	 * Return the name of the annotator
	 * 
	 * @return name
	 */
	String getSimpleName();

	String getFullName();

	default String getDescription()
	{
		return "no description";
	}

	List<Entity> annotateEntity(Entity record) throws IOException, InterruptedException;
}
