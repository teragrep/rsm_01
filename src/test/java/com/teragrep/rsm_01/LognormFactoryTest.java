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

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LognormFactoryTest {

    @BeforeAll
    public static void log4jconfig() {
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2Error.properties");
        Configurator.reconfigure(log4j2Config.toUri());
    }

    @Test
    public void setCtxOptsMsgTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_ORIGINALMSG = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                // Assert that original message is included in the result to see if opts are working
                String s = javaLognormImpl.normalize("offline");
                Assertions.assertEquals("{ \"all\": \"offline\", \"originalmsg\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void setCtxOptsRuleTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_RULE = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                // Assert that rule mockup is included in the result to see if opts are working
                String s = javaLognormImpl.normalize("offline");
                Assertions
                        .assertEquals(
                                "{ \"all\": \"offline\", \"metadata\": { \"rule\": { \"mockup\": \"%all:rest%\" } } }",
                                s
                        );
            }
        });
    }

    @Test
    public void setCtxOptsLocationTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_RULE_LOCATION = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                // Assert that rule location is included in the result to see if opts are working
                String s = javaLognormImpl.normalize("offline");
                Assertions
                        .assertEquals(
                                "{ \"all\": \"offline\", \"metadata\": { \"rule\": { \"location\": { \"file\": \"--NO-FILE--\", \"line\": 0 } } } }",
                                s
                        );
            }
        });
    }

    @Test
    public void defaultCtxOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct(); // All opts disabled by default
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                // Assert that original message is included in the result to see if opts are working
                String s = javaLognormImpl.normalize("offline");
                // Assert that the originalmsg is not added to the result
                Assertions.assertEquals("{ \"all\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void loadSamplesTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebase";
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesJsonTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/json.rulebase"; // rulebase in pure json format with v2 engine tag
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesJsonLiteralTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/jsonLiteral.rulebase"; // rulebase in json using literal format with v2 engine tag
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesWithOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebase";
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebas"; // invalid path
            File sampleFile = new File(samplesPath);
            Assertions.assertFalse(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("ln_loadSamples() returned 1 instead of 0", e.getMessage());
        });
    }

    @Test
    public void loadInvalidSamplesExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/invalid.rulebase"; // invalid rulebase
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

    @Test
    public void loadSamplesV1ExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sampleV1.rulebase"; // rulebase using V1 format.
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("Loaded rulebase is not using version 2", e.getMessage());
        });
    }

    @Test
    public void loadSamplesFromStringTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "rule=:%all:rest%");
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesFromStringWithOptsTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "rule=:%all:rest%");
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesFromStringJsonTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            String testingRulebase = "rule=:%{\"type\":\"date-rfc3164\", \"name\":\"date\"}" + "      % %"
                    + "       {\"type\":\"char-to\", \"name\":\"host\", \"extradata\":\":\"}"
                    + "      % no longer listening on %" + "        {\"type\":\"ipv4\", \"name\":\"ip\"}" + "      %#%"
                    + "        {\"type\":\"number\", \"name\":\"port\"}" + "      %";
            LognormFactory lognormFactory = new LognormFactory(opts, testingRulebase);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesFromStringExceptionTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "invalidRulebase");
            // ln_loadSamplesFromString() doesn't return an error code but the liblognorm error callback logs an error during rulebase loading triggering an exception.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, lognormFactory::lognorm);
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

    @Test
    public void loadSamplesFromStringMultipleExceptionTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "invalidRulebase\nmoreInvalidRules");
            // ln_loadSamplesFromString() doesn't return an error code but the liblognorm error callback logs 2 errors during rulebase loading triggering an exception.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, lognormFactory::lognorm);
            Assertions.assertEquals("<2> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

}
