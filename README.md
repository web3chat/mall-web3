# mall-chain 部署文档

## 一、环境要求

| 组件           | 版本   | 用途                     |
| -------------- | ------ | ------------------------ |
| JDK            | 17     | 编译和运行               |
| Maven          | 3.6+   | 构建                     |
| MySQL          | 8.0+   | 数据存储                 |
| Redis          | 7.0+   | 缓存、分布式锁           |
| Docker         | 20.10+ | 容器化部署（可选）       |
| Docker Compose | 2.0+   | 编排多容器（可选）       |

---

## 二、部署步骤

### 步骤 1：初始化数据库

项目根目录已提供完整建表脚本 `mall-chain.sql`，直接执行即可：

```bash
mysql -u root -p < mall-chain.sql
```

---

### 步骤 2：创建生产配置文件

复制 `application-dev.yml` 为 `application-prod.yml`，按以下清单逐项填写：

```yaml
server:
  port: 10010

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 110MB
  datasource:
    url: jdbc:mysql://<数据库IP>:3306/mall-chain?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false
    username: <数据库用户名>
    password: <数据库密码>
    hikari:
      minimum-idle: 20
      maximum-pool-size: 500
  data:
    redis:
      database: 2
      host: <Redis IP>
      port: 6379
      password: <Redis 密码>

# ==================== 区块链 ====================
local:
  chain:
    contract:                                          # ERC1155 合约（必填）
      chain-url: <链节点URL>                            # 如 https://mainnet.bityuan.com/eth
      chain-id: <链ID>                                  # 如 2999
      decimals: 18
      wallet-password: <钱包密码>                       # ⚠️ 首次设定后不可修改！
      mnemonic: '<12个助记词>'                          # ⚠️ 首次设定后不可修改！用于生成钱包种子
      deploy-temp-private-key: '<部署合约的私钥>'        # 已有合约可留空
      deploy-admin-address: '<合约管理员地址>'           # 选填

    siwe:                                              # SIWE 签名配置（必填）
      domain: <H5前端域名>                              # 签名消息中的域名，需与前端一致

    pay:                                               # 支付通道（根据业务开启）
      bty:
        enable: true
        chain-url: <BTY节点URL>
        chain-id: 2999
        contract-address: '<BTY合约地址>'
        decimals: 18
        calc-decimals: 4
        usdt-url: <行情接口URL>
      bep20-usd:
        enable: true
        chain-url: <BSC节点URL>
        chain-id: 56
        contract-address: '0x55d398326f99059ff775485246999027b3197955'
        decimals: 18
        calc-decimals: 4

    withdraw:                                          # 提币通道（根据业务开启）
      bty:
        enable: true
        chain-url: <BTY节点URL>
        chain-id: 2999
        decimals: 18
        min-number: 10.00
      usd:
        enable: true
        chain-url: <BSC节点URL>
        chain-id: 56
        contract-address: '0x55d398326f99059ff775485246999027b3197955'
        decimals: 18
        min-number: 10.00

  file:                                                # 文件存储（OSS / S3 二选一开启）
    ali-oss:
      enable: true
      bucket: <Bucket名称>
      region: <Region>
      endpoint: <Endpoint>
      access-key-id: <AK>
      access-key-secret: <SK>
    aws-s3:
      enable: false
      bucket: <Bucket名称>
      region: <Region>
      endpoint: <Endpoint>
      access-key-id: <AK>
      access-key-secret: <SK>

  logistics:                                           # 物流查询
    enable: true
    app-code: '<阿里云物流API AppCode>'

# ==================== 安全 ====================
knife4j:
  production: true                                     # 生产环境必须关闭文档

app:
  cors:
    allowed-origins: https://你的域名.com               # CORS 白名单，逗号分隔
```

---

### 步骤 3：Docker 部署（推荐）

项目提供了 `Dockerfile`、`docker-compose.yml` 和 `Makefile`，一键完成构建、编排和启动。

#### 3.1 构建镜像

```bash
make image
```

等价于：

```bash
docker build -t mall-chain:0.0.1-SNAPSHOT -t mall-chain:latest .
```

`Dockerfile` 采用多阶段构建：第一阶段用 `eclipse-temurin:17-jdk` 编译打包，第二阶段用 `eclipse-temurin:17-jre` 运行，最终镜像仅含 JRE，体积更小。

#### 3.2 单容器运行（需自建 MySQL / Redis）

```bash
make run
```

此命令先构建镜像，再以单容器方式启动，默认使用 `prod` 配置。确保 `application-prod.yml` 已正确填写外部 MySQL 和 Redis 地址。

#### 3.3 完整栈部署（docker compose，推荐）

```bash
make up
```

`docker-compose.yml` 会自动拉起 MySQL 8.0、Redis 7.0 和应用三个服务：

| 服务       | 容器名           | 端口  | 说明                                 |
| ---------- | ---------------- | ----- | ------------------------------------ |
| mysql      | mall-chain-mysql | 3306  | 自动执行 `mall-chain.sql` 初始化建表 |
| redis      | mall-chain-redis | 6379  | 数据持久化到 `redis-data` volume     |
| mall-chain | mall-chain-app   | 10010 | 依赖 mysql 和 redis，prod 配置启动   |

常用管理命令：

```bash
make down      # 停止并移除所有容器
make restart   # 重启应用容器
make logs      # 实时查看应用日志
make ps        # 查看容器运行状态
```

#### 3.4 自定义环境变量

可通过环境变量覆盖默认值：

```bash
# 自定义 Spring 激活配置
SPRING_PROFILE=dev make up

# 自定义 JVM 参数
JAVA_OPTS="-Xms1g -Xmx2g" make run

# 自定义 MySQL root 密码
MYSQL_ROOT_PASSWORD=mysecret make up
```

#### 3.5 Makefile 命令速查

| 命令           | 说明                                          |
| -------------- | --------------------------------------------- |
| `make jar`     | Maven 打包                                    |
| `make image`   | 构建 Docker 镜像                              |
| `make run`     | 构建镜像 + 单容器运行                         |
| `make up`      | 构建镜像 + docker compose 启动全栈            |
| `make down`    | 停止并移除全栈容器                            |
| `make restart` | 重启应用容器                                  |
| `make logs`    | 实时查看应用日志                              |
| `make ps`      | 查看容器状态                                  |
| `make push`    | 推送镜像到远程仓库                            |
| `make pull`    | 拉取最新镜像                                  |
| `make clean`   | Maven 清理                                    |
| `make init-db` | 导入 `mall-chain.sql` 到本地 MySQL（非容器）  |

#### 3.6 验证部署

```bash
# 查看容器状态
make ps

# 查看应用日志
make logs

# 端口检测
curl http://localhost:10010
```

> ⚠️ **首次启动注意**：如果 `chain_contract` 表为空，程序会自动部署 ERC1155 合约。确保 `deploy-temp-private-key` 对应的地址有足够 Gas 费。

---

### 步骤 4：传统部署

若不使用 Docker，可按以下步骤手动部署。

#### 4.1 构建打包

```bash
cd mall-chain
mvn clean package -DskipTests
```

产物：`target/mall-chain-0.0.1-SNAPSHOT.jar`

#### 4.2 上传并启动

将 jar 包上传至服务器，确保日志目录存在：

```bash
mkdir -p /data/app/logs
```

启动：

```bash
java -jar -Xms2g -Xmx4g -Dfile.encoding=UTF-8 \
     mall-chain-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=prod
```

> **JVM 建议**：堆 2G~4G，使用 G1GC `-XX:+UseG1GC`

##### systemd 托管（推荐）

`/etc/systemd/system/mall-chain.service`：

```ini
[Unit]
Description=mall-chain
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/data/app/mall-chain
ExecStart=/usr/bin/java -jar -Xms2g -Xmx4g -Dfile.encoding=UTF-8 \
          /data/app/mall-chain/mall-chain-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable --now mall-chain
```

#### 4.3 验证部署

```bash
# 查看启动状态
systemctl status mall-chain

# 查看日志
tail -f /data/app/logs/mall_chain/app.json

# 端口监听
curl http://localhost:10010
```

---

## 三、关键配置提示

| 配置项                  | 说明                                                         |
| ----------------------- | ------------------------------------------------------------ |
| `wallet-password`       | 用于 AES 加密内部钱包私钥，**首次设定后禁止修改**，否则所有私钥无法解密 |
| `mnemonic`              | 生成钱包种子的助记词，**首次设定后禁止修改**，否则链上地址会变更       |
| `siwe.domain`           | SIWE 签名消息中的域名，需与前端 H5 域名一致                           |
| `deploy-temp-private-key` | 仅首次部署合约时需要，合约部署后可从 `chain_contract` 表查到地址 |
| `knife4j.production`    | 生产环境必须设为 `true`，关闭 `/doc.html` 接口文档            |
| `app.cors.allowed-origins` | 生产环境必须配置前端域名白名单，留空则允许所有来源           |
| 文件存储                | OSS 和 S3 只能开启一个，另一个 `enable: false`               |
| `chain_contract` 表     | 程序启动时检查此表，有数据则跳过合约部署                     |

---

## 四、日志

| 项目         | 值                              |
| ------------ | ------------------------------- |
| 路径         | `/data/app/logs/mall_chain/`    |
| 当前日志     | `app.json`（JSON 结构化）        |
| 历史归档     | `app-2026-07-07.0.json.gz`      |
| 保留天数     | 90 天                           |
| 单文件上限   | 100MB                           |
| 总空间上限   | 5GB                             |

---

## 五、常见问题

**Q：启动报"合约部署失败"？**
- 检查 `chain-url` 节点是否可达
- 确认 `deploy-temp-private-key` 地址 Gas 充足
- 若已有合约，确认 `chain_contract` 表中有 id=1 的记录

**Q：链上交易一直 wait 状态？**
- 检查对应商户的内部钱包 Nonce 是否卡住（程序用 Redis 锁防并发）
- 查看日志中 `TxResult` 错误信息

**Q：文件上传失败？**
- 确认 OSS/S3 配置 `enable: true`，且密钥正确
- 确认 `sys_file` 表存在

**Q：接口文档 `/doc.html` 无法访问？**
- 检查 `knife4j.production` 是否为 `false`

**Q：Docker 部署后连不上 MySQL？**
- docker compose 方式已自动创建 MySQL，确认 `application-prod.yml` 中数据库地址为 `mysql`（容器内服务名），而非 `localhost`
- 单容器方式需自行搭建 MySQL，并确保网络互通

**Q：Docker 构建时 Maven 下载依赖很慢？**
- `Dockerfile` 已启用 BuildKit 缓存挂载 `--mount=type=cache,target=/root/.m2`，首次构建后依赖会被缓存
- 也可在 `Dockerfile` 中配置国内 Maven 镜像源加速

**Q：`application-prod.yml` 如何挂载到容器？**
- 修改 `docker-compose.yml`，在 `mall-chain` 服务下添加 volumes 挂载：
  ```yaml
  volumes:
    - ./application-prod.yml:/app/application-prod.yml
  ```
- 单容器方式使用 `-v` 参数：
  ```bash
  docker run ... -v ./application-prod.yml:/app/application-prod.yml mall-chain:latest
  ```
