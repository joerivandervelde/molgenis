package org.molgenis.integrationtest.data.abstracts.query;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;

import autovalue.shaded.com.google.common.common.collect.Lists;

public class AbstractInQueryIT extends AbstractQueryIT
{

	@Override
	void testInt()
	{
		Query<Entity> query = new QueryImpl<>().in(HEIGHT, Lists.newArrayList(180, 165, 20));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testDecimal()
	{
		Query<Entity> query = new QueryImpl<>().in(ACCOUNT_BALANCE, Lists.newArrayList(1000.00, -0.70));
		Set<Entity> resultSet = Sets.newHashSet(person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testLong()
	{
		Query<Entity> query = new QueryImpl<>().in(SERIAL_NUMBER, Lists.newArrayList(374278348334L, 50L));
		assertEquals(personsRepository.findOne(query), person1);
		assertEquals(personsRepository.findAll(query).collect(Collectors.toList()), Lists.newArrayList(person1));
		assertEquals(personsRepository.count(query), 1);
	}

	@Override
	void testString()
	{
		Query<Entity> query = new QueryImpl<>().in(LAST_NAME, Lists.newArrayList("doe", "re", "mi"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testDate() throws ParseException
	{
		Query<Entity> query = new QueryImpl<>().in(BIRTHDAY,
				Lists.newArrayList(dateFormat.parse("1980-06-07"), dateFormat.parse("1976-06-07")));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testDateTime() throws ParseException
	{
		 Query<Entity> query = new QueryImpl<>().in(
				 BIRTH_TIME, Lists.newArrayList(dateTimeFormat.parse("1976-06-07 08:08:08"), dateTimeFormat.parse("1976-06-07 06:06:06")));
		 Set<Entity> resultSet = Sets.newHashSet(person1, person3);
		 assertTrue(resultSet.contains(personsRepository.findOne(query)));
		 assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		 assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testBool()
	{
		Query<Entity> query = new QueryImpl<>().in(ACTIVE, Lists.newArrayList(true, false));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2, person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testMref()
	{
		Query<Entity> query = new QueryImpl<>().in(AUTHOR_OF,
				Lists.newArrayList("MOLGENIS for dummies", "Your database at the push of a button"));
		Set<Entity> resultSet = Sets.newHashSet(person1, person2);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

	@Override
	void testXref()
	{
		Query<Entity> query = new QueryImpl<>().in(COUNTRY, Lists.newArrayList("NL", "DE", "XX"));
		Set<Entity> resultSet = Sets.newHashSet(person3);
		assertTrue(resultSet.contains(personsRepository.findOne(query)));
		assertEquals(personsRepository.findAll(query).collect(Collectors.toSet()), resultSet);
		assertEquals(personsRepository.count(query), resultSet.size());
	}

}
