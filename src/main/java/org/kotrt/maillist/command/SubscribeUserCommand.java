package org.kotrt.maillist.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.context.Context;
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
            Context.getInstance().updateUsers(users);

            for (User user : users) {
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
        Set<User> collection = new HashSet<>();
        try (final Scanner scanner = new Scanner(path, StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                final String s = scanner.nextLine();
                collection.add(User.parse(s));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return collection;
    }
}
