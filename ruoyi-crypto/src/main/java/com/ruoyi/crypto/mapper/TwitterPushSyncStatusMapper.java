package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.TwitterPushSyncStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Twitter推送同步状态Mapper接口
 *
 * @author ruoyi
 */
public interface TwitterPushSyncStatusMapper {

    /**
     * 查询Twitter推送同步状态列表
     *
     * @param syncStatus Twitter推送同步状态
     * @return Twitter推送同步状态集合
     */
    List<TwitterPushSyncStatus> selectSyncStatusList(TwitterPushSyncStatus syncStatus);

    /**
     * 根据账号ID和推送类型查询同步状态
     *
     * @param accountId 账号ID
     * @param pushType 推送类型
     * @return Twitter推送同步状态
     */
    TwitterPushSyncStatus selectByAccountAndType(@Param("accountId") Long accountId, @Param("pushType") String pushType);

    /**
     * 根据账号ID查询所有推送类型的同步状态
     *
     * @param accountId 账号ID
     * @return Twitter推送同步状态集合
     */
    List<TwitterPushSyncStatus> selectByAccountId(@Param("accountId") Long accountId);

    /**
     * 新增Twitter推送同步状态
     *
     * @param syncStatus Twitter推送同步状态
     * @return 结果
     */
    int insertSyncStatus(TwitterPushSyncStatus syncStatus);

    /**
     * 修改Twitter推送同步状态
     *
     * @param syncStatus Twitter推送同步状态
     * @return 结果
     */
    int updateSyncStatus(TwitterPushSyncStatus syncStatus);

    /**
     * 更新或插入同步状态（UPSERT操作）
     *
     * @param syncStatus Twitter推送同步状态
     * @return 结果
     */
    int upsertSyncStatus(TwitterPushSyncStatus syncStatus);

    /**
     * 批量更新同步状态
     *
     * @param syncStatusList Twitter推送同步状态列表
     * @return 结果
     */
    int batchUpdateSyncStatus(@Param("list") List<TwitterPushSyncStatus> syncStatusList);

    /**
     * 删除Twitter推送同步状态
     *
     * @param id 主键
     * @return 结果
     */
    int deleteSyncStatusById(Long id);

    /**
     * 根据账号ID删除所有推送同步状态
     *
     * @param accountId 账号ID
     * @return 结果
     */
    int deleteSyncStatusByAccountId(@Param("accountId") Long accountId);

    /**
     * 批量删除Twitter推送同步状态
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSyncStatusByIds(Long[] ids);
}
