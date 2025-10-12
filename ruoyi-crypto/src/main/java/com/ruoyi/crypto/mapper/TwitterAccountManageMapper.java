package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.TwitterAccountManage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Twitter账号管理Mapper接口
 *
 * @author ruoyi
 */
public interface TwitterAccountManageMapper {

    /**
     * 查询Twitter账号管理列表
     *
     * @param twitterAccount Twitter账号管理
     * @return Twitter账号管理集合
     */
    List<TwitterAccountManage> selectTwitterAccountList(TwitterAccountManage twitterAccount);

    /**
     * 根据Twitter链接查询
     *
     * @param twitterUrl Twitter链接
     * @return Twitter账号管理
     */
    TwitterAccountManage selectByTwitterUrl(@Param("twitterUrl") String twitterUrl);

    /**
     * 批量查询Twitter账号
     *
     * @param twitterUrls Twitter链接列表
     * @return Twitter账号管理列表
     */
    List<TwitterAccountManage> selectByTwitterUrls(@Param("twitterUrls") List<String> twitterUrls);

    /**
     * 新增Twitter账号管理
     *
     * @param twitterAccount Twitter账号管理
     * @return 结果
     */
    int insertTwitterAccount(TwitterAccountManage twitterAccount);

    /**
     * 修改Twitter账号管理
     *
     * @param twitterAccount Twitter账号管理
     * @return 结果
     */
    int updateTwitterAccount(TwitterAccountManage twitterAccount);

    /**
     * 更新关注状态（关注）
     *
     * @param twitterUrl Twitter链接
     * @return 结果
     */
    int updateFollowStatus(@Param("twitterUrl") String twitterUrl);

    /**
     * 更新关注状态（取消关注）
     *
     * @param twitterUrl Twitter链接
     * @return 结果
     */
    int updateUnfollowStatus(@Param("twitterUrl") String twitterUrl);

    /**
     * 更新推送配置
     *
     * @param twitterAccount Twitter账号管理
     * @return 结果
     */

    int updatePushConfig(TwitterAccountManage twitterAccount);

    /**
     * 获取Twitter账号的Map（key: twitterUrl, value: TwitterAccountManage）
     *
     * @param twitterUrls Twitter链接列表
     * @return Map
     */
    Map<String, TwitterAccountManage> selectTwitterAccountMap(@Param("twitterUrls") List<String> twitterUrls);
}

