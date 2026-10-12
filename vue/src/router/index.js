import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '@/utils/auth'

/** 各角色登录后的首页 */
const roleHome = {
  ADMIN: '/admin/home',
  TEACHER: '/teacher/home',
  STUDENT: '/student/home',
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 系统默认入口：直接进入学生端 OJ 首页
    { path: '/', redirect: '/student/home' },

    // 管理员端
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      redirect: '/admin/home',
      meta: { role: 'ADMIN' },
      children: [
        { path: 'home', component: () => import('@/views/admin/Home.vue') },
        { path: 'admin', component: () => import('@/views/admin/Admin.vue') },
        { path: 'teacher', component: () => import('@/views/admin/Teacher.vue') },
        { path: 'student', component: () => import('@/views/admin/Student.vue') },
        { path: 'clazz', component: () => import('@/views/admin/Clazz.vue') },
        { path: 'contest', component: () => import('@/views/admin/Contest.vue') },
        { path: 'signup', component: () => import('@/views/admin/Signup.vue') },
        { path: 'problem', component: () => import('@/views/admin/Problem.vue') },
        { path: 'submission', component: () => import('@/views/admin/Submission.vue') },
        { path: 'password', component: () => import('@/views/common/ChangePassword.vue') },
      ],
    },

    // 教师端
    {
      path: '/teacher',
      component: () => import('@/views/teacher/TeacherLayout.vue'),
      redirect: '/teacher/home',
      meta: { role: 'TEACHER' },
      children: [
        { path: 'home', component: () => import('@/views/teacher/Home.vue') },
        { path: 'contest', component: () => import('@/views/teacher/Contest.vue') },
        { path: 'signup', component: () => import('@/views/teacher/Signup.vue') },
        { path: 'problem', component: () => import('@/views/teacher/Problem.vue') },
        { path: 'submission', component: () => import('@/views/teacher/Submission.vue') },
        { path: 'student', component: () => import('@/views/teacher/Student.vue') },
        { path: 'profile', component: () => import('@/views/teacher/Profile.vue') },
        { path: 'password', component: () => import('@/views/common/ChangePassword.vue') },
      ],
    },

    // 学生端
    {
      path: '/student',
      component: () => import('@/views/student/StudentLayout.vue'),
      redirect: '/student/home',
      meta: { role: 'STUDENT' },
      children: [
        { path: 'home', component: () => import('@/views/student/Home.vue') },
        { path: 'contest', component: () => import('@/views/student/Contest.vue') },
        // 学生端竞赛详情页面：展示竞赛信息、题目列表和排行榜
        { path: 'contest/:id', component: () => import('@/views/student/ContestDetail.vue') },
        { path: 'signup', component: () => import('@/views/student/Signup.vue') },
        { path: 'problem', component: () => import('@/views/student/Problem.vue') },
        // 学生端题目详情 + 做题页面（左题面右代码编辑，下方结果栏）
        { path: 'problem/:id', component: () => import('@/views/student/ProblemDetail.vue') },
        { path: 'submission', component: () => import('@/views/student/Submission.vue') },
        { path: 'profile', component: () => import('@/views/student/Profile.vue') },
        { path: 'password', component: () => import('@/views/common/ChangePassword.vue') },
      ],
    },

    { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
    { path: '/register', component: () => import('@/views/Register.vue'), meta: { public: true } },
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

/**
 * 全局路由守卫：
 * - 公开页面（登录、注册）直接放行；
 * - 未登录一律跳登录页；
 * - 已登录但访问了不属于自己角色的页面时，送回该角色的首页（不清除登录态）。
 */
router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }
  if (!getToken()) {
    return '/login'
  }
  const needRole = to.matched.find((r) => r.meta?.role)?.meta?.role
  if (needRole && getUser().role !== needRole) {
    return roleHome[getUser().role] || '/login'
  }
  return true
})

export default router
