/*
 * Teragrep Record Schema Mapper Library for Java RSM-01
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

    /**
     * Discards the library context, freeing the resources associated with the given library context.
     */
    private void liblognormExitCtx() {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException("ctx not initialized. Use LogNormFactory to initialize the ctx.");
        }

        int i = LibJavaLognorm.jnaInstance.exitCtx(ctx);
        if (i != 0) {
            LOGGER.error("ln_exitCtx() returned error code <{}>", i);
            throw new IllegalArgumentException("ln_exitCtx() returned " + i + " instead of 0");
        }
    }

    @Override
    public String normalize(String text) {
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
            throw new IllegalArgumentException("ctx not initialized. Use LogNormFactory to initialize the ctx.");
        }
    }

    /**
     * Reads the results of the normalization in C and converts it to a json string.
     *
     * @param jref Pointer to a C-language json object.
     * @return Json string.
     */
    private String liblognormReadResult(Pointer jref) {
        if (ctx == Pointer.NULL) {
            throw new IllegalArgumentException("ctx not initialized. Use LogNormFactory to initialize the ctx.");
        }

        String cstring = LibJavaLognorm.jnaInstance.readResult(jref);
        String javaString = String.copyValueOf(cstring.toCharArray(), 0, cstring.length());
        liblognormDestroyResult(jref);
        return javaString;
    }

    /**
     * Releases the results of the normalization from memory in C.
     *
     * @param jref Pointer to a C-language json object.
     */
    private void liblognormDestroyResult(Pointer jref) {
        LibJavaLognorm.jnaInstance.destroyResult(jref);
    }

    /**
     * Calls liblognormExitCtx() to free the resources in C.
     *
     * @throws IllegalArgumentException Throws if closing fails.
     */
    @Override
    public void close() throws IllegalArgumentException {
        liblognormExitCtx();
    }
}
