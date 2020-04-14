package com.polymerization.merchant.api;

import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.api.dto.StaffDTO;
import com.polymerization.merchant.api.dto.StoreDTO;
import com.shanjupay.common.domain.BusinessException;

public interface MerchantService {

    /***
     * 根据ID查询详细信息
     * @param merchantId
     * @return
     */
    MerchantDTO queryMerchantById(Long merchantId);

    MerchantDTO createMerchant(MerchantDTO merchantDTO);

    void applyMerchant(Long merchantId, MerchantDTO merchantDTO);

    /**
     * 商户下新增门店
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;

    /**
     * 商户新增员工
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;

    /**
     * 为门店设置管理员
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId,Long staffId) throws BusinessException;

    MerchantDTO queryMerchantByTenantId(Long tenantId);
}
