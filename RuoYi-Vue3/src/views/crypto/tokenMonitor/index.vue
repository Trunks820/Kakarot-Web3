<template>
  <div class="app-container">
    <!-- 🎯 链类型标签 -->
    <el-tag 
      :type="chainConfig.type" 
      size="large" 
      effect="dark"
      style="margin-bottom: 16px; font-size: 14px; padding: 8px 16px;"
    >
      {{ chainConfig.label }}
    </el-tag>

    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <!-- 第一行：短选项 -->
      <!-- 🎯 SOL链显示数据来源选择，BSC链隐藏（默认Fourmeme） -->
      <el-form-item 
        v-if="currentChain === 'sol'" 
        label="数据来源" 
        prop="source"
      >
        <el-select v-model="queryParams.source" placeholder="请选择" clearable style="width: 120px">
          <el-option 
            v-for="item in sourceOptions" 
            :key="item.value" 
            :label="item.label" 
            :value="item.value" 
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="监控状态" prop="monitorStatus">
        <el-select v-model="queryParams.monitorStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="已监控" value="monitored" />
          <el-option label="未监控" value="unmonitored" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="Twitter" prop="hasTwitter">
        <el-select v-model="queryParams.hasTwitter" placeholder="全部" clearable style="width: 130px">
          <el-option label="全部" value="" />
          <el-option label="推特主页" value="profile" />
          <el-option label="推文" value="tweet" />
          <el-option label="社区" value="community" />
          <el-option label="无推特" value="none" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="市值筛选" prop="minMarketCap">
        <el-select v-model="queryParams.minMarketCap" placeholder="全部" clearable style="width: 130px">
          <el-option label="全部" value="" />
          <el-option label="≥ 30万" :value="300000" />
          <el-option label="≥ 50万" :value="500000" />
          <el-option label="≥ 100万" :value="1000000" />
          <el-option label="≥ 300万" :value="3000000" />
          <el-option label="≥ 500万" :value="5000000" />
          <el-option label="≥ 1千万" :value="10000000" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="关注状态" prop="isFollowing">
        <el-select v-model="queryParams.isFollowing" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="已关注" value="1" />
          <el-option label="未关注" value="0" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="合约地址" prop="ca">
        <el-input
          v-model="queryParams.ca"
          placeholder="请输入合约地址"
          clearable
          style="width: 300px"
        />
      </el-form-item>
      
      <br />
      
      <!-- 第二行：时间范围 + 按钮 -->
      <el-form-item label="时间范围" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 - Flex布局 -->
    <div class="toolbar-container mb8">
      <el-space wrap>
        <el-button 
          type="primary" 
          plain 
          icon="Monitor" 
          @click="handleBatchMonitor"
          :disabled="multiple"
        >
          批量监控
        </el-button>
        <el-button 
          type="success" 
          plain 
          icon="MagicStick" 
          @click="applyQuickMonitor"
          :disabled="multiple"
        >
          应用智能配置
        </el-button>
        <el-button 
          type="danger" 
          plain 
          icon="RemoveFilled" 
          @click="handleBatchCancelMonitor"
          :disabled="multiple"
        >
          批量取消监控
        </el-button>
        <el-button type="info" plain icon="Refresh" @click="refreshData">刷新数据</el-button>
      </el-space>
      <div class="toolbar-right">
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="tokenList"
      :row-key="row => row.ca"
      @selection-change="handleSelectionChange"
      ref="tokenTable"
      class="token-table"
      style="width: 100%"
    >
        <el-table-column type="selection" width="50" align="center" :reserve-selection="true" />
      
      <!-- Token信息 -->
      <el-table-column label="Token信息" align="left" min-width="300" v-if="columns[0].visible">
        <template #default="scope">
          <div class="token-info-card">
            <div class="token-content">
              <!-- 第一行：符号 + 来源标签 + 市值排名 -->
              <div class="token-title-row">
                <div class="token-title-left">
                  <span class="token-symbol">{{ scope.row.tokenSymbol || 'Unknown' }}</span>
                </div>
                <div class="token-title-right">
                  <el-tag 
                    v-if="scope.row.source === 'pump'" 
                    type="primary" 
                    size="small"
                    class="source-tag"
                  >
                    Pump
                  </el-tag>
                  <el-tag 
                    v-else-if="scope.row.source === 'bonk'" 
                    type="success" 
                    size="small"
                    class="source-tag"
                  >
                    BONK
                  </el-tag>
                  <el-tag 
                    v-else-if="scope.row.source === 'fourmeme'" 
                    type="warning" 
                    size="small"
                    class="source-tag"
                  >
                    Fourmeme
                  </el-tag>
                  <!-- 状态点：市值指示（仅高市值显示） -->
                  <span v-if="scope.row.highestMarketCap >= 10000000" class="status-dot legendary" title="传奇 ≥ 1千万"></span>
                  <span v-else-if="scope.row.highestMarketCap >= 5000000" class="status-dot epic" title="史诗 ≥ 500万"></span>
                  <span v-else-if="scope.row.highestMarketCap >= 3000000" class="status-dot rare" title="稀有 ≥ 300万"></span>
                  <span v-else-if="scope.row.highestMarketCap >= 1000000" class="status-dot hot" title="火热 ≥ 100万"></span>
                  <span v-else-if="scope.row.highestMarketCap >= 500000" class="status-dot warm" title="温暖 ≥ 50万"></span>
                </div>
              </div>
              
              <!-- 第二行：Token名称（副标题） -->
              <div class="token-subtitle" :title="scope.row.tokenName">
                {{ scope.row.tokenName || '-' }}
              </div>
              
              <!-- 第三行：合约地址 -->
              <div class="token-address">
                <el-tooltip content="点击图标复制 / 双击复制整行" placement="top">
                  <span 
                    class="token-ca" 
                    :class="{ 'copied': scope.row._copied }"
                    @click="copyText(scope.row.ca, scope.row)" 
                    @dblclick="copyText(scope.row.ca, scope.row)"
                    style="white-space: normal; word-break: break-all;"
                  >
                    {{ scope.row.ca }}
                  </span>
                </el-tooltip>
                <el-tooltip :content="scope.row._copied ? '已复制!' : '复制'" placement="top">
                  <el-icon 
                    class="copy-icon" 
                    :class="{ 'copied': scope.row._copied }"
                    @click="copyText(scope.row.ca, scope.row)"
                  >
                    <DocumentCopy />
                  </el-icon>
                </el-tooltip>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <!-- 发射时间 -->
      <el-table-column label="发射时间" align="center" min-width="170" sortable prop="launchTime" v-if="columns[1].visible" show-overflow-tooltip>
        <template #default="scope">
          <div class="time-cell">
            <div>{{ parseTime(scope.row.launchTime, '{y}-{m}-{d}') }}</div>
            <div class="time-sub">{{ parseTime(scope.row.launchTime, '{h}:{i}:{s}') }}</div>
          </div>
        </template>
      </el-table-column>

      <!-- 市值 -->
      <el-table-column label="历史最高市值" align="right" min-width="170" sortable prop="highestMarketCap" v-if="columns[2].visible" show-overflow-tooltip>
        <template #default="scope">
          <span class="market-cap">{{ formatMarketCap(scope.row.highestMarketCap) }}</span>
        </template>
      </el-table-column>
      
      <!-- Twitter操作 -->
      <el-table-column label="Twitter" align="center" min-width="200" v-if="columns[3].visible">
        <template #default="scope">
          <div v-if="scope.row.twitterUrl" class="twitter-actions">
            <!-- 第一行：类型标签 -->
            <div class="twitter-tag-row">
              <el-tag 
                :type="getTwitterTypeTag(scope.row.twitterUrl).type" 
                size="small"
              >
                {{ getTwitterTypeTag(scope.row.twitterUrl).label }}
              </el-tag>
            </div>
            
            <!-- 第二行：操作按钮 -->
            <div class="twitter-button-row">
              <el-tooltip content="查看" placement="top">
                <el-button 
                  circle
                  plain
                  size="small"
                  @click="openLink(scope.row.twitterUrl)"
                  class="action-btn"
                >
                  <el-icon><Link /></el-icon>
                </el-button>
              </el-tooltip>
              
              <!-- 只有推特主页才显示推送配置和关注按钮 -->
              <template v-if="isTwitterProfile(scope.row.twitterUrl)">
                <el-tooltip content="推送配置" placement="top">
                  <el-button 
                    circle
                    plain
                    size="small"
                    @click="handleTwitterPush(scope.row)"
                    class="action-btn"
                  >
                    <el-icon><BellFilled /></el-icon>
                  </el-button>
                </el-tooltip>
                
                <el-tooltip :content="scope.row.isFollowing ? '取消关注' : '关注'" placement="top">
                  <el-button 
                    circle
                    plain
                    size="small"
                    @click="handleToggleFollow(scope.row)"
                    :loading="scope.row.followLoading"
                    :class="{'action-btn': true, 'is-following': scope.row.isFollowing}"
                  >
                    <el-icon v-if="!scope.row.followLoading">
                      <StarFilled v-if="scope.row.isFollowing" />
                      <Star v-else />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </template>
            </div>
          </div>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <!-- 监控状态 -->
      <el-table-column label="监控状态" align="center" min-width="200" v-if="columns[4].visible" show-overflow-tooltip>
        <template #default="scope">
          <div class="monitor-status-cell">
            <el-tag 
              v-if="scope.row.monitorStatus === '1'" 
              type="success"
              size="small"
              class="status-tag"
            >
              监控中
            </el-tag>
            <el-tag 
              v-else-if="scope.row.monitorStatus === '0'" 
              type="info"
              size="small"
              class="status-tag"
            >
              已停用
            </el-tag>
            <el-tag v-else type="info" size="small" class="status-tag">未监控</el-tag>
            
            <el-tooltip content="配置" placement="top">
              <el-button 
                circle
                plain
                size="small"
                @click="handleMonitorConfig(scope.row)"
                class="config-btn"
              >
                <el-icon><Setting /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      
      <!-- 入库时间 -->
      <el-table-column label="入库时间" align="center" min-width="170" v-if="columns[5].visible" show-overflow-tooltip>
        <template #default="scope">
          <div class="time-cell">
            <div>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</div>
            <div class="time-sub">{{ parseTime(scope.row.createdAt, '{h}:{i}:{s}') }}</div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Twitter推送配置对话框 -->
    <el-dialog 
      v-model="twitterPushDialog.visible" 
      title="Twitter推送配置" 
      width="600px"
      @close="handleTwitterPushClose"
    >
      <el-form :model="twitterPushDialog.form" label-width="120px">
        <el-form-item label="Token信息">
          <div class="dialog-token-info">
            <span class="token-symbol">{{ twitterPushDialog.tokenInfo.symbol }}</span>
            <span class="token-name">{{ twitterPushDialog.tokenInfo.name }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="Twitter账号">
          <el-link :href="twitterPushDialog.tokenInfo.twitterUrl" target="_blank" type="primary">
            {{ twitterPushDialog.tokenInfo.twitterUrl }}
          </el-link>
        </el-form-item>
        
        <el-divider>推送配置</el-divider>
        
        <el-form-item label="关注推送">
          <el-switch v-model="twitterPushDialog.form.followPush" />
          <span class="form-tip">监控该账号的关注动态</span>
        </el-form-item>
        
        <el-form-item label="推文推送">
          <el-switch v-model="twitterPushDialog.form.tweetPush" />
          <span class="form-tip">推送该账号发布的新推文</span>
        </el-form-item>
        
        <el-form-item label="转发推送">
          <el-switch v-model="twitterPushDialog.form.retweetPush" />
          <span class="form-tip">推送该账号的转发动态</span>
        </el-form-item>
        
        <el-form-item label="回复推送">
          <el-switch v-model="twitterPushDialog.form.replyPush" />
          <span class="form-tip">推送该账号的回复内容</span>
        </el-form-item>
        
        <el-form-item label="头像更换推送">
          <el-switch v-model="twitterPushDialog.form.avatarChangePush" />
          <span class="form-tip">监控账号头像更换</span>
        </el-form-item>
        
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="twitterPushDialog.form.notifyMethods">
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="推送状态">
          <el-switch 
            v-model="twitterPushDialog.form.enabled" 
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="twitterPushDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleTwitterPushSave" :loading="twitterPushDialog.loading">
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 监控配置对话框 -->
    <el-dialog
      v-model="monitorDialog.visible"
      title="监控配置"
      :width="'min(720px, 90vw)'"
      @close="handleMonitorDialogClose"
    >
      <el-form :model="monitorDialog.form" label-width="100px" ref="monitorFormRef">
        <!-- Token信息 -->
        <el-form-item label="Token">
          <div class="dialog-token-info">
            <span class="token-symbol">{{ monitorDialog.tokenInfo.symbol }}</span>
            <span class="token-name">{{ monitorDialog.tokenInfo.name }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="合约地址">
          <el-input v-model="monitorDialog.form.coinAddress" disabled />
        </el-form-item>
        
        <el-divider content-position="left">监控事件</el-divider>
        
        <!-- 涨跌幅监控 -->
        <el-card shadow="never" class="event-card" :class="{ 'disabled': monitorDialog.form.status === '0' }">
          <template #header>
            <el-checkbox 
              v-model="monitorDialog.events.priceChange.enabled"
              :disabled="monitorDialog.form.status === '0'"
            >
              <span class="event-title">💹 涨跌幅变化</span>
            </el-checkbox>
          </template>
          <div v-if="monitorDialog.events.priceChange.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="涨幅阈值" label-position="top" class="event-field">
                  <el-input-number 
                    v-model="monitorDialog.events.priceChange.risePercent" 
                    :min="0" 
                    :max="1000"
                    :step="5"
                    placeholder="10"
                    :disabled="monitorDialog.form.status === '0'"
                  />
                  <span class="input-suffix">%</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="跌幅阈值" label-position="top" class="event-field">
                  <el-input-number 
                    v-model="monitorDialog.events.priceChange.fallPercent" 
                    :min="0" 
                    :max="100"
                    :step="5"
                    placeholder="10"
                    :disabled="monitorDialog.form.status === '0'"
                  />
                  <span class="input-suffix">%</span>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>
        
        <!-- 持币人数监控 -->
        <el-card shadow="never" class="event-card" :class="{ 'disabled': monitorDialog.form.status === '0' }">
          <template #header>
            <el-checkbox 
              v-model="monitorDialog.events.holders.enabled"
              :disabled="monitorDialog.form.status === '0'"
            >
              <span class="event-title">👥 持币人数变化</span>
            </el-checkbox>
          </template>
          <div v-if="monitorDialog.events.holders.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="增长阈值" label-position="top" class="event-field">
                  <el-input-number 
                    v-model="monitorDialog.events.holders.increasePercent" 
                    :min="0" 
                    :max="1000"
                    :step="5"
                    placeholder="30"
                    :disabled="monitorDialog.form.status === '0'"
                  />
                  <span class="input-suffix">%</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="减少阈值" label-position="top" class="event-field">
                  <el-input-number 
                    v-model="monitorDialog.events.holders.decreasePercent" 
                    :min="0" 
                    :max="100"
                    :step="5"
                    placeholder="20"
                    :disabled="monitorDialog.form.status === '0'"
                  />
                  <span class="input-suffix">%</span>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>
        
        <!-- 交易量监控 -->
        <el-card shadow="never" class="event-card" :class="{ 'disabled': monitorDialog.form.status === '0' }">
          <template #header>
            <el-checkbox 
              v-model="monitorDialog.events.volume.enabled"
              :disabled="monitorDialog.form.status === '0'"
            >
              <span class="event-title">📊 交易量阈值</span>
            </el-checkbox>
          </template>
          <div v-if="monitorDialog.events.volume.enabled" class="event-config">
            <el-form-item label="交易量阈值" label-position="top" class="event-field">
              <el-input-number 
                v-model="monitorDialog.events.volume.threshold" 
                :min="0" 
                :max="100000000"
                :step="1000"
                :precision="0"
                placeholder="5000"
                :disabled="monitorDialog.form.status === '0'"
                style="width: 100%"
              />
              <div class="event-tip">💡 单位：USD，触发通知的最小交易量</div>
            </el-form-item>
          </div>
        </el-card>
        
        <el-divider content-position="left">触发设置</el-divider>
        
        <!-- 触发逻辑 -->
        <el-form-item label="触发逻辑">
          <el-radio-group 
            v-model="monitorDialog.form.triggerLogic"
            :disabled="monitorDialog.form.status === '0'"
          >
            <el-radio label="any">
              <span>任一条件满足即触发</span>
            </el-radio>
            <el-radio label="all">
              <span>需同时满足所有已勾选条件</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 通知方式 -->
        <el-form-item>
          <template #label>
            <span class="required-mark">*</span>通知方式
          </template>
          <el-checkbox-group 
            v-model="monitorDialog.notifyMethodsArray"
            :disabled="monitorDialog.form.status === '0'"
          >
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-divider content-position="left">其他设置</el-divider>
        
        <!-- 监控状态 -->
        <el-form-item label="监控状态">
          <el-switch 
            v-model="monitorDialog.form.status" 
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        
        <!-- 备注 -->
        <el-form-item label="备注">
          <div class="remark-tip">💡 记录触发条件备注，便于后续识别</div>
          <el-input 
            v-model="monitorDialog.form.remark" 
            type="textarea" 
            :rows="2"
            placeholder="可选：记录监控策略或特殊说明"
            :disabled="monitorDialog.form.status === '0'"
          />
        </el-form-item>
      </el-form>
      
      <!-- 监控条件实时预览 -->
      <el-alert 
        v-if="monitorConditionsSummary"
        :title="monitorConditionsSummary" 
        type="info" 
        :closable="false"
        class="monitor-preview"
      >
        <template #title>
          <div class="preview-title">📋 当前监控条件</div>
          <div class="preview-content">{{ monitorConditionsSummary }}</div>
        </template>
      </el-alert>
      
      <template #footer>
        <el-button @click="monitorDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleMonitorSave" :loading="monitorDialog.loading">
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量监控配置弹窗 -->
    <el-dialog
      v-model="batchMonitorDialog.visible"
      title="批量监控配置"
      :width="'min(720px, 90vw)'"
      @close="resetBatchMonitorForm"
    >
      <el-alert
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #title>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span>📊</span>
            <span>将为选中的 <strong>{{ selectedRows.length }}</strong> 个Token应用此监控配置</span>
          </div>
        </template>
      </el-alert>
      
      <el-form :model="batchMonitorDialog.form" label-width="100px">
        <!-- 显示选中的Token信息 -->
        <el-form-item label="应用范围">
          <el-tag
            v-for="(row, index) in selectedRows.slice(0, 5)"
            :key="row.ca"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ row.tokenSymbol || row.tokenName }}
          </el-tag>
          <el-tag v-if="selectedRows.length > 5" type="info">
            +{{ selectedRows.length - 5 }} 个
          </el-tag>
        </el-form-item>
        
        <el-divider content-position="left">
          <span style="font-weight: 600;">监控事件</span>
        </el-divider>
        
        <!-- 涨跌幅变化 -->
        <el-card class="event-card" :class="{ disabled: !batchMonitorDialog.events.priceChange.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="batchMonitorDialog.events.priceChange.enabled">
                📈 涨跌幅变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="batchMonitorDialog.events.priceChange.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="涨幅" label-position="top" class="event-field">
                  <el-input-number
                    v-model="batchMonitorDialog.events.priceChange.risePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="跌幅" label-position="top" class="event-field">
                  <el-input-number
                    v-model="batchMonitorDialog.events.priceChange.fallPercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>
        
        <!-- 持币人数变化 -->
        <el-card class="event-card" :class="{ disabled: !batchMonitorDialog.events.holders.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="batchMonitorDialog.events.holders.enabled">
                👥 持币人数变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="batchMonitorDialog.events.holders.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="增长" label-position="top" class="event-field">
                  <el-input-number
                    v-model="batchMonitorDialog.events.holders.increasePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="减少" label-position="top" class="event-field">
                  <el-input-number
                    v-model="batchMonitorDialog.events.holders.decreasePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>
        
        <!-- 交易量变化 -->
        <el-card class="event-card" :class="{ disabled: !batchMonitorDialog.events.volume.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="batchMonitorDialog.events.volume.enabled">
                💰 交易量阈值
              </el-checkbox>
            </div>
          </template>
          <div v-if="batchMonitorDialog.events.volume.enabled" class="event-config">
            <el-form-item label="交易量阈值" label-position="top" class="event-field">
              <el-input-number
                v-model="batchMonitorDialog.events.volume.threshold"
                :min="0"
                :max="100000000"
                :step="1000"
                :precision="0"
                style="width: 100%"
                placeholder="5000"
              />
              <div class="event-tip">💡 单位：USD，触发通知的最小交易量</div>
            </el-form-item>
          </div>
        </el-card>
        
        <el-divider content-position="left">
          <span style="font-weight: 600;">触发设置</span>
        </el-divider>
        
        <!-- 触发逻辑 -->
        <el-form-item label="触发逻辑">
          <el-radio-group v-model="batchMonitorDialog.form.triggerLogic">
            <el-radio label="any">
              任一条件满足即触发
              <span style="color: #909399; font-size: 12px;">（OR逻辑）</span>
            </el-radio>
            <el-radio label="all">
              需同时满足所有已勾选条件
              <span style="color: #909399; font-size: 12px;">（AND逻辑）</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-divider content-position="left">
          <span style="font-weight: 600;">其他设置</span>
        </el-divider>
        
        <!-- 通知方式 -->
        <el-form-item>
          <template #label>
            <span class="required-mark">*</span>
            <span>通知方式</span>
          </template>
          <el-checkbox-group v-model="batchMonitorDialog.notifyMethodsArray">
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <!-- 监控状态 -->
        <el-form-item label="监控状态">
          <el-switch
            v-model="batchMonitorDialog.form.status"
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        
        <!-- 备注 -->
        <el-form-item label="备注">
          <div class="remark-tip">💡 记录触发条件备注，便于后续识别</div>
          <el-input
            v-model="batchMonitorDialog.form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <!-- 当前监控条件预览 -->
      <el-alert 
        v-if="batchMonitorConditionsSummary"
        :title="batchMonitorConditionsSummary" 
        type="info" 
        :closable="false"
        class="monitor-preview"
      >
        <template #title>
          <div class="preview-title">📋 当前监控条件</div>
          <div class="preview-content">{{ batchMonitorConditionsSummary }}</div>
        </template>
      </el-alert>
      
      <template #footer>
        <el-button @click="batchMonitorDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveBatchMonitor">
          应用到选中的 {{ selectedRows.length }} 个Token
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TokenMonitor">
import { ref, reactive, computed, onMounted, onUnmounted, getCurrentInstance, watch } from 'vue'
import { useRoute } from 'vue-router'
import { 
  listToken, 
  followTwitter, 
  unfollowTwitter, 
  batchFollowTwitter, 
  batchUnfollowTwitter,
  getTwitterAccounts,
  getPushConfig,
  updatePushConfig
} from '@/api/crypto/token'
import { saveOrUpdateMonitorConfig, getMonitorConfigByCa, delMonitorConfig } from '@/api/crypto/monitorConfig'
import { 
  DocumentCopy, 
  Link, 
  BellFilled, 
  Star, 
  StarFilled, 
  Setting 
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const route = useRoute()

// 🎯 识别当前链类型（通过路由查询参数）
const currentChain = computed(() => {
  const chain = route.query.chain || 'sol'
  console.log('当前链类型:', chain, '路由参数:', route.query)
  return chain
})

// 📊 页面标题
const pageTitle = computed(() => {
  return currentChain.value === 'sol' ? 'SOL链Token监控' : 'BSC链Token监控'
})

// 📊 链类型标签配置
const chainConfig = computed(() => {
  if (currentChain.value === 'sol') {
    return {
      label: 'Solana链 (Pump + Bonk)',
      type: 'success',
      sources: ['pump', 'bonk']
    }
  } else {
    return {
      label: 'BSC链 (Fourmeme)',
      type: 'warning',
      sources: ['fourmeme']
    }
  }
})

// 📊 根据链类型动态生成数据源选项
const sourceOptions = computed(() => {
  if (currentChain.value === 'sol') {
    return [
      { label: '全部', value: 'all' },
      { label: 'Pump', value: 'pump' },
      { label: 'BONK', value: 'bonk' }
    ]
  } else {
    return [
      { label: 'Fourmeme', value: 'fourmeme' }
    ]
  }
})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  source: currentChain.value === 'sol' ? 'all' : 'fourmeme',
  monitorStatus: '',
  hasTwitter: '',
  minMarketCap: '',
  isFollowing: '',
  ca: ''
})

// 数据
const showSearch = ref(true)
const tokenList = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])
const selectedRows = ref([])
const single = ref(true)
const multiple = ref(true)

// 列配置
const columns = ref([
  { key: 0, label: 'Token信息', visible: true },
  { key: 1, label: '发射时间', visible: true },
  { key: 2, label: '市值', visible: true },
  { key: 3, label: 'Twitter', visible: true },
  { key: 4, label: '监控状态', visible: true },
  { key: 5, label: '入库时间', visible: true }
])

// Twitter推送配置对话框
const twitterPushDialog = reactive({
  visible: false,
  loading: false,
  tokenInfo: {
    ca: '',
    symbol: '',
    name: '',
    twitterUrl: ''
  },
  form: {
    followPush: false,
    tweetPush: false,
    retweetPush: false,
    replyPush: false,
    avatarChangePush: false,
    notifyMethods: [],
    enabled: true
  }
})

// 监控配置对话框
const monitorDialog = reactive({
  visible: false,
  loading: false,
  tokenInfo: {
    ca: '',
    symbol: '',
    name: ''
  },
  form: {
    id: null,
    coinAddress: '',
    tokenName: '',
    triggerLogic: 'any',  // 触发逻辑：any=任一条件，all=所有条件
    status: '1',
    remark: ''
  },
  // 事件配置
  events: {
    priceChange: {
      enabled: false,
      risePercent: null,    // 涨幅阈值
      fallPercent: null     // 跌幅阈值
    },
    holders: {
      enabled: false,
      increasePercent: null,  // 增长阈值
      decreasePercent: null   // 减少阈值
    },
    volume: {
      enabled: false,
      increasePercent: null,  // 增长阈值
      decreasePercent: null   // 减少阈值
    }
  },
  notifyMethodsArray: []  // 默认不选，让用户自己选择
})

// 定时刷新
let refreshInterval = null

// 判断是否是推特主页（提前定义，供computed使用）
const isTwitterProfile = (twitterUrl) => {
  if (!twitterUrl) return false
  // 推特主页：不包含 /status/、/communities/ 和 /search
  return !twitterUrl.includes('/status/') && 
         !twitterUrl.includes('/communities/') && 
         !twitterUrl.includes('/search')
}

// 初始化今天的时间范围
const initTodayDateRange = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  
  dateRange.value = [
    proxy.parseTime(today, '{y}-{m}-{d} {h}:{i}:{s}'),
    proxy.parseTime(endOfDay, '{y}-{m}-{d} {h}:{i}:{s}')
  ]
}

// 查询列表
const getList = () => {
  loading.value = true
  const params = { ...queryParams }
  
  // 添加时间范围
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
    console.log('时间范围:', { beginTime: params.beginTime, endTime: params.endTime })
  } else {
    console.log('时间范围为空:', dateRange.value)
  }
  
  // 🎯 处理数据源筛选
  if (currentChain.value === 'sol') {
    // SOL链：如果是'all'或为空，查询 pump 和 bonk
    if (params.source === 'all' || !params.source) {
      params.source = 'pump,bonk'
    }
  } else {
    // BSC链：固定查询 fourmeme
    params.source = 'fourmeme'
  }
  
  // 如果hasTwitter为空，则不传递
  if (params.hasTwitter === '') {
    delete params.hasTwitter
  }
  
  // 如果minMarketCap为空，则不传递
  if (params.minMarketCap === '') {
    delete params.minMarketCap
  }
  
  // 如果isFollowing为空，则不传递
  if (params.isFollowing === '') {
    delete params.isFollowing
  }
  
  // 如果monitorStatus为空，则不传递
  if (params.monitorStatus === '') {
    delete params.monitorStatus
  }
  
  console.log('最终查询参数:', params)
  
  listToken(params).then(response => {
    tokenList.value = response.rows.map(row => ({
      ...row,
      followLoading: false,
      isFollowing: false
    }))
    total.value = response.total
    
    // 加载Twitter账号状态
    loadTwitterAccountStatus()
  }).catch(error => {
    proxy.$modal.msgError('查询失败：' + (error.message || '未知错误'))
  }).finally(() => {
    loading.value = false
  })
}

// 加载Twitter账号状态
const loadTwitterAccountStatus = () => {
  // 提取所有有效的Twitter URL
  const twitterUrls = tokenList.value
    .filter(token => token.twitterUrl && isTwitterProfile(token.twitterUrl))
    .map(token => token.twitterUrl)
  
  if (twitterUrls.length === 0) {
    return
  }
  
  // 批量获取Twitter账号信息
  getTwitterAccounts(twitterUrls).then(response => {
    const accountMap = response.data || {}
    
    // 更新tokenList中的关注状态
    tokenList.value = tokenList.value.map(token => {
      if (token.twitterUrl && accountMap[token.twitterUrl]) {
        const account = accountMap[token.twitterUrl]
        return {
          ...token,
          isFollowing: account.isFollowing === 1
        }
      }
      return token
    })
  }).catch(error => {
    console.error('加载Twitter账号状态失败:', error)
  })
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  dateRange.value = []
  proxy.resetForm('queryRef')
  // 🎯 根据当前链类型重置数据源
  queryParams.source = currentChain.value === 'sol' ? 'all' : 'fourmeme'
  queryParams.monitorStatus = ''
  queryParams.hasTwitter = ''
  queryParams.minMarketCap = ''
  queryParams.isFollowing = ''
  queryParams.ca = ''
  initTodayDateRange()
  handleQuery()
}

// 刷新数据
const refreshData = () => {
  proxy.$modal.msgSuccess('数据已刷新')
  getList()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// ========================================
// 智能配置应用功能
// ========================================

// 应用智能配置
const applyQuickMonitor = async () => {
  if (selectedRows.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一个Token')
    return
  }
  
  // 从数据库读取配置
  let response
  try {
    const { getQuickMonitorByChain } = await import('@/api/crypto/quickMonitor')
    response = await getQuickMonitorByChain(currentChain.value)
  } catch (error) {
    proxy.$modal.msgError('加载配置失败: ' + (error.message || ''))
    return
  }
  
  if (!response || response.code !== 200 || !response.data || response.data.length === 0) {
    console.warn('Token监控-未找到配置:', response)
    proxy.$modal.msgWarning('未找到智能配置，请先在首页配置')
    return
  }
  
  console.log('Token监控-解析配置:', response.data)
  
  // 转换配置格式
  const configs = response.data.map(item => ({
    minMarketCap: parseFloat(item.minMarketCap),
    events: JSON.parse(item.eventsConfig || '{}'),
    notifyMethods: item.notifyMethods || ''
  }))
  
  // 按市值从高到低排序配置
  const sortedConfigs = configs.sort((a, b) => b.minMarketCap - a.minMarketCap)
  
  // 统计匹配结果
  const stats = {
    total: selectedRows.value.length,
    matched: 0,
    skipped: 0,
    byConfig: {}
  }
  
  selectedRows.value.forEach(token => {
    const marketCap = token.highestMarketCap || 0
    
    // 找到第一个满足条件的配置
    const matchedConfig = sortedConfigs.find(config => marketCap >= config.minMarketCap)
    
    if (matchedConfig) {
      stats.matched++
      const configLabel = formatMarketCap(matchedConfig.minMarketCap)
      stats.byConfig[configLabel] = (stats.byConfig[configLabel] || 0) + 1
    } else {
      stats.skipped++
    }
  })
  
  // 构建确认消息
  let confirmMessage = `<div style="line-height: 1.8;">
    <p><strong>将为选中的 ${stats.total} 个Token应用智能监控配置：</strong></p>
    <div style="margin: 12px 0; padding: 12px; background: #f5f7fa; border-radius: 6px;">`
  
  Object.entries(stats.byConfig).forEach(([label, count]) => {
    confirmMessage += `<p style="margin: 4px 0;">✅ ≥${label}：${count}个Token</p>`
  })
  
  if (stats.skipped > 0) {
    confirmMessage += `<p style="margin: 4px 0; color: #E6A23C;">⚠️ 低于300K：${stats.skipped}个Token（将跳过）</p>`
  }
  
  confirmMessage += `</div>
    <p style="color: #F56C6C; margin-top: 12px;">⚠️ 注意：已有监控配置的Token将被覆盖</p>
  </div>`
  
  try {
    await proxy.$modal.confirm(confirmMessage, '确认应用智能配置', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定应用',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  
  // 开始应用配置
  proxy.$modal.loading('正在应用配置，请稍候...')
  
  let successCount = 0
  let failCount = 0
  
  for (const token of selectedRows.value) {
    const marketCap = token.highestMarketCap || 0
    const matchedConfig = sortedConfigs.find(config => marketCap >= config.minMarketCap)
    
    if (!matchedConfig) {
      continue // 跳过低于最低门槛的Token
    }
    
    // 清理 volume 字段中的旧格式
    const cleanedEvents = { ...matchedConfig.events }
    if (cleanedEvents.volume) {
      const { enabled, threshold } = cleanedEvents.volume
      cleanedEvents.volume = { enabled, threshold }
    }
    
    try {
      await saveOrUpdateMonitorConfig({
        ca: token.ca,
        tokenName: token.tokenName,
        eventsConfig: JSON.stringify(cleanedEvents),
        triggerLogic: 'any',
        notifyMethods: matchedConfig.notifyMethods,
        status: '1',
        remark: `智能配置 - ≥${formatMarketCap(matchedConfig.minMarketCap)}`
      })
      successCount++
    } catch (error) {
      console.error(`Token ${token.ca} 配置失败:`, error)
      failCount++
    }
  }
  
  proxy.$modal.closeLoading()
  
  // 显示结果
  if (failCount === 0) {
    proxy.$modal.msgSuccess(`智能配置应用成功！已应用到 ${successCount} 个Token`)
  } else {
    proxy.$modal.msgWarning(`配置完成：成功 ${successCount} 个，失败 ${failCount} 个`)
  }
  
  // 刷新列表
  getList()
}

// 格式化市值（与首页组件保持一致）
const formatMarketCap = (value) => {
  if (!value || value === 0) return '-'
  if (value >= 10000000) return `${(value / 10000000).toFixed(1)}千万`
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
  if (value >= 1000) return `${(value / 1000).toFixed(0)}K`
  return value.toString()
}

// ========================================
// 批量监控功能
// ========================================

// 批量监控配置
const batchMonitorDialog = reactive({
  visible: false,
  form: {
    triggerLogic: 'any',
    status: '1',
    remark: ''
  },
  events: {
    priceChange: {
      enabled: false,
      risePercent: null,
      fallPercent: null
    },
    holders: {
      enabled: false,
      increasePercent: null,
      decreasePercent: null
    },
    volume: {
      enabled: false,
      threshold: null
    }
  },
  notifyMethodsArray: []
})

// 重置批量监控表单
const resetBatchMonitorForm = () => {
  batchMonitorDialog.form = {
    triggerLogic: 'any',
    status: '1',
    remark: ''
  }
  
  batchMonitorDialog.events = {
    priceChange: { enabled: false, risePercent: null, fallPercent: null },
    holders: { enabled: false, increasePercent: null, decreasePercent: null },
    volume: { enabled: false, threshold: null }
  }
  
  batchMonitorDialog.notifyMethodsArray = []
}

// 批量监控 - 打开配置弹窗
const handleBatchMonitor = () => {
  if (selectedRows.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一个Token')
    return
  }
  
  // 重置表单
  resetBatchMonitorForm()
  
  // 显示弹窗
  batchMonitorDialog.visible = true
}

// 保存批量监控
const saveBatchMonitor = async () => {
  // 1. 验证
  if (batchMonitorDialog.form.status === '1') {
    // 至少选择一个事件
    const hasEnabledEvent = Object.values(batchMonitorDialog.events).some(e => e.enabled)
    if (!hasEnabledEvent) {
      proxy.$modal.msgWarning('请至少选择一个监控事件')
      return
    }
    
    // 验证启用的事件至少有一个阈值
    for (const [key, event] of Object.entries(batchMonitorDialog.events)) {
      if (event.enabled) {
        const hasThreshold = Object.values(event)
          .filter(v => typeof v === 'number')
          .some(v => v !== null && v !== undefined)
        
        if (!hasThreshold) {
          const eventNames = {
            priceChange: '涨跌幅变化',
            holders: '持币人数变化',
            volume: '交易量变化'
          }
          proxy.$modal.msgWarning(`${eventNames[key]}至少需要设置一个阈值`)
          return
        }
      }
    }
    
    // 至少选择一个通知方式
    if (batchMonitorDialog.notifyMethodsArray.length === 0) {
      proxy.$modal.msgWarning('请至少选择一个通知方式')
      return
    }
  }
  
  // 2. 确认操作
  const confirmMessage = `
    <p>将为选中的 <strong>${selectedRows.value.length}</strong> 个Token应用此监控配置</p>
    <p style="color: #E6A23C; margin-top: 10px;">
      ⚠️ 注意：这些Token已有的监控配置将被覆盖
    </p>
  `
  
  try {
    await proxy.$modal.confirm(confirmMessage, '确认批量配置', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定应用',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  
  // 3. 组装数据（清理 volume 字段中的旧格式）
  const cleanedEvents = { ...batchMonitorDialog.events }
  if (cleanedEvents.volume) {
    const { enabled, threshold } = cleanedEvents.volume
    cleanedEvents.volume = { enabled, threshold }
  }
  
  const configData = {
    eventsConfig: JSON.stringify(cleanedEvents),
    triggerLogic: batchMonitorDialog.form.triggerLogic,
    notifyMethods: batchMonitorDialog.notifyMethodsArray.join(','),
    status: batchMonitorDialog.form.status,
    remark: batchMonitorDialog.form.remark
  }
  
  // 4. 批量保存
  let successCount = 0
  let failCount = 0
  
  proxy.$modal.loading('正在应用配置，请稍候...')
  
  for (const row of selectedRows.value) {
    try {
      await saveOrUpdateMonitorConfig({
        ca: row.ca,
        tokenName: row.tokenName,
        ...configData
      })
      successCount++
    } catch (error) {
      console.error(`Token ${row.ca} 配置失败:`, error)
      failCount++
    }
  }
  
  proxy.$modal.closeLoading()
  
  // 5. 显示结果
  if (failCount === 0) {
    proxy.$modal.msgSuccess(`批量配置成功！已应用到 ${successCount} 个Token`)
  } else {
    proxy.$modal.msgWarning(`配置完成：成功 ${successCount} 个，失败 ${failCount} 个`)
  }
  
  // 6. 关闭弹窗并刷新列表
  batchMonitorDialog.visible = false
  getList()
}

// 批量取消监控
const handleBatchCancelMonitor = () => {
  if (selectedRows.value.length === 0) {
    proxy.$modal.msgWarning('请至少选择一个Token')
    return
  }
  
  // 筛选出有监控配置的Token（不管是启用还是停用状态）
  const monitoredTokens = selectedRows.value.filter(row => row.monitorConfigId)
  
  if (monitoredTokens.length === 0) {
    proxy.$modal.msgWarning('选中的Token中没有监控配置')
    return
  }
  
  proxy.$modal.confirm(
    `确认删除选中的 ${monitoredTokens.length} 个Token的监控配置？`,
    '批量取消监控',
    { 
      type: 'warning',
      dangerouslyUseHTMLString: true,
      message: `
        <p>将删除选中的 <strong>${monitoredTokens.length}</strong> 个Token的监控配置</p>
        <p style="color: #909399; margin-top: 8px; font-size: 13px;">
          💡 包括启用和停用状态的配置
        </p>
      `
    }
  ).then(async () => {
    let successCount = 0
    let failCount = 0
    
    proxy.$modal.loading('正在取消监控，请稍候...')
    
    for (const row of monitoredTokens) {
      try {
        // 直接删除监控配置
        await delMonitorConfig(row.monitorConfigId)
        successCount++
      } catch (error) {
        console.error(`Token ${row.ca} 取消失败:`, error)
        failCount++
      }
    }
    
    proxy.$modal.closeLoading()
    
    if (failCount === 0) {
      proxy.$modal.msgSuccess(`批量取消成功！已删除 ${successCount} 个Token的监控配置`)
    } else {
      proxy.$modal.msgWarning(`取消完成：成功 ${successCount} 个，失败 ${failCount} 个`)
    }
    
    getList()
  }).catch(() => {})
}

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '-'
  if (address.length <= 14) return address
  return `${address.substring(0, 8)}...${address.substring(address.length - 6)}`
}

// 复制文本
const copyText = (text, row = null) => {
  if (!navigator.clipboard) {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      proxy.$modal.msgSuccess('已复制')
      // 添加复制成功状态
      if (row) {
        row._copied = true
        setTimeout(() => {
          row._copied = false
        }, 2000)
      }
    } catch (err) {
      proxy.$modal.msgError('复制失败')
    }
    document.body.removeChild(textarea)
    return
  }
  
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess('已复制')
    // 添加复制成功状态
    if (row) {
      row._copied = true
      setTimeout(() => {
        row._copied = false
      }, 2000)
    }
  }).catch(() => {
    proxy.$modal.msgError('复制失败')
  })
}

// 打开链接
const openLink = (url) => {
  window.open(url, '_blank')
}

// 获取Twitter类型标签
const getTwitterTypeTag = (twitterUrl) => {
  if (!twitterUrl) {
    return { label: '-', type: 'info' }
  }
  
  if (twitterUrl.includes('/status/')) {
    return { label: '推文', type: 'warning' }
  } else if (twitterUrl.includes('/communities/')) {
    return { label: '社区', type: 'success' }
  } else {
    return { label: '主页', type: 'primary' }
  }
}

// 切换关注状态
const handleToggleFollow = (row) => {
  row.followLoading = true
  
  const apiCall = row.isFollowing ? unfollowTwitter(row.twitterUrl) : followTwitter(row.twitterUrl)
  
  apiCall.then(response => {
    row.isFollowing = !row.isFollowing
    proxy.$modal.msgSuccess(row.isFollowing ? '关注成功' : '取消关注成功')
  }).catch(error => {
    proxy.$modal.msgError(row.isFollowing ? '取消关注失败' : '关注失败')
  }).finally(() => {
    row.followLoading = false
  })
}

// 打开Twitter推送配置
const handleTwitterPush = (row) => {
  twitterPushDialog.tokenInfo = {
    ca: row.ca,
    symbol: row.tokenSymbol,
    name: row.tokenName,
    twitterUrl: row.twitterUrl
  }
  
  twitterPushDialog.loading = true
  twitterPushDialog.visible = true
  
  // 从后端获取现有配置
  getPushConfig(row.twitterUrl).then(response => {
    const config = response.data || {}
    twitterPushDialog.form = {
      followPush: config.enableFollowPush === 1,
      tweetPush: config.enableTweetPush === 1,
      retweetPush: config.enableRetweetPush === 1,
      replyPush: config.enableReplyPush === 1,
      avatarChangePush: config.enableAvatarPush === 1,
      notifyMethods: config.notifyMethods ? config.notifyMethods.split(',') : [],
      enabled: config.pushStatus === '1'
    }
  }).catch(error => {
    console.error('获取推送配置失败:', error)
    // 使用默认配置
    twitterPushDialog.form = {
      followPush: false,
      tweetPush: false,
      retweetPush: false,
      replyPush: false,
      avatarChangePush: false,
      notifyMethods: [],
      enabled: true
    }
  }).finally(() => {
    twitterPushDialog.loading = false
  })
}

// 关闭Twitter推送配置对话框
const handleTwitterPushClose = () => {
  twitterPushDialog.form = {
    followPush: false,
    tweetPush: false,
    retweetPush: false,
    replyPush: false,
    avatarChangePush: false,
    notifyMethods: [],
    enabled: true
  }
}

// 保存Twitter推送配置
const handleTwitterPushSave = () => {
  if (twitterPushDialog.form.notifyMethods.length === 0) {
    proxy.$modal.msgWarning('请选择至少一种通知方式')
    return
  }
  
  twitterPushDialog.loading = true
  
  const data = {
    twitterUrl: twitterPushDialog.tokenInfo.twitterUrl,
    enableFollowPush: twitterPushDialog.form.followPush ? 1 : 0,
    enableTweetPush: twitterPushDialog.form.tweetPush ? 1 : 0,
    enableRetweetPush: twitterPushDialog.form.retweetPush ? 1 : 0,
    enableReplyPush: twitterPushDialog.form.replyPush ? 1 : 0,
    enableAvatarPush: twitterPushDialog.form.avatarChangePush ? 1 : 0,
    notifyMethods: twitterPushDialog.form.notifyMethods.join(','),
    pushStatus: twitterPushDialog.form.enabled ? '1' : '0'
  }
  
  updatePushConfig(data).then(response => {
    proxy.$modal.msgSuccess('推送配置保存成功')
    twitterPushDialog.visible = false
  }).catch(error => {
    proxy.$modal.msgError('推送配置保存失败')
  }).finally(() => {
    twitterPushDialog.loading = false
  })
}

// 打开监控配置
const handleMonitorConfig = async (row) => {
  monitorDialog.tokenInfo = {
    ca: row.ca,
    symbol: row.tokenSymbol,
    name: row.tokenName
  }
  
  // 优先使用已经查询到的监控配置（列表查询时已JOIN）
  if (row.monitorConfigId && row.monitorEventsConfig) {
    try {
      const eventsConfig = JSON.parse(row.monitorEventsConfig)
      
      // 清理 volume 字段中的旧格式数据
      if (eventsConfig.volume) {
        const { enabled, threshold } = eventsConfig.volume
        eventsConfig.volume = { enabled, threshold }
      }
      
      monitorDialog.events = eventsConfig
      monitorDialog.form = {
        id: row.monitorConfigId,
        coinAddress: row.ca,
        tokenName: row.tokenName,
        triggerLogic: row.monitorTriggerLogic || 'any',
        status: row.monitorStatus || '1',
        remark: row.monitorRemark || ''
      }
      monitorDialog.notifyMethodsArray = row.monitorNotifyMethods 
        ? row.monitorNotifyMethods.split(',') 
        : []
    } catch (e) {
      console.error('解析监控配置失败:', e)
      resetMonitorForm(row)
    }
  } else {
    // 如果列表中没有配置，再尝试从后端加载
    try {
      const response = await getMonitorConfigByCa(row.ca)
      if (response.code === 200 && response.data && response.data.length > 0) {
        const config = response.data[0]
        try {
          const eventsConfig = JSON.parse(config.eventsConfig)
          
          // 清理 volume 字段中的旧格式数据
          if (eventsConfig.volume) {
            const { enabled, threshold } = eventsConfig.volume
            eventsConfig.volume = { enabled, threshold }
          }
          
          monitorDialog.events = eventsConfig
          monitorDialog.form = {
            id: config.id,
            coinAddress: row.ca,
            tokenName: row.tokenName,
            triggerLogic: config.triggerLogic || 'any',
            status: config.status || '1',
            remark: config.remark || ''
          }
          monitorDialog.notifyMethodsArray = config.notifyMethods 
            ? config.notifyMethods.split(',') 
            : []
        } catch (e) {
          console.error('解析监控配置失败:', e)
          resetMonitorForm(row)
        }
      } else {
        // 无配置，使用默认值
        resetMonitorForm(row)
      }
    } catch (error) {
      console.error('加载监控配置失败:', error)
      resetMonitorForm(row)
    }
  }
  
  monitorDialog.visible = true
}

// 重置监控表单为默认值
const resetMonitorForm = (row) => {
  monitorDialog.form = {
    id: null,
    coinAddress: row.ca,
    tokenName: row.tokenName,
    triggerLogic: 'any',
    status: '1',
    remark: ''
  }
  monitorDialog.events = {
    priceChange: {
      enabled: false,
      risePercent: null,
      fallPercent: null
    },
    holders: {
      enabled: false,
      increasePercent: null,
      decreasePercent: null
    },
    volume: {
      enabled: false,
      threshold: null
    }
  }
  monitorDialog.notifyMethodsArray = []
}

// 关闭监控配置对话框
const handleMonitorDialogClose = () => {
  // 重置表单
  monitorDialog.form = {
    id: null,
    coinAddress: '',
    tokenName: '',
    triggerLogic: 'any',
    status: '1',
    remark: ''
  }
  monitorDialog.events = {
    priceChange: {
      enabled: true,
      risePercent: null,
      fallPercent: null
    },
    holders: {
      enabled: false,
      increasePercent: null,
      decreasePercent: null
    },
    volume: {
      enabled: false,
      threshold: null
    }
  }
  monitorDialog.notifyMethodsArray = ['telegram', 'wechat']
}

// 保存监控配置
const handleMonitorSave = () => {
  // 如果监控状态为启用（status = '1'），才需要验证事件和通知方式
  if (monitorDialog.form.status === '1') {
    // 1. 验证至少选择一个监控事件
    const hasEvent = Object.values(monitorDialog.events).some(e => e.enabled)
    if (!hasEvent) {
      proxy.$modal.msgWarning('请至少选择一个监控事件')
      return
    }
    
    // 2. 验证已选择的事件至少填写一个方向
    for (const [key, event] of Object.entries(monitorDialog.events)) {
      if (event.enabled) {
        const hasValue = Object.entries(event)
          .filter(([k, v]) => k !== 'enabled')
          .some(([k, v]) => v != null && v !== '')
        
        if (!hasValue) {
          const eventNames = {
            priceChange: '涨跌幅',
            holders: '持币人数',
            volume: '交易量'
          }
          proxy.$modal.msgWarning(`${eventNames[key]}已勾选，但未填写任何阈值`)
          return
        }
      }
    }
    
    // 3. 验证通知方式
    if (monitorDialog.notifyMethodsArray.length === 0) {
      proxy.$modal.msgWarning('请选择至少一种通知方式')
      return
    }
  }
  
  // 4. 组装数据（清理 volume 字段中的旧格式）
  const cleanedEvents = { ...monitorDialog.events }
  if (cleanedEvents.volume) {
    const { enabled, threshold } = cleanedEvents.volume
    cleanedEvents.volume = { enabled, threshold }
  }
  
  const data = {
    id: monitorDialog.form.id,
    ca: monitorDialog.form.coinAddress,
    tokenName: monitorDialog.form.tokenName,
    triggerLogic: monitorDialog.form.triggerLogic,
    status: monitorDialog.form.status,
    remark: monitorDialog.form.remark,
    notifyMethods: monitorDialog.notifyMethodsArray.join(','),
    eventsConfig: JSON.stringify(cleanedEvents)
  }
  
  // 5. 调用API保存
  monitorDialog.loading = true
  
  saveOrUpdateMonitorConfig(data).then(response => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess('保存成功')
      monitorDialog.visible = false
      getList()
    } else {
      proxy.$modal.msgError(response.msg || '保存失败')
    }
  }).catch(error => {
    console.error('保存监控配置失败:', error)
    proxy.$modal.msgError('保存失败：' + (error.message || '未知错误'))
  }).finally(() => {
    monitorDialog.loading = false
  })
}

// 自动刷新数据（每60秒）
const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    getList()
  }, 60000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

// 初始化
// 计算监控条件摘要
// 📋 单个监控配置：实时条件预览
const monitorConditionsSummary = computed(() => {
  const conditions = []
  const { priceChange, holders, volume } = monitorDialog.events
  const triggerLogicText = monitorDialog.form.triggerLogic === 'any' ? '任一条件' : '所有条件'
  
  // 涨跌幅
  if (priceChange.enabled) {
    const parts = []
    if (priceChange.risePercent) parts.push(`涨幅≥${priceChange.risePercent}%`)
    if (priceChange.fallPercent) parts.push(`跌幅≥${priceChange.fallPercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  // 持币人数
  if (holders.enabled) {
    const parts = []
    if (holders.increasePercent) parts.push(`持币人数增长≥${holders.increasePercent}%`)
    if (holders.decreasePercent) parts.push(`持币人数减少≥${holders.decreasePercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  // 交易量
  if (volume.enabled) {
    if (volume.threshold) conditions.push(`交易量≥$${volume.threshold}`)
  }
  
  if (conditions.length === 0) return ''
  
  return `${triggerLogicText}：${conditions.join(monitorDialog.form.triggerLogic === 'any' ? ' 或 ' : ' 且 ')}`
})

// 📋 批量监控配置：实时条件预览
const batchMonitorConditionsSummary = computed(() => {
  const conditions = []
  const { priceChange, holders, volume } = batchMonitorDialog.events
  const triggerLogicText = batchMonitorDialog.form.triggerLogic === 'any' ? '任一条件' : '所有条件'
  
  // 涨跌幅
  if (priceChange.enabled) {
    const parts = []
    if (priceChange.risePercent) parts.push(`涨幅≥${priceChange.risePercent}%`)
    if (priceChange.fallPercent) parts.push(`跌幅≥${priceChange.fallPercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  // 持币人数
  if (holders.enabled) {
    const parts = []
    if (holders.increasePercent) parts.push(`持币人数增长≥${holders.increasePercent}%`)
    if (holders.decreasePercent) parts.push(`持币人数减少≥${holders.decreasePercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  // 交易量
  if (volume.enabled) {
    if (volume.threshold) conditions.push(`交易量≥$${volume.threshold}`)
  }
  
  if (conditions.length === 0) return ''
  
  return `${triggerLogicText}：${conditions.join(batchMonitorDialog.form.triggerLogic === 'any' ? ' 或 ' : ' 且 ')}`
})

// 监听事件启用状态，自动填充建议阈值
watch(() => monitorDialog.events.priceChange.enabled, (newVal) => {
  if (newVal && !monitorDialog.events.priceChange.risePercent && !monitorDialog.events.priceChange.fallPercent) {
    monitorDialog.events.priceChange.risePercent = 10
    monitorDialog.events.priceChange.fallPercent = 10
  }
})

watch(() => monitorDialog.events.holders.enabled, (newVal) => {
  if (newVal && !monitorDialog.events.holders.increasePercent && !monitorDialog.events.holders.decreasePercent) {
    monitorDialog.events.holders.increasePercent = 30
    monitorDialog.events.holders.decreasePercent = 20
  }
})

watch(() => monitorDialog.events.volume.enabled, (newVal) => {
  if (newVal && !monitorDialog.events.volume.threshold) {
    monitorDialog.events.volume.threshold = 5000
  }
})

// 🎯 批量监控：监听事件启用状态，自动填充默认阈值
watch(() => batchMonitorDialog.events.priceChange.enabled, (newVal) => {
  if (newVal && !batchMonitorDialog.events.priceChange.risePercent && !batchMonitorDialog.events.priceChange.fallPercent) {
    batchMonitorDialog.events.priceChange.risePercent = 10
    batchMonitorDialog.events.priceChange.fallPercent = 10
  }
})

watch(() => batchMonitorDialog.events.holders.enabled, (newVal) => {
  if (newVal && !batchMonitorDialog.events.holders.increasePercent && !batchMonitorDialog.events.holders.decreasePercent) {
    batchMonitorDialog.events.holders.increasePercent = 30
    batchMonitorDialog.events.holders.decreasePercent = 20
  }
})

watch(() => batchMonitorDialog.events.volume.enabled, (newVal) => {
  if (newVal && !batchMonitorDialog.events.volume.threshold) {
    batchMonitorDialog.events.volume.threshold = 5000
  }
})

// 🎯 监听路由变化，自动切换链类型并刷新数据
watch(() => route.query, (newQuery, oldQuery) => {
  // 如果路由参数发生变化（不是初始化）
  if (oldQuery && JSON.stringify(newQuery) !== JSON.stringify(oldQuery)) {
    console.log('路由参数变化:', oldQuery, '→', newQuery)
    
    // 更新链类型和数据源
    const newChain = newQuery.chain || 'sol'
    queryParams.source = newChain === 'sol' ? 'all' : 'fourmeme'
    
    // 更新监控状态
    queryParams.monitorStatus = newQuery.monitorStatus || ''
    
    // 🎯 如果是从"监控中"跳转过来（有monitorStatus参数），清除日期范围
    if (newQuery.monitorStatus) {
      dateRange.value = []
      console.log('从监控中跳转，清除日期范围筛选')
    } else {
      // 否则恢复默认的今日日期范围
      initTodayDateRange()
    }
    
    // 重置分页并刷新
    queryParams.pageNum = 1
    getList()
  }
}, { deep: true })

onMounted(() => {
  // 🎯 根据当前链类型初始化数据源
  queryParams.source = currentChain.value === 'sol' ? 'all' : 'fourmeme'
  console.log('页面初始化 - 当前链:', currentChain.value, '数据源:', queryParams.source)
  
  // 🎯 从路由参数初始化筛选条件
  if (route.query.monitorStatus) {
    queryParams.monitorStatus = route.query.monitorStatus
    console.log('初始化监控状态筛选:', route.query.monitorStatus)
    // 如果有监控状态参数，不设置默认日期范围
    dateRange.value = []
  } else {
    // 否则设置默认的今日日期范围
    initTodayDateRange()
  }
  
  getList()
  startAutoRefresh()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
// 确保容器全宽
.app-container {
  padding: 20px;
  max-width: 100%;
  width: 100%;
}

// 表格样式
.token-table {
  width: 100%;
}

// 工具栏容器
.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  
  .toolbar-right {
    display: flex;
    align-items: center;
  }
}

// Token信息卡片
.token-info-card {
  padding: 10px 12px;
  background: rgba(64, 158, 255, 0.04);
  border-radius: 8px;
  transition: all 0.3s;
  
  &:hover {
    background: rgba(64, 158, 255, 0.08);
  }
  
  .token-content {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
  
  // 第一行：双行标题布局
  .token-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    
    .token-title-left {
      flex: 1;
      min-width: 0;
      
      .token-symbol {
        font-weight: 600;
        font-size: 16px;
        color: #303133;
        line-height: 1.4;
      }
    }
    
    .token-title-right {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;
      
      .source-tag {
        font-size: 11px;
        height: 20px;
        line-height: 20px;
        padding: 0 6px;
      }
      
      // 状态点
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #e4e7ed;
        display: inline-block;
        flex-shrink: 0;
        
        &.warm {
          background: #ffa940;
          box-shadow: 0 0 4px rgba(255, 169, 64, 0.5);
        }
        
        &.hot {
          background: #f5222d;
          box-shadow: 0 0 4px rgba(245, 34, 45, 0.5);
        }
        
        &.rare {
          background: #722ed1;
          box-shadow: 0 0 6px rgba(114, 46, 209, 0.6);
        }
        
        &.epic {
          background: #eb2f96;
          box-shadow: 0 0 8px rgba(235, 47, 150, 0.7);
        }
        
        &.legendary {
          background: linear-gradient(135deg, #ffd700, #ffed4e);
          box-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
          animation: pulse-gold 2s ease-in-out infinite;
        }
      }
      
      @keyframes pulse-gold {
        0%, 100% {
          transform: scale(1);
          opacity: 1;
        }
        50% {
          transform: scale(1.15);
          opacity: 0.9;
        }
      }
    }
  }
  
  // 第二行：副标题
  .token-subtitle {
    font-size: 13px;
    color: #909399;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  // 第三行：合约地址
  .token-address {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    
    .token-ca {
      flex: 1;
      font-size: 12px;
      font-family: 'JetBrains Mono', Monaco, Menlo, Consolas, 'Courier New', monospace;
      font-weight: 500;
      color: #5B8FF9;
      background: rgba(91, 143, 249, 0.08);
      border-radius: 4px;
      padding: 2px 6px;
      cursor: pointer;
      transition: all 0.3s;
      line-height: 1.6;
      
      &:hover {
        background: rgba(91, 143, 249, 0.15);
        color: #3D7EE8;
      }
      
      &:active {
        background: rgba(91, 143, 249, 0.2);
      }
      
      // 复制成功状态
      &.copied {
        background: rgba(82, 196, 26, 0.15);
        color: #52c41a;
        box-shadow: 0 0 8px rgba(82, 196, 26, 0.3);
        animation: copySuccess 0.3s ease;
      }
    }
    
    .copy-icon {
      font-size: 14px;
      color: #909399;
      cursor: pointer;
      flex-shrink: 0;
      margin-top: 2px;
      transition: all 0.3s;
      
      &:hover {
        color: #3D7EE8;
      }
      
      // 复制成功状态
      &.copied {
        color: #52c41a;
        transform: scale(1.2);
      }
    }
  }
}

// 复制成功动画
@keyframes copySuccess {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

// 时间单元格
.time-cell {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  
  .time-sub {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
  }
}

// 市值
.market-cap {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

// Twitter 操作区域
.twitter-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  .twitter-tag-row {
    text-align: center;
  }
  
  .twitter-button-row {
    display: flex;
    gap: 6px;
    justify-content: center;
  }
  
  .action-btn {
    border-color: #d9d9d9;
    color: #606266;
    
    &:hover {
      border-color: #409eff;
      color: #409eff;
      background: #ecf5ff;
    }
    
    &.is-following {
      border-color: #ffa940;
      color: #ffa940;
      
      &:hover {
        border-color: #ff7a00;
        color: #ff7a00;
        background: #fff7e6;
      }
    }
  }
}

// 监控状态单元格
.monitor-status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .status-tag {
    display: inline-flex;
    min-width: 72px;
    justify-content: center;
  }
  
  .config-btn {
    border-color: #d9d9d9;
    color: #606266;
    
    &:hover {
      border-color: #409eff;
      color: #409eff;
      background: #ecf5ff;
    }
  }
}

// 灰色文本
.text-gray-400 {
  color: #909399;
  font-size: 13px;
}

// 对话框Token信息
.dialog-token-info {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .token-symbol {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
  }
  
  .token-name {
    font-size: 14px;
    color: #606266;
  }
}

// 表单提示
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

// 监控配置对话框样式
.event-card {
  margin-bottom: 16px;
  
  &.disabled {
    opacity: 0.6;
    // 移除 pointer-events: none; 以保留 tooltip 和滚动功能
  }
  
  :deep(.el-card__header) {
    padding: 12px 16px;
    background: #fafafa;
  }
  
  .event-title {
    font-weight: 500;
    font-size: 14px;
  }
  
  .event-config {
    padding: 8px 0;
  }
  
  // 事件输入字段统一样式
  .event-field {
    margin-bottom: 8px;
    
    :deep(.el-form-item__label) {
      font-size: 13px;
      color: #606266;
      padding-bottom: 4px;
    }
    
    :deep(.el-input-number) {
      width: 100%;
      max-width: 160px;
    }
    
    // 空值高亮提醒
    &.empty-highlight :deep(.el-input-number .el-input__wrapper) {
      border-color: #ffa940;
      box-shadow: 0 0 0 1px #ffa940 inset;
    }
  }
  
  .event-tip {
    font-size: 11px;
    color: #909399;
    padding-left: 4px;
    margin-top: 4px;
    line-height: 1.4;
  }
  
  .input-suffix {
    margin-left: 6px;
    color: #606266;
    font-size: 13px;
  }
}

.required-mark {
  color: #f56c6c;
  margin-right: 4px;
  font-size: 14px;
}

.remark-tip {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.monitor-preview {
  margin-top: 16px;
  
  .preview-title {
    font-weight: 500;
    font-size: 13px;
    color: #606266;
    margin-bottom: 6px;
  }
  
  .preview-content {
    font-size: 14px;
    color: #303133;
    line-height: 1.6;
  }
}

// 现代化Token信息样式
.token-info-modern {
  padding: 4px 0;
  
  .token-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    
    .token-symbol {
      font-weight: 700;
      font-size: 15px;
      color: #303133;
      letter-spacing: 0.3px;
    }
    
    .el-tag {
      font-weight: 500;
      font-size: 11px;
      height: 20px;
      line-height: 20px;
      padding: 0 8px;
      border-radius: 4px;
    }
  }
  
  .token-name {
    font-size: 13px;
    color: #606266;
    margin-bottom: 6px;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .token-address {
    .address-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 8px;
      background: #f5f7fa;
      border-radius: 4px;
      transition: all 0.3s;
      
      &:hover {
        background: #e4e7ed;
        
        .copy-icon {
          opacity: 1;
        }
      }
      
      .address-text {
        font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
        font-size: 11px;
        color: #409EFF;
        letter-spacing: 0.3px;
        word-break: break-all;
      }
      
      .copy-icon {
        font-size: 13px;
        opacity: 0.6;
        transition: opacity 0.3s;
        flex-shrink: 0;
      }
    }
  }
}

// 表格行高优化
:deep(.el-table__row) {
  td {
    padding: 12px 0;
  }
}

// 按钮组紧凑布局
:deep(.el-button-group) {
  .el-button {
    padding: 5px 10px;
  }
}
</style>
