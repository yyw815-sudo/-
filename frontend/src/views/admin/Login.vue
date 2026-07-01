<template>
  <div class="admin-login">
    <div class="login-box">
      <h2>管理员登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules">
        <el-form-item prop="adminName">
          <el-input v-model="form.adminName" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ adminName: '', password: '' })
const rules = {
  adminName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const login = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await store.adminLogin(form)
      ElMessage.success('登录成功')
      router.push('/admin/dashboard')
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.admin-login {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  text-align: center;
}
.login-box h2 {
  margin-bottom: 30px;
  color: #333;
}
</style>