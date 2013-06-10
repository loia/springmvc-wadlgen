package com.alanloi.springmvc.wadl.mapper;

import javax.xml.namespace.QName;

/**
 * Interface for mapping between WADL and Java types. 
 * 
 * @author Alan Loi
 */
public interface WadlTypeMapper {
	
	/**
	 * Get a WADL type based on a Java type.
	 * 
	 * @param javaType the java type.
	 * @return the WADL type.
	 */
	QName getWadlType(Class<?> javaType);
}
