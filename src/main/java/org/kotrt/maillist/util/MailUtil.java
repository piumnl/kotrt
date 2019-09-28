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

import com.sun.mail.smtp.SMTPMessage;
import de.agitos.dkim.Canonicalization;
import de.agitos.dkim.DKIMSigner;
import de.agitos.dkim.SMTPDKIMMessage;
import de.agitos.dkim.SigningAlgorithm;
import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.context.Context;
import org.kotrt.maillist.logger.JavaMailLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class MailUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    private static Properties props = new Properties();

    private static Session session;

    static {
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.smtp.host", "smtp.qq.com");
        props.setProperty("mail.imap.host", "imap.qq.com");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        props.setProperty("mail.smtp.dkim.signingdomain", "mail.kakjcloud.com");
        props.setProperty("mail.smtp.dkim.selector", "*");
        props.setProperty("mail.smtp.dkim.privatekey", "C:\\ssl\\rsakey.pem");
        session = Session.getDefaultInstance(props);
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(Context.getInstance().getUsername(),
                        Context.getInstance().getPassword());
            }
        });
        session.setDebugOut(new JavaMailLogger(LoggerFactory.getLogger(MailUtil.class)));
        session.setDebug(false);
    }


    public static void batchSend(List<MimeMessage> messageList, Set<User> userList) {
        LOGGER.info("开始发送邮件.");
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.connect(Context.getInstance().getUsername(), Context.getInstance().getPassword());
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
                Message DKIMMessage = getDKIMEmail(mimeMessage);
                DKIMMessage.setRecipients(MimeMessage.RecipientType.TO, parse);

                Address[] from = DKIMMessage.getFrom();
                InternetAddress address = (InternetAddress) from[0];
                address.setAddress(Context.getInstance().getUsername());

                DKIMMessage.setFrom(address);
                DKIMMessage.saveChanges();
                transport.sendMessage(DKIMMessage, DKIMMessage.getAllRecipients());
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

    private static Message getDKIMEmail(MimeMessage mimeMessage) throws Exception {
        //Create DKIM Signer
        DKIMSigner dkimSigner = null;
        dkimSigner = new DKIMSigner(props.getProperty("mail.smtp.dkim.signingdomain"),
                props.getProperty("mail.smtp.dkim.selector"),
                props.getProperty("mail.smtp.dkim.privatekey"));
        dkimSigner.setIdentity(Context.getInstance().getUsername() + "@" + props.getProperty("mail.smtp.dkim.signingdomain"));
        dkimSigner.setHeaderCanonicalization(Canonicalization.SIMPLE);
        dkimSigner.setBodyCanonicalization(Canonicalization.RELAXED);
        dkimSigner.setLengthParam(true);
        dkimSigner.setSigningAlgorithm(SigningAlgorithm.SHA1withRSA);
        dkimSigner.setZParam(true);
        return new SMTPDKIMMessage(mimeMessage, dkimSigner);
    }

    private static MimeMessage buildSendMessage(Message message) throws Exception {
        return new MimeMessage((MimeMessage) message);
    }

    public static String getRegisterOrNotUsername(Message message) throws Exception {
        String[] registerOrNotSubject = getRegisterOrNotSubject(message);
        return registerOrNotSubject[1];
    }

    public static String getRegisterOrNotEmail(Message message) throws Exception {
        String[] registerOrNotSubject = getRegisterOrNotSubject(message);
        return registerOrNotSubject[2];
    }

    private static String[] getRegisterOrNotSubject(Message message) throws Exception {
        String subject = message.getSubject();
        subject = subject.replace("[", "");
        String[] split = subject.split("]");
        if (split.length < 3) {
            throw new Exception("注册或者取消注册的email标题格式异常!");
        }
        return split;
    }

    public static List<MimeMessage> getEmails() {
        Store store = null;
        Folder folder = null;
        try {
            store = session.getStore(props.getProperty("mail.store.protocol"));
            store.connect(props.getProperty("mail.imap.host"), Context.getInstance().getUsername(), Context.getInstance().getPassword());

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
