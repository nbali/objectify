/*
 */

package com.googlecode.objectify.test;

import static com.googlecode.objectify.test.util.TestObjectifyService.fact;
import static com.googlecode.objectify.test.util.TestObjectifyService.ofy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.test.entity.Trivial;
import com.googlecode.objectify.test.util.TestBase;
import com.googlecode.objectify.util.ResultProxy;

/**
 * Make sure that the proxies we return can be serialized sanely.
 *
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class ProxySerializationTests extends TestBase
{
	/** */
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ProxySerializationTests.class.getName());
	
	/** */
	ByteArrayOutputStream bytesOut;
	ObjectOutputStream objectOut;
	
	/** */
	@BeforeMethod
	void setUpOutput() throws Exception {
		bytesOut = new ByteArrayOutputStream();
		objectOut = new ObjectOutputStream(bytesOut);
	}
	
	private void serialize(Object o) throws Exception {
		objectOut.writeObject(o);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T deserialize() throws Exception {
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytesOut.toByteArray()));
		return (T)in.readObject();
	}

	// we could have broken equals() rules by writeReplace
	private void assertEqualsBothWays(Object obj1, Object obj2) {
		Assert.assertEquals(obj1, obj2);
		Assert.assertEquals(obj2, obj1);
	}

	/** */
	@Test
	public void queryListCanBeSerialized() throws Exception {
		fact().register(Trivial.class);

		Trivial triv = new Trivial("foo", 5);
		ofy().save().entity(triv).now();

		List<Trivial> trivs = ofy().load().type(Trivial.class).list();
		
		serialize(trivs);
		
		List<Trivial> back = deserialize();
		
		assert back.size() == 1;
		assert back.get(0).getId().equals(triv.getId());
		assertEqualsBothWays(trivs, back);
	}
	
	/** */
	@Test
	public void loadMapCanBeSerialized() throws Exception {
		fact().register(Trivial.class);

		Trivial triv = new Trivial("foo", 5);
		Key<Trivial> k = ofy().save().entity(triv).now();

		@SuppressWarnings("unchecked")
		Map<Key<Trivial>, Trivial> trivs = ofy().load().keys(k);
		
		serialize(trivs);
		
		Map<Key<Trivial>, Trivial> back = deserialize();
		
		assert back.size() == 1;
		assert back.get(k).getId().equals(triv.getId());
		assertEqualsBothWays(trivs, back);
	}

	// the Result can't be serialized, but the value given by Result.now() could be depending on the parameters
	// this guarantees we fail without calling now() during serialization, but it could work fine unwrapped
	@RequiredArgsConstructor
	static class NotSerializableResult<T> implements Result<T> {
		private final T wrapped;

		@Override
		public T now() {
			return wrapped;
		}
	}

	private static class NotSerializableList<E> extends ForwardingList<E> {
		@Override
		protected List<E> delegate() {
			return null; // not required
		}
	}

	private static class NotSerializableMap<K, V> extends ForwardingMap<K, V> {
		@Override
		protected Map<K, V> delegate() {
			return null; // not required
		}
	}

	private void assertSerialization(Object proxy) throws Exception {
		serialize(proxy);
		Object back = deserialize();
		assertEqualsBothWays(proxy, back);
	}

	private void assertProxiedListSerialization(List<Serializable> list) throws Exception {
		assertSerialization(ResultProxy.createList(new NotSerializableResult<List<Serializable>>(list)));
	}

	private void assertProxiedMapSerialization(Map<Serializable, Serializable> map) throws Exception {
		assertSerialization(ResultProxy.createMap(new NotSerializableResult<Map<Serializable, Serializable>>(map)));
	}

	@Test
	public void listProxyCanBeSerializedWithSerializableWrappedValue() throws Exception {
		assertProxiedListSerialization(Lists.<Serializable> newArrayList());
	}

	@Test(expectedExceptions = NotSerializableException.class)
	public void listProxyCantBeSerializedWithNotSerializableWrappedValue() throws Exception {
		assertProxiedListSerialization(new NotSerializableList<Serializable>());
	}

	@Test
	public void mapProxyCanBeSerializedWithSerializableWrappedValue() throws Exception {
		assertProxiedMapSerialization(Maps.<Serializable, Serializable> newHashMap());
	}

	@Test(expectedExceptions = NotSerializableException.class)
	public void mapProxyCantBeSerializedWithNotSerializableWrappedValue() throws Exception {
		assertProxiedMapSerialization(new NotSerializableMap<Serializable, Serializable>());
	}
}