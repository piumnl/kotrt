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
