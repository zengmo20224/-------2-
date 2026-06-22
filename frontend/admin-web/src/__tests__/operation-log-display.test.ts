import { describe, expect, it } from 'vitest'
import { readFileSync } from 'fs'
import { dirname, resolve } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(__dirname, '../views/operation-logs/index.vue'), 'utf-8')

describe('Operation logs readable display', () => {
  it('uses business-friendly Chinese columns instead of raw request fields', () => {
    expect(source).toContain('发生时间')
    expect(source).toContain('操作人')
    expect(source).toContain('操作内容')
    expect(source).toContain('处理结果')
    expect(source).toContain('查看技术信息')

    expect(source).not.toContain('label="方法"')
    expect(source).not.toContain('label="URL"')
  })

  it('translates modules, operations, and results into Chinese labels', () => {
    expect(source).toContain('MODULE_LABELS')
    expect(source).toContain('OPERATION_LABELS')
    expect(source).toContain('RESULT_LABELS')

    expect(source).toContain('商品管理')
    expect(source).toContain('服务项目')
    expect(source).toContain('预约管理')
    expect(source).toContain('更新商品轮播图')
    expect(source).toContain('操作成功')
    expect(source).toContain('操作失败')
  })

  it('builds a readable summary and keeps raw method/url secondary', () => {
    expect(source).toContain('formatOperationSummary')
    expect(source).toContain('formatTargetHint')
    expect(source).toContain('formatMethodLabel')
    expect(source).toContain('原始地址')
    expect(source).toContain('系统记录编号')
  })
})
