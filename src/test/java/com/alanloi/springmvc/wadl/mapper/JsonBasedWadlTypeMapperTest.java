/*
 * Copyright 2013 Virgin Australia Airlines Pty Limited. All rights reserved. Not to be copied, redistributed or
 * modified without prior written consent of Virgin Australia Airlines Pty Limited
 */
package com.alanloi.springmvc.wadl.mapper;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for JsonBasedWadlTypeMapper.
 * 
 * @author <a href="alan.loi@virginaustralia.com">Alan Loi</a>
 */
public class JsonBasedWadlTypeMapperTest {

	private JsonBasedWadlTypeMapper mapper;
	
	@Before
	public void setUp() {
		this.mapper = new JsonBasedWadlTypeMapper();
	}
	
	@Test
	public void testGetWadlTypeWithDirectMatch() {
		assertEquals(wadlQName("string"), this.mapper.getWadlType(String.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(BigInteger.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(Integer.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(Long.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(Short.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(BigDecimal.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(Double.class));
		assertEquals(wadlQName("boolean"), this.mapper.getWadlType(Boolean.class));
		assertEquals(wadlQName("array"), this.mapper.getWadlType(List.class));
		assertEquals(wadlQName("array"), this.mapper.getWadlType(Set.class));
		assertEquals(wadlQName("object"), this.mapper.getWadlType(Map.class));
		assertEquals(wadlQName("object"), this.mapper.getWadlType(Object.class));
	}
	
	@Test
	public void testGetWadlTypeWithParentClassMatch() {
		assertEquals(wadlQName("number"), this.mapper.getWadlType(MegaInteger.class));
		assertEquals(wadlQName("number"), this.mapper.getWadlType(UberInteger.class));
	}
	
	@Test
	public void testGetWadlTypeWithParentInterfaceMatch() {
		assertEquals(wadlQName("array"), this.mapper.getWadlType(MegaList.class));
		assertEquals(wadlQName("array"), this.mapper.getWadlType(UberList.class));
	}
	
	@Test
	public void testGetWadlTypeUsesPriority() {
		// integer has higher priority so should win over list
		assertEquals(wadlQName("number"), this.mapper.getWadlType(MegaIntegerList.class));
	}
	
	@Test
	public void testGetWadlTypeWithUnknownType() {
		// everything extends object so this is the catch-all
		assertEquals(wadlQName("object"), this.mapper.getWadlType(this.getClass()));
	}
	
	private QName wadlQName(String localPart) {
		return new QName("http://wadl.dev.java.net/2009/02", localPart);
	}
	
	class MegaInteger extends BigInteger {
		
		private static final long serialVersionUID = 1L;

		public MegaInteger() {
			super("test");
		}
	}
	
	class UberInteger extends MegaInteger {
		
		private static final long serialVersionUID = 1L;

	}
	
	interface MegaList<T> extends List<T> {
		void kapoow();
	}
	
	interface UberList<T> extends MegaList<T> {
		void shazam();
	}
	
	class MegaIntegerList<T> extends MegaInteger implements MegaList<T> {
		
		private static final long serialVersionUID = 1L;

		public boolean add(T e) {
			return false;
		}

		public void add(int index, T element) {
		}

		public boolean addAll(Collection<? extends T> c) {
			return false;
		}

		public boolean addAll(int index, Collection<? extends T> c) {
			return false;
		}

		public void clear() {
		}

		public boolean contains(Object o) {
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			return false;
		}

		public T get(int index) {
			return null;
		}

		public int indexOf(Object o) {
			return 0;
		}

		public boolean isEmpty() {
			return false;
		}

		public Iterator<T> iterator() {
			return null;
		}

		public int lastIndexOf(Object o) {
			return 0;
		}

		public ListIterator<T> listIterator() {
			return null;
		}

		public ListIterator<T> listIterator(int index) {
			return null;
		}

		public boolean remove(Object o) {
			return false;
		}

		public T remove(int index) {
			return null;
		}

		public boolean removeAll(Collection<?> c) {
			return false;
		}

		public boolean retainAll(Collection<?> c) {
			return false;
		}

		public T set(int index, T element) {
			return null;
		}

		public int size() {
			return 0;
		}

		public List<T> subList(int fromIndex, int toIndex) {
			return null;
		}

		public Object[] toArray() {
			return null;
		}

		public <E> E[] toArray(E[] a) {
			return null;
		}

		public void kapoow() {
		}

	}
}
