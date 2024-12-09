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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaLognormTest {

    @Test
    public void versionTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        String s = javaLognorm.liblognormVersionCheck();
        Assertions.assertEquals("2.0.6", s);
    }

    @Test
    public void ctxTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        Pointer ctx = javaLognorm.liblognormInitCtx();
        Assertions.assertNotNull(ctx);
        javaLognorm.liblognormExitCtx(ctx);
    }

    @Test
    public void setCtxOptsTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        Pointer ctx = javaLognorm.liblognormInitCtx();
        Assertions.assertNotNull(ctx);
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        opts.CTXOPT_ADD_EXEC_PATH = false;
        opts.CTXOPT_ADD_ORIGINALMSG = false;
        opts.CTXOPT_ADD_RULE = false;
        opts.CTXOPT_ADD_RULE_LOCATION = false;
        opts.CTXOPT_ALLOW_REGEX = false;
        javaLognorm.liblognormSetCtxOpts(ctx, opts);
        Assertions.assertNotNull(ctx);
        javaLognorm.liblognormExitCtx(ctx);
    }

    @Test
    public void loadSamplesTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        Pointer ctx = javaLognorm.liblognormInitCtx();
        Assertions.assertNotNull(ctx);
        String samplesPath = "src/test/resources/sample.rulebase";
        int i = javaLognorm.liblognormLoadSamples(ctx, samplesPath);
        assertEquals(0, i);
        javaLognorm.liblognormExitCtx(ctx);
    }

    @Test
    public void loadSamplesFromStringTest() {
        assertDoesNotThrow(() -> {
            JavaLognorm javaLognorm = new JavaLognorm();
            Pointer ctx = javaLognorm.liblognormInitCtx();
            Assertions.assertNotNull(ctx);
            int i = javaLognorm.liblognormLoadSamplesFromString(ctx, "rule=:%all:rest%");
            assertEquals(0, i);
            javaLognorm.liblognormExitCtx(ctx);
        });
    }

    @Test
    public void hasAdvancedStatsTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        int i = javaLognorm.liblognormHasAdvancedStats();
        assertEquals(0, i);
    }

    @Test
    public void normalizeTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        Pointer ctx = javaLognorm.liblognormInitCtx();
        Assertions.assertNotNull(ctx);
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        opts.CTXOPT_ADD_EXEC_PATH = false;
        opts.CTXOPT_ADD_ORIGINALMSG = false;
        opts.CTXOPT_ADD_RULE = false;
        opts.CTXOPT_ADD_RULE_LOCATION = false;
        opts.CTXOPT_ALLOW_REGEX = false;
        javaLognorm.liblognormSetCtxOpts(ctx, opts);
        Assertions.assertNotNull(ctx);
        String samplesString = "rule=:%all:rest%";

        int i = javaLognorm.liblognormLoadSamplesFromString(ctx, samplesString);
        assertEquals(0, i); // 0 means successful normalization, anything else means an error happened.
        Pointer jref = javaLognorm.liblognormNormalize(ctx, "offline");

        // cleanup
        javaLognorm.liblognormDestroyResult(jref);
        javaLognorm.liblognormExitCtx(ctx);
    }

    @Test
    public void readResultTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        Pointer ctx = javaLognorm.liblognormInitCtx();
        Assertions.assertNotNull(ctx);
        String samplesString = "rule=:%all:rest%";
        int i = javaLognorm.liblognormLoadSamplesFromString(ctx, samplesString);
        assertEquals(0, i);
        Pointer jref = javaLognorm.liblognormNormalize(ctx, "offline");

        String s = javaLognorm.liblognormReadResult(ctx, jref);
        Assertions.assertEquals("{ \"all\": \"offline\" }", s);

        // cleanup
        javaLognorm.liblognormDestroyResult(jref);
        javaLognorm.liblognormExitCtx(ctx);
    }

    @Test
    public void destroyResultTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        Pointer ctx = javaLognorm.liblognormInitCtx();
        Assertions.assertNotNull(ctx);
        String samplesString = "rule=:%all:rest%";
        int i = javaLognorm.liblognormLoadSamplesFromString(ctx, samplesString);
        assertEquals(0, i);
        Pointer jref = javaLognorm.liblognormNormalize(ctx, "offline");

        javaLognorm.liblognormDestroyResult(jref);
        Assertions.assertNotNull(jref);

        // cleanup
        javaLognorm.liblognormExitCtx(ctx);
    }

}
