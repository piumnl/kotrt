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
package org.kotrt.maillist.util;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.core.MailProperty;
import org.kotrt.maillist.core.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MailUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    private static Session session;

    static {
        session = Context.getInstance().getSession();
    }

    public static void batchSend(List<MimeMessage> messageList, Set<User> userList) {
        LOGGER.info("开始发送邮件.");
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            final MailProperty properties = Context.getInstance().getMailProperty();
            transport.connect(properties.getUsername(), properties.getPassword());
            for (MimeMessage mimeMessage : messageList) {
                LOGGER.info("开始发送邮件，邮件标题为: " + mimeMessage.getSubject());

                StringBuilder userListStr = new StringBuilder();
                for (User user : userList) {
                    LOGGER.info("   正在发送给接收人: " + user.getName());
                    userListStr.append(user.getEmail());
                    userListStr.append(",");
                }
                userListStr.deleteCharAt(userListStr.length() - 1);

                InternetAddress[] parse = InternetAddress.parse(userListStr.toString());
                int index = 0;
                for (User user : userList) {
                    parse[index].setPersonal(user.getName());
                    ++index;
                }
                mimeMessage.setRecipients(MimeMessage.RecipientType.TO, parse);

                Address[] from = mimeMessage.getFrom();
                InternetAddress address = (InternetAddress) from[0];
                address.setAddress(properties.getUsername());

                mimeMessage.setFrom(address);
                mimeMessage.saveChanges();
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                LOGGER.info("   全部发送成功!");
            }
        } catch (Exception e) {
            LOGGER.info("   全部发送失败!");
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        LOGGER.info("邮件发送完成.");
    }

    private static MimeMessage buildSendMessage(Message message) throws Exception {
        return new MimeMessage((MimeMessage) message);
    }

    public static String getRegisterOrNotUsername(Message message) throws MessagingException {
        String[] registerOrNotSubject = getRegisterOrNotSubject(message);
        return registerOrNotSubject[1];
    }

    public static String getRegisterOrNotEmail(Message message) throws MessagingException {
        String[] registerOrNotSubject = getRegisterOrNotSubject(message);
        return registerOrNotSubject[2];
    }

    private static String[] getRegisterOrNotSubject(Message message) throws MessagingException {
        String subject = message.getSubject();
        subject = subject.replace("[", "");
        String[] split = subject.split("]");
        if (split.length < 3) {
            throw new RuntimeException("注册或者取消注册的email标题格式异常!");
        }
        return split;
    }

    public static List<MimeMessage> getEmails() {
        Store store = null;
        Folder folder = null;
        final MailProperty properties = Context.getInstance().getMailProperty();

        try {
            store = session.getStore(properties.getStoreProtocol());
            store.connect(properties.getIMAPHost(), properties.getUsername(), properties.getPassword());

            folder = store.getFolder("inbox");
            folder.open(Folder.READ_WRITE);
            List<MimeMessage> notReadMessage = new ArrayList<>();
            Message[] messages = folder.getMessages(folder.getMessageCount() - folder.getUnreadMessageCount() + 1, folder.getMessageCount());
            for (Message msg : messages) {
                if (!msg.getFlags().contains(Flags.Flag.SEEN)) {
                    notReadMessage.add(buildSendMessage(msg));
                }
                //msg.setFlag(Flags.Flag.SEEN, true);
            }
            return notReadMessage;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (folder != null) {
                    folder.close(true);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
