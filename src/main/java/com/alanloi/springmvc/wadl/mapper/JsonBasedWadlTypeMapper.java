package com.alanloi.springmvc.wadl.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A WADL Type Mapper which is based on JSON data types.
 * 
 * The mappings are based on the JSON data types as per:
 * {@link http://www.json.org/fatfree.html}
 * 
 * @author Alan Loi
 */
public class JsonBasedWadlTypeMapper extends AbstractWadlTypeMapper {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<JavaWadlTypePair> createJavaWadlTypeBindings() {
		List<JavaWadlTypePair> bindings = new ArrayList<JavaWadlTypePair>();
		addBinding(bindings, String.class, "string");
		addBinding(bindings, BigInteger.class, "number");
		addBinding(bindings, Integer.class, "number");
		addBinding(bindings, Long.class, "number");
		addBinding(bindings, Short.class, "number");
		addBinding(bindings, BigDecimal.class, "number");
		addBinding(bindings, Float.class, "number");
		addBinding(bindings, Double.class, "number");
		addBinding(bindings, Boolean.class, "boolean");
		addBinding(bindings, List.class, "array");
		addBinding(bindings, Set.class, "array");
		addBinding(bindings, Map.class, "object");
		addBinding(bindings, Object.class, "object");
		return bindings;
	}
}
