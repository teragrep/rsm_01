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

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LibJavaLognorm extends Library {

    public static final LibJavaLognorm jnaInstance = Native.load("JavaLognorm", LibJavaLognorm.class);

    public static interface DebugCallback extends Callback {

        public abstract void invoke(Pointer cookie, String msg, int length);

        public static class DebugCallbackImpl implements DebugCallback {

            private static final Logger LOGGER = LoggerFactory.getLogger(DebugCallbackImpl.class);

            @Override
            public void invoke(Pointer cookie, String msg, int length) {
                LOGGER.debug("liblognorm: {}", msg);
            }
        }
    }

    public static interface ErrorCallback extends Callback {

        public abstract void invoke(Pointer cookie, String msg, int length);

        public static class ErrorCallbackImpl implements ErrorCallback {

            private static final Logger LOGGER = LoggerFactory.getLogger(ErrorCallbackImpl.class);

            @Override
            public void invoke(Pointer cookie, String msg, int length) {
                LOGGER.error("liblognorm error: {}", msg);
            }
        }
    }

    // JNA requires the @FieldOrder annotation so it can properly serialize data into a memory buffer before using it as an argument to the target method.
    @FieldOrder({
            "CTXOPT_ADD_ORIGINALMSG", "CTXOPT_ADD_RULE", "CTXOPT_ADD_RULE_LOCATION"
    })
    public static class OptionsStruct extends Structure {

        public boolean CTXOPT_ADD_ORIGINALMSG = false;
        public boolean CTXOPT_ADD_RULE = false;
        public boolean CTXOPT_ADD_RULE_LOCATION = false;
    }

    @FieldOrder({
            "rv", "jref"
    })
    public static class NormalizedStruct extends Structure {

        public int rv;
        public Pointer jref;
    }

    /**
     * Returns the version of the currently used library.
     *
     * @return library version string.
     */
    public abstract String version();

    /**
     * Initializes the liblognorm context. exitCtx() must be called on the produced context when it is not needed
     * anymore.
     *
     * @return Pointer to the liblognorm context object.
     */
    public abstract Pointer initCtx();

    /**
     * Discard a library context, freeing the resources associated with the given library context.
     *
     * @param ctx Pointer to the liblognorm context object to discard.
     * @return 0 on success, something else otherwise.
     */
    public abstract int exitCtx(Pointer ctx);

    /**
     * Set options on library context.
     *
     * @param ctx  Pointer to the liblognorm context.
     * @param opts OptionsStruct object holding configuration for the options.
     */
    public abstract void setCtxOpts(Pointer ctx, OptionsStruct opts);

    /**
     * Return true if liblognorm is build with advanced statistics activated.
     *
     * @return true if advanced stats are active, false if not.
     */
    public abstract boolean hasAdvancedStats();

    /**
     * Load a (log) sample file.
     *
     * @param ctx      Pointer to the liblognorm context.
     * @param filename Name of file to be loaded.
     * @return 0 on success, something else otherwise.
     */
    public abstract int loadSamples(Pointer ctx, String filename);

    /**
     * Load a rulebase via a string.
     *
     * @param ctx    Pointer to the liblognorm context.
     * @param string The string with the actual rulebase.
     * @return 0 on success, something else otherwise.
     */
    public abstract int loadSamplesFromString(Pointer ctx, String string);

    /**
     * Normalize the given message string using the given liblognorm context. NormalizedStruct object is used to
     * transfer C-language struct containing the normalization result from C to java. NormalizedStruct contains an
     * integer that is used as a success/error indicator by the liblognorm library, and a C-language json object that
     * contains either the successfully normalized message or normalization error information.
     *
     * @param ctx  Pointer to the liblognorm context.
     * @param text The message string to normalize
     * @param norm NormalizedStruct object that can be used to return an integer and a Pointer from C to java.
     * @return NormalizedStruct object that contains an integer and a pointer to a json object.
     */
    public abstract NormalizedStruct normalize(Pointer ctx, String text, NormalizedStruct norm);

    /**
     * Reads the results of the normalization in C.
     *
     * @param jref Pointer to a C-language json object.
     * @return Json string.
     */
    public abstract String readResult(Pointer jref);

    /**
     * Releases the results of the normalization from memory in C.
     *
     * @param jref Pointer to a C-language json object.
     */
    public abstract void destroyResult(Pointer jref);

    /**
     * Set a callback for liblognorm debug logging
     *
     * @param ctx  Pointer to the liblognorm context.
     * @param func DebugCallback object
     * @return 0 on success, something else otherwise.
     */
    public abstract int setDebugCB(Pointer ctx, DebugCallback func);

    /**
     * Set a callback for liblognorm error logging.
     *
     * @param ctx  Pointer to the liblognorm context.
     * @param func ErrorCallback object
     * @return 0 on success, something else otherwise.
     */
    public abstract int setErrMsgCB(Pointer ctx, ErrorCallback func);

    /**
     * Return rulebase version currently used by the liblognorm context.
     *
     * @param ctx Pointer to the liblognorm context.
     * @return 1 or 2, depending on rulebase version. 0 if no rulebase is present.
     */
    public abstract int rulebaseVersion(Pointer ctx);
}
