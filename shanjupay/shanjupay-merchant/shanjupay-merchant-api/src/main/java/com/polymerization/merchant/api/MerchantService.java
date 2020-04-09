package com.polymerization.merchant.api;

import com.polymerization.merchant.api.dto.MerchantDTO;

public interface MerchantService {

    /***
     * 根据ID查询详细信息
     * @param merchantId
     * @return
     */
    MerchantDTO queryMerchantById(Long merchantId);

    MerchantDTO createMerchant(MerchantDTO merchantDTO);
}
