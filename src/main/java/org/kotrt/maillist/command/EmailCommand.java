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
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.core.Messager;
import org.kotrt.maillist.core.context.Context;
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

    private static final String SUBSCRIBE_EMAIL = "[订阅邮件]";

    private static final String UNSUBSCRIBE_EMAIL = "[取消订阅]";

    @SuppressWarnings("AlibabaThreadPoolCreation")
    @Override
    public void run(String[] args) throws Exception {
        Executors.newSingleThreadScheduledExecutor()
                 .scheduleAtFixedRate(getTask(), 0L, 5L, TimeUnit.MINUTES);
    }

    private Runnable getTask() {
        return () -> {
            LOGGER.debug("开始收取邮件.");

            final Context instance = Context.getInstance();
            final Messager messager = instance.getMessager();

            List<Message> emails = messager.getMessages();
            final int size = emails.size();
            LOGGER.debug("总新邮件条数: {}", size);

            emails = filterEmail(emails);
            LOGGER.debug("过滤邮件条数： {}", size - emails.size());

            messager.sendMessage(emails);

            instance.closeBox();

            LOGGER.info("结束收取邮件...");
        };
    }

    private List<Message> filterEmail(List<Message> emails) {
        return emails.stream()
                     .filter(this::isOtherEmailToFilterAndDoSomething)
                     .filter(this::isEmailFromUser)
                     .collect(Collectors.toList());
    }

    private boolean isOtherEmailToFilterAndDoSomething(Message message) {
        try {
            String subject = message.getSubject();
            final Address[] from = message.getFrom();
            InternetAddress fromUserAddress = (InternetAddress) from[0];
            final String address = fromUserAddress.getAddress();
            if (subject.startsWith(SUBSCRIBE_EMAIL)) {
                User user = new User(address);
                if (subject.length() > SUBSCRIBE_EMAIL.length()) {
                    user.setName(subject.substring(subject.indexOf("]") + 1));
                }

                Context.getInstance().getUserDao().addUser(user);
                LOGGER.info("新增用户 {}-{} 订阅", user.getName(), user.getEmail().getAddress());
                return false;
            } else if (subject.startsWith(UNSUBSCRIBE_EMAIL)) {
                final User user = Context.getInstance().getUserDao().findUser(address);
                if (user == null) {
                    return false;
                }

                Context.getInstance().getUserDao().deleteUser(address);
                LOGGER.info("用户 {}-{} 取消订阅", user.getName(), user.getEmail().getAddress());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return true;
    }

    private boolean isEmailFromUser(Message message) {
        Set<User> users = Context.getInstance().getUserDao().getAllUser();
        try {
            Address[] from = message.getFrom();

            for (User user : users) {
                for (Address address : from) {
                    if (((InternetAddress) address).getAddress().equals(user.getEmail().getAddress())) {
                        return true;
                    }
                }
            }

            LOGGER.info("邮件 【{}】来自非订阅用户 {} ，不做转发处理", message.getSubject(), ((InternetAddress) from[0]).getAddress());
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
