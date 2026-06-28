<template>
  <div class="medication-plan-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="用药计划" name="plans">
        <el-card>
          <div class="card-header">
            <h3>我的用药计划</h3>
            <el-button type="primary" @click="handleAddPlan">添加用药计划</el-button>
          </div>
          <el-table :data="plans" border :loading="loading">
            <el-table-column prop="planId" label="ID" width="80" />
            <el-table-column prop="medicineName" label="药品名称" width="150" />
            <el-table-column prop="dosage" label="剂量" width="120" />
            <el-table-column prop="frequency" label="频率" width="120" />
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '进行中' : '已停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewPlan(row)">查看</el-button>
                <el-button size="small" type="primary" @click="handleEditPlan(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeletePlan(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="药品库" name="medicines">
        <el-card>
          <div class="card-header">
            <h3>药品库</h3>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索药品"
              style="width: 200px"
              clearable
              @clear="loadMedicines"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
          <el-table :data="medicines" border :loading="medicineLoading">
            <el-table-column prop="medicineId" label="ID" width="80" />
            <el-table-column prop="medicineName" label="药品名称" width="150" />
            <el-table-column prop="specification" label="规格" width="150" />
            <el-table-column prop="dosageForm" label="剂型" width="100" />
            <el-table-column prop="manufacturer" label="生产厂家" width="200" />
            <el-table-column prop="indication" label="适应症" show-overflow-tooltip />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewMedicine(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="planDialogVisible"
      :title="isEdit ? '编辑用药计划' : '添加用药计划'"
      width="600px"
    >
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="药品名称">
          <el-input v-model="planForm.medicineName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="剂量">
          <el-input v-model="planForm.dosage" placeholder="例如：100mg" />
        </el-form-item>
        <el-form-item label="服用频率">
          <el-select v-model="planForm.frequency" placeholder="请选择频率" style="width: 100%">
            <el-option label="每日一次" value="每日一次" />
            <el-option label="每日两次" value="每日两次" />
            <el-option label="每日三次" value="每日三次" />
            <el-option label="每日四次" value="每日四次" />
            <el-option label="每两日一次" value="每两日一次" />
            <el-option label="每周一次" value="每周一次" />
            <el-option label="按需服用" value="按需服用" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="planForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="planForm.endDate"
            type="date"
            placeholder="选择结束日期（可选）"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="用药目的">
          <el-input v-model="planForm.purpose" type="textarea" :rows="2" placeholder="请输入用药目的" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="planForm.notes" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-button type="primary" link @click="handleAddReminderTime">
            + 添加提醒时间
          </el-button>
          <div v-for="(time, index) in planForm.reminderTimes" :key="index" class="reminder-time-item">
            <el-time-picker
              v-model="time.suggestedTime"
              format="HH:mm"
              value-format="HH:mm:ss"
              placeholder="选择时间"
              style="width: 150px"
            />
            <el-input
              v-model="time.reason"
              placeholder="原因（可选）"
              style="width: 150px; margin-left: 10px"
            />
            <el-button type="danger" link @click="handleRemoveReminderTime(index)">删除</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePlan">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="medicineDetailVisible"
      title="药品详情"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="药品名称">{{ currentMedicine.medicineName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentMedicine.specification }}</el-descriptions-item>
        <el-descriptions-item label="剂型">{{ currentMedicine.dosageForm }}</el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ currentMedicine.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="适应症">{{ currentMedicine.indication }}</el-descriptions-item>
        <el-descriptions-item label="禁忌症">{{ currentMedicine.contraindication }}</el-descriptions-item>
        <el-descriptions-item label="注意事项">{{ currentMedicine.precautions }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getMedicationPlans,
  createMedicationPlan,
  updateMedicationPlan,
  deleteMedicationPlan,
  getAllMedicines,
  searchMedicines
} from '@/api/medicationplan'

const userStore = useUserStore()

const activeTab = ref('plans')
const plans = ref([])
const loading = ref(false)
const medicines = ref([])
const medicineLoading = ref(false)
const searchKeyword = ref('')

const planDialogVisible = ref(false)
const isEdit = ref(false)
const planForm = reactive({
  planId: null,
  medicineName: '',
  dosage: '',
  frequency: '',
  startDate: '',
  endDate: '',
  purpose: '',
  notes: '',
  reminderTimes: []
})

const medicineDetailVisible = ref(false)
const currentMedicine = reactive({
  medicineId: null,
  medicineName: '',
  specification: '',
  dosageForm: '',
  manufacturer: '',
  indication: '',
  contraindication: '',
  precautions: ''
})

onMounted(() => {
  loadPlans()
  loadMedicines()
})

const loadPlans = async () => {
  loading.value = true
  try {
    const userId = userStore.userId || 1
    const res = await getMedicationPlans(userId)
    if (res.success) {
      plans.value = res.data || []
    }
  } catch (error) {
    console.error('加载用药计划失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMedicines = async () => {
  medicineLoading.value = true
  try {
    const res = await getAllMedicines()
    if (res.success) {
      medicines.value = res.data || []
    }
  } catch (error) {
    console.error('加载药品库失败:', error)
  } finally {
    medicineLoading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    loadMedicines()
    return
  }
  medicineLoading.value = true
  try {
    const res = await searchMedicines(searchKeyword.value)
    if (res.success) {
      medicines.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    medicineLoading.value = false
  }
}

const handleAddPlan = () => {
  isEdit.value = false
  planForm.planId = null
  planForm.medicineName = ''
  planForm.dosage = ''
  planForm.frequency = ''
  planForm.startDate = ''
  planForm.endDate = ''
  planForm.purpose = ''
  planForm.notes = ''
  planForm.reminderTimes = []
  planDialogVisible.value = true
}

const handleEditPlan = (row) => {
  isEdit.value = true
  planForm.planId = row.planId
  planForm.medicineName = row.medicineName
  planForm.dosage = row.dosage
  planForm.frequency = row.frequency
  planForm.startDate = row.startDate
  planForm.endDate = row.endDate
  planForm.purpose = row.purpose
  planForm.notes = row.notes
  planForm.reminderTimes = []
  planDialogVisible.value = true
}

const handleViewPlan = (row) => {
  ElMessageBox.alert(
    `药品名称：${row.medicineName}\n剂量：${row.dosage}\n频率：${row.frequency}\n开始日期：${row.startDate}\n结束日期：${row.endDate || '长期'}\n用药目的：${row.purpose || '-'}\n备注：${row.notes || '-'}`,
    '用药计划详情',
    { confirmButtonText: '确定' }
  )
}

const handleDeletePlan = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用药计划吗？', '提示', {
      type: 'warning'
    })
    await deleteMedicationPlan(row.planId)
    ElMessage.success('删除成功')
    loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSavePlan = async () => {
  if (!planForm.medicineName) {
    ElMessage.warning('请输入药品名称')
    return
  }
  if (!planForm.dosage) {
    ElMessage.warning('请输入剂量')
    return
  }
  if (!planForm.frequency) {
    ElMessage.warning('请选择服用频率')
    return
  }
  if (!planForm.startDate) {
    ElMessage.warning('请选择开始日期')
    return
  }

  try {
    const userId = userStore.userId || 1
    const data = {
      userId,
      medicineName: planForm.medicineName,
      dosage: planForm.dosage,
      frequency: planForm.frequency,
      startDate: planForm.startDate,
      endDate: planForm.endDate || null,
      purpose: planForm.purpose,
      notes: planForm.notes,
      reminderTimes: planForm.reminderTimes
    }

    if (isEdit.value) {
      await updateMedicationPlan(planForm.planId, data)
      ElMessage.success('更新成功')
    } else {
      await createMedicationPlan(data)
      ElMessage.success('创建成功')
    }

    planDialogVisible.value = false
    loadPlans()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleAddReminderTime = () => {
  planForm.reminderTimes.push({
    suggestedTime: '08:00:00',
    reason: ''
  })
}

const handleRemoveReminderTime = (index) => {
  planForm.reminderTimes.splice(index, 1)
}

const handleViewMedicine = (row) => {
  Object.assign(currentMedicine, row)
  medicineDetailVisible.value = true
}
</script>

<style lang="scss" scoped>
.medication-plan-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.reminder-time-item {
  display: flex;
  align-items: center;
  margin-top: 10px;
}
</style>
