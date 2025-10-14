package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.crypto.domain.TwitterPushSyncStatus;
import com.ruoyi.crypto.service.ITwitterPushSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Twitter推送同步状态Controller
 *
 * @author ruoyi
 */
@Api(tags = "Twitter推送同步状态")
@RestController
@RequestMapping("/crypto/pushSync")
public class TwitterPushSyncController extends BaseController {

    @Resource
    private ITwitterPushSyncService twitterPushSyncService;

    /**
     * 查询Twitter推送同步状态列表
     */
    @ApiOperation("查询推送同步状态列表")
    @PreAuthorize("@ss.hasPermi('crypto:pushSync:list')")
    @GetMapping("/list")
    public TableDataInfo list(TwitterPushSyncStatus syncStatus) {
        startPage();
        List<TwitterPushSyncStatus> list = twitterPushSyncService.selectSyncStatusList(syncStatus);
        return getDataTable(list);
    }

    /**
     * 根据账号ID查询推送同步状态
     */
    @ApiOperation("根据账号ID查询推送同步状态")
    @PreAuthorize("@ss.hasPermi('crypto:pushSync:query')")
    @GetMapping("/account/{accountId}")
    public AjaxResult getByAccountId(
            @ApiParam(value = "账号ID", required = true)
            @PathVariable("accountId") Long accountId) {
        List<TwitterPushSyncStatus> list = twitterPushSyncService.selectSyncStatusByAccountId(accountId);
        return success(list);
    }

    /**
     * 更新推送同步状态
     */
    @ApiOperation("更新推送同步状态")
    @PreAuthorize("@ss.hasPermi('crypto:pushSync:edit')")
    @PutMapping
    public AjaxResult update(@RequestBody TwitterPushSyncStatus syncStatus) {
        boolean success = twitterPushSyncService.upsertSyncStatus(syncStatus);
        return success ? success("更新成功") : error("更新失败");
    }

    /**
     * 批量更新推送同步状态
     */
    @ApiOperation("批量更新推送同步状态")
    @PreAuthorize("@ss.hasPermi('crypto:pushSync:edit')")
    @PutMapping("/batch")
    public AjaxResult batchUpdate(@RequestBody List<TwitterPushSyncStatus> syncStatusList) {
        int count = twitterPushSyncService.batchUpdateSyncStatus(syncStatusList);
        return success("批量更新成功，影响行数：" + count);
    }
}
