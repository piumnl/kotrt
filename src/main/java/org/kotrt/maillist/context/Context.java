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
package org.kotrt.maillist.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.command.PropertyCommand;
import org.kotrt.maillist.command.SubscribeUserCommand;
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

    private Map<String, User> users;

    private Properties properties;

    private Context() {
        users = new HashMap<>();
    }

    public Set<User> getUsers() {
        return new HashSet<>(users.values());
    }

    public User findUser(String email) {
        return users.get(email);
    }

    public void addUser(User user) {
        Objects.requireNonNull(user);
        this.users.put(user.getEmail(), user);
        saveUser();
    }

    public void deleteUser(String email) {
        this.users.remove(email);
        saveUser();
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    private void saveUser() {
        try {
            new SubscribeUserCommand().writeSubscribeFile(getUsers());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
