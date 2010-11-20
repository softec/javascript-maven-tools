/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.codehaus.mojo.javascript;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.javascript.compress.JSCompressorLogger;

/**
 * Created by IntelliJ IDEA. User: DenisG Date: Nov 20, 2010 Time: 12:47:36 PM To change this template use File |
 * Settings | File Templates.
 */
public class MojoJSCompressorLogger implements JSCompressorLogger
{
    private Log log;

    MojoJSCompressorLogger(Log log) {
        this.log = log;
    }

    public void debug(CharSequence charSequence, Throwable throwable)
    {
        log.debug(charSequence, throwable);
    }

    public void debug(CharSequence charSequence)
    {
        log.debug(charSequence);
    }

    public void debug(Throwable throwable)
    {
        log.debug(throwable);
    }

    public boolean isDebugEnabled()
    {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled()
    {
        return log.isInfoEnabled();
    }

    public void info(CharSequence charSequence)
    {
        log.info(charSequence);
    }

    public void info(CharSequence charSequence, Throwable throwable)
    {
        log.info(charSequence, throwable);
    }

    public void info(Throwable throwable)
    {
        log.info(throwable);
    }

    public boolean isWarnEnabled()
    {
        return log.isWarnEnabled();
    }

    public void warn(CharSequence charSequence)
    {
        log.warn(charSequence);
    }

    public void warn(CharSequence charSequence, Throwable throwable)
    {
        log.warn(charSequence, throwable);
    }

    public void warn(Throwable throwable)
    {
        log.warn(throwable);
    }

    public boolean isErrorEnabled()
    {
        return log.isErrorEnabled();
    }

    public void error(CharSequence charSequence)
    {
        log.error(charSequence);
    }

    public void error(CharSequence charSequence, Throwable throwable)
    {
        log.error(charSequence,throwable);
    }

    public void error(Throwable throwable)
    {
        log.error(throwable);
    }
}
