package org.kotrt.maillist.core;

import java.io.IOException;
import java.security.ProviderException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.core.context.Context;

/**
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-28.
 */
public class Messager {

    private MailProperty property;

    private final Session session;

    public Messager(MailProperty property, Session session) {
        this.property = property;
        this.session = session;
    }

    public void sendMessage(List<? extends Message> messages) throws MessagingException {
        Transport transport = null;
        try {
            // 根据 session 对象获取邮件传输对象 Transport
            transport = session.getTransport();
            // 设置发件人的账户名和密码
            transport.connect(property.getUsername(), property.getPassword());

            for (Message message : messages) {
                transport.sendMessage(buildMessage(message), getAllRecipientUser());
            }
        } catch (ProviderException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭邮件连接
            close(transport);
        }
    }

    private Message buildMessage(Message originMsg) throws MessagingException {
        final Message message = newMessage(session);

        // from
        message.setFrom(Context.getInstance().getMime());
        // to
        message.setRecipients(MimeMessage.RecipientType.TO, getAllRecipientUser());
        // bb
        message.setRecipients(MimeMessage.RecipientType.CC, originMsg.getFrom());
        // cc


        // 设置邮件主题
        message.setSubject(originMsg.getSubject());
        // 设置邮件的发送时间,默认立即发送
        message.setSentDate(new Date());

        try {
            message.setContent(originMsg.getContent(), originMsg.getContentType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        message.saveChanges();

        return message;
    }

    private Address[] getAllRecipientUser() {
        final Set<User> allUser = Context.getInstance().getUserDao().getAllUser();
        return allUser.stream()
                      .map(User::getEmail)
                      .toArray(Address[]::new);
    }

    private void close(Transport transport) {
        try {
            if (transport != null) {
                transport.close();
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    protected Message newMessage(Session session) {
        return new MimeMessage(session);
    }

}
