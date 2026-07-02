<template>
  <div class="ocr-review-container">
    <PageHeader 
      title="OCR审核" 
      subtitle="审核用户上传的处方图片OCR识别结果"
      :icon="Picture"
      iconBg="linear-gradient(135deg, #fa709a 0%, #fee140 100%)"
    >
      <template #actions>
        <el-button type="primary" @click="loadData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
    </PageHeader>
    
    <ContentCard>
      <template #header>
        <div class="search-header">
          <div class="search-wrapper">
            <el-input 
              v-model="searchForm.keyword" 
              placeholder="请输入药品名称搜索" 
              clearable
              @keyup.enter="loadData"
              class="search-input"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="loadData" class="search-btn">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
          <div class="filter-wrapper">
            <el-radio-group v-model="searchForm.status" @change="loadData">
              <el-radio :value="null">全部</el-radio>
              <el-radio :value="0">待审核</el-radio>
              <el-radio :value="1">已通过</el-radio>
              <el-radio :value="2">已拒绝</el-radio>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <div class="table-wrapper">
        <el-table :data="tableData" style="width: 100%" v-loading="loading" class="custom-table">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="imageUrl" label="图片" width="100">
            <template #default="{ row }">
              <el-image 
                :src="row.imageUrl" 
                :preview-src-list="[row.imageUrl]"
                class="image-cell"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="medicationName" label="药品名称" min-width="150" />
          <el-table-column prop="dosage" label="用药剂量" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)" size="small" class="status-tag">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewTime" label="审核时间" width="180">
            <template #default="{ row }">
              <span>{{ row.reviewTime ? formatDate(row.reviewTime) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="180">
            <template #default="{ row }">
              <span>{{ formatDate(row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="success" v-if="row.status === 0" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" v-if="row.status === 0" @click="handleReject(row)">拒绝</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          class="custom-pagination"
        />
      </div>
    </ContentCard>

    <el-dialog 
      v-model="viewVisible" 
      title="OCR识别详情" 
      width="700px"
      :close-on-click-modal="false"
      :draggable="true"
      class="custom-dialog"
    >
      <div v-if="viewData" class="view-content">
        <div class="view-row">
          <span class="view-label">图片：</span>
          <el-image :src="viewData.imageUrl" class="view-image" fit="contain" />
        </div>
        <div class="view-row">
          <span class="view-label">药品名称：</span>
          <span class="view-value">{{ viewData.medicationName || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">用药剂量：</span>
          <span class="view-value">{{ viewData.dosage || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">OCR结果：</span>
          <span class="view-value">{{ viewData.ocrResult || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">状态：</span>
          <span class="view-value">{{ getStatusText(viewData.status) }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">审核时间：</span>
          <span class="view-value">{{ viewData.reviewTime || '-' }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">审核意见：</span>
          <span class="view-value">{{ viewData.reviewComment || '-' }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="reviewVisible" 
      :title="reviewTitle" 
      width="500px"
      :close-on-click-modal="false"
      :draggable="true"
      class="custom-dialog"
    >
      <el-form :model="reviewForm" label-width="100px" class="review-form">
        <el-form-item label="审核意见">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import ContentCard from '@/components/ContentCard.vue'
import { aiCenterApi } from '@/api/aiCenter'

const loading = ref(false)
const tableData = ref([])
const viewVisible = ref(false)
const reviewVisible = ref(false)
const reviewTitle = ref('')
const viewData = ref(null)
const reviewId = ref(null)
const reviewStatus = ref(null)

const searchForm = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const reviewForm = reactive({
  comment: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await aiCenterApi.getOcrReviewList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleView = async (row) => {
  try {
    const res = await aiCenterApi.getOcrReviewDetail(row.id)
    if (res.code === 200) {
      viewData.value = res.data
      viewVisible.value = true
    }
  } catch (error) {
    console.error('查看详情失败:', error)
  }
}

const handleApprove = (row) => {
  reviewTitle.value = '审核通过'
  reviewId.value = row.id
  reviewStatus.value = 1
  reviewForm.comment = ''
  reviewVisible.value = true
}

const handleReject = (row) => {
  reviewTitle.value = '审核拒绝'
  reviewId.value = row.id
  reviewStatus.value = 2
  reviewForm.comment = ''
  reviewVisible.value = true
}

const handleSubmitReview = async () => {
  try {
    await aiCenterApi.reviewOcr(reviewId.value, {
      status: reviewStatus.value,
      comment: reviewForm.comment
    })
    reviewVisible.value = false
    loadData()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await aiCenterApi.deleteOcrReview(row.id)
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return texts[status] || '-'
}

const getStatusTag = (status) => {
  const tags = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return tags[status] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ocr-review-container {
  padding: 24px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-wrapper {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.search-btn {
  white-space: nowrap;
}

.filter-wrapper {
  display: flex;
  gap: 8px;
}

.table-wrapper {
  margin-bottom: 16px;
}

.custom-table {
  --el-table-header-text-color: #666;
  --el-table-header-bg-color: #fafafa;
}

.image-cell {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}

.status-tag {
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

.custom-pagination {
  --el-pagination-text-color: #666;
}

.view-content {
  padding: 16px;
}

.view-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.view-label {
  width: 100px;
  color: #999;
  font-weight: 500;
  flex-shrink: 0;
}

.view-value {
  flex: 1;
  color: #333;
  word-break: break-all;
}

.view-image {
  flex: 1;
  max-height: 200px;
  border-radius: 8px;
}

.review-form {
  padding-top: 16px;
}
</style>