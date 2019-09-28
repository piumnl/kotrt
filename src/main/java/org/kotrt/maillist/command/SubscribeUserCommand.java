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
package org.kotrt.maillist.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.core.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扫描订阅用户信息
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class SubscribeUserCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscribeUserCommand.class);

    private static final String SUBSCRIBE_FILE = "maillist.txt";

    @Override
    public void run(String[] args) throws IOException {
        final Path path = Paths.get(SUBSCRIBE_FILE);
        if (Files.exists(path)) {
            Set<User> users = readSubscribeFile(path);

            for (User user : users) {
                Context.getInstance().getUserDao().addUser(user);
                LOGGER.info("注册订阅用户 {} - {} 成功！", user.getName(), user.getEmail());
            }

        } else {
            Files.createFile(path);
            LOGGER.warn("缺少订阅者信息!");
        }
    }

    /**
     * 从订阅用户文件中读取用户信息
     * @param path 订阅用户文件路径
     * @return 用户信息
     */
    private Set<User> readSubscribeFile(Path path) {
        LOGGER.info("读取订阅用户文件：{}", path.toAbsolutePath());
        Set<User> result = new HashSet<>(0);
        try (final Scanner scanner = new Scanner(path, StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                final String s = scanner.nextLine();
                final User user = User.parse(s);
                result.add(user);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 将用户信息写入到配置文件中
     * @param collection 用户信息
     * @throws IOException IO错误
     */
    public void writeSubscribeFile(Collection<User> collection) throws IOException {
        final Path path = Paths.get(SUBSCRIBE_FILE);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }

        StringBuilder builder = new StringBuilder();
        for (User user : collection) {
            builder.append(user.getEmail().getAddress()).append("    ").append(user.getName()).append(System.lineSeparator());
        }

        Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8));
        LOGGER.info("写入订阅用户到 {} 成功！", SUBSCRIBE_FILE);
    }
}
