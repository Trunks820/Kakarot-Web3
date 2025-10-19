package com.ruoyi.crypto.controller;


import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.crypto.domain.TokenLaunchHistory;
import com.ruoyi.crypto.domain.TwitterAccountManage;
import com.ruoyi.crypto.service.ITwitterService;
import com.ruoyi.crypto.service.TokenLaunchHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Token发射历史Controller
 *
 * @author ruoyi
 */
@Api(tags = "代币监控")
@RestController
@RequestMapping("/crypto/token")
public class TokenLaunchHistoryController extends BaseController {

    @Resource
    private TokenLaunchHistoryService tokenLaunchHistoryService;

    @Resource
    private ITwitterService twitterService;

    /**
     * 查询Token发射历史列表
     */
    @ApiOperation("查询Token列表")
    @PreAuthorize("@ss.hasPermi('crypto:token:list')")
    @GetMapping("/list")
    public TableDataInfo list(TokenLaunchHistory token, @RequestParam Map<String, Object> params) {
        startPage();
        
        // 🎯 特殊处理：source 参数可能是逗号分隔的字符串（SOL链的"全部"）
        Object sourceParam = params.get("source");
        if (sourceParam != null && sourceParam instanceof String) {
            String sourceStr = (String) sourceParam;
            if (sourceStr.contains(",")) {
                // 如果包含逗号，说明是多个数据源，转换为 List
                token.getParams().put("source", Arrays.asList(sourceStr.split(",")));
            } else {
                // 单个数据源，直接设置到 token.source
                token.setSource(sourceStr);
            }
            // 从 params 中移除，避免重复处理
            params.remove("source");
        }
        
        // 自动将所有额外参数放入 token.params，排除 pageNum 和 pageSize
        params.entrySet().stream()
            .filter(entry -> !"pageNum".equals(entry.getKey()) && !"pageSize".equals(entry.getKey()))
            .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isEmpty())
            .forEach(entry -> token.getParams().put(entry.getKey(), entry.getValue()));
        
        List<TokenLaunchHistory> list = tokenLaunchHistoryService.selectTokenList(token);
        return getDataTable(list);
    }

    /**
     * 查询Token详情
     */
    @ApiOperation("查询Token详情")
    @PreAuthorize("@ss.hasPermi('crypto:token:query')")
    @GetMapping("/{ca}")
    public AjaxResult getInfo(
            @ApiParam(value = "合约地址", required = true)
            @PathVariable("ca") String ca) {
        return success(tokenLaunchHistoryService.selectTokenByCa(ca));
    }

    /**
     * 查询统计数据
     */
    @ApiOperation("查询统计数据")
    @PreAuthorize("@ss.hasPermi('crypto:token:list')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        Map<String, Object> stats = tokenLaunchHistoryService.selectTokenStats();
        return success(stats);
    }

    /**
     * 关注Twitter账号
     */
    @ApiOperation("关注Twitter")
    @PreAuthorize("@ss.hasPermi('crypto:token:follow')")
    @PostMapping("/follow")
    public AjaxResult followTwitter(@RequestBody Map<String, String> params) {
        String twitterUrl = params.get("twitterUrl");
        if (twitterUrl == null || twitterUrl.isEmpty()) {
            return error("Twitter链接不能为空");
        }
        
        boolean success = twitterService.followTwitter(twitterUrl);
        return success ? success() : error("关注失败");
    }

    /**
     * 取消关注Twitter账号
     */
    @ApiOperation("取消关注Twitter")
    @PreAuthorize("@ss.hasPermi('crypto:token:follow')")
    @PostMapping("/unfollow")
    public AjaxResult unfollowTwitter(@RequestBody Map<String, String> params) {
        String twitterUrl = params.get("twitterUrl");
        if (twitterUrl == null || twitterUrl.isEmpty()) {
            return error("Twitter链接不能为空");
        }
        
        boolean success = twitterService.unfollowTwitter(twitterUrl);
        return success ? success() : error("取消关注失败");
    }

    /**
     * 批量关注Twitter账号
     */
    @ApiOperation("批量关注Twitter")
    @PreAuthorize("@ss.hasPermi('crypto:token:follow')")
    @PostMapping("/batchFollow")
    public AjaxResult batchFollowTwitter(@RequestBody Map<String, List<String>> params) {
        List<String> twitterUrls = params.get("twitterUrls");
        if (twitterUrls == null || twitterUrls.isEmpty()) {
            return error("Twitter链接列表不能为空");
        }
        
        int successCount = twitterService.batchFollow(twitterUrls);
        return success("成功关注 " + successCount + "/" + twitterUrls.size() + " 个账号");
    }

    /**
     * 批量取消关注Twitter账号
     */
    @ApiOperation("批量取消关注Twitter")
    @PreAuthorize("@ss.hasPermi('crypto:token:follow')")
    @PostMapping("/batchUnfollow")
    public AjaxResult batchUnfollowTwitter(@RequestBody Map<String, List<String>> params) {
        List<String> twitterUrls = params.get("twitterUrls");
        if (twitterUrls == null || twitterUrls.isEmpty()) {
            return error("Twitter链接列表不能为空");
        }
        
        int successCount = twitterService.batchUnfollow(twitterUrls);
        return success("成功取消关注 " + successCount + "/" + twitterUrls.size() + " 个账号");
    }

    /**
     * 批量获取Twitter账号信息（用于前端显示关注状态）
     */
    @ApiOperation("批量获取Twitter账号信息")
    @PreAuthorize("@ss.hasPermi('crypto:token:list')")
    @PostMapping("/getTwitterAccounts")
    public AjaxResult getTwitterAccounts(@RequestBody Map<String, List<String>> params) {
        List<String> twitterUrls = params.get("twitterUrls");
        if (twitterUrls == null || twitterUrls.isEmpty()) {
            return success(new HashMap<>());
        }
        
        // 过滤掉空值
        List<String> validUrls = twitterUrls.stream()
                .filter(url -> url != null && !url.isEmpty())
                .collect(Collectors.toList());
        
        if (validUrls.isEmpty()) {
            return success(new HashMap<>());
        }
        
        Map<String, TwitterAccountManage> accountMap = twitterService.batchGetAccounts(validUrls);
        return success(accountMap);
    }

    /**
     * 更新Twitter推送配置
     */
    @ApiOperation("更新Twitter推送配置")
    @PreAuthorize("@ss.hasPermi('crypto:token:follow')")
    @PostMapping("/updatePushConfig")
    public AjaxResult updatePushConfig(@RequestBody TwitterAccountManage twitterAccount) {
        if (twitterAccount.getTwitterUrl() == null || twitterAccount.getTwitterUrl().isEmpty()) {
            return error("Twitter链接不能为空");
        }
        
        boolean success = twitterService.updatePushConfig(twitterAccount);
        return success ? success("推送配置更新成功") : error("推送配置更新失败");
    }

    /**
     * 获取Twitter推送配置
     */
    @ApiOperation("获取Twitter推送配置")
    @PreAuthorize("@ss.hasPermi('crypto:token:list')")
    @GetMapping("/getPushConfig")
    public AjaxResult getPushConfig(@RequestParam String twitterUrl) {
        if (twitterUrl == null || twitterUrl.isEmpty()) {
            return error("Twitter链接不能为空");
        }
        
        TwitterAccountManage account = twitterService.getOrCreateAccount(twitterUrl);
        return success(account);
    }
}