package org.kotrt.maillist.context;

import java.util.Collections;
import java.util.Set;

import org.kotrt.maillist.bean.User;

/**
 * 上下文类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class Context {

    private static Context context;

    private Set<User> users;

    private Context() {
        users = Collections.emptySet();
    }

    public Set<User> getUsers() {
        return users;
    }

    public void updateUsers(Set<User> users) {
        this.users = users;
    }

    public static Context getInstance() {
        if (context == null) {
            context = new Context();
        }

        return context;
    }
}
