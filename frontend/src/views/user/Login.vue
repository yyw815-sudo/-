<template>
  <div class="login-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- 动态渐变背景 -->
    <div class="gradient-bg"></div>
    
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
    
    <!-- 浮动图标 -->
    <div class="floating-icons">
      <span class="icon-item">💊</span>
      <span class="icon-item">🏥</span>
      <span class="icon-item">❤️</span>
      <span class="icon-item">📋</span>
      <span class="icon-item">💉</span>
    </div>
    
    <!-- 粒子动画背景 -->
    <canvas id="particles" class="particles-canvas"></canvas>
    
    <!-- 昼夜模式切换 -->
    <div class="theme-toggle">
      <el-tooltip :content="isDarkMode ? '切换到浅色模式 ☀️' : '切换到深色模式 🌙'" placement="left">
        <el-button circle @click="toggleTheme" class="theme-btn">
          <span v-if="isDarkMode">☀️</span>
          <span v-else>🌙</span>
        </el-button>
      </el-tooltip>
      <span class="theme-label">{{ isDarkMode ? '深色' : '浅色' }}</span>
    </div>
    
    <div class="login-container">
      <!-- 系统介绍 -->
      <div class="system-intro">
        <div class="intro-content">
          <h1 class="intro-title">智能提醒，健康相伴</h1>
          <p class="intro-desc">慢性病用药智能提醒系统，为您的健康保驾护航</p>
          <div class="intro-features">
            <div class="feature-item">
              <span class="feature-icon">⏰</span>
              <span class="feature-text">智能定时提醒</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">👨‍👩‍👧</span>
              <span class="feature-text">家庭协作管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span class="feature-text">用药记录分析</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 登录卡片 -->
      <div class="login-card">
        <!-- Logo 区域 -->
        <div class="logo-area">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
            </svg>
          </div>
          <h1 class="system-name">慢性病用药智能提醒系统</h1>
        </div>
        
        <!-- 角色选择 -->
        <div class="role-selector">
          <div 
            class="role-btn" 
            :class="{ active: loginForm.role === 'user' }"
            @click="switchRole('user')"
          >
            <span class="role-icon">👤</span>
            <span class="role-text">用户登录</span>
          </div>
          <div 
            class="role-btn" 
            :class="{ active: loginForm.role === 'admin' }"
            @click="switchRole('admin')"
          >
            <span class="role-icon">🔐</span>
            <span class="role-text">管理员登录</span>
          </div>
        </div>
        
        <!-- 账号登录表单 -->
        <el-form 
          v-if="loginForm.loginMode === 'account'"
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules" 
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <div class="input-group">
            <el-icon class="input-icon"><User /></el-icon>
            <el-input 
              v-model="loginForm.userName" 
              :placeholder="loginForm.role === 'admin' ? '请输入管理员账号' : '请输入用户名'" 
              size="large"
              class="custom-input"
            />
          </div>
          
          <div class="input-group">
            <el-icon class="input-icon"><Lock /></el-icon>
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              class="custom-input"
              show-password
            />
          </div>
          
          <div class="form-options">
            <el-checkbox v-model="rememberPwd">记住密码</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>
          
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>
        
        <!-- 手机号登录表单 -->
        <el-form 
          v-else
          ref="phoneFormRef" 
          :model="phoneForm" 
          :rules="phoneRules" 
          class="login-form"
          @keyup.enter="handlePhoneLogin"
        >
          <div class="input-group">
            <el-icon class="input-icon"><Iphone /></el-icon>
            <el-input 
              v-model="phoneForm.phone" 
              placeholder="请输入手机号" 
              size="large"
              class="custom-input"
            />
          </div>
          
          <div class="input-group phone-code-group">
            <div class="phone-input-wrapper">
              <el-icon class="input-icon"><Message /></el-icon>
              <el-input 
                v-model="phoneForm.code" 
                placeholder="请输入验证码" 
                size="large"
                class="custom-input"
              />
            </div>
            <el-button 
              class="send-code-btn"
              :disabled="codeCooldown > 0"
              @click="sendCode"
            >
              {{ codeCooldown > 0 ? `${codeCooldown}s` : '获取验证码' }}
            </el-button>
          </div>
          
          <el-button 
            type="primary" 
            size="large" 
            :loading="phoneLoading" 
            class="login-btn"
            @click="handlePhoneLogin"
          >
            {{ phoneLoading ? '登录中...' : '验 证 登 录' }}
          </el-button>
        </el-form>
        
        <!-- 登录模式切换 -->
        <div class="login-mode-toggle">
          <el-button text @click="toggleLoginMode">
            <span v-if="loginForm.loginMode === 'account'">📱 使用手机号登录</span>
            <span v-else>👤 使用账号密码登录</span>
          </el-button>
        </div>
        
        <!-- 底部信息 -->
        <div class="login-footer">
          <template v-if="loginForm.role === 'user'">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </template>
          <template v-else>
            <span class="admin-tip">🔐 管理员专属登录通道</span>
          </template>
        </div>
      </div>
      
      <!-- 底部波浪 -->
      <div class="wave-footer">
        <svg viewBox="0 0 1440 320" preserveAspectRatio="none">
          <path fill="rgba(255, 255, 255, 0.3)" d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendCode as sendCodeApi, phoneLogin as phoneLoginApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const phoneFormRef = ref(null)
const loading = ref(false)
const phoneLoading = ref(false)
const rememberPwd = ref(false)
const isDarkMode = ref(false)
const codeCooldown = ref(0)

const loginForm = reactive({
  userName: '',
  password: '',
  role: 'user',
  loginMode: 'account'
})

const phoneForm = reactive({
  phone: '',
  code: ''
})

const loginRules = {
  userName: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { min: 11, max: 11, message: '手机号必须为11位', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' }
  ]
}

// 粒子动画
let particlesCanvas, particlesCtx, particles = []
let animationFrame

const initParticles = () => {
  particlesCanvas = document.getElementById('particles')
  if (!particlesCanvas) return
  
  particlesCtx = particlesCanvas.getContext('2d')
  particlesCanvas.width = window.innerWidth
  particlesCanvas.height = window.innerHeight
  
  for (let i = 0; i < 60; i++) {
    particles.push({
      x: Math.random() * particlesCanvas.width,
      y: Math.random() * particlesCanvas.height,
      vx: (Math.random() - 0.5) * 0.3,
      vy: (Math.random() - 0.5) * 0.3,
      size: Math.random() * 2 + 1,
      opacity: Math.random() * 0.5 + 0.1
    })
  }
  
  animateParticles()
}

const animateParticles = () => {
  if (!particlesCtx) return
  
  particlesCtx.clearRect(0, 0, particlesCanvas.width, particlesCanvas.height)
  
  particles.forEach((p, i) => {
    p.x += p.vx
    p.y += p.vy
    
    if (p.x < 0 || p.x > particlesCanvas.width) p.vx *= -1
    if (p.y < 0 || p.y > particlesCanvas.height) p.vy *= -1
    
    particlesCtx.beginPath()
    particlesCtx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    particlesCtx.fillStyle = isDarkMode.value 
      ? `rgba(255, 255, 255, ${p.opacity})` 
      : `rgba(102, 126, 234, ${p.opacity})`
    particlesCtx.fill()
    
    particles.forEach((p2, j) => {
      if (i === j) return
      const dx = p.x - p2.x
      const dy = p.y - p2.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 100) {
        particlesCtx.beginPath()
        particlesCtx.moveTo(p.x, p.y)
        particlesCtx.lineTo(p2.x, p2.y)
        particlesCtx.strokeStyle = isDarkMode.value 
          ? `rgba(255, 255, 255, ${0.08 * (1 - distance / 100)})` 
          : `rgba(102, 126, 234, ${0.08 * (1 - distance / 100)})`
        particlesCtx.stroke()
      }
    })
  })
  
  animationFrame = requestAnimationFrame(animateParticles)
}

// 昼夜模式切换
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
}

// 切换角色
const switchRole = (role) => {
  loginForm.role = role
  loginForm.userName = ''
  loginForm.password = ''
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

// 切换登录模式
const toggleLoginMode = () => {
  loginForm.loginMode = loginForm.loginMode === 'account' ? 'phone' : 'account'
}

// 发送验证码
const sendCode = async () => {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    const res = await sendCodeApi(phoneForm.phone)
    ElMessage.success('验证码已发送，请注意查收')
    console.log('验证码（开发环境）：', res.data.code)
    
    codeCooldown.value = 60
    const timer = setInterval(() => {
      codeCooldown.value--
      if (codeCooldown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('发送验证码失败，请重试')
  }
}

// 手机号登录
const handlePhoneLogin = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    phoneLoading.value = true
    try {
      const res = await phoneLoginApi({
        phone: phoneForm.phone,
        code: phoneForm.code
      })
      
      userStore.token = res.data.token
      userStore.role = 'user'
      userStore.userInfo = {
        userId: res.data.userId,
        userName: res.data.userName,
        realname: res.data.realname,
        phone: res.data.phone
      }
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      localStorage.setItem('role', 'user')
      
      ElMessage.success('登录成功')
      router.push('/home')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      phoneLoading.value = false
    }
  })
}

// 账号登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      if (loginForm.role === 'admin') {
        await userStore.adminLogin({
          adminName: loginForm.userName,
          password: loginForm.password
        })
      } else {
        await userStore.login(loginForm)
      }
      ElMessage.success('登录成功')
      router.push('/home')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

// 生命周期
onMounted(() => {
  // 恢复记住的密码
  const remembered = localStorage.getItem('rememberedUser')
  if (remembered) {
    const data = JSON.parse(remembered)
    loginForm.userName = data.userName || ''
    loginForm.password = data.password || ''
    loginForm.role = data.role || 'user'
    rememberPwd.value = true
  }
  
  // 初始化粒子动画
  initParticles()
  
  // 窗口调整
  window.addEventListener('resize', () => {
    if (particlesCanvas) {
      particlesCanvas.width = window.innerWidth
      particlesCanvas.height = window.innerHeight
    }
  })
})

onUnmounted(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame)
  }
})
</script>

<style lang="scss" scoped>
.login-page {
  width: 100vw;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  transition: all 0.3s ease;
  
  // 动态渐变背景
  .gradient-bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(-45deg, #667eea, #764ba2, #f093fb, #f5576c, #4facfe, #00f2fe);
    background-size: 400% 400%;
    animation: gradientShift 15s ease infinite;
    z-index: 0;
    
    @keyframes gradientShift {
      0% { background-position: 0% 50%; }
      50% { background-position: 100% 50%; }
      100% { background-position: 0% 50%; }
    }
  }
  
  &.dark-mode {
    .gradient-bg {
      background: linear-gradient(-45deg, #1a1a2e, #16213e, #0f3460, #1a1a2e);
      background-size: 400% 400%;
    }
    
    .system-intro {
      .intro-content {
        .intro-title,
        .intro-desc {
          color: rgba(255, 255, 255, 0.95);
        }
        
        .intro-features .feature-item {
          color: rgba(255, 255, 255, 0.9);
        }
      }
    }
    
    .login-card {
      background: rgba(30, 30, 50, 0.95);
      
      .system-name {
        color: #fff;
      }
      
      .input-group {
        :deep(.el-input__wrapper) {
          background: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.2);
          
          .el-input__inner {
            color: #fff;
            
            &::placeholder {
              color: rgba(255, 255, 255, 0.5);
            }
          }
          
          .input-icon {
            color: rgba(255, 255, 255, 0.7);
          }
        }
        
        &.phone-code-group {
          .phone-input-wrapper {
            position: relative;
            
            .input-icon {
              position: absolute;
              left: 16px;
              top: 50%;
              transform: translateY(-50%);
              z-index: 10;
              font-size: 18px;
              color: rgba(255, 255, 255, 0.7);
              pointer-events: none;
            }
            
            :deep(.el-input__wrapper) {
              background: rgba(255, 255, 255, 0.1);
              border-color: rgba(255, 255, 255, 0.2);
              
              .el-input__inner {
                color: #fff;
                
                &::placeholder {
                  color: rgba(255, 255, 255, 0.5);
                }
              }
            }
          }
          
          .send-code-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;
          }
        }
      }
      
      .form-options :deep(.el-checkbox__label) {
        color: rgba(255, 255, 255, 0.7);
      }
      
      .forgot-link {
        color: #667eea;
      }
      
      .login-footer {
        .footer-text,
        .admin-tip {
          color: rgba(255, 255, 255, 0.7);
        }
      }
    }
    
    .theme-toggle .theme-label {
      color: rgba(255, 255, 255, 0.8);
      background: rgba(255, 255, 255, 0.1);
    }
  }
  
  // 背景装饰
  .bg-decoration {
    position: fixed;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 2;
    
    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      animation: float 6s ease-in-out infinite;
      
      &.circle-1 {
        width: 300px;
        height: 300px;
        top: -100px;
        right: -100px;
        animation-delay: 0s;
      }
      
      &.circle-2 {
        width: 200px;
        height: 200px;
        bottom: 10%;
        left: -50px;
        animation-delay: 2s;
      }
      
      &.circle-3 {
        width: 150px;
        height: 150px;
        top: 40%;
        right: 5%;
        animation-delay: 4s;
      }
      
      &.circle-4 {
        width: 100px;
        height: 100px;
        bottom: 30%;
        right: 20%;
        animation-delay: 1s;
      }
    }
    
    @keyframes float {
      0%, 100% { transform: translateY(0) rotate(0deg); }
      50% { transform: translateY(-20px) rotate(5deg); }
    }
  }
  
  // 浮动图标
  .floating-icons {
    position: fixed;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 2;
    
    .icon-item {
      position: absolute;
      font-size: 28px;
      opacity: 0.15;
      animation: iconFloat 15s ease-in-out infinite;
      
      &:nth-child(1) { top: 15%; left: 10%; animation-delay: 0s; }
      &:nth-child(2) { top: 25%; right: 15%; animation-delay: 3s; }
      &:nth-child(3) { top: 60%; left: 8%; animation-delay: 6s; }
      &:nth-child(4) { top: 70%; right: 12%; animation-delay: 9s; }
      &:nth-child(5) { top: 40%; left: 5%; animation-delay: 12s; }
    }
    
    @keyframes iconFloat {
      0%, 100% { transform: translate(0, 0) rotate(0deg); }
      25% { transform: translate(15px, -20px) rotate(8deg); }
      50% { transform: translate(0, -35px) rotate(0deg); }
      75% { transform: translate(-15px, -20px) rotate(-8deg); }
    }
  }
  
  // 粒子动画画布
  .particles-canvas {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 3;
  }
  
  // 昼夜模式切换
  .theme-toggle {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 100;
    display: flex;
    align-items: center;
    gap: 10px;
    
    .theme-btn {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      border: none;
      font-size: 24px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.1);
      }
    }
    
    .theme-label {
      color: rgba(255, 255, 255, 0.8);
      font-size: 14px;
      font-weight: 500;
      background: rgba(255, 255, 255, 0.1);
      padding: 6px 12px;
      border-radius: 20px;
      backdrop-filter: blur(10px);
    }
  }
  
  // 登录容器
  .login-container {
    position: relative;
    z-index: 10;
    min-height: 100vh;
    padding: 60px 20px 120px;
    
    // 系统介绍
    .system-intro {
      text-align: center;
      margin-bottom: 40px;
      
      .intro-content {
        max-width: 600px;
        margin: 0 auto;
        padding: 40px 20px;
        
        .intro-title {
          font-size: 36px;
          font-weight: 700;
          color: white;
          margin: 0 0 15px 0;
          text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
        }
        
        .intro-desc {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.9);
          margin: 0 0 30px 0;
          line-height: 1.6;
        }
        
        .intro-features {
          display: flex;
          justify-content: center;
          gap: 30px;
          flex-wrap: wrap;
          
          .feature-item {
            display: flex;
            align-items: center;
            gap: 8px;
            color: white;
            font-size: 15px;
            
            .feature-icon {
              font-size: 20px;
            }
          }
        }
      }
    }
    
    // 登录卡片
    .login-card {
      width: 100%;
      max-width: 440px;
      margin: 0 auto;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(20px);
      border-radius: 24px;
      padding: 40px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2);
      }
      
      // Logo 区域
      .logo-area {
        text-align: center;
        margin-bottom: 25px;
        
        .logo-icon {
          width: 70px;
          height: 70px;
          margin: 0 auto 12px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
          animation: pulse 3s ease-in-out infinite;
          
          svg {
            width: 35px;
            height: 35px;
            color: white;
          }
        }
        
        @keyframes pulse {
          0%, 100% { transform: scale(1); box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4); }
          50% { transform: scale(1.05); box-shadow: 0 15px 40px rgba(102, 126, 234, 0.6); }
        }
        
        .system-name {
          font-size: 26px;
          font-weight: 600;
          color: #333;
          margin: 0;
        }
      }
      
      // 角色选择
      .role-selector {
        display: flex;
        gap: 10px;
        margin-bottom: 25px;
        
        .role-btn {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          padding: 12px 20px;
          border: 2px solid #e8e8e8;
          border-radius: 12px;
          background: #fafafa;
          cursor: pointer;
          transition: all 0.3s ease;
          
          .role-icon {
            font-size: 18px;
          }
          
          .role-text {
            font-size: 16px;
            font-weight: 500;
            color: #666;
          }
          
          &:hover {
            border-color: #667eea;
            background: white;
          }
          
          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: transparent;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
            
            .role-text {
              color: white;
            }
          }
        }
      }
      
      // 登录表单
      .login-form {
        .input-group {
          position: relative;
          margin-bottom: 18px;
          
          .input-icon {
            position: absolute;
            left: 16px;
            top: 50%;
            transform: translateY(-50%);
            z-index: 10;
            font-size: 18px;
            color: #999;
            pointer-events: none;
          }
          
          :deep(.el-input__wrapper) {
            padding: 8px 15px 8px 48px;
            border-radius: 12px;
            border: 2px solid #e8e8e8;
            background: #fafafa;
            box-shadow: none;
            transition: all 0.3s ease;
            
            &:hover {
              border-color: #667eea;
            }
            
            &.is-focus {
              border-color: #667eea;
              background: white;
              box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }
            
            .el-input__inner {
              height: 40px;
              font-size: 16px;
            }
          }
          
          &.phone-code-group {
            display: flex;
            gap: 10px;
            align-items: stretch;
            
            .phone-input-wrapper {
              position: relative;
              flex: 1;
              
              .input-icon {
                position: absolute;
                left: 16px;
                top: 50%;
                transform: translateY(-50%);
                z-index: 10;
                font-size: 18px;
                color: #999;
                pointer-events: none;
              }
              
              :deep(.el-input__wrapper) {
                padding: 8px 15px 8px 48px;
                border-radius: 12px;
                border: 2px solid #e8e8e8;
                background: #fafafa;
                box-shadow: none;
                transition: all 0.3s ease;
                
                &:hover {
                  border-color: #667eea;
                }
                
                &.is-focus {
                  border-color: #667eea;
                  background: white;
                  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
                }
                
                .el-input__inner {
                  height: 40px;
                  font-size: 16px;
                }
              }
            }
            
            .send-code-btn {
              flex-shrink: 0;
              height: auto;
              min-width: 110px;
              border-radius: 12px;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              border: none;
              color: white;
              font-size: 14px;
              padding: 0 16px;
              
              &:hover {
                opacity: 0.9;
              }
              
              &:disabled {
                background: #ccc;
                cursor: not-allowed;
              }
            }
          }
        }
        
        .form-options {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          
          :deep(.el-checkbox__label) {
            font-size: 15px;
            color: #666;
          }

          .forgot-link {
            color: #667eea;
            text-decoration: none;
            font-size: 15px;
            
            &:hover {
              text-decoration: underline;
            }
          }
        }
        
        .login-btn {
          width: 100%;
          height: 50px;
          border-radius: 12px;
          font-size: 18px;
          font-weight: 600;
          letter-spacing: 3px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;
          color: white;
          cursor: pointer;
          transition: all 0.3s ease;
          box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
          }
          
          &:active {
            transform: translateY(0);
          }
        }
      }
      
      // 登录模式切换
      .login-mode-toggle {
        text-align: center;
        margin: 15px 0;
        
        :deep(.el-button) {
          color: #667eea;
          font-size: 15px;

          &:hover {
            color: #764ba2;
          }
        }
      }
      
      // 底部信息
      .login-footer {
        text-align: center;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #f0f0f0;
        
        .footer-text {
          color: #999;
          font-size: 15px;
        }

        .register-link {
          color: #667eea;
          text-decoration: none;
          font-weight: 500;
          margin-left: 5px;
          font-size: 15px;

          &:hover {
            text-decoration: underline;
          }
        }

        .admin-tip {
          color: #999;
          font-size: 15px;
        }
      }
    }
    
    // 底部波浪
    .wave-footer {
      position: fixed;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 120px;
      pointer-events: none;
      z-index: 5;
      
      svg {
        width: 100%;
        height: 100%;
        animation: wave 10s linear infinite;
      }
      
      @keyframes wave {
        0% { transform: translateX(0); }
        100% { transform: translateX(-50%); }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-page {
    .login-container {
      padding: 40px 15px 100px;
      
      .system-intro {
        margin-bottom: 30px;
        
        .intro-content {
          padding: 30px 15px;
          
          .intro-title {
            font-size: 28px;
          }
          
          .intro-desc {
            font-size: 14px;
          }
          
          .intro-features {
            gap: 20px;
            
            .feature-item {
              font-size: 14px;
            }
          }
        }
      }
      
      .login-card {
        padding: 30px 20px;
        
        .role-selector {
          flex-direction: column;
        }
      }
      
      .wave-footer {
        height: 80px;
      }
    }
  }
}

@media (max-width: 480px) {
  .login-page {
    .login-container {
      .system-intro {
        .intro-content {
          .intro-title {
            font-size: 24px;
          }
        }
      }
      
      .login-card {
        padding: 25px 15px;
      }
    }
  }
}
</style>
