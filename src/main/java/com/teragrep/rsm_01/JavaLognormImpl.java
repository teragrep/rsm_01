/*
 * Record Schema Mapping Library for Java RSM-01
 * Copyright (C) 2021-2024 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.rsm_01;

import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JavaLognormImpl implements JavaLognorm, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaLognormImpl.class);

    private final Pointer ctx;

    public JavaLognormImpl(Pointer ctx) {
        this.ctx = ctx;
    }

    private void liblognormExitCtx() {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }

        int i = LibJavaLognorm.jnaInstance.exitCtx(ctx);
        if (i != 0) {
            LOGGER.error("ln_exitCtx() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_exitCtx() returned " + i + " instead of 0");
        }
    }

    public void liblognormLoadSamples(String samples) {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }

        int i = LibJavaLognorm.jnaInstance.loadSamples(ctx, samples);
        if (i != 0) {
            LOGGER.error("ln_loadSamples() returned error code <{}>", i);
            throw new IllegalArgumentException("Load samples returned " + i + " instead of 0");
        }
    }

    public void liblognormLoadSamplesFromString(String samples) {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }

        int i = LibJavaLognorm.jnaInstance.loadSamplesFromString(ctx, samples);
        if (i != 0) {
            LOGGER.error("ln_loadSamplesFromString() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_loadSamplesFromString() returned " + i + " instead of 0");
        }
    }

    /* If an error is detected by the library, the method returns an error code and generated jref containing further error details in normalized form.
     Otherwise, returns 0 and the message in normalized form.*/
    public String liblognormNormalize(String text) {
        if (ctx != Pointer.NULL) {
            LibJavaLognorm.NormalizedStruct norm = new LibJavaLognorm.NormalizedStruct();
            LibJavaLognorm.NormalizedStruct result = LibJavaLognorm.jnaInstance.normalize(ctx, text, norm);
            if (result.rv != 0) {
                // error occurred
                LOGGER
                        .error(
                                "ln_normalize() failed to perform extraction with error code <{}>. Generated error information: <{}>",
                                result.rv, liblognormReadResult(result.jref)
                        );
                throw new IllegalArgumentException(
                        "ln_normalize() failed to perform extraction with error code: " + result.rv
                );
            }
            return liblognormReadResult(result.jref);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    private String liblognormReadResult(Pointer jref) {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }

        String cstring = LibJavaLognorm.jnaInstance.readResult(jref);
        String javaString = String.copyValueOf(cstring.toCharArray(), 0, cstring.length());
        liblognormDestroyResult(jref);
        return javaString;
    }

    private void liblognormDestroyResult(Pointer jref) {
        LibJavaLognorm.jnaInstance.destroyResult(jref);
    }

    public void liblognormSetDebugCB() {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }

        LibJavaLognorm.DebugCallback.DebugCallbackImpl callbackImpl = new LibJavaLognorm.DebugCallback.DebugCallbackImpl();
        int i = LibJavaLognorm.jnaInstance.setDebugCB(ctx, callbackImpl);
        if (i != 0) {
            LOGGER.error("ln_setDebugCB() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_setDebugCB() returned " + i + " instead of 0");
        }
    }

    public void liblognormSetErrMsgCB() {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }

        LibJavaLognorm.ErrorCallback.ErrorCallbackImpl callbackImpl = new LibJavaLognorm.ErrorCallback.ErrorCallbackImpl();
        int i = LibJavaLognorm.jnaInstance.setErrMsgCB(ctx, callbackImpl);
        if (i != 0) {
            LOGGER.error("ln_setErrMsgCB() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_setErrMsgCB() returned " + i + " instead of 0");
        }
    }

    @Override
    public void close() throws IllegalArgumentException {
        liblognormExitCtx();
    }
}
