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
import org.kotrt.maillist.logger.JavaMailLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    private static Properties props = new Properties();

    private static Session session;

    private static String username = "kotrt-malt111@foxmail.com";

    private static String password = "111";

    static {
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.store.protocol", "imap");
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.imap.host", "imap.qq.com");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.socketFactory.class", "SSL_FACTORY");
        session = Session.getDefaultInstance(props);
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebugOut(new JavaMailLogger(LoggerFactory.getLogger(MailUtil.class)));
        session.setDebug(false);
    }


    public static void batchSend(List<MimeMessage> messageList, List<User> userList) {
        LOGGER.info("开始发送邮件.");
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.connect(username, password);
            for (MimeMessage mimeMessage : messageList) {
                for (User user : userList) {
                    mimeMessage.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(user.getEmail()));
                    mimeMessage.saveChanges();
                    transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                }
            }
        } catch (Exception e) {
             LOGGER.error(e.getMessage(),e);
        }finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                     LOGGER.error(e.getMessage(),e);
                }
            }
        }
        LOGGER.info("邮件发送完成.");
    }

    private static MimeMessage buildSendMessage(Message message) throws Exception {
        MimeMessage mimeMessage = new MimeMessage((MimeMessage) message);
        mimeMessage.setFrom(new InternetAddress(username));
        return mimeMessage;
    }

    public static List<MimeMessage> getEmails() {
        Store store = null;
        Folder folder = null;
        try {
            store = session.getStore(props.getProperty("mail.store.protocol"));
            store.connect(props.getProperty("mail.imap.host"), username, password);

            folder = store.getFolder("inbox");
            folder.open(Folder.READ_WRITE);
            List<MimeMessage> notReadMessage = new ArrayList<>();
            Message[] messages = folder.getMessages(folder.getMessageCount() - folder.getUnreadMessageCount() + 1,folder.getMessageCount());
            for(Message msg : messages){
                if (!msg.getFlags().contains(Flags.Flag.SEEN)) {
                    notReadMessage.add(buildSendMessage(msg));
                }
                msg.setFlag(Flags.Flag.SEEN, true);
            }
            return notReadMessage;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        } finally {
            try {
                if (folder != null) {
                    folder.close(true);
                }
            }catch (Exception e){
                 LOGGER.error(e.getMessage(),e);
            }
            try {
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
        return null;
    }
}
