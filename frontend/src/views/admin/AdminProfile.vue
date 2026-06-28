<template>
  <div class="admin-profile">
    <div class="profile-header">
      <div class="header-icon">
        <el-icon><UserFilled /></el-icon>
      </div>
      <div class="header-info">
        <h2 class="admin-name">{{ adminInfo.adminName }}</h2>
        <span class="admin-role">系统管理员</span>
      </div>
    </div>

    <div class="tab-container">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="个人信息" name="info">
          <el-card class="info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>
                  <el-icon><InfoFilled /></el-icon>
                  基本信息
                </span>
                <el-button type="primary" plain size="small" @click="handleEdit">
                  <el-icon><Edit /></el-icon>
                  修改信息
                </el-button>
              </div>
            </template>

            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Key /></el-icon>
                  管理员编号
                </div>
                <div class="info-value">
                  <span class="id-badge">{{ formatId(adminInfo.adminId) }}</span>
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><User /></el-icon>
                  登录账号
                </div>
                <div class="info-value">{{ adminInfo.adminName || '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><UserFilled /></el-icon>
                  真实姓名
                </div>
                <div class="info-value">{{ adminInfo.realname || '未设置' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Phone /></el-icon>
                  手机号
                </div>
                <div class="info-value">{{ adminInfo.phone || '未设置' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Calendar /></el-icon>
                  创建时间
                </div>
                <div class="info-value">{{ formatTime(adminInfo.createTime) }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Clock /></el-icon>
                  更新时间
                </div>
                <div class="info-value">{{ formatTime(adminInfo.updateTime) }}</div>
              </div>
            </div>
          </el-card>

          <el-card class="actions-card" shadow="hover">
            <template #header>
              <span>
                <el-icon><Tools /></el-icon>
                账号操作
              </span>
            </template>

            <div class="actions-grid">
              <div class="action-item" @click="handleLogout">
                <div class="action-icon logout">
                  <el-icon><SwitchButton /></el-icon>
                </div>
                <div class="action-content">
                  <div class="action-title">退出登录</div>
                  <div class="action-desc">退出当前管理员登录状态</div>
                </div>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>

              <div class="action-item danger" @click="handleDeleteAccount">
                <div class="action-icon delete">
                  <el-icon><Delete /></el-icon>
                </div>
                <div class="action-content">
                  <div class="action-title">注销账号</div>
                  <div class="action-desc">注销后账号将永久删除</div>
                </div>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="管理员管理" name="manage">
          <div class="manage-section">
            <div class="manage-header">
              <div class="header-left">
                <h3 class="manage-title">
                  <el-icon><Avatar /></el-icon>
                  管理员列表
                </h3>
                <span class="admin-count">共 {{ adminTotal }} 位管理员</span>
              </div>
              <el-button type="primary" @click="handleAddAdmin">
                <el-icon><Plus /></el-icon>
                新增管理员
              </el-button>
            </div>

            <div class="search-bar">
              <el-input
                v-model="adminSearchKeyword"
                placeholder="搜索管理员ID/账号/姓名/手机号"
                clearable
                class="search-input"
                @keyup.enter="handleSearchAdmins"
                @clear="handleSearchAdmins"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleSearchAdmins">搜索</el-button>
            </div>

            <el-table
              :data="adminTableData"
              v-loading="adminLoading"
              stripe
              border
              style="width: 100%"
              :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
            >
              <el-table-column prop="adminId" label="管理员ID" width="100" align="center" sortable>
                <template #default="{ row }">
                  <span class="admin-id-text">{{ formatId(row.adminId) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="adminName" label="登录账号" width="140" show-overflow-tooltip />
              <el-table-column prop="realname" label="真实姓名" width="120" show-overflow-tooltip />
              <el-table-column prop="phone" label="手机号" width="140" />
              <el-table-column prop="password" label="密码" width="200" align="center" show-overflow-tooltip>
                <template #default="{ row }">
                  <span class="password-text">{{ row.password || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180">
                <template #default="{ row }">
                  <span>{{ formatTime(row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="updateTime" label="更新时间" width="180">
                <template #default="{ row }">
                  <span>{{ formatTime(row.updateTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" min-width="220" align="center" fixed="right">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <el-button type="primary" link @click="handleEditAdmin(row)">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button type="warning" link @click="handleResetAdminPwd(row)">
                      <el-icon><Key /></el-icon>
                      重置密码
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      :disabled="row.adminId === adminInfo.adminId"
                      @click="handleDeleteAdmin(row)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="adminPageNum"
                v-model:page-size="adminPageSize"
                :page-sizes="[10, 20, 50]"
                :total="adminTotal"
                layout="total, sizes, prev, pager, next, jumper"
                background
                @size-change="handleAdminSizeChange"
                @current-change="handleAdminPageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editVisible" title="修改个人信息" width="500px" :close-on-click-modal="false" destroy-on-close draggable>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px" class="edit-form">
        <el-form-item label="登录账号">
          <el-input :model-value="adminInfo.adminName" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="editForm.realname" placeholder="请输入真实姓名" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="deleteVisible" title="注销账号确认" width="450px" :close-on-click-modal="false" destroy-on-close draggable>
      <div class="delete-warning">
        <el-result icon="error" title="危险操作">
          <template #sub-title>
            <p class="warning-text">您确定要注销管理员账号吗？</p>
            <p class="warning-desc">注销后，该账号将永久删除，所有数据无法恢复。</p>
          </template>
        </el-result>
        <div class="confirm-input">
          <p class="confirm-tip">请输入管理员账号名 "<strong>{{ adminInfo.adminName }}</strong>" 以确认注销：</p>
          <el-input v-model="confirmAdminName" placeholder="请输入管理员账号名" />
        </div>
      </div>
      <template #footer>
        <el-button @click="deleteVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleteLoading" :disabled="confirmAdminName !== adminInfo.adminName" @click="handleDeleteSubmit">
          确认注销
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="adminDialogVisible"
      :title="adminDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      destroy-on-close
      draggable
    >
      <el-form ref="adminFormRef" :model="adminForm" :rules="adminFormRules" label-width="100px" class="admin-form">
        <el-form-item label="登录账号" prop="adminName">
          <el-input
            v-model="adminForm.adminName"
            :disabled="isEditAdmin"
            placeholder="请输入管理员账号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item :label="isEditAdmin ? '新密码' : '密码'" prop="password">
          <el-input
            v-model="adminForm.password"
            type="password"
            show-password
            :placeholder="isEditAdmin ? '请输入新密码（留空则不修改）' : '请输入密码（6-20位）'"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="adminForm.realname" placeholder="请输入真实姓名" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adminSubmitLoading" @click="handleAdminSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adminResetPwdVisible" title="重置管理员密码" width="450px" :close-on-click-modal="false" destroy-on-close draggable>
      <el-form ref="adminResetPwdFormRef" :model="adminResetPwdForm" :rules="adminResetPwdRules" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="adminResetPwdForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（6-20位）"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="adminResetPwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
            maxlength="20"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminResetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="adminResetPwdLoading" @click="handleAdminResetPwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  UserFilled,
  Key,
  Phone,
  Calendar,
  Clock,
  Edit,
  InfoFilled,
  Tools,
  SwitchButton,
  Delete,
  ArrowRight,
  Plus,
  Search,
  Avatar
} from '@element-plus/icons-vue'
import { getAdminInfo } from '@/api/user'
import {
  updateAdminProfile,
  deleteAdminAccount,
  getAdminList,
  addAdmin,
  updateAdmin,
  deleteAdmin,
  resetAdminPassword
} from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const adminId = computed(() => userStore.userInfo?.adminId)
const activeTab = ref('info')

const adminInfo = ref({
  adminId: null,
  adminName: '',
  realname: '',
  phone: '',
  createTime: null,
  updateTime: null
})

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  realname: '',
  phone: ''
})

const editRules = {
  realname: [{ max: 50, message: '姓名最多50个字符', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const deleteVisible = ref(false)
const deleteLoading = ref(false)
const confirmAdminName = ref('')

const adminLoading = ref(false)
const adminSubmitLoading = ref(false)
const adminResetPwdLoading = ref(false)
const adminTableData = ref([])
const adminTotal = ref(0)
const adminPageNum = ref(1)
const adminPageSize = ref(10)
const adminSearchKeyword = ref('')

const adminDialogVisible = ref(false)
const isEditAdmin = ref(false)
const adminDialogTitle = ref('新增管理员')
const adminFormRef = ref(null)
const adminForm = reactive({
  adminId: null,
  adminName: '',
  password: '',
  realname: '',
  phone: ''
})

const adminFormRules = {
  adminName: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20位', trigger: 'blur' }
  ],
  password: [
    { required: false },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  realname: [{ max: 50, message: '姓名最多50个字符', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const adminResetPwdVisible = ref(false)
const adminResetPwdFormRef = ref(null)
const adminResetPwdForm = reactive({
  adminId: null,
  newPassword: '',
  confirmPassword: ''
})

const validateAdminConfirmPwd = (rule, value, callback) => {
  if (value !== adminResetPwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const adminResetPwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateAdminConfirmPwd, trigger: 'blur' }
  ]
}

function fetchAdminInfo() {
  if (!adminId.value) return
  getAdminInfo(adminId.value).then(res => {
    if (res.code === 200) {
      adminInfo.value = res.data
    }
  })
}

function fetchAdminList() {
  adminLoading.value = true
  getAdminList({
    pageNum: adminPageNum.value,
    pageSize: adminPageSize.value,
    keyword: adminSearchKeyword.value
  }).then(res => {
    if (res.code === 200) {
      adminTableData.value = res.data.list
      adminTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  }).catch(() => {
    ElMessage.error('查询失败')
  }).finally(() => {
    adminLoading.value = false
  })
}

function handleEdit() {
  editForm.realname = adminInfo.value.realname || ''
  editForm.phone = adminInfo.value.phone || ''
  editVisible.value = true
}

function handleEditSubmit() {
  editFormRef.value.validate((valid) => {
    if (valid) {
      editLoading.value = true
      updateAdminProfile(adminId.value, editForm).then(res => {
        if (res.code === 200) {
          ElMessage.success('信息更新成功')
          adminInfo.value = { ...adminInfo.value, ...res.data }
          userStore.userInfo.realname = res.data.realname
          userStore.userInfo.phone = res.data.phone
          localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
          editVisible.value = false
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      }).catch(() => {
        ElMessage.error('更新失败')
      }).finally(() => {
        editLoading.value = false
      })
    }
  })
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  }).catch(() => {})
}

function handleDeleteAccount() {
  confirmAdminName.value = ''
  deleteVisible.value = true
}

function handleDeleteSubmit() {
  if (confirmAdminName.value !== adminInfo.value.adminName) {
    ElMessage.error('输入的账号名不匹配')
    return
  }
  deleteLoading.value = true
  deleteAdminAccount(adminId.value).then(res => {
    if (res.code === 200) {
      ElMessage.success('账号已注销')
      userStore.logout()
      router.push('/login')
      deleteVisible.value = false
    } else {
      ElMessage.error(res.message || '注销失败')
    }
  }).catch(() => {
    ElMessage.error('注销失败')
  }).finally(() => {
    deleteLoading.value = false
  })
}

function handleSearchAdmins() {
  adminPageNum.value = 1
  fetchAdminList()
}

function handleAdminPageChange(page) {
  adminPageNum.value = page
  fetchAdminList()
}

function handleAdminSizeChange(size) {
  adminPageSize.value = size
  adminPageNum.value = 1
  fetchAdminList()
}

function handleAddAdmin() {
  isEditAdmin.value = false
  adminDialogTitle.value = '新增管理员'
  Object.assign(adminForm, {
    adminId: null,
    adminName: '',
    password: '',
    realname: '',
    phone: ''
  })
  adminDialogVisible.value = true
}

function handleEditAdmin(row) {
  isEditAdmin.value = true
  adminDialogTitle.value = '编辑管理员'
  Object.assign(adminForm, {
    adminId: row.adminId,
    adminName: row.adminName,
    password: '',
    realname: row.realname || '',
    phone: row.phone || ''
  })
  adminDialogVisible.value = true
}

function handleAdminSubmit() {
  adminFormRef.value.validate((valid) => {
    if (valid) {
      adminSubmitLoading.value = true
      const formData = { ...adminForm }
      if (isEditAdmin.value && !formData.password) {
        delete formData.password
      }
      const api = isEditAdmin.value
        ? updateAdmin(adminForm.adminId, formData)
        : addAdmin(formData)
      api.then(res => {
        if (res.code === 200) {
          ElMessage.success(isEditAdmin.value ? '更新成功' : '添加成功')
          adminDialogVisible.value = false
          fetchAdminList()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      }).catch(() => {
        ElMessage.error('操作失败')
      }).finally(() => {
        adminSubmitLoading.value = false
      })
    }
  })
}

function handleDeleteAdmin(row) {
  if (row.adminId === adminInfo.value.adminId) {
    ElMessage.warning('不能删除当前登录账号')
    return
  }
  ElMessageBox.confirm(`确定要删除管理员 "${row.adminName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAdmin(row.adminId).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchAdminList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

function handleResetAdminPwd(row) {
  adminResetPwdForm.adminId = row.adminId
  adminResetPwdForm.newPassword = ''
  adminResetPwdForm.confirmPassword = ''
  adminResetPwdVisible.value = true
}

function handleAdminResetPwdSubmit() {
  adminResetPwdFormRef.value.validate((valid) => {
    if (valid) {
      adminResetPwdLoading.value = true
      resetAdminPassword(adminResetPwdForm.adminId, adminResetPwdForm.newPassword).then(res => {
        if (res.code === 200) {
          ElMessage.success('密码重置成功')
          adminResetPwdVisible.value = false
        } else {
          ElMessage.error(res.message || '重置失败')
        }
      }).catch(() => {
        ElMessage.error('重置失败')
      }).finally(() => {
        adminResetPwdLoading.value = false
      })
    }
  })
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

function formatId(id) {
  if (!id) return '-'
  return String(id).padStart(4, '0')
}

onMounted(() => {
  fetchAdminInfo()
})

watch(activeTab, (newTab) => {
  if (newTab === 'manage' && adminTableData.value.length === 0) {
    fetchAdminList()
  }
})
</script>

<style scoped>
.admin-profile {
  padding: 0;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  color: white;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.header-info {
  flex: 1;
}

.admin-name {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.admin-role {
  font-size: 14px;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
}

.tab-container {
  max-width: 100%;
}

.profile-tabs {
  --el-tabs-header-padding: 0;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}

.info-card,
.actions-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.info-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #f1f5f9;
  transform: translateY(-2px);
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 8px;
}

.info-value {
  color: #1e293b;
  font-size: 15px;
  font-weight: 500;
}

.id-badge {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
  color: #4f46e5;
  background: #e0e7ff;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 14px;
}

.admin-id-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
  color: #475569;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
}

.password-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #64748b;
  word-break: break-all;
}

.actions-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.action-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
  transform: translateX(4px);
}

.action-item.danger:hover {
  background: #fef2f2;
  border-color: #fecaca;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-icon.logout {
  background: #fef3c7;
  color: #f59e0b;
}

.action-icon.delete {
  background: #fee2e2;
  color: #ef4444;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #64748b;
}

.action-arrow {
  color: #94a3b8;
  font-size: 18px;
}

.delete-warning {
  text-align: center;
}

.warning-text {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.warning-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.confirm-input {
  margin-top: 20px;
  padding: 16px;
  background: #fef2f2;
  border-radius: 8px;
}

.confirm-tip {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 12px 0;
  text-align: left;
}

.edit-form,
.admin-form {
  padding: 10px 0;
}

.manage-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.manage-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-count {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }

  .admin-name {
    font-size: 24px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-item {
    padding: 16px;
  }

  .manage-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-input {
    width: 100%;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
