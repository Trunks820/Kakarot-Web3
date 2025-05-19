package com.ruoyi.cryptomonitor.mapper;

import java.util.List;
import com.ruoyi.cryptomonitor.domain.CryptoBotGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 机器人群组Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoBotGroupMapper
{
    /**
     * 查询机器人群组
     *
     * @param id 机器人群组ID
     * @return 机器人群组
     */
    public CryptoBotGroup selectCryptoBotGroupById(Long id);

    /**
     * 查询机器人群组列表
     *
     * @param cryptoBotGroup 机器人群组
     * @return 机器人群组集合
     */
    public List<CryptoBotGroup> selectCryptoBotGroupList(CryptoBotGroup cryptoBotGroup);

    /**
     * 根据平台和群组ID查询机器人群组
     *
     * @param platform 平台
     * @param groupId 群组ID
     * @return 机器人群组
     */
    public CryptoBotGroup selectCryptoBotGroupByPlatformAndGroupId(@Param("platform") String platform, @Param("groupId") String groupId);

    /**
     * 根据平台查询所有激活的机器人群组
     *
     * @param platform 平台
     * @return 机器人群组集合
     */
    public List<CryptoBotGroup> selectActiveCryptoBotGroupByPlatform(String platform);

    /**
     * 新增机器人群组
     *
     * @param cryptoBotGroup 机器人群组
     * @return 结果
     */
    public int insertCryptoBotGroup(CryptoBotGroup cryptoBotGroup);

    /**
     * 修改机器人群组
     *
     * @param cryptoBotGroup 机器人群组
     * @return 结果
     */
    public int updateCryptoBotGroup(CryptoBotGroup cryptoBotGroup);

    /**
     * 删除机器人群组
     *
     * @param id 机器人群组ID
     * @return 结果
     */
    public int deleteCryptoBotGroupById(Long id);

    /**
     * 批量删除机器人群组
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteCryptoBotGroupByIds(Long[] ids);
}