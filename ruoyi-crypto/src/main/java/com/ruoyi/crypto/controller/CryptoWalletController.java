package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.crypto.domain.CryptoWallet;
import com.ruoyi.crypto.domain.vo.CryptoWalletData;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import com.ruoyi.crypto.service.CryptoCoinService;
import com.ruoyi.crypto.service.CryptoWalletService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/crypto/wallet")
public class CryptoWalletController extends BaseController {

    @Resource
    private CryptoWalletService cryptoWalletService;

    /**
     * 查询钱包列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:wallet:list')")
    @GetMapping("/list")
    public TableDataInfo list(CryptoWallet cryptoWallet)
    {
        startPage();
        List<CryptoWallet> list = cryptoWalletService.selectCryptoWalletList(cryptoWallet);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('crypto:wallet:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CryptoWallet cryptoWallet){
        if (!cryptoWalletService.checkWalletUnique(cryptoWallet))
        {
            return error("新增地址'" + cryptoWallet.getWalletName() + "'失败，该地址已存在");
        }
        cryptoWallet.setCreateBy(getUsername());
        return toAjax(cryptoWalletService.insertWallet(cryptoWallet));
    }

    /**
     * 获取钱包详细信息
     */
    @ApiOperation("获取钱包详细信息")
    @PreAuthorize("@ss.hasPermi('crypto:wallet:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@ApiParam(value = "钱包ID", required = true) @PathVariable("id") Long  id)
    {
        return success(cryptoWalletService.selectCryptoWalletById(id));
    }


    @PreAuthorize("@ss.hasPermi('crypto:wallet:edit')")
    @Log(title = "钱包管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody CryptoWallet cryptoWallet)
    {
        if (!cryptoWalletService.checkWalletUnique(cryptoWallet))
        {
            return error("修改地址'" + cryptoWallet.getWalletAddress() + "'失败，该地址已存在");
        }
        cryptoWallet.setUpdateBy(getUsername());
        return toAjax(cryptoWalletService.updateWallet(cryptoWallet));
    }
    @PreAuthorize("@ss.hasPermi('crypto:wallet:edit')")
    @Log(title = "钱包管理", businessType = BusinessType.UPDATE)
    @PutMapping("/batchUpdateStatus")
    public AjaxResult batchUpdateWalletStatus(@RequestBody Map<String, Object> params) {
        Long[] ids = ((List<Integer>) params.get("ids")).stream()
                .map(Long::valueOf)
                .toArray(Long[]::new);
        Integer monitorState = (Integer) params.get("monitorState");

        int result = cryptoWalletService.batchUpdateWalletStatus(ids, monitorState);
        return result > 0 ? success() : error("批量更新失败");
    }

    /**
     * 钱包导入模板
     */
    @ApiOperation("钱包导入模板")
    @PreAuthorize("@ss.hasPermi('crypto:wallet:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<CryptoWallet> util = new ExcelUtil<>(CryptoWallet.class);
        util.importTemplateExcel(response, "钱包信息");
    }


    @Log(title = "钱包管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('crypto:wallet:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<CryptoWallet> util = new ExcelUtil<>(CryptoWallet.class);
        List<CryptoWallet> cryptoWalletList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = cryptoWalletService.importWallet(cryptoWalletList, operName);
        return success(message);
    }

    /**
     * 删除钱包
     */
    @PreAuthorize("@ss.hasPermi('crypto:wallet:remove')")
    @Log(title = "钱包管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cryptoWalletService.deleteCryptoWalletByIds(ids));
    }



}
