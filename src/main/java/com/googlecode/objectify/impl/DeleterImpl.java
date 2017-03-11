package com.googlecode.objectify.impl;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.cmd.DeleteType;
import com.googlecode.objectify.cmd.Deleter;
import com.googlecode.objectify.util.ResultNow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Implementation of the Delete command.
 *
 * @author Jeff Schnitzer <jeff@infohazard.org>
 */
public class DeleterImpl implements Deleter
{
	/** */
	ObjectifyImpl<?> ofy;

	/** */
	public DeleterImpl(ObjectifyImpl<?> ofy) {
		this.ofy = ofy;
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Delete#type(java.lang.Class)
	 */
	@Override
	public DeleteType type(Class<?> type) {
		return new DeleteTypeImpl(this, type);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Deleter#key(com.googlecode.objectify.Key)
	 */
	@Override
	public Result<Void> key(Key<?> key) {
		return this.keys(key);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Deleter#keys(com.googlecode.objectify.Key<?>[])
	 */
	@Override
	public Result<Void> keys(Key<?>... keys) {
		return this.keys(Arrays.asList(keys));
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Delete#values(java.lang.Iterable)
	 */
	@Override
	public Result<Void> keys(Iterable<? extends Key<?>> keys) {
		List<com.google.appengine.api.datastore.Key> rawKeys = new ArrayList<>();
		for (Key<?> key: keys)
			rawKeys.add(key.getRaw());

		if (rawKeys.isEmpty())
			return new ResultNow<Void>(null);

		return ofy.createWriteEngine().delete(rawKeys);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Deleter#entity(java.lang.Object)
	 */
	@Override
	public Result<Void> entity(Object entity) {
		return this.entities(entity);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Deleter#entities(java.lang.Iterable)
	 */
	@Override
	public Result<Void> entities(Iterable<?> entities) {
		List<com.google.appengine.api.datastore.Key> keys = new ArrayList<>();
		for (Object obj: entities)
			keys.add(ofy.factory().keys().anythingToRawKey(obj));

		if (keys.isEmpty())
			return new ResultNow<Void>(null);

		return ofy.createWriteEngine().delete(keys);
	}

	/* (non-Javadoc)
	 * @see com.googlecode.objectify.cmd.Deleter#entities(java.lang.Object[])
	 */
	@Override
	public Result<Void> entities(Object... entities) {
		return this.entities(Arrays.asList(entities));
	}
}
