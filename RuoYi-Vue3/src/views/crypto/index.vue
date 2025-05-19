<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>代币总数</span>
          </div>
          <div class="card-panel-num">{{ stats.totalCoins || 0 }}</div>
          <div class="card-panel-icon">
            <svg-icon icon-class="money" style="font-size: 42px;" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>今日查询次数</span>
          </div>
          <div class="card-panel-num">{{ stats.todayQueries || 0 }}</div>
          <div class="card-panel-icon">
            <svg-icon icon-class="search" style="font-size: 42px;" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>监控中代币</span>
          </div>
          <div class="card-panel-num">{{ stats.monitoringCoins || 0 }}</div>
          <div class="card-panel-icon">
            <svg-icon icon-class="eye-open" style="font-size: 42px;" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>活跃用户</span>
          </div>
          <div class="card-panel-num">{{ stats.activeUsers || 0 }}</div>
          <div class="card-panel-icon">
            <svg-icon icon-class="peoples" style="font-size: 42px;" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 热门代币排行 -->
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>热门代币排行</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToCoinList">查看更多</el-button>
          </div>
          <el-table :data="hotCoins" style="width: 100%" v-loading="loading">
            <el-table-column label="排名" width="70">
              <template slot-scope="scope">
                <el-tag :type="scope.$index === 0 ? 'danger' : scope.$index === 1 ? 'warning' : scope.$index === 2 ? 'success' : 'info'">{{ scope.$index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="代币" min-width="180">
              <template slot-scope="scope">
                <div class="coin-info">
                  <el-image 
                    style="width: 30px; height: 30px; margin-right: 10px;" 
                    :src="scope.row.logoUrl" 
                    fit="contain">
                    <div slot="error" class="image-slot">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </el-image>
                  <div>
                    <div class="coin-name">{{ scope.row.name }}</div>
                    <div class="coin-symbol">{{ scope.row.symbol }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="查询次数" prop="queryCount" align="right"></el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 最近查询记录 -->
      <el-col :span="12">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>最近查询记录</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="goToQueryRecords">查看更多</el-button>
          </div>
          <el-table :data="recentQueries" style="width: 100%" v-loading="loading">
            <el-table-column label="代币" min-width="180">
              <template slot-scope="scope">
                <div class="coin-info">
                  <el-image 
                    style="width: 30px; height: 30px; margin-right: 10px;" 
                    :src="scope.row.logoUrl" 
                    fit="contain">
                    <div slot="error" class="image-slot">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </el-image>
                  <div>
                    <div class="coin-name">{{ scope.row.name }}</div>
                    <div class="coin-symbol">{{ scope.row.symbol }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="查询时间" align="right" width="160">
              <template slot-scope="scope">
                {{ parseTime(scope.row.queryTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 功能导航卡片 -->
      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click.native="goToCoinList">
          <div class="feature-icon">
            <i class="el-icon-coin"></i>
          </div>
          <div class="feature-title">代币管理</div>
          <div class="feature-desc">管理系统中的加密代币信息</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click.native="goToMonitorConfig">
          <div class="feature-icon">
            <i class="el-icon-monitor"></i>
          </div>
          <div class="feature-title">监控配置</div>
          <div class="feature-desc">设置价格监控和通知规则</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click.native="goToQueryRecords">
          <div class="feature-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="feature-title">查询记录</div>
          <div class="feature-desc">查看历史查询记录和统计</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="feature-card" shadow="hover" @click.native="goToRankings">
          <div class="feature-icon">
            <i class="el-icon-trophy"></i>
          </div>
          <div class="feature-title">排行榜</div>
          <div class="feature-desc">查看各类币种和用户排行</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
// import { getCoinStatistics, getRecentQueries, getRankingData } from "@/api/crypto/index";

export default {
  name: "CryptoDashboard",
  data() {
    return {
      loading: false,
      stats: {
        totalCoins: 0,
        todayQueries: 0,
        monitoringCoins: 0,
        activeUsers: 0
      },
      hotCoins: [
        {
          coinId: 1,
          name: "Bitcoin",
          symbol: "BTC",
          logoUrl: "https://cryptologos.cc/logos/bitcoin-btc-logo.png",
          queryCount: 1024
        },
        {
          coinId: 2,
          name: "Ethereum",
          symbol: "ETH",
          logoUrl: "https://cryptologos.cc/logos/ethereum-eth-logo.png",
          queryCount: 768
        },
        {
          coinId: 3,
          name: "Binance Coin",
          symbol: "BNB",
          logoUrl: "https://cryptologos.cc/logos/bnb-bnb-logo.png",
          queryCount: 512
        },
        {
          coinId: 4,
          name: "Solana",
          symbol: "SOL",
          logoUrl: "https://cryptologos.cc/logos/solana-sol-logo.png",
          queryCount: 384
        },
        {
          coinId: 5,
          name: "Ripple",
          symbol: "XRP",
          logoUrl: "https://cryptologos.cc/logos/xrp-xrp-logo.png",
          queryCount: 256
        }
      ],
      recentQueries: [
        {
          queryId: 1,
          name: "Bitcoin",
          symbol: "BTC",
          logoUrl: "https://cryptologos.cc/logos/bitcoin-btc-logo.png",
          queryTime: new Date(Date.now() - 5 * 60 * 1000) // 5分钟前
        },
        {
          queryId: 2,
          name: "Ethereum",
          symbol: "ETH",
          logoUrl: "https://cryptologos.cc/logos/ethereum-eth-logo.png",
          queryTime: new Date(Date.now() - 15 * 60 * 1000) // 15分钟前
        },
        {
          queryId: 3,
          name: "Binance Coin",
          symbol: "BNB",
          logoUrl: "https://cryptologos.cc/logos/bnb-bnb-logo.png",
          queryTime: new Date(Date.now() - 30 * 60 * 1000) // 30分钟前
        },
        {
          queryId: 4, 
          name: "Dogecoin",
          symbol: "DOGE",
          logoUrl: "https://cryptologos.cc/logos/dogecoin-doge-logo.png",
          queryTime: new Date(Date.now() - 45 * 60 * 1000) // 45分钟前
        },
        {
          queryId: 5,
          name: "Cardano",
          symbol: "ADA",
          logoUrl: "https://cryptologos.cc/logos/cardano-ada-logo.png",
          queryTime: new Date(Date.now() - 60 * 60 * 1000) // 1小时前
        }
      ]
    };
  },
  created() {
    this.getStatData();
  },
  methods: {
    getStatData() {
      this.loading = true;
      
      // 模拟数据
      this.stats = {
        totalCoins: 158,
        todayQueries: 1256,
        monitoringCoins: 42,
        activeUsers: 83
      };
      
      this.loading = false;
      
      // 实际API调用，当后端接口准备好后取消注释
      // getCoinStatistics().then(response => {
      //   this.stats = response.data;
      //   this.loading = false;
      // }).catch(() => {
      //   this.loading = false;
      // });
      
      // getRecentQueries({limit: 5}).then(response => {
      //   this.recentQueries = response.data;
      // });
      
      // getRankingData('hot').then(response => {
      //   this.hotCoins = response.data;
      // });
    },
    goToCoinList() {
      this.$router.push('/crypto/coin');
    },
    goToMonitorConfig() {
      this.$router.push('/crypto/monitor');
    },
    goToQueryRecords() {
      this.$router.push('/crypto/records');
    },
    goToRankings() {
      this.$router.push('/crypto/ranking');
    }
  }
};
</script>

<style scoped>
.card-panel-num {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}
.card-panel-icon {
  float: right;
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 48px;
  color: #eaeaea;
}
.coin-info {
  display: flex;
  align-items: center;
}
.coin-name {
  font-weight: bold;
}
.coin-symbol {
  font-size: 12px;
  color: #909399;
}

.feature-card {
  cursor: pointer;
  height: 150px;
  transition: all 0.3s;
}
.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}
.feature-icon {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 10px;
}
.feature-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 5px;
}
.feature-desc {
  font-size: 14px;
  color: #909399;
}
</style> 