package org.kotrt.maillist.context;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.command.PropertyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 上下文类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private static Context context;

    private Set<User> users;

    private Properties properties;

    private Context() {
        users = Collections.emptySet();
    }

    public Set<User> getUsers() {
        return users;
    }

    public void updateUsers(Set<User> users) {
        this.users = users;
    }

    public String getUsername() {
        return properties.getProperty("username");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static Context getInstance() {
        if (context == null) {
            context = new Context();
            try {
                new PropertyCommand().run(null);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return context;
    }
}
