package com.polymerization.merchant.api;

import com.polymerization.merchant.api.dto.AppDTO;
import com.polymerization.common.domain.BusinessException;

import java.util.List;

public interface AppService {
    /**
     *  商户下创建应用
     * @param merchantId
     * @param appDTO
     * @return
     */
    AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException;


    /**
     * 根据业务id查询应用
     * @return
     * @throws BusinessException
     */
    AppDTO getAppById(String id ) throws BusinessException;

    /**
     * 查询商户的应用列表
     * @param merchantId
     * @return
     */
    List<AppDTO> queryAppByMerchant(Long merchantId);

    /**
     * 查询应用是否属于某个商户
     * @param appId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    Boolean queryAppInMerchant(String appId,Long merchantId) throws BusinessException;
}
