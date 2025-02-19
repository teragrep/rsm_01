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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaLognormImplTest {

    @Test
    public void versionTest() {
        String s = new JavaLognorm.LibraryInformation().liblognormVersionCheck();
        Assertions.assertEquals("2.0.6", s);
    }

    @Test
    public void hasAdvancedStatsTest() {
        boolean i = new JavaLognorm.LibraryInformation().liblognormHasAdvancedStats();
        assertFalse(i);
    }

    @Test
    public void normalizeTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LognormFactory lognormFactory = new LognormFactory(samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline"); // Throws exception if fails.
                Assertions.assertEquals("{ \"all\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void normalizeExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=tag1:Quantity: %N:number%"; // load rulebase that can cause exception with specific message normalization
            LognormFactory lognormFactory = new LognormFactory(samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                IllegalArgumentException e = Assertions
                        .assertThrows(IllegalArgumentException.class, () -> javaLognormImpl.normalize("unparseable"));
                Assertions
                        .assertEquals(
                                "ln_normalize() failed to perform extraction with error code: -1000", e.getMessage()
                        );
            }
        });
    }

    @Test
    public void closeTest() {
        assertDoesNotThrow(() -> {
            LognormFactory lognormFactory = new LognormFactory("rule=:%all:rest%");
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
            javaLognormImpl.close(); // Throws if ln_exitCtx doesn't return zero.
        });
    }

}
