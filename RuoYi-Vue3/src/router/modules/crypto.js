import Layout from '@/layout'

export default {
  path: '/crypto',
  component: Layout,
  redirect: '/crypto/dashboard',
  name: 'Crypto',
  meta: {
    title: '加密货币管理',
    icon: 'money'
  },
  children: [
    {
      path: 'dashboard',
      component: () => import('@/views/crypto/index'),
      name: 'CryptoDashboard',
      meta: { title: '概览', icon: 'dashboard' }
    },
    {
      path: 'coin',
      component: () => import('@/views/crypto/coin/index'),
      name: 'CryptoCoin',
      meta: { title: '代币管理', icon: 'list' }
    },
    {
      path: 'monitor',
      component: () => import('@/views/crypto/monitor/index'),
      name: 'CryptoMonitor',
      meta: { title: '监控配置', icon: 'eye-open' },
      hidden: true
    },
    {
      path: 'records',
      component: () => import('@/views/crypto/records/index'),
      name: 'CryptoRecords',
      meta: { title: '查询记录', icon: 'documentation' },
      hidden: true
    },
    {
      path: 'ranking',
      component: () => import('@/views/crypto/ranking/index'),
      name: 'CryptoRanking',
      meta: { title: '排行榜', icon: 'chart' },
      hidden: true
    }
  ]
} 