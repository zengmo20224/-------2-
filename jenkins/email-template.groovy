// Jenkins Email Extension 模板（HTML）
// 关联文档：jenkins/README.md §4.4、docs/03-configuration-management-plan.md §9
// CI：CI-CI-003
//
// 用法：在 Jenkinsfile 的 emailext 步骤中通过 mimeType 'text/html' + template
// 或者直接用本文件作为 Jelly/Groovy 模板。
// 本文件提供一段可粘贴到 emailext body 的 HTML 模板（见 Jenkinsfile post 段）。

// =============================================================================
// 模板内容（HTML 字符串，由 Jenkinsfile 注入变量）
// 在 Jenkinsfile 中：
//   emailext(
//       subject: "[PetCare] ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
//       body: '''<INSERT_HERE>''',
//       to: env.TEAM_EMAIL,
//       attachmentsPattern: 'target/site/jacoco/index.html'
//   )
// =============================================================================

def buildHtml = """
<h2 style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
  🐾 PetCare O2O 构建报告
</h2>

<table style="border-collapse: collapse; font-family: sans-serif; font-size: 14px;">
  <tr><td style="padding: 4px 12px; color: #555;">构建状态</td>
      <td style="padding: 4px 12px;"><b>${currentBuild.currentResult}</b></td></tr>
  <tr><td style="padding: 4px 12px; color: #555;">提交 SHA</td>
      <td style="padding: 4px 12px;"><code>${env.GIT_COMMIT_SHORT ?: 'N/A'}</code></td></tr>
  <tr><td style="padding: 4px 12px; color: #555;">分支</td>
      <td style="padding: 4px 12px;">${env.GIT_BRANCH ?: 'N/A'}</td></tr>
  <tr><td style="padding: 4px 12px; color: #555;">持续时间</td>
      <td style="padding: 4px 12px;">${currentBuild.durationString}</td></tr>
  <tr><td style="padding: 4px 12px; color: #555;">触发原因</td>
      <td style="padding: 4px 12px;">${currentBuild.getBuildCauses()?.collect { it.shortDescription }?.join('; ') ?: '手动触发'}</td></tr>
</table>

<h3 style="font-family: sans-serif;">阶段结果</h3>
<table style="border-collapse: collapse; font-family: sans-serif; font-size: 13px; border: 1px solid #ddd;">
  <thead>
    <tr style="background: #f5f5f5;">
      <th style="padding: 6px 12px; border: 1px solid #ddd; text-align: left;">阶段</th>
      <th style="padding: 6px 12px; border: 1px solid #ddd; text-align: left;">状态</th>
      <th style="padding: 6px 12px; border: 1px solid #ddd; text-align: left;">耗时</th>
    </tr>
  </thead>
  <tbody>
"""
currentBuild.rawBuild.getStages().each { stage ->
    def color = stage.status?.toString() == 'SUCCESS' ? '#28a745' : '#dc3545'
    buildHtml += """
    <tr>
      <td style="padding: 6px 12px; border: 1px solid #ddd;">${stage.displayName}</td>
      <td style="padding: 6px 12px; border: 1px solid #ddd; color: ${color};"><b>${stage.status}</b></td>
      <td style="padding: 6px 12px; border: 1px solid #ddd;">${stage.duration / 1000}s</td>
    </tr>
    """
}
buildHtml += """
  </tbody>
</table>

<p style="font-family: sans-serif; margin-top: 20px;">
  📊 <b>覆盖率报告</b>：登录 Jenkins 查看左侧 "JaCoCo Coverage Report"。<br>
  🔗 <b>构建详情</b>：<a href="${env.BUILD_URL}">${env.BUILD_URL}</a><br>
  🚀 <b>访问地址</b>：
    <a href="http://localhost:8080">管理端</a> |
    <a href="http://localhost:8081">用户端 H5</a>
</p>

<hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
<p style="font-family: sans-serif; font-size: 12px; color: #888;">
  本邮件由 Jenkins 流水线自动发送 · PetCare O2O 配置管理 · 课程《软件配置管理》
</p>
"""

return buildHtml
