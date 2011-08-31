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

import java.io.File;
import java.util.UUID;

/**
 * Class representing the titanium configuration.
 */
public class Tiapp {

    /**
     * The project identifier.
     */
    private String projectId;

    /**
     * The name of the application.
     * Defaults to the name of the project.
     */
    private String name;

    /**
     * Version of the application
     */
    private String version;

    /**
     * Publisher of the application.
     */
    private String publisher;

    /**
     * The url of the application.
     */
    private String url;

    /**
     * The description of the application.
     */
    private String description;

    /**
     * The copyright of the application.
     */
    private String copyright;

    /**
     * The icon file to use.
     */
    private File icon;

    private boolean persistentWifi = false;
    private boolean prerenderedIcon = false;
    private String  statusBarStyle = "default";
    private boolean  statusBarHidden = false;
    private boolean  fullScreen = false;
    private boolean  navBarHidden = false;
    private boolean analytics = true;
    private String  guid = UUID.randomUUID().toString();

    /**
     * File containing the iphone configuration part of the tiapp.xml file.
     */
    private File iphoneConfiguration;

    /**
     * File containing the android configuration part of the tiapp.xml file.
     */
    private File androidConfiguration;

    /**
     * File containing the modules of the tiapp.xml file.
     */
    private File modules;

    public boolean isAnalytics() {
        return analytics;
    }

    public void setAnalytics(boolean analytics) {
        this.analytics = analytics;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public File getIcon() {
        return icon;
    }

    public void setIcon(File icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNavBarHidden() {
        return navBarHidden;
    }

    public void setNavBarHidden(boolean navBarHidden) {
        this.navBarHidden = navBarHidden;
    }

    public boolean isPersistentWifi() {
        return persistentWifi;
    }

    public void setPersistentWifi(boolean persistentWifi) {
        this.persistentWifi = persistentWifi;
    }

    public boolean isPrerenderedIcon() {
        return prerenderedIcon;
    }

    public void setPrerenderedIcon(boolean prerenderedIcon) {
        this.prerenderedIcon = prerenderedIcon;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public boolean isStatusBarHidden() {
        return statusBarHidden;
    }

    public void setStatusBarHidden(boolean statusBarHidden) {
        this.statusBarHidden = statusBarHidden;
    }

    public String getStatusBarStyle() {
        return statusBarStyle;
    }

    public void setStatusBarStyle(String statusBarStyle) {
        this.statusBarStyle = statusBarStyle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

