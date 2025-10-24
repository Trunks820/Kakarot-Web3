import Layout from '@/layout'

export default {
  path: '/crypto',
  component: Layout,
  redirect: '/crypto/caRecord',
  name: 'Crypto',
  meta: {
    title: '加密货币管理',
    icon: 'money'
  },
  children: [
    {
      path: 'tokenMonitor',
      component: () => import('@/views/crypto/tokenMonitor/index'),
      name: 'TokenMonitor',
      meta: { title: '代币监控', icon: 'data-analysis' }
    },
    {
      path: 'caRecord',
      component: () => import('@/views/crypto/caRecord/index'),
      name: 'CryptoCaRecord',
      meta: { title: 'CA记录', icon: 'documentation' }
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
      meta: { title: '监控配置', icon: 'monitor' }
    },
    {
      path: 'wallet',
      component: () => import('@/views/crypto/wallet/index'),
      name: 'CryptoWallet',
      meta: { title: '钱包管理', icon: 'wallet' }
    },
    {
      path: 'okx-signals',
      component: () => import('@/views/crypto/okx-signals/index'),
      name: 'OkxSignals',
      meta: { title: 'OKX信号', icon: 'chart' }
    },
    {
      path: 'blockMonitor',
      component: () => import('@/views/crypto/blockMonitor/index'),
      name: 'BlockMonitor',
      meta: { title: '历史播报', icon: 'bell' }
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