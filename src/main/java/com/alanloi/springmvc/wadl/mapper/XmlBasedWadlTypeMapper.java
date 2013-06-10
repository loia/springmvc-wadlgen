package com.alanloi.springmvc.wadl.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

/**
 * A WADL Type Mapper which is based on XML Schema data types.
 * 
 * The mappings are based on the JAXB Default Data Type Bindings section at:
 * {@link http://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html}
 * 
 * @author Alan Loi
 */
public class XmlBasedWadlTypeMapper extends AbstractWadlTypeMapper {

	private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<JavaWadlTypePair> createJavaWadlTypeBindings() {
		List<JavaWadlTypePair> bindings = new ArrayList<JavaWadlTypePair>();
		addBinding(bindings, String.class, "string");
		addBinding(bindings, BigInteger.class, "integer");
		addBinding(bindings, Integer.class, "integer");
		addBinding(bindings, Long.class, "long");
		addBinding(bindings, Short.class, "short");
		addBinding(bindings, BigDecimal.class, "decimal");
		addBinding(bindings, Float.class, "float");
		addBinding(bindings, Double.class, "double");
		addBinding(bindings, Boolean.class, "boolean");
		addBinding(bindings, Byte.class, "byte");
		addBinding(bindings, Calendar.class, "dateTime");
		addBinding(bindings, Date.class, "dateTime");
		addBinding(bindings, Duration.class, "duration");
		addBinding(bindings, List.class, "anyType");
		addBinding(bindings, Set.class, "anyType");
		addBinding(bindings, Map.class, "anyType");
		addBinding(bindings, Object.class, "anyType");
		return bindings;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Uses the XML Schema (XSD) namespace instead of the WADL one.
	 */
	protected static void addBinding(List<JavaWadlTypePair> bindings, Class<?> javaType, String wadlLocalName) {
		addBinding(bindings, javaType, xsdQName(wadlLocalName));
	}

	protected static QName xsdQName(String localPart) {
		return new QName(XSD_NAMESPACE, localPart);
	}
}
