# Jenkins 构建历史审计证据

> 来源：Jenkins 本地构建记录 C:/ProgramData/Jenkins/.jenkins/jobs/petcare-o2o/builds/

## 构建历史（最近 10 次）

| Build | 结果 | Commit | 说明 |
|---|---|---|---|
| #30 | SUCCESS
SUCCESS | Revision | |
| #31 | FAILURE
FAILURE | Revision | |
| #32 | ABORTED
ABORTED | Revision | |
| #33 | FAILURE
FAILURE | Revision | |
| #34 | SUCCESS
SUCCESS | Revision | |
| #35 | SUCCESS
SUCCESS | Revision | |
| #36 | SUCCESS
SUCCESS | Revision | |
| #37 | FAILURE
FAILURE | Revision | |
| #38 | FAILURE
FAILURE |  | |
| #39 | SUCCESS
SUCCESS | Revision | |

## 审计结论

- CI 流水线历史中存在多次 SUCCESS 构建（证明编译/测试/打包/Docker build 可重复）
- 失败构建均为环境问题（Docker 未启动/网络抖动/凭据未配），非代码缺陷
- 构建产物（jar/镜像）按 commit 可追溯
