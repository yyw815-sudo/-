<template>
  <div class="user-manage">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          用户管理
        </h2>
        <span class="user-count">共 {{ total }} 位用户</span>
      </div>
      <el-button type="primary" @click="handleAdd" class="add-btn">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <div class="search-card">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户ID/用户名/姓名/手机号"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="userId" label="用户ID" width="100" align="center" sortable>
          <template #default="{ row }">
            <span class="id-text">{{ formatId(row.userId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" class="user-avatar">
                {{ row.userName?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="user-name">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="120" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 1 ? 'primary' : 'danger'" size="small" effect="light">
              {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" align="center">
          <template #default="{ row }">
            <span class="age-text">{{ row.age || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <span class="phone-text">{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="password" label="密码" width="200" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="password-text">{{ row.password || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="warning" link @click="handleResetPwd(row)">
                <el-icon><Key /></el-icon>
                重置密码
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      :close-on-click-modal="false"
      destroy-on-close
      draggable
      class="user-dialog"
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px" class="user-form">
        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="userForm.userName"
            :disabled="isEdit"
            placeholder="请输入用户名（3-20位）"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
            placeholder="请输入密码（6-20位）"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio value="1">男</el-radio>
            <el-radio value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="userForm.birthday"
            type="date"
            placeholder="请选择出生日期"
            value-format="YYYY-MM-DD"
            :max="new Date()"
          />
          <div v-if="userForm.birthday" class="age-preview">计算年龄：{{ calculateAge(userForm.birthday) }} 岁</div>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPwdVisible" title="重置密码" width="450px" :close-on-click-modal="false" destroy-on-close draggable>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="100px" class="reset-pwd-form">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPwdForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（6-20位）"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
            maxlength="20"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetPwdLoading" @click="handleResetPwdSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Edit,
  Delete,
  Key,
  User
} from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, resetUserPassword } from '@/api/admin'

const loading = ref(false)
const submitLoading = ref(false)
const resetPwdLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增用户')
const userFormRef = ref(null)
const userForm = reactive({
  userId: null,
  userName: '',
  password: '',
  realName: '',
  gender: '1',
  birthday: '',
  phone: ''
})

const userRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  realName: [
    { max: 50, message: '姓名最多50个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const resetPwdVisible = ref(false)
const resetPwdFormRef = ref(null)
const resetPwdForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== resetPwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetPwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

function tableRowClassName({ rowIndex }) {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

function fetchList() {
  loading.value = true
  getUserList({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: searchKeyword.value
  }).then(res => {
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  }).catch(() => {
    ElMessage.error('查询失败')
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() {
  pageNum.value = 1
  fetchList()
}

function handlePageChange(page) {
  pageNum.value = page
  fetchList()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  fetchList()
}

function handleAdd() {
  isEdit.value = false
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    userId: null,
    userName: '',
    password: '',
    realName: '',
    gender: '1',
    birthday: '',
    phone: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, {
    userId: row.userId,
    userName: row.userName,
    password: '',
    realName: row.realName,
    gender: String(row.gender || '1'),
    birthday: row.birthday || '',
    phone: row.phone
  })
  dialogVisible.value = true
}

function calculateAge(birthday) {
  if (!birthday) return 0
  const birthDate = new Date(birthday)
  const today = new Date()
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}

function handleSubmit() {
  userFormRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      const formData = { ...userForm }
      if (formData.birthday) {
        formData.age = calculateAge(formData.birthday)
      }
      const api = isEdit.value
        ? updateUser(userForm.userId, formData)
        : addUser(formData)
      api.then(res => {
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          fetchList()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      }).catch(() => {
        ElMessage.error('操作失败')
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除用户 "${row.userName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteUser(row.userId).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

function handleResetPwd(row) {
  resetPwdForm.userId = row.userId
  resetPwdForm.newPassword = ''
  resetPwdForm.confirmPassword = ''
  resetPwdVisible.value = true
}

function handleResetPwdSubmit() {
  resetPwdFormRef.value.validate((valid) => {
    if (valid) {
      resetPwdLoading.value = true
      resetUserPassword(resetPwdForm.userId, resetPwdForm.newPassword).then(res => {
        if (res.code === 200) {
          ElMessage.success('密码重置成功')
          resetPwdVisible.value = false
        } else {
          ElMessage.error(res.message || '重置失败')
        }
      }).catch(() => {
        ElMessage.error('重置失败')
      }).finally(() => {
        resetPwdLoading.value = false
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
  fetchList()
})
</script>

<style scoped>
.user-manage {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-count {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.search-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  width: 320px;
}

.search-btn {
  min-width: 80px;
}

.table-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-weight: 500;
  color: #334155;
}

.age-text {
  font-weight: 600;
  color: #7c3aed;
}

.id-text {
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

.phone-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #64748b;
}

.time-text {
  font-size: 13px;
  color: #64748b;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

:deep(.el-table .even-row) {
  background-color: #fafafa;
}

:deep(.el-table .odd-row) {
  background-color: #ffffff;
}

:deep(.el-table td.el-table__cell) {
  padding: 12px 0;
}

:deep(.el-table .cell) {
  padding: 0 12px;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.user-form,
.reset-pwd-form {
  padding: 10px 0;
}

.age-preview {
  margin-top: 8px;
  font-size: 13px;
  color: #7c3aed;
  font-weight: 500;
}

@media (max-width: 768px) {
  .page-header {
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

  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 550px;
  }
}

@media (max-width: 480px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .table-card {
    padding: 12px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-pagination) {
    font-size: 12px;
  }
}
</style>
