package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.crypto.domain.CryptoCoin;
import com.ruoyi.crypto.service.CryptoCoinService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/crypto/coin")
public class CryptoCoinController extends BaseController {

    @Resource
    private CryptoCoinService cryptoCoinService;

    /**
     * 查询代币列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:coin:list')")
    @GetMapping("/list")
    public TableDataInfo list(CryptoCoin cryptoCoin)
    {
        startPage();
        List<CryptoCoin> list = cryptoCoinService.selectCryptoCoinList(cryptoCoin);
        return getDataTable(list);
    }

    /**
     * 导出代币列表
     */
    @ApiOperation("导出代币列表")
    @PreAuthorize("@ss.hasPermi('crypto:coin:export')")
    @Log(title = "代币", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(CryptoCoin cryptoCoin)
    {
        List<CryptoCoin> list = cryptoCoinService.selectCryptoCoinList(cryptoCoin);
        ExcelUtil<CryptoCoin> util = new ExcelUtil<>(CryptoCoin.class);
        return util.exportExcel(list, "代币数据");
    }

    /**
     * 获取代币详细信息
     */
    @ApiOperation("获取代币详细信息")
    @PreAuthorize("@ss.hasPermi('crypto:coin:query')")
    @GetMapping(value = "/{coinId}")
    public AjaxResult getInfo(@ApiParam(value = "代币ID", required = true) @PathVariable("coinId") Long coinId)
    {
        CryptoCoin query = new CryptoCoin();
        query.setCoinId(coinId);
        return success(cryptoCoinService.selectCryptoCoin(query));
    }

    /**
     * 根据代币地址查询代币信息
     */
    @ApiOperation("根据代币地址查询代币信息")
    @PreAuthorize("@ss.hasPermi('crypto:coin:query')")
    @GetMapping(value = "/address/{address}")
    public AjaxResult getInfoByAddress(@ApiParam(value = "代币地址", required = true) @PathVariable("address") String address)
    {
        CryptoCoin query = new CryptoCoin();
        query.setAddress(address);
        return success(cryptoCoinService.selectCryptoCoin(query));
    }

    /**
     * 新增代币
     */
    @ApiOperation("新增代币")
    @PreAuthorize("@ss.hasPermi('crypto:coin:add')")
    @Log(title = "代币", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CryptoCoin cryptoCoin)
    {
        return toAjax(cryptoCoinService.insertCryptoCoin(cryptoCoin));
    }

    /**
     * 修改代币
     */
    @ApiOperation("修改代币")
    @PreAuthorize("@ss.hasPermi('crypto:coin:edit')")
    @Log(title = "代币", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CryptoCoin cryptoCoin)
    {
        return toAjax(cryptoCoinService.updateCryptoCoin(cryptoCoin));
    }

    /**
     * 删除代币
     */
    @ApiOperation("删除代币")
    @PreAuthorize("@ss.hasPermi('crypto:coin:remove')")
    @Log(title = "代币", businessType = BusinessType.DELETE)
    @DeleteMapping("/{coinIds}")
    public AjaxResult remove(@ApiParam(value = "代币ID串", required = true) @PathVariable Long[] coinIds)
    {
        return toAjax(cryptoCoinService.deleteCryptoCoinByIds(coinIds));
    }



}
