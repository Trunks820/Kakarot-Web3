package com.ruoyi.crypto.domain.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "主页信息视图对象", description = "主页信息聚合视图")
public class CryptoIndexVo {

    private Long totalQueries;

    private Long activeUsers;
}
