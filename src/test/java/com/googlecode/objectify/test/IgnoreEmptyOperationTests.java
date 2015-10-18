package com.googlecode.objectify.test;

import static com.googlecode.objectify.ObjectifyService.factory;
import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.test.entity.Trivial;
import com.googlecode.objectify.test.util.TestBase;
import com.googlecode.objectify.test.util.TestObjectifyFactory;

public class IgnoreEmptyOperationTests extends TestBase {

	@Override
	protected void setUpObjectifyFactory(TestObjectifyFactory factory) {
		factory.register(Trivial.class);
		super.setUpObjectifyFactory(Mockito.spy(factory));
		Mockito.verify(factory(), Mockito.times(1)).begin();
	}

	// engines are responsible for datastore actions, and because all of them requires an AsyncDatastoreService
	// it seems like a good idea to verify if it has been created or not
	private void verifyNoRemoteCall() {
		Mockito.verify(factory(), Mockito.never())
				.createAsyncDatastoreService(Mockito.any(DatastoreServiceConfig.class), Mockito.anyBoolean());
		Mockito.verifyNoMoreInteractions(factory());
	}

	private void verifyThatRemoteCallWasExecuted(int times) {
		Mockito.verify(factory(), Mockito.times(times))
				.createAsyncDatastoreService(Mockito.any(DatastoreServiceConfig.class), Mockito.anyBoolean());
	}

	@Test
	public void testEmptySave() {
		ofy().save().entities(Lists.newArrayList()).now().size();

		verifyNoRemoteCall();
	}

	@Test
	public void testNotEmptySave() {
		ofy().save().entities(Arrays.asList(new Trivial())).now().size();

		verifyThatRemoteCallWasExecuted(1);
	}

	@Test
	public void testEmptyDelete() {
		ofy().delete().entities(Lists.newArrayList()).now();
		ofy().delete().keys(Lists.<Key<Trivial>> newArrayList()).now();
		ofy().delete().type(Trivial.class).ids(Lists.newArrayList()).now();

		verifyNoRemoteCall();
	}

	@Test
	public void testNotEmptyDelete() {
		final Trivial trivial = new Trivial(1L, "", 1L);
		ofy().delete().entities(Arrays.asList(trivial)).now();
		ofy().delete().keys(Arrays.asList(Key.create(trivial))).now();
		ofy().delete().type(Trivial.class).ids(Arrays.asList(trivial.getId())).now();

		verifyThatRemoteCallWasExecuted(3);
	}

	@Test
	public void testEmptyLoad() {
		ofy().load().entities(Lists.newArrayList()).size();
		ofy().load().keys(Lists.<Key<Trivial>> newArrayList()).size();
		ofy().load().type(Trivial.class).ids(Lists.newArrayList()).size();

		verifyNoRemoteCall();
	}

	@Test
	public void testNotEmptyLoad() {
		final Trivial trivial = new Trivial(1L, "", 1L);
		ofy().load().entities(Arrays.asList(trivial)).size();
		ofy().load().keys(Arrays.asList(Key.<Trivial> create(trivial))).size();
		ofy().load().type(Trivial.class).ids(Arrays.asList(trivial.getId())).size();

		verifyThatRemoteCallWasExecuted(3);
	}
}
