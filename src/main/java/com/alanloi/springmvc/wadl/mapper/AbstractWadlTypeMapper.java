package com.alanloi.springmvc.wadl.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import lombok.extern.log4j.Log4j;

/**
 * Abstract base class for WadlTypeMapper.
 * 
 * @author Alan Loi
 */
@Log4j
public abstract class AbstractWadlTypeMapper implements WadlTypeMapper {

	private static final String WADL_NAMESPACE = "http://wadl.dev.java.net/2009/02";

	private final List<JavaWadlTypePair> javaWadlTypeBindings;

	public AbstractWadlTypeMapper() {
		this.javaWadlTypeBindings = createJavaWadlTypeBindings();
	}

	/**
	 * Create a mapping of Java types (classes) to the WADL types (QNames).
	 * 
	 * @return Java to WADL type bindings
	 */
	protected abstract List<JavaWadlTypePair> createJavaWadlTypeBindings();

	/**
	 * {@inheritDoc}
	 */
	public QName getWadlType(Class<?> javaType) {

		// if the param type is an array - get the real class
		Class<?> componentType = javaType.getComponentType();
		if (componentType != null) {
			javaType = componentType;
		}

		// add all the super classes and interfaces too
		List<Class<?>> javaTypes = new ArrayList<Class<?>>();
		javaTypes.add(javaType);
		javaTypes.addAll(Arrays.asList(javaType.getClasses()));

		if (log.isTraceEnabled()) {
			log.trace("Param types: " + javaTypes);
		}

		for (JavaWadlTypePair binding : javaWadlTypeBindings) {
			Class<?> bindingType = binding.getJavaType();

			if (javaTypes.contains(bindingType)) {
				QName wadlType = binding.getWadlType();

				if (log.isTraceEnabled()) {
					log.trace("Mapped class '" + javaType + "' to WADL param type: " + wadlType);
				}

				return wadlType;
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Could not map class '" + javaType + "' to a WADL param type.");
		}

		// not found
		return null;
	}

	/**
	 * NOTE: This uses the WADL namespace for the type defined. Using this
	 * method means your WADL will not technically be compliant with the WADL
	 * schema as it does not define any built-in data type.  If this is an
	 * issue, you should define and use your own namespace.
	 * 
	 * @param bindings the bindings to update
	 * @param javaType the java type
	 * @param wadlLocalName the local name of the WADL type
	 * 
	 * @see #addBinding(List, Class, QName)
	 */
	protected static void addBinding(List<JavaWadlTypePair> bindings, Class<?> javaType, String wadlLocalName) {
		addBinding(bindings, javaType, wadlQName(wadlLocalName));
	}

	/**
	 * Add a Java-to-WADL type binding.
	 * 
	 * @param bindings the bindings to update
	 * @param javaType the java type
	 * @param wadlLocalName the local name of the WADL type
	 */
	protected static void addBinding(List<JavaWadlTypePair> bindings, Class<?> javaType, QName wadlType) {
		bindings.add(new JavaWadlTypePair(javaType, wadlType));

		if (log.isDebugEnabled()) {
			log.debug("Added Java-WADL data type binding - class: " + javaType.getName() + ", qName: " + wadlType);
		}
	}

	protected static QName wadlQName(String localPart) {
		return new QName(WADL_NAMESPACE, localPart);
	}
}
