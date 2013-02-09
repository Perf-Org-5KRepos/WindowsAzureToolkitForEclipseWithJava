/**
 * Copyright 2013 Microsoft Open Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoftopentechnologies.wacommon.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
/**
 * Class parses the preferencesets.xml file
 * and returns required attribute values.
 */
public class PreferenceSetUtil {

	public static final String PREF_SET_DEFNAME = "/preferencesets/@default";
	public static final String PREF_SET_NAME = "/preferencesets/preferenceset[@name='%s']/@name";
	public static final String PREF_SET_PUBLISHSET = "/preferencesets/preferenceset[@name='%s']/@publishsettings";
	public static final String PREF_SET_PORTURL = "/preferencesets/preferenceset[@name='%s']/@portalURL";
	public static final String PREF_SET_PORTURL_SMALL = "/preferencesets/preferenceset[@name='%s']/@portalurl";
	public static final String PREF_SET_BLOB = "/preferencesets/preferenceset[@name='%s']/@blob";
	public static final String PREF_SET_MGT = "/preferencesets/preferenceset[@name='%s']/@management";

	private static String prefFilePath = String.format("%s%s%s%s%s%s%s",
			new File(Platform.getInstallLocation().getURL().
					getFile()).getPath().toString(),
					File.separator,
					Messages.pluginFolder ,
					File.separator,
					Messages.waCommonFolderID,
					File.separator,
			"preferencesets.xml");

	/**
	 * Method returns preferenceset's name.
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedPreferenceSetName()
			throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String expr = PREF_SET_DEFNAME;
			String defname = getExpressionValue(doc, expr);
			String name = getExpressionValue(doc, expr);
			if (name == null || name.isEmpty()) {
				throw new Exception(Messages.nameNtErMsg);
			}
			return defname;
		} catch (Exception e) {
			throw new WACommonException(Messages.nameGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's publish settings URL.
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedPublishSettingsURL()
			throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_PUBLISHSET, name);
			String pubSetURL = getExpressionValue(doc, expr);
			if (pubSetURL == null || pubSetURL.isEmpty()) {
				throw new WACommonException(Messages.pubUrlNtErMsg);
			}
			return pubSetURL;
		} catch (Exception e) {
			throw new WACommonException(Messages.pubUrlGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's portal URL.
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedPortalURL()
			throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_PORTURL, name);
			String portalURL = getExpressionValue(doc, expr);
			/*
			 * If "portalURL" attribute is not present then
			 * check for "portalurl" attribute
			 */
			if (portalURL.isEmpty() || portalURL == null) {
				String exprSmall = String.format(PREF_SET_PORTURL_SMALL, name);
				portalURL = getExpressionValue(doc, exprSmall);
			}
			return portalURL;
		} catch (Exception e) {
			throw new WACommonException(
					Messages.portUrlGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's blob service URL.
	 * @param storageName
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedBlobServiceURL(String storageName)
			throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_BLOB, name);
			String blobURL = getExpressionValue(doc, expr);
			if (blobURL == null || blobURL.isEmpty()) {
				throw new WACommonException(
						Messages.blbUrlNtErMsg);
			}
			blobURL = blobURL.replace(
					"${storage-service-name}", storageName);
			// For blob it always needs to end with forward slash
			// and customers may forgot about this,
			// while editing preferences,
			// hence its safe to append if not exists
			if (!blobURL.endsWith("/")) {
				return blobURL + "/";
			}
			return blobURL;
		} catch (Exception e) {
			throw new WACommonException(Messages.blbUrlGetErMsg, e);
		}
	}

	/**
	 * Method returns preferenceset's management URL.
	 * @param subscriptionID
	 * @return
	 * @throws WACommonException
	 */
	public static String getSelectedManagementURL(String subscriptionID)
			throws WACommonException {
		try {
			Document doc = parseXMLFile(prefFilePath);
			String name = getSelectedPreferenceSetName();
			String expr = String.format(PREF_SET_MGT, name);
			String mgtURL = getExpressionValue(doc, expr);
			if (mgtURL == null || mgtURL.isEmpty()) {
				throw new WACommonException(
						Messages.mngUrlNtErMsg);
			}
			mgtURL = mgtURL.replace(
					"${subscription-id}", subscriptionID);
			return mgtURL;
		} catch (Exception e) {
			throw new WACommonException(Messages.mngUrlGetErMsg, e);
		}
	}

	/**
	 * This API evaluates  XPath expression
	 * and return the result as a String.
	 * @param doc
	 * @param expr
	 * @return
	 * @throws XPathExpressionException
	 */
	public static String getExpressionValue(Document doc, String expr)
			throws XPathExpressionException {
		if (doc == null || expr == null || expr.isEmpty()) {
			throw new IllegalArgumentException(Messages.inValArg);
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		return xPath.evaluate(expr, doc);
	}

	/** Parses XML file and returns XML document.
	 * @param fileName .
	 * @return XML document or <B>null</B> if error occurred
	 * @throws Exception
	 */
	protected static Document parseXMLFile(final String fileName)
			throws Exception {
		try {
			DocumentBuilder docBuilder;
			Document doc = null;
			DocumentBuilderFactory docBuilderFactory =
					DocumentBuilderFactory.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			docBuilder = docBuilderFactory.newDocumentBuilder();
			File xmlFile = new File(fileName);
			doc = docBuilder.parse(xmlFile);
			return doc;
		} catch (Exception e) {
			throw new Exception(Messages.parseErMsg);
		}
	}
}
