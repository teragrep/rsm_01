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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class ErrorCallbackImplTest {

    @BeforeAll
    public static void log4jconfig() {
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2.properties");
        Configurator.reconfigure(log4j2Config.toUri());
    }

    @Test
    public void errorCallbackImplTest() {
        ErrorCallbackImpl callbackImpl = new ErrorCallbackImpl();
        Logger loggerForTarget = (Logger) LogManager.getLogger(ErrorCallbackImpl.class);
        String s = "Something happened";
        String expectedLogMessages = "liblognorm error: Something happened";

        final Appender appender = mock(Appender.class);
        when(appender.getName()).thenReturn("Mock appender");
        when(appender.isStarted()).thenReturn(true);
        final ArgumentCaptor<LogEvent> logCaptor = ArgumentCaptor.forClass(LogEvent.class);
        final Level effectiveLevel = loggerForTarget.getLevel(); // Save the initial logger state
        // Attach our test appender and make sure the messages will be logged
        loggerForTarget.addAppender(appender);
        loggerForTarget.setLevel(Level.DEBUG);
        try {
            // invoke callback
            callbackImpl.invoke(Pointer.NULL, s, s.length());
            // Assert that the expected log messages are seen
            verify(appender, times(1)).append(logCaptor.capture());
            Arrays.stream(new String[] {
                    expectedLogMessages
            }
            )
                    .forEach(
                            expectedLogMessage -> Assertions
                                    .assertEquals(
                                            logCaptor.getValue().getMessage().getFormattedMessage(), expectedLogMessage
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
