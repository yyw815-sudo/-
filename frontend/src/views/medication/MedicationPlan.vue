<template>
  <div class="medication-plan-page">
    <!-- 三栏导航 -->
    <el-tabs v-model="activeTab" class="plan-tabs">
      <el-tab-pane label="用药计划" name="plans" />
      <el-tab-pane label="服药情况" name="daily" />
      <el-tab-pane label="数据分析" name="analysis" />
    </el-tabs>

    <!-- Tab 1: 用药计划 -->
    <div v-show="activeTab === 'plans'" class="tab-content">
      <!-- 病历选择 -->
      <el-card class="section-card">
        <template #header>选择病历</template>
        <el-select v-model="selectedRecordId" placeholder="选择病历以查看或创建用药计划"
                   style="width: 100%" @change="handleRecordChange" filterable>
          <el-option v-for="r in records" :key="r.recordId"
                     :label="`${r.diseaseName}（${r.recordDate || '未知日期'}）`"
                     :value="r.recordId">
            <span>{{ r.diseaseName }}</span>
            <span style="float: right; color: #909399; font-size: 12px">
              {{ planCountMap[r.recordId] !== undefined ? planCountMap[r.recordId] + '个计划' : '加载中...' }}
            </span>
          </el-option>
        </el-select>
        <div v-if="selectedRecord" class="record-info">
          <div class="record-detail-card">
            <h4>{{ selectedRecord.diseaseName }}</h4>
            <div class="detail-grid">
              <div><label>诊断：</label><span>{{ selectedRecord.diagnosis || '无' }}</span></div>
              <div><label>处方：</label><span>{{ selectedRecord.prescription || '无' }}</span></div>
              <div><label>医生：</label><span>{{ selectedRecord.doctor || '未知' }}</span></div>
              <div><label>医院：</label><span>{{ selectedRecord.hospital || '未知' }}</span></div>
              <div><label>日期：</label><span>{{ selectedRecord.recordDate || '未知' }}</span></div>
            </div>
          </div>
          <el-button type="primary" @click="handleAnalyzePrescription" :loading="analyzing"
                     :disabled="!selectedRecord.prescription || hasPlans">
            {{ hasPlans ? '已有用药计划' : 'AI分析处方创建计划' }}
          </el-button>
          <span v-if="!selectedRecord.prescription" class="warn-text">该病历暂无处方信息</span>
        </div>
      </el-card>

      <!-- 总计划列表 -->
      <el-card v-if="planList.length > 0" class="section-card">
        <template #header>
          <span>用药计划列表（共{{ planList.length }}条）</span>
        </template>
        <el-table :data="planList" stripe>
          <el-table-column prop="medicineName" label="药品" min-width="120" />
          <el-table-column prop="dosage" label="用量" width="80" />
          <el-table-column prop="frequency" label="频率" width="80" />
          <el-table-column prop="timesPerDay" label="每日次数" width="80" />
          <el-table-column prop="dailyProgress" label="进度" width="100" />
          <el-table-column prop="statusDescription" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'warning'">
                {{ row.statusDescription }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button text type="primary" @click="viewPlanDetail(row)">查看日程</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 每日日程表 -->
      <el-card v-if="selectedPlan" class="section-card">
        <template #header>
          <span>{{ selectedPlan.medicineName || '' }} - 每日服药日程（{{ selectedPlan.startDate }} ~ {{ selectedPlan.endDate || '长期' }}）</span>
        </template>
        <el-table :data="dailyRecords" stripe max-height="400">
          <el-table-column prop="scheduledTime" label="计划时间" width="160">
            <template #default="{ row }">{{ formatDateTime(row.scheduledTime) }}</template>
          </el-table-column>
          <el-table-column prop="statusText" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'">
                {{ row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="aiResult" label="识别结果" min-width="120" />
          <el-table-column prop="aiAccuracy" label="准确率" width="80">
            <template #default="{ row }">
              {{ row.aiAccuracy ? (row.aiAccuracy * 100).toFixed(1) + '%' : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Tab 2: 服药情况 -->
    <div v-show="activeTab === 'daily'" class="tab-content">
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span>今日服药情况（{{ today }}）</span>
            <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期"
                            @change="loadDailyRecords" style="width: 160px" />
          </div>
        </template>
        <template v-if="medicationRecords.length > 0">
          <div v-for="rec in medicationRecords" :key="rec.takeId" class="daily-item">
            <div class="daily-info">
              <span class="medicine-name">{{ rec.medicineName || '药品' }}</span>
              <span class="dosage">{{ rec.dosage || '' }}</span>
              <span class="time">{{ formatTime(rec.scheduledTime) }}</span>
              <el-tag :type="rec.status === 1 ? 'success' : rec.status === 2 ? 'danger' : 'info'" size="small">
                {{ rec.statusText }}
              </el-tag>
            </div>
            <div class="daily-actions">
              <el-button v-if="rec.status === 0 && isNearestPending(rec)" text type="primary"
                         @click="openPhotoDialog(rec)">拍照确认</el-button>
              <span v-if="rec.photoUrl" class="photo-link" @click="previewPhoto(rec.photoUrl)">查看照片</span>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无服药记录" />
      </el-card>

      <!-- 拍照弹窗 -->
      <el-dialog v-model="photoDialogVisible" title="拍照确认服药" width="400px">
        <div class="photo-upload">
          <el-upload ref="uploadRef" :auto-upload="false" accept="image/*"
                     :on-change="handleFileChange" :show-file-list="false">
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

      <!-- 图片预览弹窗 -->
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
            <template #default="{ row }">
              <el-tag :type="row.status === 2 ? 'success' : 'info'">{{ row.statusDescription }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecordsByUserId } from '@/api/record'
import { getPlansByRecordId, countPlansByRecordId, analyzePrescription,
         getDailyByPlanId, getDailyByUserIdAndDate, getHistoryPlans,
         uploadTakePhoto } from '@/api/plan'

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
const selectedRecordId = ref(null)
const selectedRecord = computed(() =>
  records.value.find(r => r.recordId === selectedRecordId.value))
const selectedPlan = ref(null)
const planCountMap = ref({})
const analyzing = ref(false)
const hasPlans = computed(() => planList.value.length > 0)
const historyLoading = ref(false)

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
    // 自动选择第一个有计划的病历或第一个病历
    const hasPlanRecord = records.value.find(r => planCountMap.value[r.recordId] > 0)
    selectedRecordId.value = hasPlanRecord ? hasPlanRecord.recordId
                           : records.value.length > 0 ? records.value[0].recordId
                           : null
    if (selectedRecordId.value) handleRecordChange(selectedRecordId.value)
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
    if (selectedRecordId.value) {
      const stillExists = records.value.find(r => r.recordId === selectedRecordId.value)
      if (stillExists) {
        handleRecordChange(selectedRecordId.value)
      } else {
        selectedRecordId.value = records.value.length > 0 ? records.value[0].recordId : null
        if (selectedRecordId.value) handleRecordChange(selectedRecordId.value)
      }
    }
    loadHistoryPlans()
  } catch (e) {
    console.error('刷新数据失败', e)
  }
}

// 切换病历
async function handleRecordChange(recordId) {
  if (!recordId) return
  try {
    const res = await getPlansByRecordId(recordId)
    planList.value = res.data || []
    selectedPlan.value = null
    dailyRecords.value = []
  } catch (e) {
    console.error('加载计划失败', e)
  }
}

// AI分析处方
async function handleAnalyzePrescription() {
  if (!selectedRecord.value?.prescription) {
    ElMessage.warning('该病历暂无处方信息')
    return
  }
  analyzing.value = true
  try {
    await analyzePrescription(userId, selectedRecordId.value)
    ElMessage.success('用药计划创建成功')
    await handleRecordChange(selectedRecordId.value)
    // 刷新计划数
    const cntRes = await countPlansByRecordId(selectedRecordId.value)
    planCountMap.value[selectedRecordId.value] = cntRes.data || 0
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '分析失败')
  } finally {
    analyzing.value = false
  }
}

// 查看计划日程
async function viewPlanDetail(plan) {
  selectedPlan.value = plan
  try {
    const res = await getDailyByPlanId(plan.planId)
    dailyRecords.value = (res.data || []).map(r => ({
      ...r,
      scheduledTime: r.scheduledTime
    }))
  } catch (e) {
    console.error('加载日程失败', e)
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
.medication-plan-page { padding: 20px; }
.plan-tabs { margin-bottom: 20px; }
.tab-content { min-height: 400px; }
.section-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.record-info { margin-top: 15px; }
.record-detail-card {
  background: #f5f7fa; padding: 15px; border-radius: 8px; margin-bottom: 15px;
  h4 { margin: 0 0 10px; color: #303133; }
}
.detail-grid {
  div { margin: 5px 0; font-size: 14px; }
  label { color: #909399; }
}
.warn-text { color: #e6a23c; margin-left: 10px; font-size: 13px; }
.daily-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 0; border-bottom: 1px solid #ebeef5;
  &:last-child { border-bottom: none; }
}
.daily-info {
  display: flex; align-items: center; gap: 15px;
  .medicine-name { font-weight: bold; color: #303133; }
  .dosage { color: #909399; font-size: 13px; }
  .time { color: #606266; font-size: 13px; }
}
.daily-actions { display: flex; gap: 10px; align-items: center; }
.photo-link { color: #409eff; cursor: pointer; font-size: 13px; &:hover { text-decoration: underline; } }
.photo-upload { text-align: center; padding: 20px; }
.preview-img { max-width: 100%; max-height: 300px; margin-top: 15px; border-radius: 4px; }
.file-name { margin-top: 10px; color: #909399; font-size: 13px; }
.stat-card { text-align: center; padding: 10px; }
.stat-value { font-size: 36px; font-weight: bold; color: #409eff; }
.stat-label { color: #909399; margin-top: 8px; font-size: 14px; }
.plan-progress {
  margin-bottom: 15px;
  .progress-header { display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 14px; }
}
</style>
