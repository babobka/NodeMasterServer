package ru.babobka.nodeServer.model;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dolgopolov.a on 22.12.15.
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {
	private final Map<E, Object> map;

	private static final Object dummy = new Object();

	public ConcurrentHashSet() {
		map = new ConcurrentHashMap<>();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean add(final E o) {
		return map.put(o, ConcurrentHashSet.dummy) == null;
	}

	@Override
	public boolean contains(final Object o) {
		return map.containsKey(o);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean remove(final Object o) {
		return map.remove(o) == ConcurrentHashSet.dummy;
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		int oldSize = this.map.size();
		Iterator<? extends E> iterator = collection.iterator();
		while (iterator.hasNext()) {
			this.add(iterator.next());
		}
		return oldSize == this.map.size();

	}

}
