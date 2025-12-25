# Online Bookshop Project

## 环境要求
- Docker Desktop (已启动)
- Java 17 (用于打包)
- Maven

## 如何运行

### 1. 打包项目
在项目根目录下执行：
mvn clean package -DskipTests

### 2. 启动环境
执行以下命令，自动构建镜像并启动 MySQL/Redis：
docker compose up -d --build

### 3. 验证
等待约 30 秒等待数据库初始化完成。
访问接口文档或测试： http://localhost:8080/actuator

## 注意事项
- 数据库端口映射为: 3307
- Redis 端口映射为: 6380
- MySQL 账号/密码: root / 123456