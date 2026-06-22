import { existsSync, readdirSync, rmSync } from 'node:fs'
import { dirname, isAbsolute, relative, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const projectRoot = resolve(__dirname, '..')
const defaultOutputDir = resolve(projectRoot, 'dist', 'build', 'mp-weixin')
const outputDir = process.env.UNI_OUTPUT_DIR
  ? resolve(process.env.UNI_OUTPUT_DIR)
  : defaultOutputDir

assertSafeOutputDir(outputDir)
cleanOutputDirContents(outputDir)

console.log(`mp-weixin output cleaned: ${outputDir}`)

function cleanOutputDirContents(targetDir) {
  if (!existsSync(targetDir)) return

  for (const entry of readdirSync(targetDir)) {
    rmSync(resolve(targetDir, entry), {
      recursive: true,
      force: true,
      maxRetries: 5,
      retryDelay: 100,
    })
  }
}

function assertSafeOutputDir(targetDir) {
  const relativeToProject = relative(projectRoot, targetDir)
  const isInsideProject = Boolean(relativeToProject)
    && !relativeToProject.startsWith('..')
    && !isAbsolute(relativeToProject)
  const isMpWeixinOutput = targetDir === defaultOutputDir

  if (!isInsideProject || !isMpWeixinOutput) {
    throw new Error(`Refuse to clean unexpected mp-weixin output directory: ${targetDir}`)
  }
}
