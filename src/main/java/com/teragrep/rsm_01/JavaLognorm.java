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

public class JavaLognorm {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaLognorm.class);

    JavaLognorm() {
    }

    public String liblognormVersionCheck() {
        return LibJavaLognorm.INSTANCE.version();
    }

    public Pointer liblognormInitCtx() {
        return LibJavaLognorm.INSTANCE.initCtx();
    }

    public void liblognormExitCtx(Pointer ctx) {
        LibJavaLognorm.INSTANCE.exitCtx(ctx);
    }

    public void liblognormSetCtxOpts(Pointer ctx, LibJavaLognorm.OptionsStruct opts) {
        LibJavaLognorm.INSTANCE.setCtxOpts(ctx, opts);
    }

    public int liblognormLoadSamples(Pointer ctx, String samples) {
        return LibJavaLognorm.INSTANCE.loadSamples(ctx, samples);
    }

    public int liblognormLoadSamplesFromString(Pointer ctx, String samples) {
        return LibJavaLognorm.INSTANCE.loadSamplesFromString(ctx, samples);
    }

    public int liblognormHasAdvancedStats() {
        return LibJavaLognorm.INSTANCE.hasAdvancedStats();
    }

    public Pointer liblognormNormalize(Pointer ctx, String text) {
        if (ctx != Pointer.NULL) {
            Pointer jref = LibJavaLognorm.INSTANCE.normalize(ctx, text);
            if (jref == Pointer.NULL) {
                throw new NullPointerException("LogNorm() failed to perform extraction.");
            }
            return jref;
        }
        else {
            throw new IllegalArgumentException("LogNorm() not initialized.");
        }
    }

    public String liblognormReadResult(Pointer ctx, Pointer jref) {
        if (ctx != Pointer.NULL) {
            if (jref == Pointer.NULL) {
                throw new NullPointerException("LogNorm() failed to perform extraction.");
            }
            String cstring = LibJavaLognorm.INSTANCE.readResult(jref);
            String javaString = String.copyValueOf(cstring.toCharArray(), 0, cstring.length());
            return javaString;
        }
        else {
            throw new IllegalArgumentException("LogNorm() not initialized.");
        }
    }

    public void liblognormDestroyResult(Pointer jref) {
        LibJavaLognorm.INSTANCE.destroyResult(jref);
    }

    public void liblognormEnableDebug(Pointer ctx, int i) {
        LibJavaLognorm.INSTANCE.enableDebug(ctx, i);
    }

    public int liblognormSetDebugCB(Pointer ctx) {
        LibJavaLognorm.DebugCallback.DebugCallbackImpl callbackImpl = new LibJavaLognorm.DebugCallback.DebugCallbackImpl();
        return LibJavaLognorm.INSTANCE.setDebugCB(ctx, callbackImpl);
    }

    public int liblognormSetErrMsgCB(Pointer ctx) {
        LibJavaLognorm.ErrorCallback.ErrorCallbackImpl callbackImpl = new LibJavaLognorm.ErrorCallback.ErrorCallbackImpl();
        return LibJavaLognorm.INSTANCE.setErrMsgCB(ctx, callbackImpl);
    }

}
