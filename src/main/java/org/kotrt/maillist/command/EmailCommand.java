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

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.core.context.Context;
import org.kotrt.maillist.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
                if (emails != null) {
                    LOGGER.info("新邮件条数:" + emails.size());
                    emails = filterEmail(emails);
                    MailUtil.batchSend(emails, Context.getInstance().getUserDao().getAllUser());
                    // emails = filterEmail(emails);
//                    try {
//                    Context.getInstance().getMessager().sendMessage(emails);
//                    } catch (MessagingException e) {
//                        LOGGER.error(e.getMessage(), e);
//                    }
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

    private static List<MimeMessage> filterEmail(List<MimeMessage> emails) {
        return emails.stream()
                .filter(EmailCommand::isOtherEmailToFilterAndDoSomething)
                .filter(EmailCommand::isEmailFromUser).
                        collect(Collectors.toList());
    }

    private static boolean isOtherEmailToFilterAndDoSomething(MimeMessage mimeMessage) {
        try {
            String subject = mimeMessage.getSubject();
            if (subject.startsWith("[订阅邮件]") || subject.startsWith("[取消订阅邮件]")) {

                String registerOrNotUsername = MailUtil.getRegisterOrNotUsername(mimeMessage);
                String registerOrNotEmail = MailUtil.getRegisterOrNotEmail(mimeMessage);
                if (subject.startsWith("[订阅邮件]")) {
                    User user = new User(registerOrNotEmail);
                    user.setName(registerOrNotUsername);
                    LOGGER.info("正在给用户: " + user.getName() + " 订阅, 他的邮件是: " + user.getEmail());
                    Context.getInstance().getUserDao().addUser(user);
                    LOGGER.info("订阅成功");
                } else {
                    LOGGER.info("正在给用户: " + registerOrNotUsername + " 取消订阅, 他的邮件是: " + registerOrNotEmail);
                    Context.getInstance().getUserDao().deleteUser(registerOrNotEmail);
                    LOGGER.info("取消订阅成功");
                }

                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    private static boolean isEmailFromUser(MimeMessage mimeMessage) {
        try {
            Set<User> users = Context.getInstance().getUserDao().getAllUser();
            Address[] from = mimeMessage.getFrom();
            for (User user : users) {
                for (Address address : from) {
                    if (((InternetAddress) address).getAddress().equals(user.getEmail())) {
                        return true;
                    }
                }
            }
            LOGGER.info("邮件标题为: " + mimeMessage.getSubject() + " 被过滤, 邮件来源: " + ((InternetAddress) from[0]).getAddress());
            return false;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
