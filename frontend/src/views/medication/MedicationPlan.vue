<template>
  <div class="medication-plan-page">
    <div class="plan-layout">
      <!-- 左侧二级导航 -->
      <div class="plan-sidebar">
        <div class="sidebar-title">用药管理</div>
        <div class="sidebar-item" :class="{ active: activeTab === 'plans' }" @click="activeTab = 'plans'">
          <span class="sidebar-icon">📋</span> 用药计划
        </div>
        <div class="sidebar-item" :class="{ active: activeTab === 'daily' }" @click="activeTab = 'daily'">
          <span class="sidebar-icon">💊</span> 服药情况
        </div>
        <div class="sidebar-item" :class="{ active: activeTab === 'analysis' }" @click="activeTab = 'analysis'">
          <span class="sidebar-icon">📊</span> 数据分析
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="plan-content">
        <!-- Tab 1: 用药计划 -->
        <div v-show="activeTab === 'plans'" class="tab-content">
          <!-- 病历选择（支持加号添加多个选择器） -->
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span>选择病历</span>
                <el-button type="primary" size="default" @click="addRecordSelector">+ 添加病例</el-button>
              </div>
            </template>
            <div class="record-selectors">
              <div v-for="(selector, idx) in recordSelectors" :key="idx" class="selector-row">
                <div class="selector-header">
                  <span class="selector-index">病历 {{ idx + 1 }}</span>
                  <el-button v-if="recordSelectors.length > 1" type="danger" size="small" text @click="removeRecordSelector(idx)">移除</el-button>
                </div>
                <el-select v-model="selector.recordId" placeholder="选择病历" filterable clearable
                           style="width: 100%" @change="(val) => handleSelectorChange(idx, val)">
                  <el-option v-for="r in records" :key="r.recordId"
                             :label="`${r.diseaseName}（${r.recordDate || '未知日期'}）`"
                             :value="r.recordId"
                             :disabled="isRecordIdSelected(r.recordId, idx)">
                    <span>{{ r.diseaseName }}</span>
                    <span style="float: right; color: #909399; font-size: 12px">
                      {{ planCountMap[r.recordId] !== undefined ? planCountMap[r.recordId] + '个计划' : '加载中...' }}
                    </span>
                  </el-option>
                </el-select>
                <div v-if="selector.recordId" class="record-detail-card">
                  <div class="record-header">
                    <h4>{{ getRecordName(selector.recordId) }}</h4>
                    <el-tag v-if="planCountMap[selector.recordId] > 0" type="success" size="small">已分析 ({{ planCountMap[selector.recordId] }}个计划)</el-tag>
                    <el-tag v-else type="info" size="small">未分析</el-tag>
                  </div>
                  <div class="detail-grid">
                    <div><label>诊断：</label><span>{{ getRecordField(selector.recordId, 'diagnosis') || '无' }}</span></div>
                    <div><label>处理意见：</label><span>{{ getRecordField(selector.recordId, 'treatment') || '无' }}</span></div>
                    <div><label>药品：</label><span>{{ getRecordField(selector.recordId, 'medicines') || '无' }}</span></div>
                  </div>
                  <el-button type="primary" size="small" @click="handleAnalyzeSingle(selector.recordId)" :loading="analyzingMap[selector.recordId]"
                             :disabled="planCountMap[selector.recordId] > 0">
                    {{ planCountMap[selector.recordId] > 0 ? '已分析' : 'AI分析生成计划' }}
                  </el-button>
                </div>
              </div>
              <!-- 操作区域 -->
              <div v-if="selectedRecordList.length > 0" class="record-actions-bar">
                <el-button type="warning" size="large" @click="handleGenerateCombinedPlan" :loading="combinedLoading" :disabled="selectedRecordList.length < 1">
                  AI分析
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- AI分析结果 -->
          <el-card v-if="conflictResult" class="section-card conflict-card">
            <template #header>
              <div class="card-header">
                <span>AI分析结果</span>
                <el-button size="small" @click="conflictResult = null">关闭</el-button>
              </div>
            </template>
            <!-- 概览 -->
            <div class="analysis-summary">
              <div class="summary-row">
                <div class="summary-item">
                  <div class="summary-label">检测药物</div>
                  <div class="summary-value highlight">{{ conflictResult.totalDrugs || 0 }} 种</div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">药物冲突</div>
                  <div class="summary-value" :class="conflictResult.hasConflict ? 'danger' : 'safe'">
                    {{ conflictResult.hasConflict ? '有' : '无' }}
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">风险等级</div>
                  <div class="summary-value">
                    <el-tag :type="riskLevelTagType" size="large">{{ riskLevelText }}</el-tag>
                  </div>
                </div>
                <div class="summary-item">
                  <div class="summary-label">涉及病历</div>
                  <div class="summary-value">{{ conflictResult.recordCount || selectedRecordList.length }} 个</div>
                </div>
              </div>
              <div v-if="conflictResult.allMedicines && conflictResult.allMedicines.length > 0" class="medicines-list">
                <span class="medicines-label">药品清单：</span>
                <el-tag v-for="med in conflictResult.allMedicines" :key="med" size="small" style="margin: 2px 4px 2px 0">{{ med }}</el-tag>
              </div>
            </div>
            <!-- 无冲突 -->
            <el-alert v-if="!conflictResult.hasConflict" type="success" :closable="false" title="✅ 未检测到药物冲突，当前用药方案安全" style="margin-top:12px" />
            <!-- 有冲突 -->
            <div v-else>
              <el-alert type="warning" title="检测到药物冲突" :description="'共 ' + conflictResult.conflicts.length + ' 项冲突，请查看下方的替代方案建议'" :closable="false" style="margin-bottom: 12px; margin-top:12px" />
              <el-table :data="conflictResult.conflicts" stripe style="width: 100%">
                <el-table-column prop="drugA" label="药品A" min-width="120" />
                <el-table-column prop="drugB" label="药品B" min-width="120" />
                <el-table-column prop="level" label="严重程度" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.level === 'HIGH' || row.level === 'CRITICAL' ? 'danger' : row.level === 'MEDIUM' ? 'warning' : 'info'" size="small">{{ row.level }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="冲突描述" min-width="180" show-overflow-tooltip />
                <el-table-column prop="suggestion" label="替代方案建议" min-width="360" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span style="color:#e67e22; font-weight:500; word-break:break-all">{{ row.suggestion }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <!-- 风险提示 -->
            <div v-if="riskWarnings.length > 0" style="margin-top:16px">
              <el-divider />
              <h4 style="margin: 0 0 8px; color: #e67e22">⚠️ 风险提示</h4>
              <el-table :data="riskWarnings" stripe>
                <el-table-column type="index" label="#" width="50" />
                <el-table-column prop="warning" label="提示内容" min-width="300" show-overflow-tooltip />
              </el-table>
            </div>
          </el-card>

          <!-- 总计划列表 -->
          <el-card v-if="planList.length > 0" class="section-card">
            <template #header>
              <div class="card-header">
                <span>📋 用药计划列表（共{{ planList.length }}条）</span>
                <span v-if="selectedRecordList.length > 1" class="plan-source-hint">来源：{{ selectedRecordList.length }}个病历</span>
                <el-button text type="primary" size="small" @click="viewPlanDetail()">查看日程</el-button>
              </div>
            </template>
            <el-table :data="planList" stripe style="width: 100%">
              <el-table-column prop="medicineName" label="药品名称" min-width="140" />
              <el-table-column prop="dosage" label="用量" width="80" />
              <el-table-column prop="frequency" label="频率" width="90" />
              <el-table-column prop="timesPerDay" label="每日次数" width="90" />
              <el-table-column prop="dailyProgress" label="执行进度" width="100">
                <template #default="{ row }"><el-progress :percentage="calcProgress(row)" :stroke-width="14" :status="row.status === 2 ? 'success' : ''" /></template>
              </el-table-column>
              <el-table-column prop="purpose" label="备注/替代说明" min-width="180" show-overflow-tooltip />
              <el-table-column prop="statusDescription" label="状态" width="80">
                <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'warning'" size="small">{{ row.statusDescription }}</el-tag></template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- 每日日程表 — 改为弹窗展示 -->
        </div>

        <!-- Tab 2: 服药情况 -->
        <div v-show="activeTab === 'daily'" class="tab-content">
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span>今日服药情况（{{ today }}）</span>
                <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期" @change="loadDailyRecords" style="width: 160px" />
              </div>
            </template>
            <template v-if="medicationRecords.length > 0">
              <div v-for="rec in medicationRecords" :key="rec.takeId" class="daily-item">
                <div class="daily-info">
                  <span class="medicine-name">{{ rec.medicineName || '药品' }}</span>
                  <span class="dosage">{{ rec.dosage || '' }}</span>
                  <span class="time">{{ formatTime(rec.scheduledTime) }}</span>
                  <el-tag :type="rec.status === 1 ? 'success' : rec.status === 2 ? 'danger' : 'info'" size="small">{{ rec.statusText }}</el-tag>
                </div>
                <div class="daily-actions">
                  <el-button v-if="rec.status === 0 && isNearestPending(rec)" text type="primary" @click="openPhotoDialog(rec)">拍照确认</el-button>
                  <span v-if="rec.photoUrl" class="photo-link" @click="previewPhoto(rec.photoUrl)">查看照片</span>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无服药记录" />
          </el-card>

          <!-- 拍照弹窗 -->
          <el-dialog v-model="photoDialogVisible" title="拍照确认服药" width="400px">
            <div class="photo-upload">
              <el-upload ref="uploadRef" :auto-upload="false" accept="image/*" :on-change="handleFileChange" :show-file-list="false">
                <el-button type="primary">选择药片照片</el-button>
              </el-upload>
              <img v-if="previewUrl" :src="previewUrl" class="preview-img" />
              <p v-if="selectedFile" class="file-name">{{ selectedFile.name }}</p>
            </div>
            <template #footer>
              <el-button @click="photoDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="uploading" @click="confirmPhoto">确认服药</el-button>
            </template>
          </el-dialog>

          <el-image-viewer v-if="showPreview" :url-list="[previewPhotoUrl]" @close="showPreview=false" />
        </div>

        <!-- Tab 3: 数据分析 -->
        <div v-show="activeTab === 'analysis'" class="tab-content">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="stat-card">
                <div class="stat-value">{{ complianceRate }}%</div>
                <div class="stat-label">依从率</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card">
                <div class="stat-value">{{ missedCount }}</div>
                <div class="stat-label">漏服次数</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card">
                <div class="stat-value">{{ activePlanCount }}</div>
                <div class="stat-label">进行中计划</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 计划进度 -->
          <el-card class="section-card" v-if="planList.length > 0">
            <template #header>计划进度</template>
            <div v-for="plan in planList" :key="plan.planId" class="plan-progress">
              <div class="progress-header">
                <span>{{ plan.medicineName }}</span>
                <span>{{ plan.dailyProgress }}（{{ plan.statusDescription }}）</span>
              </div>
              <el-progress :percentage="calcProgress(plan)" :status="plan.status === 2 ? 'success' : ''" />
            </div>
          </el-card>

          <!-- 历史计划 -->
          <el-card class="section-card">
            <template #header>历史计划</template>
            <el-table :data="historyPlans" stripe v-loading="historyLoading">
              <el-table-column prop="medicineName" label="药品" />
              <el-table-column prop="dosage" label="用量" />
              <el-table-column prop="startDate" label="开始日期" />
              <el-table-column prop="endDate" label="结束日期" />
              <el-table-column prop="statusDescription" label="状态">
                <template #default="{ row }"><el-tag :type="row.status === 2 ? 'success' : 'info'">{{ row.statusDescription }}</el-tag></template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </div>
  </div>

  <!-- 总计划日程弹窗 -->
  <el-dialog v-model="scheduleDialogVisible" title="总计划每日日程" width="90%" top="5vh" :close-on-click-modal="false">
    <template v-if="flatScheduleData.length > 0">
      <div style="margin-bottom:16px;display:flex;gap:16px;flex-wrap:wrap">
        <el-tag type="info">共 {{ totalScheduleDays }} 天</el-tag>
        <el-tag type="primary">涉及 {{ totalScheduleMedicines }} 种药品</el-tag>
      </div>
      <el-table :data="flatScheduleData" stripe style="width:100%" max-height="70vh" v-loading="scheduleLoading">
        <el-table-column label="进度" min-width="180">
          <template #default="{ row }">
            <span style="font-weight:600">第 {{ row.dayNumber }} 天</span>
          </template>
        </el-table-column>
        <el-table-column label="日期" min-width="240">
          <template #default="{ row }">{{ row.date }}</template>
        </el-table-column>
        <el-table-column label="药品及数量" min-width="800">
          <template #default="{ row }">
            <div class="schedule-medicine-list">
              <div v-for="med in row.medicines" :key="med.takeId" class="schedule-medicine-item">
                <span class="med-name">{{ med.medicineName || '未知药品' }}</span>
                <span v-if="med.dosage" class="med-dosage">（{{ med.dosage }}）</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </template>
    <el-empty v-else description="暂无日程数据" />
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecordsByUserId } from '@/api/record'
import { getPlansByRecordId, countPlansByRecordId, analyzePrescription,
         getDailyByUserIdAndDate, getHistoryPlans,
         uploadTakePhoto, getCombinedSchedule, applyConflictReplacements } from '@/api/plan'
import { checkConflicts } from '@/api/prescription'

// 假设 userId=1（TODO: 登录后改为实际userId）
const userId = 1
const today = new Date().toISOString().slice(0, 10)

// 状态变量
const activeTab = ref('plans')
const records = ref([])
const planList = ref([])
const dailyRecords = ref([])
const medicationRecords = ref([])
const historyPlans = ref([])
const selectedPlan = ref(null)
const planCountMap = ref({})
const analyzing = ref(false)
const analyzingMap = ref({})
const combinedLoading = ref(false)
const conflictResult = ref(null)
const riskWarnings = ref([])
const hasPlans = computed(() => planList.value.length > 0)
const historyLoading = ref(false)
const combinedPlanSource = ref([]) // 总计划来源病历列表

// 日程弹窗
const scheduleDialogVisible = ref(false)
const scheduleData = ref([])
const scheduleLoading = ref(false)

// 展开后的扁平日程数据
const flatScheduleData = computed(() => {
  const flat = []
  for (const day of scheduleData.value) {
    if (day.periods && day.periods.length > 0) {
      for (const period of day.periods) {
        flat.push({
          dayNumber: day.dayNumber,
          date: day.date,
          period: period.period,
          medicines: period.medicines || []
        })
      }
    }
  }
  return flat
})
const totalScheduleDays = computed(() => scheduleData.value.length)
const totalScheduleMedicines = computed(() => {
  const set = new Set()
  for (const day of scheduleData.value) {
    if (day.periods) {
      for (const p of day.periods) {
        if (p.medicines) {
          for (const m of p.medicines) {
            if (m.medicineName) set.add(m.medicineName)
          }
        }
      }
    }
  }
  return set.size
})

// 风险等级计算（优先使用后端返回的风险等级，否则基于冲突严重程度推算）
const riskLevelText = computed(() => {
  if (!conflictResult.value) return 'LOW'
  if (conflictResult.value.riskLevel) return conflictResult.value.riskLevel
  if (!conflictResult.value.conflicts || conflictResult.value.conflicts.length === 0) return 'LOW'
  const levels = conflictResult.value.conflicts.map(c => c.level)
  if (levels.some(l => l === 'CRITICAL' || l === 'HIGH')) return 'HIGH'
  if (levels.some(l => l === 'MEDIUM')) return 'MEDIUM'
  return 'LOW'
})
const riskLevelTagType = computed(() => {
  const l = riskLevelText.value
  if (l === 'CRITICAL' || l === 'HIGH') return 'danger'
  if (l === 'MEDIUM') return 'warning'
  return 'info'
})

// 选择器模型：每个选择器一个对象，可独立选病历
const recordSelectors = ref([{ recordId: null }])

// 获取所有已选病历ID列表（去重）
const selectedRecordList = computed(() => {
  const ids = recordSelectors.value.map(s => s.recordId).filter(id => id != null)
  return [...new Set(ids)]
})

/** 检查某个病历是否已被其他选择器选中（禁用重复选择） */
function isRecordIdSelected(recordId, currentIdx) {
  return recordSelectors.value.some((s, i) => i !== currentIdx && s.recordId === recordId)
}

function getRecordName(recordId) {
  const r = records.value.find(r => r.recordId === recordId)
  return r ? r.diseaseName : '未知病历'
}
function getRecordField(recordId, field) {
  const r = records.value.find(r => r.recordId === recordId)
  return r ? r[field] : null
}

// 服药页面
const selectedDate = ref(today)
const photoDialogVisible = ref(false)
const selectedFile = ref(null)
const previewUrl = ref(null)
const uploading = ref(false)
const currentTakeId = ref(null)
const showPreview = ref(false)
const previewPhotoUrl = ref('')

// 数据分析
const activePlanCount = computed(() => planList.value.filter(p => p.status === 1).length)
const complianceRate = computed(() => {
  let total = 0, taken = 0
  medicationRecords.value.forEach(r => {
    total++
    if (r.status === 1) taken++
  })
  return total > 0 ? Math.round(taken / total * 100) : 0
})
const missedCount = computed(() =>
  medicationRecords.value.filter(r => r.status === 2).length)

// 初始化：获取所有病历
onMounted(async () => {
  try {
    const res = await getRecordsByUserId(userId)
    records.value = res.data || []
    // 加载各病历的计划数
    for (const r of records.value) {
      try {
        const cntRes = await countPlansByRecordId(r.recordId)
        planCountMap.value[r.recordId] = cntRes.data || 0
      } catch {}
    }
    // 默认初始一个选择器
    if (records.value.length > 0) {
      recordSelectors.value = [{ recordId: null }]
    }
    // 加载当日服药情况
    if (records.value.length > 0) loadDailyRecords()
    // 加载历史计划
    loadHistoryPlans()

  } catch (e) {
    console.error('加载病历失败', e)
  }

  // 监听从病例管理页面删除病历后的事件，刷新三栏数据
  window.addEventListener('refresh-medication-plans', refreshPlans)
})

onUnmounted(() => {
  window.removeEventListener('refresh-medication-plans', refreshPlans)
})

async function refreshPlans() {
  try {
    const res = await getRecordsByUserId(userId)
    records.value = res.data || []
    for (const r of records.value) {
      try {
        const cntRes = await countPlansByRecordId(r.recordId)
        planCountMap.value[r.recordId] = cntRes.data || 0
      } catch {}
    }
    // 重置选择器，清空已选的无效ID
    for (const sel of recordSelectors.value) {
      if (sel.recordId != null && !records.value.find(r => r.recordId === sel.recordId)) {
        sel.recordId = null
      }
    }
    if (selectedRecordList.value.length > 0) {
      await loadPlansForSelected()
    }
    loadHistoryPlans()
  } catch (e) {
    console.error('刷新数据失败', e)
  }
}

// === 选择器操作 ===

/** 添加一个病历选择器 */
function addRecordSelector() {
  recordSelectors.value.push({ recordId: null })
}

/** 移除一个病历选择器 */
function removeRecordSelector(index) {
  if (recordSelectors.value.length <= 1) return
  recordSelectors.value.splice(index, 1)
  loadPlansForSelected()
}

/** 选择器值变更 */
async function handleSelectorChange(index, val) {
  await loadPlansForSelected()
}

/** 加载所有已选病历的用药计划 */
async function loadPlansForSelected() {
  const ids = selectedRecordList.value
  if (ids.length === 0) {
    planList.value = []
    dailyRecords.value = []
    selectedPlan.value = null
    return
  }
  try {
    const allPlans = []
    for (const rid of ids) {
      const res = await getPlansByRecordId(rid)
      allPlans.push(...(res.data || []))
    }
    planList.value = allPlans
    selectedPlan.value = null
    dailyRecords.value = []
  } catch (e) {
    console.error('加载计划失败', e)
  }
}

// 单病历AI分析
async function handleAnalyzeSingle(recordId) {
  if (analyzingMap.value[recordId]) return
  if (planCountMap.value[recordId] > 0) {
    ElMessage.info('该病历已有用药计划')
    return
  }
  analyzingMap.value[recordId] = true
  try {
    const res = await analyzePrescription(userId, recordId)
    ElMessage.success('用药计划创建成功')
    if (res.data?.warnings && res.data.warnings.length > 0) {
      const medWarnings = res.data.warnings.filter(w => 
        !w.startsWith('病史风险') && !w.startsWith('过敏风险') && 
        !w.startsWith('严重警告') && !w.startsWith('替代方案') &&
        !w.startsWith('AI') &&
        !w.includes('冲突') &&
        !/^[A-Za-z\u4e00-\u9fa5]+\(/.test(w))
      riskWarnings.value = medWarnings.map(w => ({ warning: w }))
    }
    await loadPlansForSelected()
    const cntRes = await countPlansByRecordId(recordId)
    planCountMap.value[recordId] = cntRes.data || 0
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '分析失败')
  } finally {
    analyzingMap.value[recordId] = false
  }
}

// 生成总用药计划（多病历冲突检测 + 计划合并去重）
async function handleGenerateCombinedPlan() {
  const ids = selectedRecordList.value
  if (ids.length === 0) {
    ElMessage.warning('请至少选择一个病历')
    return
  }
  combinedLoading.value = true
  conflictResult.value = null
  riskWarnings.value = []
  try {
    // 先分析未分析的病历
    for (const rid of ids) {
      if (planCountMap.value[rid] === 0) {
        const res = await analyzePrescription(userId, rid)
        if (res.data?.warnings && res.data.warnings.length > 0) {
          const medWarnings = res.data.warnings.filter(w => 
            !w.startsWith('病史风险') && !w.startsWith('过敏风险') && 
            !w.startsWith('严重警告') && !w.startsWith('替代方案') &&
            !w.startsWith('AI') &&
            !w.includes('冲突') &&
            !/^[A-Za-z\u4e00-\u9fa5]+\(/.test(w))
          riskWarnings.value = [...riskWarnings.value, ...medWarnings.map(w => ({ warning: w }))]
        }
        const cntRes = await countPlansByRecordId(rid)
        planCountMap.value[rid] = cntRes.data || 0
      }
    }
    // 冲突检测
    const res = await checkConflicts(ids)
    conflictResult.value = res.data
    // 自动应用替换建议到计划
    if (res.data?.conflicts && res.data.conflicts.length > 0) {
      try {
        await applyConflictReplacements(ids, res.data.conflicts)
      } catch (e) {
        console.warn('应用冲突替换失败', e)
      }
    }
    // 从冲突检测结果中提取风险提示（仅保留药物相关的）
    if (res.data?.warnings && res.data.warnings.length > 0) {
      const existingWarnings = new Set(riskWarnings.value.map(w => w.warning))
      for (const w of res.data.warnings) {
        if (w.startsWith('病史风险') || w.startsWith('过敏风险') || w.startsWith('严重警告') || w.startsWith('风险标签') || w.startsWith('替代方案') || w.startsWith('AI') || w.includes('冲突') || /^[A-Za-z\u4e00-\u9fa5]+\(/.test(w)) continue
        if (!existingWarnings.has(w)) {
          riskWarnings.value.push({ warning: w })
          existingWarnings.add(w)
        }
      }
    }
    // 加载总计划（合并去重）
    await loadPlansForSelected()
    combinedPlanSource.value = ids
    if (res.data?.hasConflict) {
      ElMessage.warning('检测到药物冲突，请查看详情中的建议方案')
    } else {
      ElMessage.success('总用药计划已生成，未检测到药物冲突')
    }
  } catch (e) {
    ElMessage.error('生成总计划失败: ' + (e.response?.data?.message || e.message))
  } finally {
    combinedLoading.value = false
  }
}

// 查看计划日程 — 打开总计划每日日程弹窗
async function viewPlanDetail() {
  if (!planList.value || planList.value.length === 0) {
    ElMessage.warning('暂无计划数据')
    return
  }
  scheduleDialogVisible.value = true
  scheduleLoading.value = true
  scheduleData.value = []
  try {
    const planIds = planList.value.map(p => p.planId)
    const res = await getCombinedSchedule(planIds)
    scheduleData.value = res.data || []
  } catch (e) {
    console.error('加载合并日程失败', e)
    ElMessage.error('加载日程失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 每日服药
async function loadDailyRecords() {
  try {
    const date = typeof selectedDate.value === 'string'
      ? selectedDate.value : selectedDate.value?.toISOString?.().slice(0, 10) || today
    const res = await getDailyByUserIdAndDate(userId, date)
    medicationRecords.value = (res.data || []).map(r => ({
      ...r,
      scheduledTime: r.scheduledTime
    }))
  } catch (e) {
    console.error('加载服药记录失败', e)
  }
}

// 判断是否为最近的待服药记录
function isNearestPending(rec) {
  if (rec.status !== 0) return false
  const now = new Date()
  const target = new Date(rec.scheduledTime)
  // 只显示今日未服药且距离当前时间最近的记录
  const pendingRecords = medicationRecords.value
    .filter(r => r.status === 0)
    .sort((a, b) => new Date(a.scheduledTime) - new Date(b.scheduledTime))
  return pendingRecords.length > 0 && pendingRecords[0].takeId === rec.takeId
}

// 打开拍照弹窗
function openPhotoDialog(rec) {
  currentTakeId.value = rec.takeId
  selectedFile.value = null
  previewUrl.value = null
  photoDialogVisible.value = true
}

// 选择文件
function handleFileChange(file) {
  selectedFile.value = file.raw
  previewUrl.value = URL.createObjectURL(file.raw)
}

// 确认服药
async function confirmPhoto() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择药片照片')
    return
  }
  uploading.value = true
  try {
    await uploadTakePhoto(currentTakeId.value, selectedFile.value)
    ElMessage.success('已确认服药')
    photoDialogVisible.value = false
    loadDailyRecords()
  } catch (e) {
    ElMessage.error('确认失败：' + (e.response?.data?.message || e.message))
  } finally {
    uploading.value = false
  }
}

// 预览照片
function previewPhoto(url) {
  previewPhotoUrl.value = 'http://localhost:8080' + url
  showPreview.value = true
}

// 历史计划
async function loadHistoryPlans() {
  historyLoading.value = true
  try {
    const res = await getHistoryPlans(userId, 2)
    historyPlans.value = res.data || []
  } catch (e) {
    console.error('加载历史计划失败', e)
  } finally {
    historyLoading.value = false
  }
}

// 进度百分比
function calcProgress(plan) {
  if (!plan.dailyProgress) return 0
  const [taken, total] = plan.dailyProgress.split('/').map(Number)
  return total > 0 ? Math.round(taken / total * 100) : 0
}

// 工具函数
function formatDateTime(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  return d.toLocaleString('zh-CN', { hour12: false })
}
function formatTime(dt) {
  if (!dt) return '-'
  const d = new Date(dt)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
}
</script>

<style lang="scss" scoped>
.medication-plan-page {
  .plan-layout {
    display: flex;
    gap: 20px;
    min-height: 600px;
    padding: 20px;
  }

  .plan-sidebar {
    width: 160px;
    min-width: 160px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    position: sticky;
    top: 20px;
    align-self: flex-start;
    max-height: calc(100vh - 40px);
    overflow-y: auto;
    .sidebar-title {
      padding: 16px 16px 8px;
      font-size: 12px;
      color: #909399;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    .sidebar-item {
      padding: 12px 16px;
      cursor: pointer;
      font-size: 14px;
      color: #606266;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      gap: 8px;
      &:hover {
        background: #f5f7fa;
        color: #409eff;
      }
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        font-weight: 500;
      }
    }
    .sidebar-icon {
      font-size: 16px;
    }
  }

  .plan-content {
    flex: 1;
    min-width: 0;
    min-height: 400px;
  }

  .section-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .record-selectors {
    width: 100%;
  }

  .selector-row {
    background: #fafbfc;
    border: 1px solid #e4e7ed;
    border-radius: 10px;
    padding: 16px;
    margin-bottom: 12px;
    transition: all 0.2s ease;
    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 8px rgba(64,158,255,0.08);
    }
  }

  .selector-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .selector-index {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .record-info {
    margin-top: 15px;
  }

  .record-detail-card {
    background: linear-gradient(135deg, #f5f7fa 0%, #eef1f6 100%);
    padding: 16px 20px;
    border-radius: 10px;
    margin-bottom: 12px;
    border: 1px solid #e4e7ed;
    transition: all 0.2s ease;
    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px rgba(64,158,255,0.12);
    }
  }

  .record-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    h4 {
      margin: 0;
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }
  }

  .detail-grid {
    div {
      margin: 0;
      font-size: 14px;
      line-height: 1.8;
    }
    label {
      color: #909399;
      margin-right: 4px;
    }
    span {
      color: #303133;
    }
  }

  .record-actions-bar {
    display: flex;
    gap: 12px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
    flex-wrap: wrap;
    justify-content: center;
  }

  .analysis-summary {
    .summary-row {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      margin-bottom: 12px;
    }
    .summary-item {
      flex: 1;
      min-width: 100px;
      text-align: center;
      padding: 12px 8px;
      background: #f8f9fa;
      border-radius: 8px;
    }
    .summary-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 6px;
    }
    .summary-value {
      font-size: 22px;
      font-weight: 700;
      color: #303133;
      &.highlight { color: #409eff; }
      &.danger { color: #f56c6c; }
      &.safe { color: #67c23a; }
    }
  }
  .medicines-list {
    margin-top: 8px;
    padding: 10px 12px;
    background: #f0f9ff;
    border-radius: 6px;
    .medicines-label {
      font-size: 13px;
      color: #606266;
      margin-right: 6px;
    }
  }

  .conflict-card {
    margin-bottom: 20px;
    border: 1px solid #e6a23c;
    border-radius: 8px;
    .el-card__header {
      background: #fdf6ec;
      border-bottom: 1px solid #e6a23c;
    }
  }

  .warning-card {
    margin-bottom: 20px;
    border: 1px solid #f56c6c;
    border-radius: 8px;
    .el-card__header {
      background: #fef0f0;
      border-bottom: 1px solid #f56c6c;
    }
  }

  .plan-source-hint {
    font-size: 13px;
    color: #909399;
  }

  .daily-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #ebeef5;
    transition: background 0.15s;
    &:hover {
      background: #f5f7fa;
      margin: 0 -12px;
      padding: 14px 12px;
      border-radius: 6px;
    }
    &:last-child {
      border-bottom: none;
    }
  }

  .daily-info {
    display: flex;
    align-items: center;
    gap: 15px;
    flex-wrap: wrap;
    .medicine-name {
      font-weight: 600;
      color: #303133;
      font-size: 15px;
    }
    .dosage {
      color: #909399;
      font-size: 13px;
    }
    .time {
      color: #606266;
      font-size: 13px;
      font-family: monospace;
    }
  }

  .daily-actions {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .photo-link {
    color: #409eff;
    cursor: pointer;
    font-size: 13px;
    &:hover {
      text-decoration: underline;
    }
  }

  .photo-upload {
    text-align: center;
    padding: 20px;
  }

  .preview-img {
    max-width: 100%;
    max-height: 300px;
    margin-top: 15px;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }

  .file-name {
    margin-top: 10px;
    color: #909399;
    font-size: 13px;
  }

  .stat-card {
    text-align: center;
    padding: 20px 10px;
    border-radius: 8px;
    transition: transform 0.2s;
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
  }

  .stat-value {
    font-size: 40px;
    font-weight: bold;
    color: #409eff;
  }

  .stat-label {
    color: #909399;
    margin-top: 8px;
    font-size: 14px;
  }

  .plan-progress {
    margin-bottom: 18px;
    &:last-child {
      margin-bottom: 0;
    }
    .progress-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
      font-size: 14px;
      color: #303133;
    }
  }

  .schedule-medicine-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    width: 100%;
  }
  .schedule-medicine-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background: #f5f7fa;
    border-radius: 4px;
    .med-name {
      font-weight: 500;
      color: #303133;
      font-size: 13px;
    }
    .med-dosage {
      color: #909399;
      font-size: 12px;
    }
  }
}
</style>
