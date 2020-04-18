package com.polymerization.merchant.api;

import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.api.dto.MerchantQueryDTO;
import com.polymerization.merchant.api.dto.StaffDTO;
import com.polymerization.merchant.api.dto.StoreDTO;
import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.PageVO;

import java.util.List;

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
     *
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;

    StoreDTO createStore(StoreDTO storeDTO, List<Long> staffIds) throws BusinessException;

    /**
     * 商户新增员工
     *
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;

    /**
     * 为门店设置管理员
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;

    MerchantDTO queryMerchantByTenantId(Long tenantId);

    /**
     * 分页查询商户下的门店
     *
     * @param storeDTO
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BusinessException
     */
    PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) throws BusinessException;

    /**
     * 查询门店是否属于某商户
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    Boolean queryStoreInMerchant(Long merchantId, Long storeId);

    /***
     * 商户分页查询条件
     * @param merchantQueryDTO
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageVO<MerchantDTO> queryMerchantPage(MerchantQueryDTO merchantQueryDTO, Integer pageNo, Integer pageSize);

    /**
     * 商户资质审核
     *
     * @param merchantId
     * @param auditStatus
     */
    void verifyMerchant(Long merchantId, String auditStatus) throws BusinessException;

    /**
     * 分页查询商户下的员工信息
     * @param staffDTO
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BusinessException
     */

    PageVO<StaffDTO> queryStaffByPage(StaffDTO staffDTO, Integer pageNo, Integer pageSize) throws BusinessException;


    /**
     * 查询某个门店
     * @param id
     * @return
     */
    StoreDTO queryStoreById(Long id) throws BusinessException;


    /**
     * 商户修改门店
     * @param storeDTO
     * @param longList
     * @throws BusinessException
     */
    void modifyStore(StoreDTO storeDTO,List<Long> longList) throws BusinessException;
    /**
     * 删除某门店
     * @param id
     */
    void removeStore(Long id) throws BusinessException;
    /**
     * 商户新增员工和账号
     * @param staffDTO
     * @throws BusinessException
     */
    void createStaffAndAccount(StaffDTO staffDTO, String [] roleCodes) throws BusinessException;


    abstract StaffDTO queryStaffDetail(Long id, Long tenantId);

    /**
     * 修改员工信息
     * @param staff
     * @throws BusinessException
     */
    void modifyStaff(StaffDTO staff, String [] roleCodes) throws BusinessException;

    /**
     * 删除员工
     * @throws BusinessException
     */
    void removeStaff(Long id) throws BusinessException;
}
