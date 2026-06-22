# Jenkins 接入指南

> 关联文档：`docs/06-build-guide.md` §3、`docs/07-deployment-guide.md` §6.1、`docs/03-configuration-management-plan.md` §9
>
> CI 配置项：`CI-CI-001`（Jenkinsfile）、`CI-CI-002`（本文件）、`CI-CI-003`（邮件模板）
>
> 本指南面向**本地笔记本演示**场景，指导从零接入 Jenkins 并触发完整流水线。

---

## 1. 环境要求

| 项 | 要求 |
|---|---|
| 操作系统 | Windows 10/11、macOS、Linux 均可 |
| Java | JDK 17（Jenkins 2.400+ 要求） |
| Docker Desktop | 24+，已启用且 Docker daemon 运行中 |
| 内存 | ≥ 8 GB（Jenkins + Maven 构建 + Docker 4 容器） |
| Git | 2.40+ |

---

## 2. 安装 Jenkins（本地）

### 2.1 下载与启动

- **Windows**：从 https://www.jenkins.io/download/ 下载 LTS Windows 安装包，按向导安装。
- **通用（推荐 Docker 方式）**：
  ```bash
  docker run -d --name jenkins \
    --restart unless-stopped \
    -p 8888:8080 -p 50000:50000 \
    -v jenkins_home:/var/jenkins_home \
    -v /var/run/docker.sock:/var/run/docker.sock \
    jenkins/jenkins:lts-jdk17
  ```
  > 挂载 `/var/run/docker.sock` 让 Jenkins 容器内能调用宿主机 Docker（构建镜像与部署）。
  > Windows Docker Desktop：`/var/run/docker.sock` 需改用 `-v //var/run/docker.sock:/var/run/docker.sock`。

### 2.2 解锁与初始化

1. 浏览器打开 http://localhost:8888
2. 用初始密码解锁：
   ```bash
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. 选择 **"Install suggested plugins"**。
4. 创建管理员账号。

---

## 3. 必备插件

进入 `Manage Jenkins → Plugins → Available plugins`，安装：

| 插件 | 用途 |
|---|---|
| **Pipeline** | 执行 Jenkinsfile（默认已装） |
| **Git** | 从 GitHub 拉代码（默认已装） |
| **GitHub Integration** | Webhook 触发 |
| **JUnit** | 解析 surefire 测试报告 |
| **JaCoCo** | 覆盖率报告 |
| **HTML Publisher** | 归档覆盖率 HTML 报告 |
| **Docker Pipeline** | 在流水线中调用 docker |
| **Email Extension** | 发送测试报告邮件（加分项） |

> 安装后重启 Jenkins。

---

## 4. 配置凭证与全局工具

### 4.1 JDK

`Manage Jenkins → Tools → JDK installations`：
- 取消 "Install automatically"，指向本机 `JAVA_HOME`（JDK 17）。

### 4.2 Maven

`Manage Jenkins → Tools → Maven installations`：
- 勾选 "Install automatically"，版本选 `3.9.6`。

### 4.3 Docker

确保 Jenkins 节点（或容器挂载）能执行 `docker` 命令：
```bash
docker version       # 在 Jenkins 节点验证
docker compose version
```

### 4.4 邮件（Email Extension）

`Manage Jenkins → System → Extended E-mail Notification`：
- SMTP Server：填学校或组内 SMTP（如 `smtp.qq.com`、`smtp.163.com`）。
- SMTP Port：`465`（SSL）或 `587`（STARTTLS）。
- 凭证：用 `SMTP_USERNAME` / `SMTP_PASSWORD`（在 Credentials 中添加为 "Username with password"）。
- Default Recipients：小组所有成员邮箱（对应 `.env.example` 的 `TEAM_EMAIL`）。

---

## 5. 创建 Pipeline 任务

1. Jenkins 首页 → `New Item` → 名字填 `petcare-o2o` → 选 **Pipeline** → OK。
2. **General**：
   - 勾选 "This project is parameterized"（可选），加：
     - Boolean Parameter `DEPLOY`（默认 false）：手动触发部署。
     - String Parameter `GIT_BRANCH`（默认 `*/main`）。
3. **Build Triggers**：
   - ☑ **GitHub hook trigger for GITScm polling**（配合 Webhook）。
4. **Pipeline**：
   - Definition：**Pipeline script from SCM**。
   - SCM：**Git**。
   - Repository URL：`https://github.com/zengmo20224/-------2-.git`
   - Credentials：如为私有仓库则添加；公开仓库可不填。
   - Branch Specifier：`*/main`（或 `*/phase-11-user-prerequisites`）。
   - Script Path：`Jenkinsfile`。
5. 保存 → 点 **Build Now** 测试。

---

## 6. 配置 GitHub Webhook（自动触发）

GitHub 仓库 → `Settings → Webhooks → Add webhook`：

| 字段 | 值 |
|---|---|
| Payload URL | `http://<你的IP>:8888/github-webhook/` |
| Content type | `application/json` |
| Trigger | ☑ Just the `push` event |

> 本地演示可用 [ngrok](https://ngrok.com) 暴露 Jenkins：
> ```bash
> ngrok http 8888
> ```
> 把 ngrok 给的公网 URL 填入 Payload URL。

---

## 7. 流水线阶段说明

详见 `Jenkinsfile`。阶段顺序：

```
Checkout → Backend Build → Backend Test → Backend Package
       → Docker Build → Deploy (条件) → Health Check (条件) → Post
```

- **Deploy / Health Check 阶段**仅在 `main` / `develop` 分支或 `DEPLOY=true` 时执行，避免每次 push 都重建生产。
- **Post**：无论成功失败都收集 JUnit/JaCoCo/HTML 报告，并发送邮件。

---

## 8. 演示流程（配合录屏）

1. 在 GitHub 上展示 `Jenkinsfile` 与 CMP 文档。
2. 改一行前端代码（如 H5 文案）：
   ```bash
   git commit -m "feat(h5): tweak hero text"
   git push
   ```
3. 切到 Jenkins，观察 webhook 触发 → 流水线各阶段实时日志（编译/测试/打包/镜像构建/部署）。
4. 等流水线绿 → 浏览器刷新 http://localhost:8081 → 看到改动生效。
5. 展示 "JaCoCo Coverage Report" 与组员收到的测试邮件。
6. 展示 `git tag -l`、配置项登记表、变更申请单实例。

---

## 9. 常见问题

| 问题 | 解决 |
|---|---|
| `mvn not found` | 在 Tools 配置 Maven；或 Jenkinsfile 用 `sh 'mvn ...'` 时确认 PATH |
| `docker compose` 命令找不到 | 升级 Docker Desktop 到含 compose v2 的版本；旧版用 `docker-compose` |
| Docker socket 权限拒绝 | 把 jenkins 用户加入 docker 组：`usermod -aG docker jenkins` |
| 端口 8080/8081 占用 | 在 `.env` 改 `ADMIN_WEB_PORT` / `H5_PORT` |
| 邮件发送失败 | 检查 SMTP 凭证；QQ/163 需用授权码而非登录密码 |
| Webhook 不触发 | 检查 ngrok 是否运行；Jenkins "GitHub hook trigger" 是否勾选 |

---

## 修订记录

| 版本 | 日期 | 修订人 | 说明 |
|---|---|---|---|
| v1.0 | 2026-06-22 | 配置管理组 | 首次发布 |
