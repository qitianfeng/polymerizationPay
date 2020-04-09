package com.polymerization.merchant.service;

import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.entity.Merchant;
import com.polymerization.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;

    /***
     * 根据ID查询详细信息
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = new MerchantDTO();
        BeanUtils.copyProperties(merchant, merchantDTO);

        return merchantDTO;
    }
}
