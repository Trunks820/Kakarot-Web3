package com.ruoyi.crypto.service;

import com.ruoyi.common.core.domain.AjaxResult;

@FunctionalInterface
public interface ApiSupplier {

    AjaxResult get();
}
