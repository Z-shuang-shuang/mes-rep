<script lang="ts" setup>
import router from '@/router';
import axios from 'axios';
import {ref} from 'vue'

let username = ref("")
let password = ref("")

// ⭐ 加这一段配置（在 submit 函数之前）
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

function submit():void{
    axios.post("http://localhost:8080/api/v1/auth/login",{
        username:username.value,
        password:password.value
    }).then(res=>{
        console.log(res.data)
        if(res.data.code === 200){
            localStorage.setItem("token",res.data.data.token)
            router.push({name:"Index"})
        }
    })
}
</script>

<template>
    <form
        @submit.prevent="submit"
    >
        <input
            v-model="username"
            placeholder="请输入用户名"
            placeholder-class="input-placeholder"
        />
        <input
            v-model="password"
            placeholder="请输入密码"
            placeholder-class="input-placeholder"
            
        />
        <button form-type="submit">提交</button>
        <button form-type="reset">重置</button>
    </form>
</template>


<style scoped>

</style>