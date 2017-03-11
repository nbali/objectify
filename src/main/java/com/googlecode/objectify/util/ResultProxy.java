package com.googlecode.objectify.util;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingMap;
import com.googlecode.objectify.Result;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * A utility class that wraps a Result<?> value. For example, if you had a Result<List<String>>, the
 * proxy would implement List<String> and call through to the inner object.
 */
public class ResultProxy<T>
{
	public static <K, V> Map<K, V> createMap(final Result<Map<K, V>> result) {
		return new ResultProxyMap<K, V>(result);
	}

	public static <E> List<E> createList(final Result<List<E>> result) {
		return new ResultProxyList<E>(result);
	}

	@RequiredArgsConstructor
	private static class ResultProxyList<E> extends ForwardingList<E> implements Serializable {

		private final Result<List<E>> result;

		@Override
		protected List<E> delegate() {
			return result.now();
		}

		private Object writeReplace() throws ObjectStreamException {
			return delegate();
		}
	}

	@RequiredArgsConstructor
	private static class ResultProxyMap<K, V> extends ForwardingMap<K, V> implements Serializable {

		private final Result<Map<K, V>> result;

		@Override
		protected Map<K, V> delegate() {
			return result.now();
		}

		private Object writeReplace() throws ObjectStreamException {
			return delegate();
		}
	}
}
