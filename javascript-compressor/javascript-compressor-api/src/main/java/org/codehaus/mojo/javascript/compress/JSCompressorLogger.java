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

package org.codehaus.mojo.javascript.compress;

/**
 * Generic Logger interface for compressors 
 */
public interface JSCompressorLogger
{
    void debug(CharSequence charSequence, Throwable throwable);

    void debug(CharSequence charSequence);

    void debug(Throwable throwable);

    boolean isDebugEnabled();

    boolean isInfoEnabled();

    void info(CharSequence charSequence);

    void info(CharSequence charSequence, Throwable throwable);

    void info(Throwable throwable);

    boolean isWarnEnabled();

    void warn(CharSequence charSequence);

    void warn(CharSequence charSequence, Throwable throwable);

    void warn(Throwable throwable);

    boolean isErrorEnabled();

    void error(CharSequence charSequence);

    void error(CharSequence charSequence, Throwable throwable);

    void error(Throwable throwable);
}
