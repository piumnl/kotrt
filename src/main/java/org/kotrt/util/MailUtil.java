package org.kotrt.util;

import org.kotrt.bean.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailUtil {

    private static Properties props = new Properties();

    private static Session session;

    private static String username = "2357431193@qq.com";
    private static String password = "zqwhxwygsdsuebed";


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
        session.setDebug(false);
    }


    public static void batchSend(List<MimeMessage> messageList, List<User> userList) {
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
            e.printStackTrace();
        }finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static MimeMessage buildSendMessage(Message message) throws Exception {
        MimeMessage mimeMessage = new MimeMessage((MimeMessage) message);
        mimeMessage.setFrom(new InternetAddress(username));
        return mimeMessage;
    }

    public static List<MimeMessage> getEmail() {
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
            e.printStackTrace();
        } finally {
            try {
                if(folder!=null)
                folder.close(true);
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
