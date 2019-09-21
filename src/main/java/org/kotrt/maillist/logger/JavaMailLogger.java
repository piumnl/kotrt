/**
 * Copyright 2019-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kotrt.maillist.logger;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class JavaMailLogger extends PrintStream {

    private String lineSeparator;

    private Logger actualLogger;

    private ByteArrayOutputStream bos;

    private Charset charset;

    public JavaMailLogger(Logger logger) {
        this(System.out, logger, StandardCharsets.UTF_8);
    }

    public JavaMailLogger(Logger logger, Charset charset) {
        this(System.out, logger, charset);
    }

    public JavaMailLogger(OutputStream out, Logger actualLogger) {
        this(out, actualLogger, StandardCharsets.UTF_8);
    }

    public JavaMailLogger(OutputStream out, Logger actualLogger, Charset charset) {
        // out not be null,
        super(out);

        this.actualLogger = actualLogger;
        this.charset = Optional.of(charset).orElse(StandardCharsets.UTF_8);
        this.bos = new ByteArrayOutputStream();
        // by BufferedWriter
        this.lineSeparator = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("line.separator"));
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        bos.write(buf, off, len);
        if (len > 0) {
            byte[] lineSeparators = lineSeparator.getBytes();
            int length = lineSeparators.length;
            for (int i = 0; i < lineSeparators.length; i++) {
                if (buf[len - i - 1] != lineSeparators[length - i - 1]) {
                    return;
                }
            }

            flush();
        }
    }

    @Override
    public void flush() {
        String msg = new String(bos.toByteArray(), charset);
        if (msg.endsWith(lineSeparator)) {
            msg = msg.substring(0, lineSeparator.length());
        }

        actualLogger.debug(msg);
        bos.reset();
    }

    @Override
    public void println(String x) {
        actualLogger.debug(x);
    }
}
