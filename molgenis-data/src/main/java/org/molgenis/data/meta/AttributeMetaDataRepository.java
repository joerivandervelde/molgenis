package org.molgenis.data.meta;

import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.IDENTIFIER;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;

import java.util.concurrent.atomic.AtomicInteger;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Query;
import org.molgenis.data.Range;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.fieldtypes.EnumField;

import com.google.common.base.Joiner;

/**
 * Helper class around the {@link AttributeMetaDataMetaData} repository. Internal implementation class, use
 * {@link MetaDataServiceImpl} instead.
 */
class AttributeMetaDataRepository
{
	public static final AttributeMetaDataMetaData META_DATA = new AttributeMetaDataMetaData();

	private AtomicInteger idCounter = new AtomicInteger();

	private CrudRepository repository;

	private EntityMetaDataRepository entityMetaDataRepository;

	public AttributeMetaDataRepository(ManageableCrudRepositoryCollection collection,
			EntityMetaDataRepository entityMetaDataRepository)
	{
		this.entityMetaDataRepository = entityMetaDataRepository;
		this.repository = collection.add(META_DATA);
	}

	/**
	 * Creates {@link AttributeMetaData} for all {@link AttributeMetaDataMetaData} entities in the repository and adds
	 * them to the {@link EntityMetaData} in the {@link EntityMetaDataRepository}.
	 */
	public void fillAllEntityAttributes()
	{
		for (Entity attributeEntity : repository)
		{
			AttributeMetaData attributeMetaData = toAttributeMetaData(attributeEntity);
			DefaultEntityMetaData entityMetaData = entityMetaDataRepository.get(attributeEntity
					.getString(AttributeMetaDataMetaData.ENTITY_NAME));
			entityMetaData.addAttributeMetaData(attributeMetaData);
		}
	}

	/**
	 * Adds an attribute to an entity, both in the repository and in its {@link EntityMetaData}.
	 * 
	 * @param entity
	 *            {@link EntityMetaData} {@link Entity} that represents the Entity that the attribute should be added
	 *            to.
	 * @param att
	 *            {@link AttributeMetaData} to be added to the entity
	 */
	public void add(Entity entity, AttributeMetaData att)
	{
		Entity attributeMetaDataEntity = new MapEntity();
		// autoid
		attributeMetaDataEntity.set(IDENTIFIER, idCounter.incrementAndGet());
		attributeMetaDataEntity.set(ENTITY, entity);
		attributeMetaDataEntity.set(NAME, att.getName());
		attributeMetaDataEntity.set(DATA_TYPE, att.getDataType());
		attributeMetaDataEntity.set(ID_ATTRIBUTE, att.isIdAtrribute());
		attributeMetaDataEntity.set(NILLABLE, att.isNillable());
		attributeMetaDataEntity.set(AUTO, att.isAuto());
		attributeMetaDataEntity.set(VISIBLE, att.isVisible());
		attributeMetaDataEntity.set(LABEL, att.getLabel());
		attributeMetaDataEntity.set(DESCRIPTION, att.getDescription());
		attributeMetaDataEntity.set(AGGREGATEABLE, att.isAggregateable());
		attributeMetaDataEntity.set(LOOKUP_ATTRIBUTE, att.isLookupAttribute());
		attributeMetaDataEntity.set(LABEL_ATTRIBUTE, att.isLabelAttribute());
		attributeMetaDataEntity.set(READ_ONLY, att.isReadonly());
		attributeMetaDataEntity.set(UNIQUE, att.isUnique());

		if (att.getDataType() instanceof EnumField)
		{
			attributeMetaDataEntity.set(ENUM_OPTIONS, Joiner.on(",").join(att.getEnumOptions()));
		}

		if (att.getRange() != null)
		{
			attributeMetaDataEntity.set(RANGE_MIN, att.getRange().getMin());
			attributeMetaDataEntity.set(RANGE_MAX, att.getRange().getMax());
		}

		if (att.getRefEntity() != null)
		{
			Entity refEntity = entityMetaDataRepository.getEntity(att.getRefEntity().getName());
			attributeMetaDataEntity.set(REF_ENTITY, refEntity);
		}

		repository.add(attributeMetaDataEntity);
		entityMetaDataRepository.get(entity.getString(EntityMetaDataMetaData.FULL_NAME)).addAttributeMetaData(att);
	}

	/**
	 * Removes an attribute from an entity.
	 * 
	 * @param entityName
	 *            fully qualified name of the entity
	 * @param attributeName
	 *            name of the attribute to remove.
	 */
	public void remove(String entityName, String attributeName)
	{
		Query q = new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityName).and()
				.eq(AttributeMetaDataMetaData.NAME, attributeName);
		Entity entity = repository.findOne(q);
		if (entity != null)
		{
			repository.delete(entity);
			DefaultEntityMetaData emd = entityMetaDataRepository.get(entityName);
			emd.removeAttributeMetaData(emd.getAttribute(attributeName));
		}
	}

	/**
	 * Deletes all attributes for a particular entity from the repository.
	 * 
	 * @param entityName
	 *            fully qualified name of the entity whose attributes are deleted.
	 */
	public void deleteAllAttributes(String entityName)
	{
		repository.delete(repository.findAll(new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, entityName)));
	}

	/**
	 * Deletes all Attributes from the repository.
	 */
	public void deleteAll()
	{
		repository.deleteAll();
	}

	/**
	 * Creates a {@link DefaultAttributeMetaData} instance for an Entity in the repository.
	 * 
	 * @param entity
	 *            {@link AttributeMetaDataMetaData} Entity
	 * @return {@link DefaultAttributeMetaData}, with {@link DefaultAttributeMetaData#getRefEntity()} properly filled if
	 *         needed.
	 */
	private DefaultAttributeMetaData toAttributeMetaData(Entity entity)
	{
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(entity.getString(NAME));
		attributeMetaData.setDataType(MolgenisFieldTypes.getType(entity.getString(DATA_TYPE)));
		attributeMetaData.setNillable(entity.getBoolean(NILLABLE));
		attributeMetaData.setAuto(entity.getBoolean(AUTO));
		attributeMetaData.setIdAttribute(entity.getBoolean(ID_ATTRIBUTE));
		attributeMetaData.setLookupAttribute(entity.getBoolean(LOOKUP_ATTRIBUTE));
		attributeMetaData.setVisible(entity.getBoolean(VISIBLE));
		attributeMetaData.setLabel(entity.getString(LABEL));
		attributeMetaData.setDescription(entity.getString(DESCRIPTION));
		attributeMetaData.setAggregateable(entity.getBoolean(AGGREGATEABLE) == null ? false : entity
				.getBoolean(AGGREGATEABLE));
		attributeMetaData.setEnumOptions(entity.getList(ENUM_OPTIONS));
		attributeMetaData.setLabelAttribute(entity.getBoolean(LABEL_ATTRIBUTE) == null ? false : entity
				.getBoolean(LABEL_ATTRIBUTE));
		attributeMetaData.setReadOnly(entity.getBoolean(READ_ONLY) == null ? false : entity.getBoolean(READ_ONLY));
		attributeMetaData.setUnique(entity.getBoolean(UNIQUE) == null ? false : entity.getBoolean(UNIQUE));

		Long rangeMin = entity.getLong(RANGE_MIN);
		Long rangeMax = entity.getLong(RANGE_MAX);
		if ((rangeMin != null) || (rangeMax != null))
		{
			attributeMetaData.setRange(new Range(rangeMin, rangeMax));
		}
		if (entity.getEntity(REF_ENTITY) != null)
		{
			final String refEntityName = entity.getEntity(REF_ENTITY).getString(EntityMetaDataMetaData.ENTITY_NAME);
			attributeMetaData.setRefEntity(entityMetaDataRepository.get(refEntityName));
		}

		return attributeMetaData;
	}
}