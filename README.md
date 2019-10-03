# kotrt

邮件列表服务

## 运行环境

- Java：要求 JDK8+
- Maven
- Git

## 如何使用？

1. 获取运行文件

    ```bash
    # 打包，获取打包后的 jar ，格式一般为 maillist-X.x.x-SNAPSHOT.jar
    mvn package
    ```

1. 创建配置文件

    - `maillist.properties`: 必须，记录用户名(`username`)和密码(`password`)
    - `maillist.txt`: 非必须，记录订阅用户信息(格式为 `<email> <username>`)

1. 启动运行

    ```bash
    java -jar maillist-X.x.x-SNAPSHOT.jar
    ```

## 提供的功能

1. 自动转发订阅用户分享的邮件
1. 订阅/取消订阅 功能

    - 发送主题为 `[订阅邮件]用户名` 则服务会记录该发件人为订阅用户，服务会将分享的邮件转发到该用户的邮箱
    - 发送主题为 `[取消订阅]` 则服务会删除该用户的订阅，该用户将不再接收转发的邮件
