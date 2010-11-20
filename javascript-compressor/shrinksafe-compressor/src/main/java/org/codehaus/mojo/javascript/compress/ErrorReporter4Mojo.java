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

/*
 * Derivative Work
 * Copyright SOFTEC sa. All rights reserved.
 *
 * Original work from yuicompressor-maven-plugin
 * Copyright David Bernard
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.codehaus.mojo.javascript.compress;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class ErrorReporter4Mojo implements ErrorReporter {

    private String defaultFilename_;
    private boolean acceptWarn_;
    private JSCompressorLogger log_;
    private int warningCnt_;
    private int errorCnt_;

    public ErrorReporter4Mojo(JSCompressorLogger log, boolean jswarn) {
        log_ = log;
        acceptWarn_ = jswarn;
    }

    public JSCompressorLogger getLogger()
    {
        return log_;
    }

    public void setDefaultFileName(String v) {
        if (v.length() == 0) {
            v = null;
        }
        defaultFilename_ = v;
    }

    public int getErrorCnt() {
        return errorCnt_;
    }

    public int getWarningCnt() {
        return warningCnt_;
    }

    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        String fullMessage = newMessage(message, sourceName, line, lineSource, lineOffset);
        log_.error(fullMessage);
        errorCnt_++;
    }

    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        throw new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }

    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        if( message.indexOf("is declared but is apparently never used.") != -1
            || message.indexOf("Try to use a single 'var' statement per scope.") != -1) return;
        
        if (acceptWarn_) {
            String fullMessage = newMessage(message, sourceName, line, lineSource, lineOffset);
            log_.warn(fullMessage);
            warningCnt_++;
        }
    }

    private String newMessage(String message, String sourceName, int line, String lineSource, int lineOffset) {
        StringBuilder back = new StringBuilder();
        if ((sourceName == null) || (sourceName.length() == 0)) {
            sourceName = defaultFilename_;
        }
        if (sourceName != null) {
            back.append(sourceName)
                .append(":line ")
                .append(line)
                .append(":column ")
                .append(lineOffset)
                .append(':')
                ;
        }
        if ((message != null) && (message.length() != 0)) {
            back.append(message);
        } else {
            back.append("unknown error");
        }
        if ((lineSource != null) && (lineSource.length() != 0)) {
            back.append("\n\t")
                .append(lineSource)
                ;
        }
        return back.toString();
    }

}
