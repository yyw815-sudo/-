<template>
  <div class="statistics-container">
    <PageHeader 
      title="数据统计" 
      subtitle="系统整体数据概览与趋势分析"
      :icon="TrendCharts"
      iconBg="linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
    />
    
    <div class="stats-grid">
      <div 
        v-for="(stat, index) in statCards" 
        :key="stat.label"
        class="stat-card"
        :style="{ animationDelay: index * 0.1 + 's' }"
      >
        <div class="stat-glow" :style="{ background: stat.glowColor }"></div>
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon :size="28"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span ref="valueRefs">{{ stat.value }}</span>
          </div>
          <div class="stat-label">{{ stat.label }}</div>
          <div v-if="stat.change" class="stat-change" :class="stat.change > 0 ? 'positive' : 'negative'">
            <el-icon :size="14"><component :is="stat.change > 0 ? ArrowUp : ArrowDown" /></el-icon>
            <span>{{ Math.abs(stat.change) }}%</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="charts-section">
      <ContentCard>
        <template #header>
          <div class="chart-header">
            <h3 class="chart-title">数据趋势分析</h3>
            <div class="chart-tabs">
              <el-button 
                v-for="tab in timeTabs" 
                :key="tab.value"
                :type="activeTab === tab.value ? 'primary' : 'default'"
                @click="handleTabChange(tab.value)"
                class="tab-btn"
              >
                {{ tab.label }}
              </el-button>
            </div>
          </div>
        </template>
        <div class="chart-container">
          <div ref="lineChartRef" class="line-chart"></div>
        </div>
      </ContentCard>
      
      <div class="bottom-cards">
        <ContentCard>
          <template #header>
            <h3 class="chart-title">用户分布</h3>
          </template>
          <div class="chart-container">
            <div ref="pieChartRef" class="pie-chart"></div>
          </div>
        </ContentCard>
        
        <ContentCard>
          <template #header>
            <h3 class="chart-title">数据概览</h3>
          </template>
          <div class="overview-section">
            <div 
              v-for="(item, index) in overviewData" 
              :key="index" 
              class="overview-item"
              @click="openDetailModal(item)"
            >
              <div class="overview-icon" :style="{ background: item.color }">
                <el-icon :size="20" color="white"><component :is="item.icon" /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-value">{{ item.value }}</div>
                <div class="overview-label">{{ item.label }}</div>
              </div>
              <el-icon :size="16" class="overview-arrow">
                <ArrowRight />
              </el-icon>
            </div>
          </div>
        </ContentCard>
      </div>
    </div>

    <el-dialog 
      v-model="detailModalVisible" 
      :title="currentDetail?.label + '详情'"
      width="500px"
      class="detail-modal"
      draggable
    >
      <div v-if="currentDetail" class="detail-content">
        <div class="detail-header">
          <div class="detail-icon" :style="{ background: currentDetail.color }">
            <el-icon :size="32" color="white"><component :is="currentDetail.icon" /></el-icon>
          </div>
          <div class="detail-title">
            <div class="detail-value">{{ currentDetail.value }}</div>
            <div class="detail-label">{{ currentDetail.label }}</div>
          </div>
        </div>
        
        <div class="detail-stats">
          <div class="detail-stat-item" v-for="(stat, idx) in getDetailStats(currentDetail.label)" :key="idx">
            <div class="detail-stat-label">{{ stat.label }}</div>
            <div class="detail-stat-value" :class="stat.trend === 'up' ? 'trend-up' : stat.trend === 'down' ? 'trend-down' : ''">
              {{ stat.value }}
              <span v-if="stat.trend" class="detail-stat-trend">
                <el-icon :size="12"><component :is="stat.trend === 'up' ? ArrowUp : ArrowDown" /></el-icon>
                {{ stat.percent }}
              </span>
            </div>
          </div>
        </div>
        
        <div class="detail-chart">
          <div ref="detailChartRef" class="mini-chart"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { 
  User, UserFilled, Bell, Setting, TrendCharts, 
  ArrowUp, ArrowDown, DataAnalysis, Message, Document, View, ArrowRight
} from '@element-plus/icons-vue'
import { getStatistics } from '@/api/systemCenter'
import PageHeader from '@/components/PageHeader.vue'
import ContentCard from '@/components/ContentCard.vue'

const statistics = ref({
  userCount: 0,
  adminCount: 0,
  announcementCount: 0,
  activeConfigCount: 0
})

const activeTab = ref('week')
const valueRefs = ref([])
const lineChartRef = ref(null)
const pieChartRef = ref(null)
const detailChartRef = ref(null)
let lineChart = null
let pieChart = null
let detailChart = null

const detailModalVisible = ref(false)
const currentDetail = ref(null)

const timeTabs = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '全年', value: 'year' }
]

const generateMockData = () => {
  const data = {
    today: {
      labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
      userGrowth: [12500, 8200, 45800, 68500, 52300, 38600],
      apiCalls: [125600, 98900, 485600, 685200, 523600, 368900],
      announcements: [12, 5, 28, 45, 18, 15]
    },
    week: {
      labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      userGrowth: [45600, 52800, 38500, 65200, 72800, 48900, 55300],
      apiCalls: [285600, 325800, 235600, 415200, 468500, 325600, 289500],
      announcements: [35, 42, 28, 55, 68, 45, 32]
    },
    month: {
      labels: ['第1周', '第2周', '第3周', '第4周'],
      userGrowth: [285600, 325800, 295300, 345600],
      apiCalls: [1856000, 2158000, 1985000, 2356000],
      announcements: [156, 185, 225, 168]
    },
    year: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      userGrowth: [1052000, 985600, 1258000, 1156000, 1458000, 1385000, 1685000, 1585000, 1856000, 1785000, 2056000, 1985000],
      apiCalls: [6856000, 6258000, 8256000, 7685000, 9568000, 8985000, 11256000, 10585000, 12568000, 11985000, 14568000, 13856000],
      announcements: [685, 565, 856, 758, 985, 896, 1156, 1058, 1285, 1186, 1456, 1358]
    }
  }
  return data[activeTab.value] || data.week
}

const overviewData = computed(() => {
  const mockData = generateMockData()
  const totalUsers = mockData.userGrowth.reduce((a, b) => a + b, 0)
  const totalApi = mockData.apiCalls.reduce((a, b) => a + b, 0)
  const totalAnnouncements = mockData.announcements.reduce((a, b) => a + b, 0)
  
  return [
    { icon: DataAnalysis, value: totalUsers, label: '新增用户', color: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)', dataKey: 'userGrowth' },
    { icon: Message, value: totalApi.toLocaleString(), label: 'API调用', color: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)', dataKey: 'apiCalls' },
    { icon: Document, value: totalAnnouncements, label: '公告发布', color: 'linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%)', dataKey: 'announcements' },
    { icon: View, value: Math.round(totalApi / totalUsers) + '%', label: '平均活跃度', color: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)', dataKey: 'activity' }
  ]
})

const statCards = computed(() => [
  {
    icon: User,
    value: statistics.value.userCount,
    label: '用户总数',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    glowColor: 'rgba(102, 126, 234, 0.3)',
    change: 12
  },
  {
    icon: UserFilled,
    value: statistics.value.adminCount,
    label: '管理员数量',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    glowColor: 'rgba(240, 147, 251, 0.3)',
    change: 5
  },
  {
    icon: Bell,
    value: statistics.value.announcementCount,
    label: '系统公告',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    glowColor: 'rgba(79, 172, 254, 0.3)',
    change: -3
  },
  {
    icon: Setting,
    value: statistics.value.activeConfigCount,
    label: '启用配置',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    glowColor: 'rgba(67, 233, 123, 0.3)',
    change: 8
  }
])

const animateValue = (element, endValue) => {
  if (!element || endValue === 0) return
  let startValue = 0
  const duration = 1500
  const startTime = performance.now()
  
  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOut = 1 - Math.pow(1 - progress, 3)
    const currentValue = Math.floor(startValue + (endValue - startValue) * easeOut)
    
    element.textContent = currentValue.toLocaleString()
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  requestAnimationFrame(animate)
}

const initLineChart = () => {
  if (!lineChartRef.value) return
  
  if (lineChart) {
    lineChart.dispose()
  }
  
  lineChart = echarts.init(lineChartRef.value)
  updateLineChart()
}

const updateLineChart = () => {
  if (!lineChart) return
  
  const mockData = generateMockData()
  
  // 计算平均值
  const avgApi = Math.round(mockData.apiCalls.reduce((a, b) => a + b, 0) / mockData.apiCalls.length)
  const avgUser = Math.round(mockData.userGrowth.reduce((a, b) => a + b, 0) / mockData.userGrowth.length)
  
  // 仅周三、周五显示数据标签
  const wedIdx = 2
  const friIdx = 4
  
  // API调用数据（主系列：平滑面积+折线）
  const apiCallsData = mockData.apiCalls.map((val, idx) => {
    if (idx === wedIdx || idx === friIdx) {
      return { value: val, symbolSize: 7 }
    }
    return { value: val, symbolSize: 0 }
  })
  
  // 用户增长数据（次系列1：平滑折线无面积）
  const userGrowthData = mockData.userGrowth.map((val, idx) => {
    if (idx === wedIdx || idx === friIdx) {
      return { value: val, symbolSize: 6 }
    }
    return { value: val, symbolSize: 0 }
  })
  
  // 公告发布数据（次系列2：平滑折线无面积）
  const announcementsData = mockData.announcements.map((val, idx) => {
    if (idx === wedIdx || idx === friIdx) {
      return { value: val, symbolSize: 6 }
    }
    return { value: val, symbolSize: 0 }
  })
  
  const option = {
    backgroundColor: '#FFFFFF',
    // 增强版提示框
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      padding: [12, 16],
      textStyle: {
        color: '#333333',
        fontSize: 12,
        fontFamily: 'Microsoft YaHei, Source Han Sans CN, sans-serif'
      },
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#B0BCCC',
          width: 1,
          type: 'dashed'
        }
      },
      formatter: (params) => {
        const axisValue = params[0].axisValue
        let html = `<div style="font-weight: 600; font-size: 13px; margin-bottom: 10px; padding-bottom: 8px; border-bottom: 1px solid #E4E7ED;">${axisValue}</div>`
        params.forEach(param => {
          if (param.seriesName && param.value !== '-') {
            let val = param.value
            if (param.seriesName === 'API调用' || param.seriesName === '用户增长') {
              val = val >= 10000 ? (val / 10000).toFixed(1) + '万' : val.toLocaleString()
            }
            html += `<div style="display: flex; align-items: center; justify-content: space-between; margin: 6px 0; gap: 20px;">
              <span style="display: flex; align-items: center; gap: 8px;">
                <span style="width: 10px; height: 10px; border-radius: 50%; background: ${param.color};"></span>
                <span style="color: #666;">${param.seriesName}</span>
              </span>
              <span style="font-weight: 600; color: #222;">${val}</span>
            </div>`
          }
        })
        return html
      }
    },
    // 图例
    legend: {
      show: true,
      orient: 'horizontal',
      top: 12,
      right: 12,
      itemWidth: 14,
      itemHeight: 8,
      itemGap: 20,
      icon: 'roundRect',
      textStyle: {
        color: '#333333',
        fontSize: 12,
        fontFamily: 'Microsoft YaHei, sans-serif'
      }
    },
    grid: {
      left: 40,
      right: 40,
      top: 52,
      bottom: 36,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: mockData.labels,
      axisLine: {
        show: true,
        lineStyle: {
          color: '#888888',
          width: 1
        }
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#555555',
        fontSize: 12,
        fontFamily: 'Microsoft YaHei, sans-serif',
        align: 'center',
        margin: 12
      },
      splitLine: {
        show: false
      }
    },
    yAxis: [
      {
        type: 'value',
        position: 'left',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#888888',
            width: 1
          }
        },
        axisTick: {
          show: false
        },
        axisLabel: {
          color: '#666666',
          fontSize: 11,
          fontFamily: 'Microsoft YaHei, sans-serif',
          formatter: (val) => {
            if (val >= 10000) return (val / 10000).toFixed(1) + '万'
            return val
          }
        },
        splitLine: {
          lineStyle: {
            color: '#E4E7ED',
            width: 0.75,
            type: 'dashed',
            opacity: 0.8
          }
        }
      },
      {
        type: 'value',
        position: 'right',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#888888',
            width: 1
          }
        },
        axisTick: {
          show: false
        },
        axisLabel: {
          show: false
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      // 周三虚线标注
      {
        name: '标注线',
        type: 'line',
        markLine: {
          silent: true,
          symbol: 'none',
          label: {
            show: true,
            position: 'end',
            formatter: '峰值线',
            color: '#B0BCCC',
            fontSize: 10,
            fontFamily: 'Microsoft YaHei'
          },
          lineStyle: {
            color: '#B0BCCC',
            width: 1,
            type: 'dashed'
          },
          data: [
            { xAxis: wedIdx }
          ]
        },
        data: []
      },
      // API调用（主系列：平滑面积图+平滑折线，左Y轴）
      {
        name: 'API调用',
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        yAxisIndex: 0,
        lineStyle: {
          width: 2.5,
          color: '#6B8CA6'
        },
        itemStyle: {
          color: '#6B8CA6',
          borderColor: '#FFFFFF',
          borderWidth: 2,
          shadowBlur: 8,
          shadowColor: 'rgba(107, 140, 166, 0.3)'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(107, 140, 166, 0.15)' },
            { offset: 0.5, color: 'rgba(107, 140, 166, 0.05)' },
            { offset: 1, color: 'rgba(107, 140, 166, 0.01)' }
          ])
        },
        emphasis: {
          scale: true,
          scaleSize: 10,
          itemStyle: {
            color: '#6B8CA6',
            borderColor: '#FFFFFF',
            borderWidth: 3,
            shadowBlur: 12,
            shadowColor: 'rgba(107, 140, 166, 0.5)'
          }
        },
        // 端点数值标签
        label: {
          show: true,
          position: 'top',
          distance: 8,
          formatter: (params) => {
            if (params.value >= 10000) {
              return (params.value / 10000).toFixed(1) + '万'
            }
            return params.value
          },
          color: '#6B8CA6',
          fontSize: 11,
          fontWeight: 500,
          fontFamily: 'Microsoft YaHei'
        },
        animationDuration: 1800,
        animationEasing: 'cubicOut',
        data: apiCallsData
      },
      // 用户增长（次系列1：平滑折线，右Y轴）
      {
        name: '用户增长',
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        yAxisIndex: 1,
        lineStyle: {
          width: 2,
          color: '#2D4059'
        },
        itemStyle: {
          color: '#2D4059',
          borderColor: '#FFFFFF',
          borderWidth: 2,
          shadowBlur: 6,
          shadowColor: 'rgba(45, 64, 89, 0.25)'
        },
        emphasis: {
          scale: true,
          scaleSize: 8,
          itemStyle: {
            color: '#2D4059',
            borderColor: '#FFFFFF',
            borderWidth: 2,
            shadowBlur: 10,
            shadowColor: 'rgba(45, 64, 89, 0.4)'
          }
        },
        label: {
          show: true,
          position: 'bottom',
          distance: 8,
          formatter: (params) => {
            if (params.value >= 10000) {
              return (params.value / 10000).toFixed(1) + '万'
            }
            return params.value
          },
          color: '#2D4059',
          fontSize: 11,
          fontWeight: 500,
          fontFamily: 'Microsoft YaHei'
        },
        animationDuration: 1800,
        animationEasing: 'cubicOut',
        animationDelay: 150,
        data: userGrowthData
      },
      // 公告发布（次系列2：平滑折线，右Y轴）
      {
        name: '公告发布',
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        yAxisIndex: 1,
        lineStyle: {
          width: 1.75,
          color: '#8696A7'
        },
        itemStyle: {
          color: '#8696A7',
          borderColor: '#FFFFFF',
          borderWidth: 2,
          shadowBlur: 4,
          shadowColor: 'rgba(134, 150, 167, 0.2)'
        },
        emphasis: {
          scale: true,
          scaleSize: 8,
          itemStyle: {
            color: '#8696A7',
            borderColor: '#FFFFFF',
            borderWidth: 2,
            shadowBlur: 8,
            shadowColor: 'rgba(134, 150, 167, 0.35)'
          }
        },
        label: {
          show: true,
          position: 'top',
          distance: 8,
          formatter: '{c}',
          color: '#8696A7',
          fontSize: 11,
          fontWeight: 500,
          fontFamily: 'Microsoft YaHei'
        },
        animationDuration: 1800,
        animationEasing: 'cubicOut',
        animationDelay: 300,
        data: announcementsData
      },
      // 平均值参考线
      {
        name: '平均API',
        type: 'line',
        markLine: {
          silent: true,
          symbol: 'none',
          label: {
            show: true,
            position: 'insideEndTop',
            formatter: '均值 ' + (avgApi >= 10000 ? (avgApi / 10000).toFixed(1) + '万' : avgApi),
            color: '#FFFFFF',
            fontSize: 10,
            fontFamily: 'Microsoft YaHei',
            backgroundColor: 'rgba(107, 140, 166, 0.85)',
            padding: [3, 8],
            borderRadius: 4
          },
          lineStyle: {
            color: '#6B8CA6',
            width: 1,
            type: 'dotted',
            opacity: 0.6
          },
          data: [
            { type: 'average', yAxis: avgApi }
          ]
        },
        lineStyle: {
          opacity: 0
        },
        itemStyle: {
          opacity: 0
        },
        data: []
      }
    ]
  }
  
  lineChart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  
  if (pieChart) {
    pieChart.dispose()
  }
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    backgroundColor: '#FFFFFF',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E2E5EB',
      borderWidth: 1,
      padding: [12, 16],
      textStyle: {
        color: '#333333',
        fontSize: 13,
        fontFamily: 'Microsoft YaHei, sans-serif'
      },
      formatter: (params) => {
        return `<div style="font-weight: 600; margin-bottom: 8px; font-size: 14px;">${params.name}</div>
                <div style="display: flex; justify-content: space-between; gap: 24px;">
                  <span style="color: #666666;">数量</span>
                  <span style="font-weight: 600; color: #333333;">${params.value.toLocaleString()}</span>
                </div>
                <div style="display: flex; justify-content: space-between; gap: 24px;">
                  <span style="color: #666666;">占比</span>
                  <span style="font-weight: 600; color: #333333;">${params.percent.toFixed(1)}%</span>
                </div>`
      }
    },
    legend: {
      orient: 'vertical',
      right: '8%',
      top: 'center',
      textStyle: {
        color: '#666666',
        fontSize: 12,
        fontFamily: 'Microsoft YaHei, sans-serif'
      },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 14,
      icon: 'circle'
    },
    series: [
      {
        name: '用户分布',
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#FFFFFF',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          scale: true,
          scaleSize: 6,
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          },
          label: {
            show: true,
            fontSize: 18,
            fontWeight: '600',
            color: '#333333',
            fontFamily: 'Microsoft YaHei, sans-serif'
          }
        },
        labelLine: {
          show: false
        },
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: (idx) => idx * 80,
        data: [
          { value: 4200, name: '活跃用户', itemStyle: { color: '#2D4059' } },
          { value: 2800, name: '休眠用户', itemStyle: { color: '#94A3B8' } },
          { value: 1500, name: '新注册', itemStyle: { color: '#6B8CA6' } },
          { value: 800, name: 'VIP用户', itemStyle: { color: '#333333' } },
          { value: 700, name: '管理员', itemStyle: { color: '#CBD5E1' } }
        ]
      }
    ]
  }
  
  pieChart.setOption(option)
}

const openDetailModal = (item) => {
  currentDetail.value = item
  detailModalVisible.value = true
  nextTick(() => {
    initDetailChart()
  })
}

const getDetailStats = (label) => {
  const mockData = generateMockData()
  switch (label) {
    case '新增用户':
      return [
        { label: '峰值', value: Math.max(...mockData.userGrowth), trend: 'up', percent: '+15%' },
        { label: '均值', value: Math.round(mockData.userGrowth.reduce((a, b) => a + b, 0) / mockData.userGrowth.length), trend: 'up', percent: '+8%' },
        { label: '最小值', value: Math.min(...mockData.userGrowth), trend: 'down', percent: '-5%' },
        { label: '环比增长', value: '12.5%', trend: 'up', percent: '+3.2%' }
      ]
    case 'API调用':
      return [
        { label: '峰值', value: Math.max(...mockData.apiCalls).toLocaleString(), trend: 'up', percent: '+22%' },
        { label: '均值', value: Math.round(mockData.apiCalls.reduce((a, b) => a + b, 0) / mockData.apiCalls.length).toLocaleString(), trend: 'up', percent: '+15%' },
        { label: '最小值', value: Math.min(...mockData.apiCalls).toLocaleString(), trend: 'down', percent: '-8%' },
        { label: '成功率', value: '99.2%', trend: 'up', percent: '+0.5%' }
      ]
    case '公告发布':
      return [
        { label: '总发布', value: mockData.announcements.reduce((a, b) => a + b, 0), trend: 'up', percent: '+18%' },
        { label: '日均发布', value: Math.round(mockData.announcements.reduce((a, b) => a + b, 0) / mockData.announcements.length), trend: 'up', percent: '+12%' },
        { label: '最高日', value: Math.max(...mockData.announcements), trend: 'up', percent: '+25%' },
        { label: '阅读率', value: '85.7%', trend: 'up', percent: '+2.1%' }
      ]
    case '平均活跃度':
      return [
        { label: '活跃用户', value: '4,200', trend: 'up', percent: '+12%' },
        { label: '日活率', value: '68.5%', trend: 'up', percent: '+3.2%' },
        { label: '周活率', value: '82.3%', trend: 'up', percent: '+5.1%' },
        { label: '月活率', value: '91.2%', trend: 'up', percent: '+2.8%' }
      ]
    default:
      return []
  }
}

const initDetailChart = () => {
  if (!detailChartRef.value || !currentDetail.value) return
  
  if (detailChart) {
    detailChart.dispose()
  }
  
  detailChart = echarts.init(detailChartRef.value)
  
  const mockData = generateMockData()
  const colors = {
    '新增用户': '#6366f1',
    'API调用': '#10b981',
    '公告发布': '#3b82f6',
    '平均活跃度': '#f59e0b'
  }
  
  let data = []
  if (currentDetail.value.dataKey === 'userGrowth') {
    data = mockData.userGrowth
  } else if (currentDetail.value.dataKey === 'apiCalls') {
    data = mockData.apiCalls
  } else if (currentDetail.value.dataKey === 'announcements') {
    data = mockData.announcements
  } else {
    data = mockData.userGrowth.map((_, i) => Math.round(mockData.apiCalls[i] / (mockData.userGrowth[i] || 1)))
  }
  
  const mainColor = colors[currentDetail.value.label] || '#6366f1'
  
  const option = {
    backgroundColor: '#FFFFFF',
    grid: {
      left: '5%',
      right: '5%',
      bottom: '5%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: mockData.labels,
      axisLine: {
        lineStyle: { color: '#e2e8f0' }
      },
      axisLabel: {
        color: '#64748b',
        fontSize: 11
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#64748b',
        fontSize: 11
      },
      splitLine: {
        lineStyle: { color: '#f1f5f9', type: 'dashed' }
      }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: mainColor },
          { offset: 1, color: mainColor + '66' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      animationDuration: 800,
      animationEasing: 'cubicOut',
      data: data
    }]
  }
  
  detailChart.setOption(option)
}

const handleTabChange = (tab) => {
  activeTab.value = tab
  nextTick(() => {
    updateLineChart()
  })
}

const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
  detailChart?.resize()
}

onMounted(() => {
  loadStatistics()
  nextTick(() => {
    initLineChart()
    initPieChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
  detailChart?.dispose()
})

watch(activeTab, () => {
  nextTick(() => {
    updateLineChart()
  })
})

watch(detailModalVisible, (val) => {
  if (!val && detailChart) {
    detailChart.dispose()
    detailChart = null
  }
})
</script>

<style scoped>
.statistics-container {
  padding: 0 24px 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  animation: cardFadeIn 0.6s ease forwards;
  opacity: 0;
  transform: translateY(20px);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
  }
}

@keyframes cardFadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-glow {
  position: absolute;
  top: -50%;
  right: -30%;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.5;
}

.stat-icon {
  position: relative;
  z-index: 1;
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.stat-content {
  position: relative;
  z-index: 1;
  
  .stat-value {
    font-size: 32px;
    font-weight: 800;
    color: #1e293b;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 14px;
    color: #64748b;
    margin-top: 4px;
  }
  
  .stat-change {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    margin-top: 8px;
    padding: 2px 8px;
    border-radius: 12px;
    
    &.positive {
      color: #10b981;
      background: rgba(16, 185, 129, 0.1);
    }
    
    &.negative {
      color: #ef4444;
      background: rgba(239, 68, 68, 0.1);
    }
  }
}

.charts-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.chart-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  
  &.el-button--primary {
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);
  }
}

.chart-container {
  padding: 20px 0;
  width: 100%;
  min-width: 0;
}

.line-chart {
  width: 100%;
  height: 380px;
}

.pie-chart {
  width: 100%;
  height: 280px;
}

.bottom-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  width: 100%;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.overview-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  
  &:hover {
    background: white;
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    border-color: rgba(99, 102, 241, 0.2);
  }
}

.overview-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.overview-item:hover .overview-icon {
  transform: scale(1.1);
}

.overview-info {
  flex: 1;
  
  .overview-value {
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
  }
  
  .overview-label {
    font-size: 13px;
    color: #64748b;
    margin-top: 2px;
  }
}

.overview-arrow {
  color: #94a3b8;
  transition: all 0.3s ease;
}

.overview-item:hover .overview-arrow {
  color: #6366f1;
  transform: translateX(4px);
}

.detail-modal {
  .el-dialog__header {
    padding: 24px 24px 0;
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__title {
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
  }
}

.detail-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.detail-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-title {
  .detail-value {
    font-size: 32px;
    font-weight: 800;
    color: #1e293b;
  }
  
  .detail-label {
    font-size: 14px;
    color: #64748b;
    margin-top: 4px;
  }
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.detail-stat-item {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  
  .detail-stat-label {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 8px;
  }
  
  .detail-stat-value {
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
    display: flex;
    align-items: center;
    gap: 6px;
    
    &.trend-up {
      color: #10b981;
    }
    
    &.trend-down {
      color: #ef4444;
    }
  }
  
  .detail-stat-trend {
    font-size: 12px;
    font-weight: 500;
  }
}

.detail-chart {
  height: 180px;
}

.mini-chart {
  width: 100%;
  height: 100%;
}
</style>