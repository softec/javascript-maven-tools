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

/**
 * Represent a Virtual Device configuration.
 * Used by {@link TitaniumPackageMojo} when executeMode is virtual
 * to retrieve the emulator settings.
 */
public class VirtualDevice {

    /**
     * <p>The version on which the virtual device should run.</p>
     * <p>If not specified, the latest android API version will be used.
     * Regardless of the global androidAPI value.</p>
     * @parameter expression="${virtualDevice.androidAPI}"
     */
    private String androidAPI;

    /**
     * <p>The ios version of the virtual device.</p>
     * <p>If not specified the latest available version will be used.
     * Regardless of the global parameter value</p>
     *
     * @parameter expression="${virtualDevice.iosVersion}"
     */
    private String iosVersion;

    /**
     * The skin of the android emulator.
     * Defaults to HVGA for version less than 10 and to WXGA for version greater than 10
     * @parameter expression="${virtualDevice.skin}"
     */
    private String skin;

    /**
     * The iOS device family.
     * valid values are iphone or ipad.
     * @parameter default-value="iphone" expression="${virtualDevice.family}"
     */
    private String family;

    /**
     * How much miliseconds to wait after launching emulator before
     * installing the android application.
     * @parameter default-value="30" expression="${virtualDevice.wait}"
     */
    private Long wait;

    /**
     * Retrieve the virtual device family for the iphone/ipad/universal platform.
     * @return The virtual device family.
     * If not set, "iphone" will be returned.
     */
    public String getFamily() {
        if (family == null) {
            family = "iphone";
        }
        return family;
    }

    /**
     * Set the virtual device family of the iphone emulator.
     * @param family The virtual device family.
     * <p>Accepted values are:</p>
     * <ul>
     *     <li>iphone</li>
     *     <li>ipad</li>
     * </ul>
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * Retrieve the skin of the android virtual device.
     * @return The skin of the android virtual device.
     */
    public String getSkin() {
        return skin;
    }

    /**
     * Set the skin of the android virtual device.
     * @param skin The skin of the android virtual device.
     */
    public void setSkin(String skin) {
        this.skin = skin;
    }

    /**
     * Retrieve the android API version.
     * @return The android API version.
     */
    public String getAndroidAPI() {
        return androidAPI;
    }

    /**
     * Set the android API version.
     * @param androidAPI The android API version.
     */
    public void setAndroidAPI(String androidAPI) {
        this.androidAPI = androidAPI;
    }

    /**
     * Retrieve the wait time between the android emulator and simulator launch.
     * @return The wait time.
     */
    public Long getWait() {
        return wait;
    }

    /**
     * Set the wait time before launching the simulator after having launched the
     * android simulator.
     * @param wait The wait time.
     */
    public void setWait(Long wait) {
        this.wait = wait;
    }

    /**
     * Retrieve the iOS version.
     * @return The iOS version.
     */
    public String getIosVersion() {
        return iosVersion;
    }

    /**
     * Set the iOS version.
     * @param iosVersion The iOS version.
     */
    public void setIosVersion(String iosVersion) {
        this.iosVersion = iosVersion;
    }
}
