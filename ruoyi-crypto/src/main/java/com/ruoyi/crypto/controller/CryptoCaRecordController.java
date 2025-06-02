package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.crypto.domain.vo.CryptoCaRecordVO;
import com.ruoyi.crypto.service.CryptoCaRecordService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/crypto/caRecord")
public class CryptoCaRecordController extends BaseController {

    @Resource
    private CryptoCaRecordService cryptoCaRecordService;

    /**
     * 查询代币列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:caRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(CryptoCaRecordVO cryptoCoin)
    {
        startPage();
        List<CryptoCaRecordVO> list = cryptoCaRecordService.selectCryptoCoinList(cryptoCoin);
        return getDataTable(list);
    }

    /**
     * 导出代币列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:caRecord:export')")
    @Log(title = "代币", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CryptoCaRecordVO cryptoCoin, HttpServletResponse response)
    {
        List<CryptoCaRecordVO> list = cryptoCaRecordService.selectCryptoCoinList(cryptoCoin);
        ExcelUtil<CryptoCaRecordVO> util = new ExcelUtil<>(CryptoCaRecordVO.class);
        util.exportExcel(response, list, "代币数据");
    }

    /**
     * 获取代币详细信息
     */
    @ApiOperation("获取代币详细信息")
    @PreAuthorize("@ss.hasPermi('crypto:caRecord:query')")
    @GetMapping(value = "/{coinId}")
    public AjaxResult getInfo(@ApiParam(value = "代币ID", required = true) @PathVariable("coinId") Long coinId)
    {
        CryptoCaRecordVO query = new CryptoCaRecordVO();
        query.setCoinId(coinId);
        return success(cryptoCaRecordService.selectCryptoCoin(query));
    }

    /**
     * 删除代币
     */
    @ApiOperation("删除代币")
    @PreAuthorize("@ss.hasPermi('crypto:caRecord:remove')")
    @Log(title = "代币", businessType = BusinessType.DELETE)
    @DeleteMapping("/{coinIds}")
    public AjaxResult remove(@ApiParam(value = "代币ID串", required = true) @PathVariable Long[] coinIds)
    {
        return toAjax(cryptoCaRecordService.deleteCryptoCoinByIds(coinIds));
    }


}
