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
assert new File( basedir, "target/titanium/android/tiapp.xml" ).exists();
assert new File( basedir, "target/titanium/android/Resources/test1.js" ).exists();
assert new File( basedir, "target/titanium/android/Resources/app.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/main.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/merged.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/utils.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/android/merged.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/android/test1.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/iphone/merged.js" ).exists();
assert ! new File( basedir, "target/titanium/android/Resources/iphone/test1.js" ).exists();
assert new File( basedir, "target/titanium/android-bin/TiTest.apk" ).exists();
return true;

