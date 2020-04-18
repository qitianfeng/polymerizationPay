package com.polymerization.transaction.api.dto.com.shanjupay.transaction.api;

import com.polymerization.common.domain.BusinessException;
import com.polymerization.transaction.api.dto.PayChannelDTO;
import com.polymerization.transaction.api.dto.PayChannelParamDTO;
import com.polymerization.transaction.api.dto.PlatformChannelDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 为应用配置支付渠道参数接口
 */
public interface PayChannelService {
    /**
     * 获取平台服务类型
     * @return
     */
    List<PlatformChannelDTO> queryPlatformChannel();

    /**
     * 为APP绑定平台服务类型
     * @param appId
     * @param platformChannelCodes
     * @throws BusinessException
     */
    void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException;

    /**
     * 应用是否绑定某个服务类型
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    Integer queryAppBindPlatformChannel(String appId,String platformChannel) throws BusinessException;

    /**
     * 根据平台服务类型获取支付渠道列表
     * @param platformChannelCode
     * @return
     * @throws BusinessException
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(@Param("platformChannelCode") String platformChannelCode) throws BusinessException;

    /**
     * 保存支付渠道参数
     * @param payChannelParamDTO
     * @throws BusinessException
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException;


    /**
     * 获取指定应用服务类型下所包含的原始支付参数
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId,String platformChannel,String payChannel) throws BusinessException;

    List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel);
}
