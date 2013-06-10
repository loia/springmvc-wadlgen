package com.alanloi.springmvc.wadl.mapper;

import javax.xml.namespace.QName;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A pair of Java & WADL types.
 * 
 * @author Alan Loi
 */
@RequiredArgsConstructor
@Getter
@ToString
public class JavaWadlTypePair {
	
	private final Class<?> javaType;
	private final QName wadlType;
}
