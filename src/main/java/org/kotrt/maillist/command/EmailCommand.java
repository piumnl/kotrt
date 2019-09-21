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
package org.kotrt.maillist.command;

import java.util.List;
import java.util.concurrent.Executors;

import javax.mail.internet.MimeMessage;

import org.kotrt.maillist.context.Context;
import org.kotrt.maillist.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件的读取和发送
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class EmailCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailCommand.class);

    @Override
    public void run(String[] args) throws Exception {
        Executors.newSingleThreadExecutor().execute(getTask());
    }

    private Runnable getTask() {
        return () -> {
            while (true) {
                LOGGER.info("开始收取邮件.");
                List<MimeMessage> emails = MailUtil.getEmails();
                LOGGER.info("新邮件条数:" + emails);
                if (emails != null) {
                    MailUtil.batchSend(emails, Context.getInstance().getUsers());
                }
                LOGGER.info("邮件收取结束.");
                try {
                    Thread.sleep(5 * 1000 * 60);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        };
    }
}
