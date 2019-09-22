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

import java.util.Properties;

import org.kotrt.maillist.command.PropertyCommand;
import org.kotrt.maillist.dao.UserDao;
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

    private UserDao users;

    private Properties properties;

    private Context() {
        users = new UserDao();
    }

    public UserDao getUserDao() {
        return users;
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
