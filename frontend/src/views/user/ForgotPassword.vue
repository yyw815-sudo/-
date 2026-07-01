<template>
  <div class="forgot-page">
    <div class="gradient-bg"></div>
    <div class="forgot-card">
      <h2 class="forgot-title">重置密码</h2>
      <p class="forgot-desc">请输入您的手机号，我们将发送验证码帮您重置密码</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="forgot-form">
        <!-- 步骤1：输入手机号 -->
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
            :disabled="step > 1"
          />
        </el-form-item>

        <!-- 步骤1：获取验证码按钮 -->
        <el-form-item v-if="step === 1">
          <el-button
            type="primary"
            size="large"
            :loading="sendingCode"
            :disabled="!form.phone || form.phone.length !== 11"
            class="send-btn"
            @click="handleSendCode"
          >
            获取验证码
          </el-button>
        </el-form-item>

        <!-- 步骤2：输入验证码 -->
        <el-form-item v-if="step === 2" prop="code">
          <el-input
            v-model="form.code"
            placeholder="请输入验证码"
            prefix-icon="Message"
            size="large"
            maxlength="6"
          />
        </el-form-item>

        <!-- 步骤2：输入新密码 -->
        <el-form-item v-if="step === 2" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 步骤2：确认新密码 -->
        <el-form-item v-if="step === 2" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 步骤2：重置密码按钮 -->
        <el-form-item v-if="step === 2">
          <el-button
            type="primary"
            size="large"
            :loading="resetting"
            class="reset-btn"
            @click="handleResetPassword"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 返回登录 -->
      <div class="back-login">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode as sendCodeApi, resetPassword } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const step = ref(1)
const sendingCode = ref(false)
const resetting = ref(false)

const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { len: 11, message: '手机号必须为11位', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validateField('phone')
  } catch {
    return
  }

  sendingCode.value = true
  try {
    const res = await sendCodeApi(form.phone)
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      step.value = 2
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleResetPassword = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  resetting.value = true
  try {
    const res = await resetPassword({
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (error) {
    ElMessage.error('重置密码失败')
  } finally {
    resetting.value = false
  }
}
</script>

<style lang="scss" scoped>
.forgot-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;

  .gradient-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f64f59 100%);
    animation: gradientShift 10s ease infinite;
    z-index: 0;
  }

  .forgot-card {
    width: 400px;
    padding: 40px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    z-index: 1;
    position: relative;

    .forgot-title {
      text-align: center;
      margin-bottom: 10px;
      color: #333;
      font-size: 24px;
      font-weight: 600;
    }

    .forgot-desc {
      text-align: center;
      margin-bottom: 30px;
      color: #666;
      font-size: 14px;
    }

    .forgot-form {
      .send-btn,
      .reset-btn {
        width: 100%;
        height: 48px;
        font-size: 16px;
        border-radius: 12px;
      }
    }

    .back-login {
      text-align: center;
      margin-top: 20px;

      a {
        color: #667eea;
        font-size: 14px;
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}
</style>