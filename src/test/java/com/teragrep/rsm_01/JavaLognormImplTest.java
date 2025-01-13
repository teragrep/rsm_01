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

class JavaLognormImplTest {

    @Test
    public void versionTest() {
        String s = new JavaLognorm.Smart().liblognormVersionCheck();
        Assertions.assertEquals("2.0.6", s);
    }

    @Test
    public void ctxTest() {
        assertDoesNotThrow(() -> {
            Pointer ctx = new JavaLognorm.Smart().liblognormInitCtx();
            Assertions.assertNotNull(ctx);
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl(ctx);
            javaLognormImpl.liblognormExitCtx(); // Throws exception if fails to initialize
        });
    }

    @Test
    public void setCtxOptsTest() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = false;
            opts.CTXOPT_ADD_ORIGINALMSG = true;
            opts.CTXOPT_ADD_RULE = false;
            opts.CTXOPT_ADD_RULE_LOCATION = false;
            opts.CTXOPT_ALLOW_REGEX = false;
            javaLognormImpl.liblognormSetCtxOpts(opts);
            // Assert that original message is included in the result to see if opts are working
            String samplesString = "rule=:%all:rest%";
            javaLognormImpl.liblognormLoadSamplesFromString(samplesString); // Throws exception if fails to load samples
            String s = javaLognormImpl.liblognormNormalize("offline");
            Assertions.assertEquals("{ \"all\": \"offline\", \"originalmsg\": \"offline\" }", s);

            // cleanup
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void loadSamplesTest() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            String samplesPath = "src/test/resources/sample.rulebase";
            javaLognormImpl.liblognormLoadSamples(samplesPath); // Throws exception if fails to load samples
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void loadSamplesFromStringTest() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            javaLognormImpl.liblognormLoadSamplesFromString("rule=:%all:rest%"); // Throws exception if fails to load samples
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void hasAdvancedStatsTest() {
        boolean i = new JavaLognorm.Smart().liblognormHasAdvancedStats();
        assertFalse(i);
    }

    @Test
    public void normalizeTest() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = false;
            opts.CTXOPT_ADD_ORIGINALMSG = false;
            opts.CTXOPT_ADD_RULE = false;
            opts.CTXOPT_ADD_RULE_LOCATION = false;
            opts.CTXOPT_ALLOW_REGEX = false;
            javaLognormImpl.liblognormSetCtxOpts(opts);
            String samplesString = "rule=:%all:rest%";

            javaLognormImpl.liblognormLoadSamplesFromString(samplesString);
            String s = javaLognormImpl.liblognormNormalize("offline"); // Throws exception if fails.
            Assertions.assertEquals("{ \"all\": \"offline\" }", s);

            // cleanup
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void normalizeExceptionTest() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = false;
            opts.CTXOPT_ADD_ORIGINALMSG = false;
            opts.CTXOPT_ADD_RULE = false;
            opts.CTXOPT_ADD_RULE_LOCATION = false;
            opts.CTXOPT_ALLOW_REGEX = false;
            javaLognormImpl.liblognormSetCtxOpts(opts);
            String samplesString = "invalidRulebase"; // load rulebase that will cause exception

            javaLognormImpl.liblognormLoadSamplesFromString(samplesString);
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> javaLognormImpl.liblognormNormalize("offline"));
            Assertions
                    .assertEquals("ln_normalize() failed to perform extraction with error code: -1000", e.getMessage());

            // cleanup
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void setDebugCBTest() {
        JavaLognormImpl javaLognormImpl = new JavaLognormImpl();

        int a = javaLognormImpl.liblognormSetDebugCB();
        Assertions.assertEquals(0, a); // 0 if setting debug message handler was a success.

        // cleanup
        javaLognormImpl.liblognormExitCtx();
    }

    @Test
    public void setErrMsgCBTest() {
        JavaLognormImpl javaLognormImpl = new JavaLognormImpl();

        int a = javaLognormImpl.liblognormSetErrMsgCB();
        Assertions.assertEquals(0, a); // 0 if setting error message handler was a success.

        // cleanup
        javaLognormImpl.liblognormExitCtx();
    }

    @Test
    public void exitCtxTest() {
        JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
        int i = javaLognormImpl.liblognormExitCtx(); // Returns zero on success, something else otherwise.
        Assertions.assertEquals(0, i);
    }

}
