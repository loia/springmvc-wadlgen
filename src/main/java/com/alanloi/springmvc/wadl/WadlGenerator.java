package com.alanloi.springmvc.wadl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import lombok.extern.log4j.Log4j;
import net.java.dev.wadl._2009._02.WadlApplication;
import net.java.dev.wadl._2009._02.WadlDoc;
import net.java.dev.wadl._2009._02.WadlMethod;
import net.java.dev.wadl._2009._02.WadlParam;
import net.java.dev.wadl._2009._02.WadlParamStyle;
import net.java.dev.wadl._2009._02.WadlRepresentation;
import net.java.dev.wadl._2009._02.WadlRequest;
import net.java.dev.wadl._2009._02.WadlResource;
import net.java.dev.wadl._2009._02.WadlResources;
import net.java.dev.wadl._2009._02.WadlResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.alanloi.springmvc.wadl.mapper.JsonBasedWadlTypeMapper;
import com.alanloi.springmvc.wadl.mapper.WadlTypeMapper;

/**
 * Generates a WadlApplication from Spring MVC request mappings.
 * 
 * @author Alan Loi
 */
@Log4j
public class WadlGenerator {
	
	private static final WadlTypeMapper DEFAULT_WADL_TYPE_MAPPER = new JsonBasedWadlTypeMapper();
	
	/**
	 * Generates a WadlApplication from Spring MVC request mappings.
	 * 
	 * Supported Spring MVC annotations:
	 * 
	 * <code>RequestMapping</code>
	 *   Mapped to a WADL <code>application/resources/resource</code> element 
	 * 
	 *   Parameters:
	 *    - value: mapped to <code>resource/@path<code>
	 *    - method: mapped to <code>resource/method/@name<code>
	 *    - produces: mapped to <code>resource/response/representation/@mediaType<code>
	 *    - consumes: mapped to <code>resource/request/representation/@mediaType<code>
	 * 
	 * <code>PathVariable</code>
	 *   Mapped to a WADL <code>application/resources/resource/method/request/param</code>
	 *   element with <code>@style="template" @required="true"</code>
	 *   
	 *   Parameters:
	 *    - value: mapped to <code>param/@name<code> 
	 * 
	 * <code>RequestParam</code>
	 *   Mapped to a WADL <code>application/resources/resource/method/request/param</code>
	 *   element with <code>@style="query"</code>
	 *   
	 *   Parameters:
	 *    - value: mapped to <code>param/@name<code> 
	 *    - required: mapped to <code>param/@required<code>
	 *    - defaultValue: mapped to <code>param/@default<code>
	 * 
	 * @param handlerMapping the Spring MVC request mappings
	 * @param request the HTTP request to retrieve the WADL
	 * @param applicationName name of the application
	 * @param ignoreControllers list of controller classes to ignore
	 * @param wadlTypeMapper custom WADL type mapper to use
	 * 
	 * @return a WadlApplication describing the application's API
	 */
	public static WadlApplication generate(RequestMappingHandlerMapping handlerMapping,
			HttpServletRequest request, String applicationName, List<Class<?>> ignoreControllers,
			WadlTypeMapper wadlTypeMapper) {

		WadlApplication result = new WadlApplication();

		WadlDoc doc = createWadlDoc(applicationName);
		result.getDoc().add(doc);

		WadlResources wadlResources = new WadlResources();

		String baseUrl = getBaseUrl(request);
		wadlResources.setBase(baseUrl);

		Map<RequestMappingInfo, HandlerMethod> handletMethods = handlerMapping.getHandlerMethods();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handletMethods.entrySet()) {
			RequestMappingInfo mappingInfo = entry.getKey();
			HandlerMethod handlerMethod = entry.getValue();
			
			Class<?> controllerClass = handlerMethod.getBeanType();
			if (ignoreControllers.contains(controllerClass)) {
				if (log.isDebugEnabled()) {
					log.debug("Ignoring controller class: " + controllerClass);
				}
				
				continue;  // skip
			}

			WadlResource wadlResource = mapToWadlResource(mappingInfo, handlerMethod, wadlTypeMapper);
			wadlResources.getResource().add(wadlResource);
		}

		result.getResources().add(wadlResources);

		return result;
	}

	public static WadlApplication generate(RequestMappingHandlerMapping handlerMapping,
			HttpServletRequest request, String applicationName, List<Class<?>> ignoreControllers) {
		return generate(handlerMapping, request, applicationName, ignoreControllers, DEFAULT_WADL_TYPE_MAPPER);
	}
	
	private static WadlResource mapToWadlResource(RequestMappingInfo mappingInfo, HandlerMethod handlerMethod,
			WadlTypeMapper wadlTypeMapper) {
		WadlResource wadlResource = new WadlResource();

		Set<String> pattern = mappingInfo.getPatternsCondition().getPatterns();
		for (String uri : pattern) {
			wadlResource.setPath(uri);
		}

		Set<MediaType> consumableMediaTypes = mappingInfo.getConsumesCondition().getConsumableMediaTypes();
		Set<MediaType> producibleMediaTypes = mappingInfo.getProducesCondition().getProducibleMediaTypes();
		Set<RequestMethod> httpMethods = mappingInfo.getMethodsCondition().getMethods();

		for (RequestMethod httpMethod : httpMethods) {
			WadlMethod wadlMethod = mapToWadlMethod(httpMethod, handlerMethod.getMethod(), consumableMediaTypes,
					producibleMediaTypes, wadlTypeMapper);
			wadlResource.getMethodOrResource().add(wadlMethod);
		}

		return wadlResource;
	}

	private static WadlMethod mapToWadlMethod(RequestMethod httpMethod, Method method,
			Set<MediaType> consumableMediaTypes, Set<MediaType> producibleMediaTypes,
			WadlTypeMapper wadlTypeMapper) {
		WadlMethod wadlMethod = new WadlMethod();

		wadlMethod.setName(httpMethod.name());
		wadlMethod.setId(method.getName());

		WadlDoc wadlDocMethod = createWadlDoc(method.getName());
		wadlMethod.getDoc().add(wadlDocMethod);

		WadlRequest wadlRequest = mapToWadlRequest(method, consumableMediaTypes, wadlTypeMapper);
		wadlMethod.setRequest(wadlRequest);

		WadlResponse wadlResponse = mapToWadlResponse(method, producibleMediaTypes, wadlTypeMapper);
		wadlMethod.getResponse().add(wadlResponse);

		return wadlMethod;
	}

	private static WadlRequest mapToWadlRequest(Method method, Set<MediaType> consumableMediaTypes,
			WadlTypeMapper wadlTypeMapper) {
		WadlRequest wadlRequest = new WadlRequest();

		List<String> paramNames = getParameterNames(method);
		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		List<WadlParam> wadlParams = mapToWadlParams(paramAnnotations, paramNames, paramTypes, wadlTypeMapper);

		// if there's no params, there's no request!
		if (wadlParams.isEmpty()) {
			return null;
		} else {
			wadlRequest.getParam().addAll(wadlParams);
		}

		if (!consumableMediaTypes.isEmpty()) {
			List<WadlRepresentation> representations = mapToWadlRepresentations(consumableMediaTypes);
			wadlRequest.getRepresentation().addAll(representations);
		}

		return wadlRequest;
	}

	private static WadlResponse mapToWadlResponse(Method method, Set<MediaType> producibleMediaTypes,
			WadlTypeMapper wadlTypeMapper) {
		Class<?> returnType = method.getReturnType();
		if (returnType == null) {
			return null;  // early abort
		}
		
		WadlResponse wadlResponse = new WadlResponse();
		
		if (!producibleMediaTypes.isEmpty()) {
			QName returnParamName = wadlTypeMapper.getWadlType(returnType);
			List<WadlRepresentation> representations = mapToWadlRepresentations(producibleMediaTypes,
					returnParamName);
			wadlResponse.getRepresentation().addAll(representations);
		}

		return wadlResponse;
	}

	private static List<WadlRepresentation> mapToWadlRepresentations(Set<MediaType> mediaTypes) {
		return mapToWadlRepresentations(mediaTypes, null);
	}

	private static List<WadlRepresentation> mapToWadlRepresentations(Set<MediaType> mediaTypes, QName element) {
		List<WadlRepresentation> representations = new ArrayList<WadlRepresentation>();

		for (MediaType mediaType : mediaTypes) {
			WadlRepresentation wadlRepresentation = new WadlRepresentation();
			wadlRepresentation.setMediaType(mediaType.toString());
			wadlRepresentation.setElement(element);
			representations.add(wadlRepresentation);
		}

		return representations;
	}

	private static List<WadlParam> mapToWadlParams(Annotation[][] paramAnnotations, List<String> paramNames,
			Class<?>[] paramTypes, WadlTypeMapper wadlTypeMapper) {
		List<WadlParam> wadlParams = new ArrayList<WadlParam>();

		if (paramAnnotations == null) {
			return wadlParams; // early abort
		}

		if (paramAnnotations.length != paramNames.size()) {
			throw new IllegalArgumentException("Annotations length '" + paramAnnotations.length
					+ "' does not match parameter names size: " + paramNames.size());
		} else if (paramAnnotations.length != paramTypes.length) {
			throw new IllegalArgumentException("Annotations length '" + paramAnnotations.length
					+ "' does not match parameter types size: " + paramTypes.length);
		}

		for (int i = 0; i < paramAnnotations.length; i++) {
			Annotation[] annotations = paramAnnotations[i];

			/*
			 * TODO if no annotations, this means its a param in the
			 * HTTP request body ???
			 */

			if (annotations == null) {
				continue; // skip
			}

			String paramName = paramNames.get(i);
			QName paramType = wadlTypeMapper.getWadlType(paramTypes[i]);

			for (Annotation annotation : annotations) {
				if (annotation instanceof PathVariable) {
					WadlParam waldParam = mapToWadlParam((PathVariable) annotation, paramName, paramType);
					wadlParams.add(waldParam);

				} else if (annotation instanceof RequestParam) {
					WadlParam waldParam = mapToWadlParam((RequestParam) annotation, paramName, paramType);
					wadlParams.add(waldParam);
				}
			}
		}

		return wadlParams;
	}

	private static WadlParam mapToWadlParam(PathVariable paramAnnotation, String paramName,
			QName paramType) {
		WadlParam wadlParam = new WadlParam();
		String wadlParamName = getParameterName(paramAnnotation.value(), paramName);
		wadlParam.setName(wadlParamName);
		wadlParam.setType(paramType);
		wadlParam.setStyle(WadlParamStyle.TEMPLATE);
		wadlParam.setRequired(true);
		return wadlParam;
	}

	private static WadlParam mapToWadlParam(RequestParam paramAnnotation, String paramName,
			QName paramType) {
		WadlParam wadlParam = new WadlParam();

		String wadlParamName = getParameterName(paramAnnotation.value(), paramName);
		wadlParam.setName(wadlParamName);
		wadlParam.setType(paramType);
		wadlParam.setStyle(WadlParamStyle.QUERY);
		wadlParam.setRequired(paramAnnotation.required());

		String defaultValue = cleanDefault(paramAnnotation.defaultValue());
		if (StringUtils.isNotEmpty(defaultValue)) {
			wadlParam.setDefault(defaultValue);
		}

		return wadlParam;
	}

	/**
	 * Get the name of a parameter of a REST resource or method.
	 * 
	 * As per Spring MVC docs, the annotation value takes precedence over the
	 * method parameter value. If neither are available, returns "param".
	 * 
	 * @param annotationValue
	 *            the value of the annotation e.g. @PathVariable("a")
	 * @param methodParamName
	 *            the name of the parameter from the method
	 * @return the parameter value
	 */
	private static String getParameterName(String annotationValue, String methodParamName) {

		if (StringUtils.isNotBlank(annotationValue)) {
			return annotationValue;
		} else if (StringUtils.isNotBlank(methodParamName)) {
			return methodParamName;
		} else {
			// can't determine - return default
			return "param";
		}
	}

	private static List<String> getParameterNames(Method method) {
		ParameterNameDiscoverer paramNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
		String[] parameterNames = paramNameDiscoverer.getParameterNames(method);
		return toList(parameterNames);
	}

	private static WadlDoc createWadlDoc(String title) {
		WadlDoc wadlDoc = new WadlDoc();
		wadlDoc.setTitle(title);
		return wadlDoc;
	}

	private static String getBaseUrl(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://");
		sb.append(request.getServerName()).append(":");
		sb.append(request.getServerPort());
		sb.append(request.getContextPath());
		return sb.toString();
	}

	private static <T> List<T> toList(T... array) {
		if (array == null) {
			return Collections.emptyList();
		}

		return Arrays.asList(array);
	}

	/**
	 * For some strange reason, either Spring MVC or Java Reflections API
	 * can return some weird characters which we need to remove.
	 * 
	 * @param value the string to be cleaned.
	 * @return the cleaned string.
	 */
	private static String cleanDefault(String value) {
		value = StringUtils.deleteWhitespace(value);
		value = StringUtils.remove(value, '\uE000');
		value = StringUtils.remove(value, '\uE001');
		value = StringUtils.remove(value, '\uE002');
		return value;
	}
}
