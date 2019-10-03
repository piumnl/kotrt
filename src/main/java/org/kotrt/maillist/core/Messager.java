package org.kotrt.maillist.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Service;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.core.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-28.
 */
public class Messager {

    private static final Logger LOGGER = LoggerFactory.getLogger(Messager.class);

    private MailProperty property;

    private final Session session;

    public Messager(MailProperty property, Session session) {
        this.property = property;
        this.session = session;
    }

    public List<Message> getMessages() {
        return handle(store(), store -> {
            try {
                Folder folder = store.getFolder("inbox");
                folder.open(Folder.READ_WRITE);
                Context.getInstance().setFolder(folder);

                final Message[] search = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                if (search == null) {
                    return new ArrayList<>();
                } else {
                    return Arrays.asList(search);
                }
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Supplier<Store> store() {
        return () -> {
            try {
                return session.getStore(property.getStoreProtocol());
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 转发邮件
     * @param messages 原邮件信息
     */
    public void sendMessage(List<? extends Message> messages) {
        handle(transport(), service -> {
            try {
                LOGGER.debug("开始转发邮件：");
                for (Message message : messages) {
                    service.sendMessage(buildMessage(message), getAllRecipientUser());
                    LOGGER.debug("    邮件【{}】", message.getSubject());
                }
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            // 不需要返回值
            return null;
        });
    }

    private Supplier<Transport> transport() {
        return () -> {
            try {
                return session.getTransport();
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T extends Service, R> R handle(Supplier<T> supplier, Function<T, R> fun) {
        T service = null;
        try {
            // 获取操作对象
            service = supplier.get();
            // 设置发件人的账户名和密码
            service.connect(property.getUsername(), property.getPassword());

            return fun.apply(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 不在此处处理
            // close(service);
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

    protected Message newMessage(Session session) {
        return new MimeMessage(session);
    }

}
