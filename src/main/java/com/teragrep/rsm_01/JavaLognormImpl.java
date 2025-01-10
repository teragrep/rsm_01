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

public final class JavaLognormImpl implements JavaLognorm {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaLognormImpl.class);

    private final Pointer ctx;

    public JavaLognormImpl() {
        this(new JavaLognorm.Smart().liblognormInitCtx());
    }

    public JavaLognormImpl(Pointer ctx) {
        this.ctx = ctx;
    }

    public int liblognormExitCtx() {
        if (ctx != Pointer.NULL) {
            return LibJavaLognorm.jnaInstance.exitCtx(ctx);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    public void liblognormSetCtxOpts(LibJavaLognorm.OptionsStruct opts) {
        if (ctx != Pointer.NULL) {
            LibJavaLognorm.jnaInstance.setCtxOpts(ctx, opts);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    public int liblognormLoadSamples(String samples) {
        if (ctx != Pointer.NULL) {
            return LibJavaLognorm.jnaInstance.loadSamples(ctx, samples);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    public int liblognormLoadSamplesFromString(String samples) {
        if (ctx != Pointer.NULL) {
            return LibJavaLognorm.jnaInstance.loadSamplesFromString(ctx, samples);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    /* If an error is detected by the library, the method returns an error code and generated jref containing further error details in normalized form.
     Otherwise returns 0 and the message in normalized form.*/
    public Pointer liblognormNormalize(String text) {
        if (ctx != Pointer.NULL) {
            LibJavaLognorm.NormalizedStruct norm = new LibJavaLognorm.NormalizedStruct();
            LibJavaLognorm.NormalizedStruct result = LibJavaLognorm.jnaInstance.normalize(ctx, text, norm);
            if (result.rv != 0) {
                // error occurred
                LOGGER
                        .error(
                                "LogNorm() failed to perform extraction with error code <{}>. Generated error information: <{}>",
                                result.rv, liblognormReadResult(result.jref)
                        );
                liblognormDestroyResult(result.jref); // cleanup
                throw new IllegalArgumentException(
                        "LogNorm() failed to perform extraction with error code: " + result.rv
                );
            }
            return result.jref;
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    public String liblognormReadResult(Pointer jref) {
        if (ctx != Pointer.NULL) {
            if (jref == Pointer.NULL) {
                throw new NullPointerException("LogNorm() failed to perform extraction.");
            }
            String cstring = LibJavaLognorm.jnaInstance.readResult(jref);
            String javaString = String.copyValueOf(cstring.toCharArray(), 0, cstring.length());
            return javaString;
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    public void liblognormDestroyResult(Pointer jref) {
        LibJavaLognorm.jnaInstance.destroyResult(jref);
    }

    public int liblognormSetDebugCB() {
        if (ctx != Pointer.NULL) {
            LibJavaLognorm.DebugCallback.DebugCallbackImpl callbackImpl = new LibJavaLognorm.DebugCallback.DebugCallbackImpl();
            return LibJavaLognorm.jnaInstance.setDebugCB(ctx, callbackImpl);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

    public int liblognormSetErrMsgCB() {
        if (ctx != Pointer.NULL) {
            LibJavaLognorm.ErrorCallback.ErrorCallbackImpl callbackImpl = new LibJavaLognorm.ErrorCallback.ErrorCallbackImpl();
            return LibJavaLognorm.jnaInstance.setErrMsgCB(ctx, callbackImpl);
        }
        else {
            throw new IllegalArgumentException(
                    "LogNorm() not initialized. Use liblognormInitCtx() to initialize the ctx."
            );
        }
    }

}
