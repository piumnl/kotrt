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
package org.kotrt.maillist.core.context;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.kotrt.maillist.core.MailProperty;
import org.kotrt.maillist.core.Messager;
import org.kotrt.maillist.core.dao.UserDao;
import org.kotrt.maillist.logger.JavaMailLogger;
import org.kotrt.maillist.util.MailUtil;
import org.slf4j.LoggerFactory;

/**
 * 上下文类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class Context {

    private InternetAddress mime;

    private static Context context;

    private UserDao users;

    private MailProperty properties;

    private Session session;

    private Messager messager;

    private Folder folder;

    private Store store;

    private Transport transport;

    private Context() {
        users = new UserDao();
        properties = new MailProperty();
        try {
            mime = new InternetAddress(properties.getUsername());
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }

        session = Session.getInstance(properties.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getUsername(), properties.getPassword());
            }
        });
        session.setDebugOut(new JavaMailLogger(LoggerFactory.getLogger(MailUtil.class)));
        session.setDebug(false);

        messager = new Messager(properties, session);
    }

    public UserDao getUserDao() {
        return users;
    }

    public MailProperty getMailProperty() {
        return properties;
    }

    public InternetAddress getMime() {
        return mime;
    }

    public Session getSession() {
        return session;
    }

    public Messager getMessager() {
        return messager;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public void closeBox() {
        if (folder != null) {
            try {
                folder.close(true);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }

        if (store != null) {
            try {
                store.close();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }

        if (transport != null) {
            try {
                transport.close();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Context getInstance() {
        if (context == null) {
            context = new Context();
        }

        return context;
    }
}
