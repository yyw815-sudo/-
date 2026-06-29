<template>
  <div class="family-container">
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon size="32" color="#409EFF"><User /></el-icon>
        </div>
        <div class="header-text">
          <h1>家庭协作</h1>
          <p>管理您的家庭成员及权限</p>
        </div>
      </div>
      <div class="header-actions">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索家属姓名/电话/关系..."
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon color="#909399"><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <el-button 
          type="primary" 
          @click="showAllRemindersDialog" 
          class="action-btn"
        >
          <el-icon><Bell /></el-icon>
          <span>查看所有提醒</span>
        </el-button>
        <el-button 
          type="primary" 
          @click="showAddDialog = true" 
          class="action-btn primary-btn"
        >
          <el-icon><Plus /></el-icon>
          <span>添加家属</span>
        </el-button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <el-icon size="24"><UserFilled /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ familyMembers.length }}</span>
          <span class="stat-label">家属成员</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <el-icon size="24"><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ allReminderLogs.length }}</span>
          <span class="stat-label">提醒记录</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <el-icon size="24"><Lock /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ advancedCount }}</span>
          <span class="stat-label">高级权限</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon size="24"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ activeCount }}</span>
          <span class="stat-label">正常状态</span>
        </div>
      </div>
    </div>

    <div class="members-grid">
      <div 
        v-for="member in familyMembers" 
        :key="member.memberId"
        class="member-card"
        :class="{ 'disabled': member.status !== 1 }"
      >
        <div class="card-header">
          <div class="member-avatar">
            <el-icon size="36" :color="getAvatarColor(member.realname)"><User /></el-icon>
          </div>
          <div class="member-basic">
            <div class="member-name-row">
              <span class="member-name">{{ member.realname }}</span>
              <el-tag 
                :type="member.permissionLevel === 'basic' ? 'info' : 'warning'" 
                size="small"
              >
                {{ member.permissionLevel === 'basic' ? '基础' : '高级' }}
              </el-tag>
            </div>
            <span class="member-relation">{{ member.relation }}</span>
          </div>
        </div>
        
        <div class="card-body">
          <div class="info-row">
            <el-icon size="16" color="#909399"><Phone /></el-icon>
            <span>{{ member.phone || '-' }}</span>
          </div>
          <div class="info-row">
            <el-icon size="16" color="#909399"><Clock /></el-icon>
            <span>{{ formatTime(member.createTime) }}</span>
          </div>
          <div class="info-row">
            <el-icon size="16" color="#909399"><UserFilled /></el-icon>
            <span>成员ID: #{{ String(member.memberId).padStart(4, '0') }}</span>
          </div>
        </div>
        
        <div class="card-actions">
          <button 
            class="action-btn-small" 
            @click="showReminderDialog(member)"
            title="查看提醒记录"
          >
            <el-icon><Bell /></el-icon>
            <span>提醒</span>
          </button>
          <button 
            class="action-btn-small" 
            @click="showAuthDialog(member)"
            title="设置权限"
          >
            <el-icon><Lock /></el-icon>
            <span>权限</span>
          </button>
          <button 
            class="action-btn-small" 
            @click="openEditDialog(member)"
            title="编辑信息"
          >
            <el-icon><Edit /></el-icon>
            <span>编辑</span>
          </button>
          <button 
            class="action-btn-small danger" 
            @click="deleteMember(member)"
            title="删除成员"
          >
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </button>
        </div>
      </div>
      
      <div v-if="familyMembers.length === 0" class="empty-state">
        <el-icon size="64" color="#C0C4CC"><User /></el-icon>
        <p>暂无家属成员</p>
        <p class="empty-tip">点击右上角"添加家属"按钮添加成员</p>
      </div>
    </div>

    <el-dialog 
      v-model="showAddDialog" 
      title="添加家属" 
      width="580px" 
      draggable
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="form-header">
          <el-icon size="24" color="#409EFF"><Plus /></el-icon>
          <span>新增家属成员</span>
        </div>
        <el-form :model="addForm" :rules="rules" ref="addFormRef" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="家属姓名" prop="realname">
                <el-input v-model="addForm.realname" placeholder="请输入家属姓名" size="large"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="addForm.phone" placeholder="请输入联系电话" size="large"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="关系" prop="relation">
                <el-select v-model="addForm.relation" placeholder="请选择关系" size="large">
                  <el-option label="父母" value="父母"></el-option>
                  <el-option label="配偶" value="配偶"></el-option>
                  <el-option label="子女" value="子女"></el-option>
                  <el-option label="兄弟姐妹" value="兄弟姐妹"></el-option>
                  <el-option label="其他" value="其他"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="权限级别" prop="permissionLevel">
                <el-select v-model="addForm.permissionLevel" placeholder="请选择权限级别" size="large">
                  <el-option label="基础权限" value="basic"></el-option>
                  <el-option label="高级权限" value="advanced"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <div class="permission-preview">
            <div class="preview-header">
              <el-icon size="16" color="#409EFF"><InfoFilled /></el-icon>
              <span>{{ permissionLevelMap[addForm.permissionLevel]?.label || '基础' }}权限包含以下内容：</span>
            </div>
            <div class="feature-tags">
              <el-tag 
                v-for="(feature, index) in permissionLevelMap[addForm.permissionLevel]?.features || []" 
                :key="index"
                :type="addForm.permissionLevel === 'advanced' ? 'warning' : 'info'"
                size="small"
                effect="light"
              >
                {{ feature }}
              </el-tag>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showAddDialog = false" class="dialog-btn">取消</el-button>
        <el-button type="primary" @click="addMember" class="dialog-btn primary">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="showEditDialog" 
      title="修改家属信息" 
      width="580px" 
      draggable
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="form-header">
          <el-icon size="24" color="#E6A23C"><Edit /></el-icon>
          <span>编辑家属信息</span>
        </div>
        <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="家属姓名" prop="realname">
                <el-input v-model="editForm.realname" placeholder="请输入家属姓名" size="large"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="editForm.phone" placeholder="请输入联系电话" size="large"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="关系" prop="relation">
                <el-select v-model="editForm.relation" placeholder="请选择关系" size="large">
                  <el-option label="父母" value="父母"></el-option>
                  <el-option label="配偶" value="配偶"></el-option>
                  <el-option label="子女" value="子女"></el-option>
                  <el-option label="兄弟姐妹" value="兄弟姐妹"></el-option>
                  <el-option label="其他" value="其他"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="权限级别" prop="permissionLevel">
                <el-select v-model="editForm.permissionLevel" placeholder="请选择权限级别" size="large">
                  <el-option label="基础权限" value="basic"></el-option>
                  <el-option label="高级权限" value="advanced"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <div class="permission-preview">
            <div class="preview-header">
              <el-icon size="16" color="#E6A23C"><Warning /></el-icon>
              <span>修改权限级别将自动调整具体权限，当前 {{ permissionLevelMap[editForm.permissionLevel]?.label || '基础' }}权限包含：</span>
            </div>
            <div class="feature-tags">
              <el-tag 
                v-for="(feature, index) in permissionLevelMap[editForm.permissionLevel]?.features || []" 
                :key="index"
                :type="editForm.permissionLevel === 'advanced' ? 'warning' : 'info'"
                size="small"
                effect="light"
              >
                {{ feature }}
              </el-tag>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showEditDialog = false" class="dialog-btn">取消</el-button>
        <el-button type="primary" @click="updateMember" class="dialog-btn primary">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="showAuthDialogVisible" 
      title="设置家属权限" 
      width="580px" 
      draggable
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="form-header">
          <el-icon size="24" color="#67C23A"><Lock /></el-icon>
          <span>权限设置</span>
          <el-tag 
            :type="authMemberPermissionLevel === 'basic' ? 'info' : 'warning'" 
            size="small" 
            class="permission-tag"
          >
            {{ authMemberPermissionLevel === 'basic' ? '基础权限' : '高级权限' }}
          </el-tag>
        </div>
        <el-form :model="authForm" label-width="160px" class="auth-form">
          <template v-if="authMemberPermissionLevel === 'advanced'">
            <el-form-item label="查看病历记录">
              <div class="switch-wrapper">
                <el-switch v-model="authForm.viewMedicalRecord" :active-value="1" :inactive-value="0" active-color="#409EFF" inactive-color="#DCDFE6"></el-switch>
                <span class="switch-desc">允许查看病历历史记录</span>
              </div>
            </el-form-item>
            <el-form-item label="查看统计数据">
              <div class="switch-wrapper">
                <el-switch v-model="authForm.viewStatistics" :active-value="1" :inactive-value="0" active-color="#409EFF" inactive-color="#DCDFE6"></el-switch>
                <span class="switch-desc">允许查看健康统计图表</span>
              </div>
            </el-form-item>
          </template>
          <el-form-item label="查看用药计划">
            <div class="switch-wrapper">
              <el-switch v-model="authForm.viewMedication" :active-value="1" :inactive-value="0" active-color="#409EFF" inactive-color="#DCDFE6"></el-switch>
              <span class="switch-desc">允许查看用药提醒计划</span>
            </div>
          </el-form-item>
          <el-form-item label="接收漏服提醒">
            <div class="switch-wrapper">
              <el-switch v-model="authForm.receiveMissAlert" :active-value="1" :inactive-value="0" active-color="#E6A23C" inactive-color="#DCDFE6"></el-switch>
              <span class="switch-desc">接收用药漏服通知</span>
            </div>
          </el-form-item>
          <template v-if="authMemberPermissionLevel === 'advanced'">
            <el-form-item label="接收紧急通知">
              <div class="switch-wrapper">
                <el-switch v-model="authForm.receiveEmergency" :active-value="1" :inactive-value="0" active-color="#F56C6C" inactive-color="#DCDFE6"></el-switch>
                <span class="switch-desc">接收紧急医疗通知</span>
              </div>
            </el-form-item>
            <el-form-item label="接收断联提醒">
              <div class="switch-wrapper">
                <el-switch v-model="authForm.disconnAlert" :active-value="1" :inactive-value="0" active-color="#909399" inactive-color="#DCDFE6"></el-switch>
                <span class="switch-desc">设备断联时接收提醒</span>
              </div>
            </el-form-item>
          </template>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showAuthDialogVisible = false" class="dialog-btn">取消</el-button>
        <el-button type="primary" @click="saveAuth" class="dialog-btn primary">保存权限</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="showReminderDialogVisible" 
      title="家属提醒记录" 
      width="900px" 
      draggable
      :close-on-click-modal="false"
      class="beautified-dialog"
    >
      <div class="reminder-header">
        <div class="reminder-info">
          <div class="member-avatar-sm">
            <el-icon size="24" :color="getAvatarColor(currentMember?.realname || '')"><User /></el-icon>
          </div>
          <div class="member-detail">
            <span class="member-name">{{ currentMember?.realname }}</span>
            <span class="member-relation-sm">{{ currentMember?.relation }}</span>
          </div>
        </div>
        <div class="reminder-stats">
          <div class="stat-item">
            <span class="stat-num">{{ reminderLogs.length }}</span>
            <span class="stat-text">总记录</span>
          </div>
          <div class="stat-item success">
            <span class="stat-num">{{ reminderLogs.filter(l => l.status === 'received').length }}</span>
            <span class="stat-text">已接收</span>
          </div>
          <div class="stat-item warning">
            <span class="stat-num">{{ reminderLogs.filter(l => l.status === 'missed').length }}</span>
            <span class="stat-text">已漏服</span>
          </div>
        </div>
      </div>
      <div class="reminder-content">
        <div v-if="reminderLogs.length === 0" class="empty-reminder">
          <el-icon size="48" color="#C0C4CC"><Bell /></el-icon>
          <p>暂无提醒记录</p>
        </div>
        <el-table 
          :data="reminderLogs" 
          border 
          style="width: 100%;" 
          v-loading="reminderLoading" 
          class="reminder-table"
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
          :row-class-name="row => row.row.status === 'missed' ? 'row-missed' : ''"
        >
          <el-table-column prop="logId" label="记录ID" width="80">
            <template #default="scope">
              <span class="log-id">#{{ String(scope.row.logId).padStart(4, '0') }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="提醒内容" min-width="280" show-overflow-tooltip>
            <template #default="scope">
              <div class="content-cell">
                <el-icon size="14" color="#409EFF" class="content-icon"><Bell /></el-icon>
                <span>{{ scope.row.content }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="channel" label="渠道" width="90">
            <template #default="scope">
              <el-tag 
                size="small" 
                :type="getChannelType(scope.row.channel)"
                effect="light"
              >
                {{ scope.row.channel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small" effect="dark">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sendTime" label="发送时间" width="160">
            <template #default="scope">
              <span class="time-text">{{ formatTime(scope.row.sendTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="receiveTime" label="接收时间" width="160">
            <template #default="scope">
              <span class="time-text">{{ formatTime(scope.row.receiveTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showReminderDialogVisible = false" class="dialog-btn">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="showAllReminderDialogVisible" 
      title="所有家属提醒记录" 
      width="900px" 
      draggable
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="form-header">
          <el-icon size="24" color="#409EFF"><Bell /></el-icon>
          <span>全部提醒记录</span>
          <span class="record-count">共 {{ allReminderLogs.length }} 条</span>
        </div>
        <el-table :data="allReminderLogs" border style="width: 100%; margin-top: 15px;" v-loading="allReminderLoading" class="reminder-table">
          <el-table-column prop="logId" label="记录ID" width="90">
            <template #default="scope">{{ String(scope.row.logId).padStart(4, '0') }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="家属姓名" width="120">
            <template #default="scope">
              <span class="remark-name">{{ scope.row.remark || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="提醒内容" min-width="250" show-overflow-tooltip></el-table-column>
          <el-table-column prop="channel" label="渠道" width="100">
            <template #default="scope">
              <el-tag size="small" type="info">{{ scope.row.channel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170">
            <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showAllReminderDialogVisible = false" class="dialog-btn">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Search,
  Bell,
  Plus,
  UserFilled,
  Lock,
  CircleCheck,
  Phone,
  Clock,
  Edit,
  Delete,
  InfoFilled,
  Warning
} from '@element-plus/icons-vue'
import { familyApi } from '@/api/family'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const familyMembers = ref([])
const searchKeyword = ref('')

const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showAuthDialogVisible = ref(false)
const showReminderDialogVisible = ref(false)
const showAllReminderDialogVisible = ref(false)

const addFormRef = ref(null)
const editFormRef = ref(null)

const currentMember = ref(null)
const authMemberPermissionLevel = ref('basic')
const reminderLogs = ref([])
const reminderLoading = ref(false)
const allReminderLogs = ref([])
const allReminderLoading = ref(false)

const addForm = reactive({
  realname: '',
  phone: '',
  relation: '',
  permissionLevel: 'basic'
})

const editForm = reactive({
  memberId: '',
  realname: '',
  phone: '',
  relation: '',
  permissionLevel: 'basic'
})

const authForm = reactive({
  memberId: '',
  viewMedicalRecord: 0,
  viewMedication: 1,
  viewStatistics: 1,
  receiveMissAlert: 1,
  receiveEmergency: 1,
  disconnAlert: 0
})

const rules = {
  realname: [{ required: true, message: '请输入家属姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  relation: [{ required: true, message: '请选择关系', trigger: 'change' }]
}

const editRules = {
  realname: [{ required: true, message: '请输入家属姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  relation: [{ required: true, message: '请选择关系', trigger: 'change' }]
}

const advancedCount = computed(() => {
  return familyMembers.value.filter(m => m.permissionLevel === 'advanced').length
})

const activeCount = computed(() => {
  return familyMembers.value.filter(m => m.status === 1).length
})

const permissionLevelMap = {
  basic: {
    label: '基础',
    viewMedicalRecord: 0,
    viewMedication: 1,
    viewStatistics: 0,
    receiveMissAlert: 1,
    receiveEmergency: 0,
    disconnAlert: 0,
    features: ['查看用药计划', '接收漏服提醒']
  },
  advanced: {
    label: '高级',
    viewMedicalRecord: 1,
    viewMedication: 1,
    viewStatistics: 1,
    receiveMissAlert: 1,
    receiveEmergency: 1,
    disconnAlert: 1,
    features: ['全部权限', '查看病历记录', '查看用药计划', '查看统计数据', '接收漏服提醒', '接收紧急通知', '接收断联提醒']
  }
}

const getUserId = () => {
  return userStore.userId || localStorage.getItem('userId') || 1
}

const getAvatarColor = (name) => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#C0C4CC']
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const getFamilyMembers = async () => {
  loading.value = true
  try {
    const res = await familyApi.getFamilyMembers(getUserId())
    if (res.code === 200) {
      familyMembers.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取家属列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await familyApi.searchFamilyMembers(getUserId(), searchKeyword.value)
    if (res.code === 200) {
      familyMembers.value = res.data
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const showAllRemindersDialog = async () => {
  showAllReminderDialogVisible.value = true
  allReminderLoading.value = true
  try {
    const res = await familyApi.getAllFamilyReminders(getUserId())
    if (res.code === 200) {
      allReminderLogs.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取提醒记录失败')
  } finally {
    allReminderLoading.value = false
  }
}

const addMember = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await familyApi.addFamilyMember(getUserId(), {
          realname: addForm.realname,
          phone: addForm.phone,
          relation: addForm.relation,
          permissionLevel: addForm.permissionLevel
        })
        if (res.code === 200) {
          ElMessage.success('添加成功')
          showAddDialog.value = false
          addForm.realname = ''
          addForm.phone = ''
          addForm.relation = ''
          addForm.permissionLevel = 'basic'
          getFamilyMembers()
        } else {
          ElMessage.error(res.message)
        }
      } catch (error) {
        ElMessage.error('添加失败')
      }
    }
  })
}

const openEditDialog = (row) => {
  editForm.memberId = row.memberId
  editForm.realname = row.realname
  editForm.phone = row.phone
  editForm.relation = row.relation
  editForm.permissionLevel = row.permissionLevel
  showEditDialog.value = true
}

const updateMember = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await familyApi.updateFamilyMember(getUserId(), editForm.memberId, {
          realname: editForm.realname,
          phone: editForm.phone,
          relation: editForm.relation,
          permissionLevel: editForm.permissionLevel
        })
        if (res.code === 200) {
          ElMessage.success('更新成功')
          showEditDialog.value = false
          getFamilyMembers()
        } else {
          ElMessage.error(res.message)
        }
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }
  })
}

const showAuthDialog = async (row) => {
  authForm.memberId = row.memberId
  authMemberPermissionLevel.value = row.permissionLevel || 'basic'
  try {
    const res = await familyApi.getFamilyAuthByMember(getUserId(), row.memberId)
    if (res.code === 200) {
      authForm.viewMedicalRecord = res.data.viewMedicalRecord
      authForm.viewMedication = res.data.viewMedication
      authForm.viewStatistics = res.data.viewStatistics
      authForm.receiveMissAlert = res.data.receiveMissAlert
      authForm.receiveEmergency = res.data.receiveEmergency
      authForm.disconnAlert = res.data.disconnAlert
    }
  } catch (error) {
    const defaultAuth = permissionLevelMap[authMemberPermissionLevel.value]
    authForm.viewMedicalRecord = defaultAuth.viewMedicalRecord
    authForm.viewMedication = defaultAuth.viewMedication
    authForm.viewStatistics = defaultAuth.viewStatistics
    authForm.receiveMissAlert = defaultAuth.receiveMissAlert
    authForm.receiveEmergency = defaultAuth.receiveEmergency
    authForm.disconnAlert = defaultAuth.disconnAlert
  }
  showAuthDialogVisible.value = true
}

const saveAuth = async () => {
  try {
    const res = await familyApi.updateFamilyAuth(getUserId(), {
      memberId: authForm.memberId,
      viewMedicalRecord: authForm.viewMedicalRecord,
      viewMedication: authForm.viewMedication,
      viewStatistics: authForm.viewStatistics,
      receiveMissAlert: authForm.receiveMissAlert,
      receiveEmergency: authForm.receiveEmergency,
      disconnAlert: authForm.disconnAlert
    })
    if (res.code === 200) {
      ElMessage.success('权限更新成功')
      showAuthDialogVisible.value = false
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('权限更新失败')
  }
}

const showReminderDialog = async (row) => {
  currentMember.value = row
  showReminderDialogVisible.value = true
  await loadReminderLogs(row.memberId)
}

const loadReminderLogs = async (memberId) => {
  reminderLoading.value = true
  try {
    const res = await familyApi.getFamilyReminders(getUserId(), memberId)
    if (res.code === 200) {
      reminderLogs.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取提醒记录失败')
  } finally {
    reminderLoading.value = false
  }
}

const deleteMember = (row) => {
  ElMessageBox.confirm('确定要删除该家属成员吗？删除后相关权限记录也会被清除。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await familyApi.deleteFamilyMember(getUserId(), row.memberId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getFamilyMembers()
      } else {
        ElMessage.error(res.message)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const getChannelType = (channel) => {
  const map = {
    'APP': 'primary',
    '短信': 'success',
    '电话': 'warning'
  }
  return map[channel] || 'info'
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    sent: 'primary',
    received: 'success',
    missed: 'danger',
    failed: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待发送',
    sent: '已发送',
    received: '已接收',
    missed: '已漏服',
    failed: '发送失败'
  }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  getFamilyMembers()
})
</script>

<style scoped>
.family-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  padding: 30px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.header-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 14px;
  color: #909399;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  width: 280px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.primary-btn {
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  border: none;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #e8f0fe 0%, #d6e4ff 100%);
  color: #409EFF;
}

.stat-icon.green {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67C23A;
}

.stat-icon.orange {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #E6A23C;
}

.stat-icon.purple {
  background: linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%);
  color: #9B59B6;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

.member-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.member-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409EFF 0%, #667eea 100%);
}

.member-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.member-card.disabled {
  opacity: 0.7;
}

.member-card.disabled::before {
  background: linear-gradient(90deg, #909399 0%, #C0C4CC 100%);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.member-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.member-basic {
  flex: 1;
  min-width: 0;
}

.member-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.member-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.member-relation {
  font-size: 14px;
  color: #909399;
}

.card-body {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #f2f6fc;
}

.info-row:last-child {
  border-bottom: none;
}

.card-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.action-btn-small {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  border: 1px solid #e4e7ed;
  background: #fff;
  border-radius: 8px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn-small:hover {
  background: #f5f7fa;
  border-color: #409EFF;
  color: #409EFF;
}

.action-btn-small.danger:hover {
  background: #fef0f0;
  border-color: #F56C6C;
  color: #F56C6C;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #C0C4CC;
}

.empty-state p {
  margin: 16px 0 8px;
  font-size: 16px;
}

.empty-tip {
  font-size: 14px !important;
  margin-bottom: 0 !important;
}

.dialog-content {
  padding: 8px 0;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.record-count {
  margin-left: auto;
  font-size: 14px;
  font-weight: 400;
  color: #909399;
}

.permission-preview {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
  margin-top: 8px;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  font-weight: 500;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.auth-form {
  background: #fafafa;
  border-radius: 12px;
  padding: 20px;
}

.switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-desc {
  font-size: 13px;
  color: #909399;
}

.dialog-btn {
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.dialog-btn.primary {
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  border: none;
}

.reminder-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #f2f6fc;
}

.reminder-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.separator {
  margin: 0 12px;
  color: #dcdfe6;
}

.reminder-table :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

.remark-name {
  font-weight: 600;
  color: #409EFF;
}

.beautified-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.beautified-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409EFF 0%, #667eea 100%);
  padding: 20px 24px;
}

.beautified-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.beautified-dialog :deep(.el-dialog__headerbtn) {
  top: 18px;
}

.beautified-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.8);
  font-size: 20px;
}

.beautified-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #fff;
}

.reminder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.reminder-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-avatar-sm {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ed 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.member-detail {
  display: flex;
  flex-direction: column;
}

.member-detail .member-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.member-relation-sm {
  font-size: 13px;
  color: #909399;
}

.reminder-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.stat-item.success {
  border-color: #c2e7b0;
  background: #f0f9eb;
}

.stat-item.warning {
  border-color: #fcd34d;
  background: #fef3c7;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.stat-item.success .stat-num {
  color: #67C23A;
}

.stat-item.warning .stat-num {
  color: #E6A23C;
}

.stat-text {
  font-size: 12px;
  color: #909399;
}

.reminder-content {
  min-height: 200px;
}

.empty-reminder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #C0C4CC;
}

.empty-reminder p {
  margin-top: 16px;
  font-size: 14px;
}

.log-id {
  font-size: 13px;
  color: #909399;
  font-family: monospace;
}

.content-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-icon {
  flex-shrink: 0;
}

.time-text {
  font-size: 13px;
  color: #909399;
}

.row-missed {
  background: rgba(245, 108, 108, 0.05) !important;
}

.permission-tag {
  margin-left: auto;
}

.auth-form {
  background: #fafafa;
  border-radius: 12px;
  padding: 24px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
</style>
