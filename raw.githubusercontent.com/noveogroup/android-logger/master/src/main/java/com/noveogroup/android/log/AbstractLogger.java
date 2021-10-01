/*
 * Copyright (c) 2013 Noveo Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Except as contained in this notice, the name(s) of the above copyright holders
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.noveogroup.android.log;

/**
 * Abstract implementation of {@link Logger} interface.
 */
public abstract class AbstractLogger implements Logger {

    private final String name;

    /**
     * Constructor of {@link AbstractLogger}.
     *
     * @param name the name of the logger.
     */
    public AbstractLogger(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isVerboseEnabled() {
        return isEnabled(Level.VERBOSE);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }

    @Override
    public boolean isAssertEnabled() {
        return isEnabled(Level.ASSERT);
    }

    @Override
    public void v(String message, Throwable throwable) {
        print(Logger.Level.VERBOSE, throwable, message);
    }

    @Override
    public void d(String message, Throwable throwable) {
        print(Logger.Level.DEBUG, throwable, message);
    }

    @Override
    public void i(String message, Throwable throwable) {
        print(Logger.Level.INFO, throwable, message);
    }

    @Override
    public void w(String message, Throwable throwable) {
        print(Logger.Level.WARN, throwable, message);
    }

    @Override
    public void e(String message, Throwable throwable) {
        print(Logger.Level.ERROR, throwable, message);
    }

    @Override
    public void a(String message, Throwable throwable) {
        print(Logger.Level.ASSERT, throwable, message);
    }

    @Override
    public void v(Throwable throwable) {
        print(Logger.Level.VERBOSE, throwable, null);
    }

    @Override
    public void d(Throwable throwable) {
        print(Logger.Level.DEBUG, throwable, null);
    }

    @Override
    public void i(Throwable throwable) {
        print(Logger.Level.INFO, throwable, null);
    }

    @Override
    public void w(Throwable throwable) {
        print(Logger.Level.WARN, throwable, null);
    }

    @Override
    public void e(Throwable throwable) {
        print(Logger.Level.ERROR, throwable, null);
    }

    @Override
    public void a(Throwable throwable) {
        print(Logger.Level.ASSERT, throwable, null);
    }

    @Override
    public void v(Throwable throwable, String messageFormat, Object... args) {
        print(Logger.Level.VERBOSE, throwable, messageFormat, args);
    }

    @Override
    public void d(Throwable throwable, String messageFormat, Object... args) {
        print(Logger.Level.DEBUG, throwable, messageFormat, args);
    }

    @Override
    public void i(Throwable throwable, String messageFormat, Object... args) {
        print(Logger.Level.INFO, throwable, messageFormat, args);
    }

    @Override
    public void w(Throwable throwable, String messageFormat, Object... args) {
        print(Logger.Level.WARN, throwable, messageFormat, args);
    }

    @Override
    public void e(Throwable throwable, String messageFormat, Object... args) {
        print(Logger.Level.ERROR, throwable, messageFormat, args);
    }

    @Override
    public void a(Throwable throwable, String messageFormat, Object... args) {
        print(Logger.Level.ASSERT, throwable, messageFormat, args);
    }

    @Override
    public void v(Throwable throwable, String message) {
        print(Level.VERBOSE, throwable, message);
    }

    @Override
    public void d(Throwable throwable, String message) {
        print(Level.DEBUG, throwable, message);
    }

    @Override
    public void i(Throwable throwable, String message) {
        print(Level.INFO, throwable, message);
    }

    @Override
    public void w(Throwable throwable, String message) {
        print(Level.WARN, throwable, message);
    }

    @Override
    public void e(Throwable throwable, String message) {
        print(Level.ERROR, throwable, message);
    }

    @Override
    public void a(Throwable throwable, String message) {
        print(Level.ASSERT, throwable, message);
    }

    @Override
    public void v(String messageFormat, Object... args) {
        print(Logger.Level.VERBOSE, null, messageFormat, args);
    }

    @Override
    public void d(String messageFormat, Object... args) {
        print(Logger.Level.DEBUG, null, messageFormat, args);
    }

    @Override
    public void i(String messageFormat, Object... args) {
        print(Logger.Level.INFO, null, messageFormat, args);
    }

    @Override
    public void w(String messageFormat, Object... args) {
        print(Logger.Level.WARN, null, messageFormat, args);
    }

    @Override
    public void e(String messageFormat, Object... args) {
        print(Logger.Level.ERROR, null, messageFormat, args);
    }

    @Override
    public void a(String messageFormat, Object... args) {
        print(Logger.Level.ASSERT, null, messageFormat, args);
    }

    @Override
    public void v(String message) {
        print(Level.VERBOSE, null, message);
    }

    @Override
    public void d(String message) {
        print(Level.DEBUG, null, message);
    }

    @Override
    public void i(String message) {
        print(Level.INFO, null, message);
    }

    @Override
    public void w(String message) {
        print(Level.WARN, null, message);
    }

    @Override
    public void e(String message) {
        print(Level.ERROR, null, message);
    }

    @Override
    public void a(String message) {
        print(Level.ASSERT, null, message);
    }

}
