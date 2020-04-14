package com.polymerization.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.api.dto.StaffDTO;
import com.polymerization.merchant.api.dto.StoreDTO;
import com.polymerization.merchant.entity.Merchant;
import com.polymerization.merchant.entity.Staff;
import com.polymerization.merchant.entity.Store;
import com.polymerization.merchant.entity.StoreStaff;
import com.polymerization.merchant.mapper.MerchantMapper;
import com.polymerization.merchant.mapper.StaffMapper;
import com.polymerization.merchant.mapper.StoreMapper;
import com.polymerization.merchant.mapper.StoreStaffMapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;

    @Autowired
    StoreMapper storeMapper;

    @Autowired
    StaffMapper staffMapper;

    @Autowired
    StoreStaffMapper storeStaffMapper;


    @Reference
    TenantService tenantService;


    /***
     * 根据ID查询详细信息
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) throws BusinessException {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = new MerchantDTO();
        BeanUtils.copyProperties(merchant, merchantDTO);

        return merchantDTO;
    }

    @Override
    @Transactional  //添加事务性
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //手机号非空校验
        if (StringUtils.isBlank(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        //校验手机的合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        //联系人非空校验‘
        if (StringUtils.isBlank(merchantDTO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }

        //校验手机号的唯一性，根据商户的手机号查询商户表
        LambdaQueryWrapper<Merchant> merchantLambdaQueryWrapper =
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile());
        Integer count = merchantMapper.selectCount(merchantLambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        //添加租户和账号 绑定关系
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        //表示该租户类型为商户
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");
        //设置租户套餐为初始化套餐
        createTenantRequestDTO.setBundleCode("shanju-merchant");
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());

        //新增租户并设置为管理员
        createTenantRequestDTO.setName(merchantDTO.getUsername());

        TenantDTO tenantDTO = tenantService.createTenantAndAccount(createTenantRequestDTO);
        if (tenantDTO == null || tenantDTO.getId() == null){
            throw new BusinessException(CommonErrorCode.E_200012);
        }

        //判断租户是否已经注册过商户
        Merchant merchant= merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantDTO.getId()));
        if (merchant != null && merchant.getId() != null ){
            throw new BusinessException(CommonErrorCode.E_200017);
        }

        //设置商户所属租户
        merchantDTO.setTenantId(tenantDTO.getId());
        //设置审核状态0-未申请 1-已申请 2-审核通过 3- 审核拒绝
        merchantDTO.setAuditStatus("0"); //审核状态 0-未申请 1-已申请待审核 2-审核通过 3-审核拒绝
        Merchant entity = new Merchant();

        BeanUtils.copyProperties(merchantDTO, entity);
        //保存商户
        merchantMapper.insert(entity);

        //新增门店，创建根门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(entity.getId());
        storeDTO.setStoreName("启程商城");
        storeDTO = createStore(storeDTO);

        log.info("门店信息 {}", JSON.toJSONString(storeDTO));

        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMerchantId(entity.getId());
        staffDTO.setMobile(merchantDTO.getMobile());
        staffDTO.setUsername(merchantDTO.getUsername());

        //为员工选择归属门店
        staffDTO.setStoreId(storeDTO.getId());
        staffDTO = createStaff(staffDTO);

        //为门店设置管理员
        bindStaffToStore(storeDTO.getId(),staffDTO.getId());

        //将新增商户id返回
        merchantDTO.setId(entity.getId());
        return merchantDTO;
    }

    @Override
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        //接收资质申请信息。更新到商户表
        if (merchantDTO == null || merchantId == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        Merchant entity = new Merchant();
        BeanUtils.copyProperties(merchantDTO, entity);
        //将dto转成entity
        //将必要的参数设置到entity
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());//因为资质申请的时候手机号不让改，还使用数据库中原来的手机号
        entity.setAuditStatus("1");//审核状态1-已申请待审核
        entity.setTenantId(merchant.getTenantId());
        //调用mapper更新商户表
        merchantMapper.updateById(entity);

    }

    /**
     * 商户下新增门店
     *
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = new Store();
        BeanUtils.copyProperties(storeDTO, store);
        storeMapper.insert(store);
        StoreDTO storeDTO1 = new StoreDTO();
        BeanUtils.copyProperties(store, storeDTO1);
        return storeDTO1;
    }

    /**
     * 商户新增员工
     *
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //校验手机号格式即是否存在
        String mobile = staffDTO.getMobile();
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        String username = staffDTO.getUsername();
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        if (isExitStaffByUsername(username,staffDTO.getMerchantId())){
            throw new BusinessException("对象不唯一");
        }

        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDTO,staff);
        staffMapper.insert(staff);
        StaffDTO staffDTO1 = new StaffDTO();
        BeanUtils.copyProperties(staff,staffDTO1);
        return staffDTO1;
    }

    /**
     * 为门店设置管理员
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStoreId(storeId);
        storeStaff.setStaffId(staffId);
        storeStaffMapper.insert(storeStaff);

    }

    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId)  throws BusinessException{

        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        MerchantDTO merchant1 = new MerchantDTO();
        BeanUtils.copyProperties(merchant,merchant1);
        return merchant1;
    }

    private Boolean isExitStaffByUsername(String usernamr, Long merchantId) {
        Integer integer = staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, usernamr)
                .eq(Staff::getMerchantId, merchantId));
        return integer>0;
    }

    /**
     * 数据账号判断员工是否已在指定商户
     *
     * @param mobile
     * @param merchantId
     * @return
     */
    private Boolean isExitStaffByMobile(String mobile, Long merchantId)  throws BusinessException{
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId);
        Integer count = staffMapper.selectCount(lambdaQueryWrapper);
        return count > 0;
    }

}
