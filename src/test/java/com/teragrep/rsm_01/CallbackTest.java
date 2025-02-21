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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class CallbackTest {

    @Test
    public void setDebugCBTest() {
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2.properties");
        Configurator.reconfigure(log4j2Config.toUri());

        LognormFactory lognormFactory = new LognormFactory("rule=:%all:rest%");
        Logger loggerForTarget = (Logger) LogManager.getLogger(DebugCallbackImpl.class);
        String expectedLogMessages = "liblognorm: =======================================";

        final Appender appender = mock(Appender.class);
        when(appender.getName()).thenReturn("Mock appender");
        when(appender.isStarted()).thenReturn(true);
        final ArgumentCaptor<LogEvent> logCaptor = ArgumentCaptor.forClass(LogEvent.class);
        final Level effectiveLevel = loggerForTarget.getLevel(); // Save the initial logger state
        // Attach our test appender and make sure the messages will be logged
        loggerForTarget.addAppender(appender);
        loggerForTarget.setLevel(Level.DEBUG);
        try {
            // invoke debug callback via loadSamplesFromString()
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
            // Assert that the expected log messages are seen
            verify(appender, times(28)).append(logCaptor.capture());
            Arrays.stream(new String[] {
                    expectedLogMessages
            }
            )
                    .forEach(
                            expectedLogMessage -> Assertions
                                    .assertEquals(
                                            expectedLogMessage, logCaptor.getValue().getMessage().getFormattedMessage()
                                    )
                    );
            javaLognormImpl.close();
        }
        finally {
            // Restore logger state in case this affects other tests
            loggerForTarget.removeAppender(appender);
            loggerForTarget.setLevel(effectiveLevel);
        }
    }

    @Test
    public void setErrMsgCBTest() {
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2Error.properties");
        Configurator.reconfigure(log4j2Config.toUri());

        LognormFactory lognormFactory = new LognormFactory("invalidSample");
        Logger loggerForTarget = (Logger) LogManager.getLogger(ErrorCallbackImpl.class);
        String expectedLogMessages = "liblognorm error: rulebase file --NO-FILE--[0]: invalid record type detected: 'invalidSample'";

        final Appender appender = mock(Appender.class);
        when(appender.getName()).thenReturn("Mock appender");
        when(appender.isStarted()).thenReturn(true);
        final ArgumentCaptor<LogEvent> logCaptor = ArgumentCaptor.forClass(LogEvent.class);
        final Level effectiveLevel = loggerForTarget.getLevel(); // Save the initial logger state
        // Attach our test appender and make sure the messages will be logged
        loggerForTarget.addAppender(appender);
        loggerForTarget.setLevel(Level.ERROR);
        try {
            // invoke error callback via lognormFactory using invalid sample,
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, lognormFactory::lognorm);
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
            // Assert that the expected log messages are seen after the exception is thrown
            verify(appender, times(1)).append(logCaptor.capture());
            Arrays.stream(new String[] {
                    expectedLogMessages
            }
            )
                    .forEach(
                            expectedLogMessage -> Assertions
                                    .assertEquals(
                                            expectedLogMessage, logCaptor.getValue().getMessage().getFormattedMessage()
                                    )
                    );
        }
        finally {
            // Restore logger state in case this affects other tests
            loggerForTarget.removeAppender(appender);
            loggerForTarget.setLevel(effectiveLevel);
        }
    }

}
