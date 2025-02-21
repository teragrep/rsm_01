/*
 * Teragrep Record Schema Mapper Library for Java (rsm_01)
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

import java.io.File;

public final class LognormFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LognormFactory.class);

    private final LibJavaLognorm.OptionsStruct options;
    private final String samples;
    private final boolean fileUsed;
    private final ErrorCallbackImpl errorCallbackImpl;

    public LognormFactory(final String samples) {
        this(new LibJavaLognorm.OptionsStruct(), samples, false);
    }

    public LognormFactory(final LibJavaLognorm.OptionsStruct options, final String samples) {
        this(options, samples, false);
    }

    public LognormFactory(final File samplesFile) {
        this(new LibJavaLognorm.OptionsStruct(), samplesFile.getAbsolutePath(), true);
    }

    public LognormFactory(final LibJavaLognorm.OptionsStruct options, final File samplesFile) {
        this(options, samplesFile.getAbsolutePath(), true);
    }

    public LognormFactory(final LibJavaLognorm.OptionsStruct options, final String samples, final boolean fileUsed) {
        this.options = options;
        this.samples = samples;
        this.fileUsed = fileUsed;
        this.errorCallbackImpl = new ErrorCallbackImpl();
    }

    /**
     * Create a configured version of JavaLognormImpl.
     *
     * @return Configured version of JavaLognormImpl.
     */
    public JavaLognormImpl lognorm() {
        final Pointer ctx = LibJavaLognorm.jnaInstance.initCtx();
        // Do java exception handling that can't be done in C.
        if (ctx == Pointer.NULL) {
            throw new NullPointerException(
                    "ln_initCtx() returned a null pointer, liblognorm failed to initialize the context."
            );
        }
        // Enable error logging for liblognorm ctx. Mandatory for proper exception handling in java.
        liblognormSetErrMsgCB(ctx, errorCallbackImpl);
        // Enable debug logging
        if (LOGGER.isDebugEnabled()) {
            liblognormSetDebugCB(ctx);
        }
        // Load options
        LibJavaLognorm.jnaInstance.setCtxOpts(ctx, options);
        // Load samples
        if (fileUsed) {
            liblognormLoadSamples(ctx, samples);
        }
        else {
            liblognormLoadSamplesFromString(ctx, samples);
        }
        return new JavaLognormImpl(ctx, errorCallbackImpl);
    }

    /**
     * Load a rulebase via a string to the library context.
     *
     * @param ctx      Pointer to the liblognorm context.
     * @param rulebase The string with the actual rulebase.
     */
    private void liblognormLoadSamplesFromString(Pointer ctx, String rulebase) {
        int i = LibJavaLognorm.jnaInstance.loadSamplesFromString(ctx, rulebase);
        if (i != 0) {
            LOGGER.error("ln_loadSamplesFromString() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_loadSamplesFromString() returned " + i + " instead of 0");
        }
        errorCallbackImpl.throwOccurredErrors();
    }

    /**
     * Load a (log) sample file to the library context.
     *
     * @param ctx          Pointer to the liblognorm context.
     * @param rulebaseFile Name of file to be loaded.
     */
    private void liblognormLoadSamples(Pointer ctx, String rulebaseFile) {
        int i = LibJavaLognorm.jnaInstance.loadSamples(ctx, rulebaseFile);
        if (i != 0) {
            LOGGER.error("ln_loadSamples() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_loadSamples() returned " + i + " instead of 0");
        }
        // Check rulebase version.
        if (LibJavaLognorm.jnaInstance.rulebaseVersion(ctx) != 2) {
            throw new IllegalArgumentException("Loaded rulebase is not using version 2");
        }
        errorCallbackImpl.throwOccurredErrors();
    }

    /**
     * Set a callback for liblognorm debug message logging.
     *
     * @param ctx Pointer to the liblognorm context.
     */
    private void liblognormSetDebugCB(Pointer ctx) {
        DebugCallbackImpl callbackImpl = new DebugCallbackImpl();
        int i = LibJavaLognorm.jnaInstance.setDebugCB(ctx, callbackImpl);
        if (i != 0) {
            LOGGER.error("ln_setDebugCB() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_setDebugCB() returned " + i + " instead of 0");
        }
    }

    /**
     * Set a callback for liblognorm error message logging.
     *
     * @param ctx          Pointer to the liblognorm context.
     * @param callbackImpl Error callback implementation for liblognorm to use.
     */
    private void liblognormSetErrMsgCB(Pointer ctx, ErrorCallbackImpl callbackImpl) {
        int i = LibJavaLognorm.jnaInstance.setErrMsgCB(ctx, callbackImpl);
        if (i != 0) {
            LOGGER.error("ln_setErrMsgCB() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_setErrMsgCB() returned " + i + " instead of 0");
        }
    }

}
