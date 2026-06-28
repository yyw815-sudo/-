<template>
  <div class="reminder-page">
    <el-tabs v-model="activeTab" class="reminder-tabs">
      <el-tab-pane label="提醒设置" name="settings">
        <el-card>
          <div class="card-header">
            <h3>提醒配置</h3>
            <el-button type="primary" @click="handleAddReminder">添加提醒</el-button>
          </div>
          <el-table :data="reminders" border :loading="loading">
            <el-table-column prop="reminderId" label="ID" width="80" />
            <el-table-column prop="planId" label="计划ID" width="90">
              <template #default="{ row }">
                <span v-if="row.planId">{{ row.planId }}</span>
                <span v-else style="color: #C0C4CC;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="reminderTime" label="提醒时间" width="120" />
            <el-table-column prop="channel" label="渠道" width="120">
              <template #default="{ row }">
                <el-tag :type="getChannelType(row.channel)" size="small">
                  {{ getChannelLabel(row.channel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)" size="small">第{{ row.level }}级</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="handleToggle(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleSendReminder(row)">发送提醒</el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="提醒记录" name="logs">
        <el-card>
          <h3>提醒发送记录</h3>
          <el-table :data="reminderLogs" border :loading="logsLoading">
            <el-table-column prop="logId" label="ID" width="80" />
            <el-table-column prop="channel" label="渠道" width="120">
              <template #default="{ row }">
                <el-tag :type="getChannelType(row.channel)" size="small">
                  {{ getChannelLabel(row.channel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="提醒内容" show-overflow-tooltip>
              <template #default="{ row }">
                <span style="color: #303133;">{{ row.content || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="response" label="用户响应" width="120">
              <template #default="{ row }">
                <span v-if="row.response" style="color: #67C23A;">{{ row.response }}</span>
                <span v-else style="color: #C0C4CC;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="sendTime" label="发送时间" width="180">
              <template #default="{ row }">
                <span>{{ row.sendTime ? formatTime(row.sendTime) : '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="addDialogVisible"
      title="添加提醒"
      width="550px"
    >
      <el-form :model="addForm" label-width="120px">
        <el-form-item label="选择用药计划">
          <el-select
            v-model="addForm.planId"
            placeholder="请选择用药计划"
            style="width: 100%"
            @change="handlePlanChange"
          >
            <el-option
              v-for="plan in plans"
              :key="plan.planId"
              :label="`${plan.medicineName} - ${plan.dosage} - ${plan.frequency}`"
              :value="plan.planId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="selectedPlan" label="计划详情">
          <el-descriptions :column="1" border size="small" style="width: 100%">
            <el-descriptions-item label="药品">{{ selectedPlan.medicineName }}</el-descriptions-item>
            <el-descriptions-item label="剂量">{{ selectedPlan.dosage }}</el-descriptions-item>
            <el-descriptions-item label="频率">{{ selectedPlan.frequency }}</el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ selectedPlan.startDate }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-time-picker
            v-model="addForm.reminderTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择提醒时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="选择提醒级别">
          <el-checkbox-group v-model="addForm.levels">
            <el-checkbox :value="1" label="第1级 - APP推送" />
            <el-checkbox :value="2" label="第2级 - 短信提醒" />
            <el-checkbox :value="3" label="第3级 - 电话提醒" />
          </el-checkbox-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            可同时选择多个级别，每个级别会创建独立的提醒
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddReminderSubmit">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="appReminderDialogVisible"
      title="用药提醒 - APP通知"
      width="450px"
      center
      :close-on-click-modal="false"
    >
      <div class="app-reminder-content">
        <el-icon :size="64" color="#409EFF">
          <BellFilled />
        </el-icon>
        <h2 style="color: #409EFF; margin: 20px 0;">该服药啦！</h2>
        <el-card shadow="hover" style="margin: 20px 0;">
          <p class="reminder-detail"><strong>时间：</strong>{{ currentReminder.reminderTime }}</p>
          <p class="reminder-detail"><strong>渠道：</strong>{{ getChannelLabel(currentReminder.channel) }}</p>
          <p class="reminder-detail"><strong>级别：</strong>第{{ currentReminder.level }}级</p>
        </el-card>
        <p class="reminder-tip">请确认是否已服药</p>
      </div>
      <template #footer>
        <el-button type="success" size="large" @click="handleConfirmTake('APP')">确认服药</el-button>
        <el-button type="warning" size="large" @click="handleDelay('APP')">稍后提醒</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="smsReminderDialogVisible"
      title="用药提醒 - 短信通知"
      width="450px"
      center
      :close-on-click-modal="false"
    >
      <div class="sms-reminder-content">
        <el-icon :size="64" color="#E6A23C">
          <Message />
        </el-icon>
        <h2 style="color: #E6A23C; margin: 20px 0;">短信提醒已发送</h2>
        <el-card shadow="hover" style="margin: 20px 0;">
          <p class="reminder-detail"><strong>时间：</strong>{{ currentReminder.reminderTime }}</p>
          <p class="reminder-detail"><strong>渠道：</strong>{{ getChannelLabel(currentReminder.channel) }}</p>
          <p class="reminder-detail"><strong>级别：</strong>第{{ currentReminder.level }}级</p>
        </el-card>
        <el-alert
          title="短信提醒已发送至您的手机，请注意查收"
          type="warning"
          :closable="false"
          show-icon
        />
        <p class="reminder-tip" style="margin-top: 15px;">短信内容已记录在后台数据库中</p>
      </div>
      <template #footer>
        <el-button type="success" size="large" @click="handleConfirmTake('短信')">确认收到</el-button>
        <el-button type="warning" size="large" @click="handleDelay('短信')">稍后提醒</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="phoneReminderDialogVisible"
      title="用药提醒 - 电话通知"
      width="450px"
      center
      :close-on-click-modal="false"
    >
      <div class="phone-reminder-content">
        <el-icon :size="64" color="#F56C6C">
          <Phone />
        </el-icon>
        <h2 style="color: #F56C6C; margin: 20px 0;">电话提醒已拨打</h2>
        <el-card shadow="hover" style="margin: 20px 0;">
          <p class="reminder-detail"><strong>时间：</strong>{{ currentReminder.reminderTime }}</p>
          <p class="reminder-detail"><strong>渠道：</strong>{{ getChannelLabel(currentReminder.channel) }}</p>
          <p class="reminder-detail"><strong>级别：</strong>第{{ currentReminder.level }}级</p>
        </el-card>
        <el-alert
          title="系统已自动拨打电话提醒您服药，请注意接听"
          type="error"
          :closable="false"
          show-icon
        />
        <p class="reminder-tip" style="margin-top: 15px;">电话提醒记录已保存至后台数据库</p>
      </div>
      <template #footer>
        <el-button type="success" size="large" @click="handleConfirmTake('电话')">确认服药</el-button>
        <el-button type="warning" size="large" @click="handleDelay('电话')">稍后提醒</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BellFilled, Message, Phone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getReminders,
  createReminder,
  createRemindersFromPlan,
  toggleReminder,
  deleteReminder,
  getReminderLogs,
  updateLogStatus,
  sendReminder
} from '@/api/reminder'
import { getMedicationPlans } from '@/api/medicationplan'

const userStore = useUserStore()

const activeTab = ref('settings')

const reminders = ref([])
const loading = ref(false)
const plans = ref([])

const reminderLogs = ref([])
const logsLoading = ref(false)

const addDialogVisible = ref(false)
const addForm = reactive({
  planId: null,
  reminderTime: '',
  levels: [1, 2, 3]
})

const selectedPlan = computed(() => {
  if (!addForm.planId) return null
  return plans.value.find(p => p.planId === addForm.planId)
})

const currentReminder = reactive({
  reminderId: null,
  reminderTime: '',
  channel: 'APP',
  level: 1,
  logId: null
})

const appReminderDialogVisible = ref(false)
const smsReminderDialogVisible = ref(false)
const phoneReminderDialogVisible = ref(false)

onMounted(() => {
  loadReminders()
  loadReminderLogs()
  loadPlans()
})

const loadPlans = async () => {
  try {
    const userId = userStore.userId || 1
    const res = await getMedicationPlans(userId)
    if (res.success) {
      plans.value = res.data || []
    }
  } catch (error) {
    console.error('加载用药计划失败:', error)
  }
}

const loadReminders = async () => {
  loading.value = true
  try {
    const userId = userStore.userId || 1
    const res = await getReminders(userId)
    if (res.success) {
      reminders.value = res.data || []
    }
  } catch (error) {
    console.error('加载提醒配置失败:', error)
  } finally {
    loading.value = false
  }
}

const loadReminderLogs = async () => {
  logsLoading.value = true
  try {
    const userId = userStore.userId || 1
    const res = await getReminderLogs(userId)
    if (res.success) {
      reminderLogs.value = res.data || []
    }
  } catch (error) {
    console.error('加载提醒记录失败:', error)
  } finally {
    logsLoading.value = false
  }
}

const handleAddReminder = () => {
  addForm.planId = null
  addForm.reminderTime = '08:00:00'
  addForm.levels = [1, 2, 3]
  addDialogVisible.value = true
}

const handlePlanChange = (planId) => {
}

const handleAddReminderSubmit = async () => {
  if (!addForm.planId) {
    ElMessage.warning('请选择用药计划')
    return
  }
  if (!addForm.reminderTime) {
    ElMessage.warning('请选择提醒时间')
    return
  }
  if (!addForm.levels || addForm.levels.length === 0) {
    ElMessage.warning('请至少选择一个提醒级别')
    return
  }

  try {
    const userId = userStore.userId || 1
    const includeApp = addForm.levels.includes(1)
    const includeSms = addForm.levels.includes(2)
    const includePhone = addForm.levels.includes(3)
    
    await createRemindersFromPlan({
      userId,
      planId: addForm.planId,
      reminderTime: addForm.reminderTime,
      includeApp,
      includeSms,
      includePhone
    })

    ElMessage.success('提醒添加成功')
    addDialogVisible.value = false
    loadReminders()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleToggle = async (row) => {
  try {
    await toggleReminder(row.reminderId, row.enabled)
    ElMessage.success(row.enabled === 1 ? '提醒已启用' : '提醒已禁用')
  } catch (error) {
    ElMessage.error('操作失败')
    row.enabled = row.enabled === 1 ? 0 : 1
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个提醒配置吗？', '提示', {
      type: 'warning'
    })
    await deleteReminder(row.reminderId)
    ElMessage.success('提醒已删除')
    loadReminders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSendReminder = async (row) => {
  try {
    const res = await sendReminder(row.reminderId)
    if (res.success) {
      Object.assign(currentReminder, {
        reminderId: row.reminderId,
        reminderTime: row.reminderTime,
        channel: row.channel,
        level: row.level,
        logId: res.data.logId
      })

      if (row.channel === 'APP') {
        appReminderDialogVisible.value = true
      } else if (row.channel === '短信') {
        smsReminderDialogVisible.value = true
      } else if (row.channel === '电话') {
        phoneReminderDialogVisible.value = true
      }
      loadReminderLogs()
    }
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const handleConfirmTake = async (channel) => {
  if (channel === 'APP') {
    appReminderDialogVisible.value = false
  } else if (channel === '短信') {
    smsReminderDialogVisible.value = false
  } else if (channel === '电话') {
    phoneReminderDialogVisible.value = false
  }

  if (currentReminder.logId) {
    try {
      await updateLogStatus(currentReminder.logId, 'read', '确认服药')
    } catch (error) {
      console.error('更新状态失败', error)
    }
  }

  ElMessage.success('已确认服药，记得按时服药哦！')
  loadReminderLogs()
}

const handleDelay = async (channel) => {
  if (channel === 'APP') {
    appReminderDialogVisible.value = false
  } else if (channel === '短信') {
    smsReminderDialogVisible.value = false
  } else if (channel === '电话') {
    phoneReminderDialogVisible.value = false
  }

  if (currentReminder.logId) {
    try {
      await updateLogStatus(currentReminder.logId, 'read', '稍后提醒')
    } catch (error) {
      console.error('更新状态失败', error)
    }
  }

  ElMessage.info('将在5分钟后再次提醒您')
  loadReminderLogs()
}

const getChannelLabel = (channel) => {
  const labels = { 'APP': 'APP推送', '短信': '短信提醒', '电话': '电话提醒' }
  return labels[channel] || channel
}

const getChannelType = (channel) => {
  const types = { 'APP': '', '短信': 'warning', '电话': 'danger' }
  return types[channel] || ''
}

const getLevelType = (level) => {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[level] || ''
}

const getStatusLabel = (status) => {
  const labels = {
    'pending': '待发送',
    'sent': '已发送',
    'received': '已接收',
    'read': '已读',
    'failed': '失败'
  }
  return labels[status] || status || '-'
}

const getStatusType = (status) => {
  const types = {
    'pending': 'info',
    'sent': 'warning',
    'received': '',
    'read': 'success',
    'failed': 'danger'
  }
  return types[status] || ''
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}
</script>

<style lang="scss" scoped>
.reminder-page {
  padding: 20px;
}

.reminder-tabs {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.app-reminder-content,
.sms-reminder-content,
.phone-reminder-content {
  text-align: center;
  padding: 20px;

  .reminder-detail {
    text-align: left;
    font-size: 16px;
    line-height: 2;
    margin: 0;
  }

  .reminder-tip {
    color: #909399;
    font-size: 14px;
    margin-top: 10px;
  }
}

:deep(.el-alert p) {
  margin: 5px 0;
  line-height: 1.8;
}
</style>
