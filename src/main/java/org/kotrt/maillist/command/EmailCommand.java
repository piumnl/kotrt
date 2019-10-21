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

            if (emails.size() > 0) {
                messager.resendMessage(emails);
            }

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
                final User find = Context.getInstance().getUserDao().findUser(address);
                if (find != null) {
                    return false;
                }

                // 设置为已读
                message.getContent();

                User user = new User(address, address);
                if (subject.length() > SUBSCRIBE_EMAIL.length()) {
                    user.setName(subject.substring(subject.indexOf("]") + 1).trim());
                }

                final User realUser = new User(address, user.getName());
                Context.getInstance().getUserDao().addUser(realUser);
                LOGGER.info("新增用户 {}-{} 订阅", user.getName(), user.getEmail().getAddress());

                try {
                    final Messager messager = Context.getInstance().getMessager();
                    final Message thanksForSub = messager.buildMessage(Context.getInstance().getMime(), new Address[]{realUser.getEmail()}, "订阅成功", "感谢您的订阅！如果您打算取消订阅，请发送 " + UNSUBSCRIBE_EMAIL + "开头的主题的邮件。");
                    messager.sendMessage(thanksForSub);
                    LOGGER.info("发送欢迎 {} - {} 邮件成功", user.getName(), address);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

                return false;
            } else if (subject.startsWith(UNSUBSCRIBE_EMAIL)) {
                final User user = Context.getInstance().getUserDao().findUser(address);
                if (user == null) {
                    return false;
                }

                // 设置为已读
                message.getContent();

                try {
                    final Messager messager = Context.getInstance().getMessager();
                    final Message thanksForSub = messager.buildMessage(Context.getInstance().getMime(), new Address[]{user.getEmail()}, "取消订阅成功", "您已取消订阅成功，不再收到来自此处的邮件！如果您想再次订阅，请发送 " + SUBSCRIBE_EMAIL + "开头的主题的邮件。");
                    messager.sendMessage(thanksForSub);
                    LOGGER.info("发送 {} - {} 取消邮件订阅的邮件成功", user.getName(), address);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
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
        return false;
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
