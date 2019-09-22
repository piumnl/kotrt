package org.kotrt.maillist.core.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.command.SubscribeUserCommand;

/**
 * 用户操作类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-22.
 */
public class UserDao {

    private Map<String, User> users;

    public UserDao() {
        users = new HashMap<>();
    }

    /**
     * @return 获取所有用户
     */
    public Set<User> getAllUser() {
        return new HashSet<>(users.values());
    }

    /**
     * 通过邮箱查找指定用户信息
     * @param email 用户的邮箱
     * @return 如果没有找到返回 null，否则返回该用户信息
     */
    public User findUser(String email) {
        return users.get(email);
    }

    /**
     * 添加用户
     * @param user 用户信息
     */
    public void addUser(User user) {
        Objects.requireNonNull(user);
        this.users.put(user.getEmail(), user);
        saveUser();
    }

    /**
     * 删除用户
     * @param email 用户信息
     */
    public void deleteUser(String email) {
        this.users.remove(email);
        saveUser();
    }

    /**
     * 序列化用户信息到磁盘
     */
    public void saveUser() {
        try {
            new SubscribeUserCommand().writeSubscribeFile(getAllUser());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
