/*
 * Copyright 2011 SOFTEC sa. All rights reserved.
 *
 * This source code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Luxembourg
 * License.
 *
 * To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-nc-nd/3.0/lu/
 * or send a letter to Creative Commons, 171 Second Street,
 * Suite 300, San Francisco, California, 94105, USA.
 */

package org.codehaus.mojo.javascript;

import org.apache.commons.jxpath.ri.compiler.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.javascript.titanium.TitaniumUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class containing information on the titanium SDK to use.
 */
public class TitaniumSettings {

    /**
     * The titanium android build script.
     * This parameter is optional as it may be guessed based on the operating system.
     * @parameter expression="${androidBuilder}"
     */
    protected File androidBuilder;

    /**
     * The titanium iOS build script.
     * This parameter is optional as it may be guessed based on the operating system.
     * @parameter expression="${iosBuilder}"
     */
    protected File iosBuilder;

    /**
     * The android SDK location.
     * This parameter is optional, by default the android SDK is retrieved based on the environment
     * variable ANDROID_HOME.
     * @parameter expression="${androidSDK}"
     */
    protected File androidSDK;

    /**
     * The android keystore to use to sign the application.
     * @parameter expression="${sign.keystore}"
     */
    private String keystore;

    /**
     * The android keystore password.
     * @parameter expression="${sign.password}"
     */
    private String keystorePassword = "tirocks";

    /**
     * The android keystore alias
     * @parameter expression="${sign.alias}"
     */
    private String keystoreAlias = "tidev";

    /**
     * The iOS development provisioning profile.
     * This profile is use when {@link AbstractTitaniumPackageMojo#executeMode}
     * is <code>virtual</code>
     * or <code>device</code>.
     * @parameter expression="${iosDevelopmentProvisioningProfile}"
     */
    protected String iosDevelopmentProvisioningProfile;

    /**
     * The ios distribution provisioning profile.
     * This profile is used when {@link AbstractTitaniumPackageMojo#executeMode}
     * is <code>none</code>.
     * @parameter expression="${iosDistributionProvisioningProfile}"
     */
    protected String iosDistributionProvisioningProfile;

    /**
     * <p>The ios Development certificate.</p>
     * <p>This certificate is used when {@link AbstractTitaniumPackageMojo#executeMode}
     * is <code>virtual</code>
     * or <code>device</code>.</p>
     * @parameter expression="${iosDevelopmentCertificate}"
     */
    protected String iosDevelopmentCertificate;

    /**
     * <p>The ios Distribution certificate.</p>
     * <p>This certificate is used when {@link AbstractTitaniumPackageMojo#executeMode}
     * is <code>none</code>.</p>
     * @parameter expression="${iosDistributionCertificate}"
     */
    protected String iosDistributionCertificate;


    public void setKeystoreAlias(String keystoreAlias) {
        this.keystoreAlias = keystoreAlias;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getKeystore(String version, File targetDir) throws MojoExecutionException {
        if (keystore == null) {
            ensureKeystore(version, targetDir);
        }
        return keystore;
    }

    public String getKeystoreAlias() {
        return keystoreAlias;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public File getAndroidBuilder(String version) {
        if (androidBuilder != null) {
            return androidBuilder;
        } else {
            String sdkPath = TitaniumUtils.getTitaniumSdkPath(version);
            if (sdkPath != null) {
                return new File(sdkPath + "android" + File.separator + "builder.py");
            }
        }
        return androidBuilder;
    }

    public void setAndroidBuilder(File androidBuilder) {
        this.androidBuilder = androidBuilder;
    }

    public File getAndroidSdk() throws MojoExecutionException {
        if (androidSDK == null) {
            androidSDK = new File(TitaniumUtils.getAndroidHome());
        }
        return androidSDK;
    }

    public void setAndroidSdk(File androidSdk) {
        this.androidSDK = androidSdk;
    }

    public File getIosBuilder(String version) {
        if (iosBuilder != null) {
            return iosBuilder;
        } else {
            String sdkPath = TitaniumUtils.getTitaniumSdkPath(version);
            if (sdkPath != null) {
                return new File(sdkPath + "iphone" + File.separator + "builder.py");
            }
        }
        return iosBuilder;
    }

    public void setIosBuilder(File iosBuilder) {
        this.iosBuilder = iosBuilder;
    }

    public String getIosDistributionCertificate() {
        return iosDistributionCertificate;
    }

    public void setIosDistributionCertificate(String iosDistributionCertificate) {
        this.iosDistributionCertificate = iosDistributionCertificate;
    }

    public String getIosDevelopmentCertificate() {
        return iosDevelopmentCertificate;
    }

    public void setIosDevelopmentCertificate(String iosDevelopmentCertificate) {
        this.iosDevelopmentCertificate = iosDevelopmentCertificate;
    }

    public String getIosDevelopmentProvisioningProfile() {
        return iosDevelopmentProvisioningProfile;
    }

    public void setIosDevelopmentProvisioningProfile(String iosDevelopmentProvisioningProfile) {
        this.iosDevelopmentProvisioningProfile = iosDevelopmentProvisioningProfile;
    }

    public String getIosDistributionProvisioningProfile() {
        return iosDistributionProvisioningProfile;
    }

    public void setIosDistributionProvisioningProfile(String iosDistributionProvisioningProfile) {
        this.iosDistributionProvisioningProfile = iosDistributionProvisioningProfile;
    }

    private void ensureKeystore(String version, File targetDir) throws MojoExecutionException{
        if (keystore == null) {
            String sdk = TitaniumUtils.getTitaniumSdkPath(version);
            if (sdk == null) {
                sdk = TitaniumUtils.getTitaniumArtifactSdkPath(version, targetDir);
            }
            if (sdk != null) {
                File keystoreFile = new File(sdk, "android" + File.separator + "dev_keystore");
                if (keystoreFile.exists()) {
                    keystore = keystoreFile.getAbsolutePath();
                }
            }
        }
        if (keystore == null) {
            keystore = "";
        }

        File keystoreFile = new File(keystore);
        if (!keystoreFile.exists()) {
            throw new MojoExecutionException("Invalid keystore location: " + keystoreFile.getAbsolutePath());
        }
    }

    public Tiapp getTiappFromXML(File xmlFile) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        XPath xpath = XPathFactory.newInstance().newXPath();

        Tiapp tiapp = new Tiapp();

        tiapp.setName(getFromXPath(doc, xpath, "/ti:app/ti:name/text()"));
        tiapp.setAnalytics(getFromXPathAsBoolean(doc, xpath, "/ti:app/ti:analytics/text()"));
        tiapp.setCopyright(getFromXPath(doc, xpath, "/ti:app/ti:copyright/text()"));
        tiapp.setDescription(getFromXPath(doc, xpath, "/ti:app/ti:description/text()"));
        tiapp.setFullScreen(getFromXPathAsBoolean(doc, xpath, "/ti:app/ti:fullscreen/text()"));
        tiapp.setGuid(getFromXPath(doc, xpath, "/ti:app/ti:guid/text()"));
        if (tiapp.getGuid().trim().equals("")) {
            tiapp.setGuid(getFromXPath(doc, xpath, "/ti:app/guid/text()"));
        }
        if (tiapp.getGuid().equals("")) {
            NodeList nl = doc.getDocumentElement().getElementsByTagName("guid");
            if (nl != null && nl.getLength() > 0) {
                Element guidElem = (Element) nl.item(0);
                tiapp.setGuid(guidElem.getTextContent());
            }
        }

        NodeList nl = doc.getDocumentElement().getElementsByTagName("id");
        if (nl != null && nl.getLength() > 0) {
            Element idElem = (Element) nl.item(0);
            tiapp.setProjectId(idElem.getTextContent());
        }
        return tiapp;
    }

    private String getFromXPath(Document doc, XPath xpath, String query) throws XPathExpressionException {
        return (String) xpath.evaluate(query, doc, XPathConstants.STRING);
    }

    private boolean getFromXPathAsBoolean(Document doc, XPath xpath, String query) throws XPathExpressionException {
        String result = getFromXPath(doc, xpath, query);
        if (result != null && result.compareToIgnoreCase("true") == 0) {
            return true;
        } else {
            return false;
        }
    }
}
