package com.polymerization.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.*;
import com.polymerization.merchant.entity.Merchant;
import com.polymerization.merchant.entity.Staff;
import com.polymerization.merchant.entity.Store;
import com.polymerization.merchant.entity.StoreStaff;
import com.polymerization.merchant.mapper.MerchantMapper;
import com.polymerization.merchant.mapper.StaffMapper;
import com.polymerization.merchant.mapper.StoreMapper;
import com.polymerization.merchant.mapper.StoreStaffMapper;
import com.polymerization.common.domain.BusinessException;
import com.polymerization.common.domain.CommonErrorCode;
import com.polymerization.common.domain.PageVO;
import com.polymerization.common.util.PhoneUtil;
import com.polymerization.common.util.StringUtil;
import com.polymerization.user.api.AuthorizationService;
import com.polymerization.user.api.TenantService;
import com.polymerization.user.api.dto.authorization.RoleDTO;
import com.polymerization.user.api.dto.tenant.CreateAccountRequestDTO;
import com.polymerization.user.api.dto.tenant.CreateTenantRequestDTO;
import com.polymerization.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        if (tenantDTO == null || tenantDTO.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_200012);
        }

        //判断租户是否已经注册过商户
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantDTO.getId()));
        if (merchant != null && merchant.getId() != null) {
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
        bindStaffToStore(storeDTO.getId(), staffDTO.getId());

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
     * 商户下新增门店
     *
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO, List<Long> staffIds) throws BusinessException {
        //设置根门店
        Long rootStoreId = getRootStore(storeDTO.getMerchantId());
        storeDTO.setParentId(rootStoreId);

        //新增门店
        Store store = new Store();
        BeanUtils.copyProperties(storeDTO, store);
        storeMapper.insert(store);

        if (staffIds != null) {
            //设置管理员
            staffIds.forEach(id -> {
                bindStaffToStore(store.getId(), id);
            });
        }
        StoreDTO storeDTO1 = new StoreDTO();
        BeanUtils.copyProperties(store, storeDTO1);
        return storeDTO1;
    }

    /**
     * 查询根门店
     *
     * @param merchantId
     * @return
     */
    private Long getRootStore(Long merchantId) {
        Store store = storeMapper.selectOne(new QueryWrapper<Store>().lambda().eq(Store::getMerchantId, merchantId).isNull(Store::getParentId));
        if (store == null) {
            throw new BusinessException(CommonErrorCode.E_200014);
        }
        return store.getId();
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
        if (isExitStaffByUsername(username, staffDTO.getMerchantId())) {
            throw new BusinessException("对象不唯一");
        }

        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDTO, staff);
        staffMapper.insert(staff);
        StaffDTO staffDTO1 = new StaffDTO();
        BeanUtils.copyProperties(staff, staffDTO1);
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
    public MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException {

        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId));
        MerchantDTO merchant1 = new MerchantDTO();
        BeanUtils.copyProperties(merchant, merchant1);
        return merchant1;
    }

    /**
     * 分页查询商户下的门店
     *
     * @param storeDTO
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BusinessException
     */
    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) throws BusinessException {
        Page<Store> page = new Page<>(pageNo, pageSize);
        if (storeDTO.getMerchantId() == null || storeDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 构造查询条件
        QueryWrapper<Store> qw = new QueryWrapper();
        if (null != storeDTO && null != storeDTO.getMerchantId()) {
            qw.lambda().eq(Store::getMerchantId, storeDTO.getMerchantId());
        }
        if (null != storeDTO && null != storeDTO.getStoreName()) {
            qw.lambda().like(Store::getStoreName, storeDTO.getStoreName());
        }
        //查询
        IPage<Store> storeIPage = storeMapper.selectPage(page, new LambdaQueryWrapper<Store>().eq(Store::getMerchantId, storeDTO.getMerchantId()));

        //转化list
        List<Store> records = storeIPage.getRecords();
        List<StoreDTO> storeDTOS = new ArrayList<>();
        for (Store record : records) {

            StoreDTO storeDTO1 = new StoreDTO();
            BeanUtils.copyProperties(record, storeDTO1);
            storeDTOS.add(storeDTO1);
        }
        return new PageVO<StoreDTO>(storeDTOS, storeIPage.getTotal(), pageNo, pageSize);
    }

    /**
     * 查询门店是否属于某商户
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    @Override
    public Boolean queryStoreInMerchant(Long merchantId, Long storeId) {

        Integer count = storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getMerchantId, merchantId)
                .eq(Store::getId, storeId));

        return count > 0;
    }

    /***
     * 商户分页查询条件
     * @param merchantQueryDTO
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageVO<MerchantDTO> queryMerchantPage(MerchantQueryDTO merchantQueryDTO, Integer pageNo, Integer pageSize) {

        IPage<Merchant> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);

        LambdaQueryWrapper<Merchant> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        if (merchantQueryDTO != null) {
            LambdaQueryWrapper<Merchant> wrapper = lambdaQueryWrapper.like(StringUtil.isNotBlank(merchantQueryDTO.getMerchantName()), Merchant::getMerchantName, merchantQueryDTO.getMerchantName())
                    .eq(StringUtil.isNotBlank(merchantQueryDTO.getMobile()), Merchant::getMobile, merchantQueryDTO.getMobile())
                    .eq(StringUtils.isNotBlank(merchantQueryDTO.getMerchantType()), Merchant::getMerchantType, merchantQueryDTO.getMerchantType())
                    .eq(merchantQueryDTO.getAuditStatus() != null, Merchant::getAuditStatus, merchantQueryDTO.getAuditStatus());

            IPage<Merchant> result = merchantMapper.selectPage(page, wrapper);
            if (result.getTotal() > 0) {
                List<MerchantDTO> merchantDTOList = new ArrayList<>();
                List<Merchant> records = result.getRecords();
                for (Merchant record : records) {
                    MerchantDTO merchantDTO = new MerchantDTO();
                    BeanUtils.copyProperties(record, merchantDTO);
                    merchantDTOList.add(merchantDTO);
                }
                return new PageVO<MerchantDTO>(merchantDTOList, result.getTotal(), new Long(result.getCurrent()).intValue(), new Long(result.getSize()).intValue());
            }
        }
        return new PageVO<MerchantDTO>(new ArrayList<MerchantDTO>(), 0, pageNo, pageSize);
    }

    /**
     * 商户资质审核
     *
     * @param merchantId
     * @param auditStatus
     */
    @Override
    public void verifyMerchant(Long merchantId, String auditStatus) throws BusinessException {

        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        merchant.setAuditStatus(auditStatus);
        merchantMapper.updateById(merchant);

    }

    /**
     * 分页查询商户下的员工信息
     *
     * @param staffDTO
     * @param pageNo
     * @param pageSize
     * @return
     * @throws BusinessException
     */
    @Override
    public PageVO<StaffDTO> queryStaffByPage(StaffDTO staffDTO, Integer pageNo, Integer pageSize) throws BusinessException {

        IPage<Staff> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        // 构造查询条件
        QueryWrapper<Staff> qw = new QueryWrapper();
        if (staffDTO != null) {
            if (staffDTO.getMerchantId() != null) {
                qw.lambda().eq(Staff::getMerchantId, staffDTO.getMerchantId());
            }
            if (StringUtils.isNotBlank(staffDTO.getUsername())) {
                qw.lambda().like(Staff::getUsername, staffDTO.getUsername());
            }
            if (StringUtils.isNotBlank(staffDTO.getFullName())) {
                qw.lambda().like(Staff::getFullName, staffDTO.getFullName());
            }
            if (staffDTO.getStaffStatus() != null) {
                qw.lambda().eq(Staff::getStaffStatus, staffDTO.getStaffStatus());
            }
        }

        //search

        IPage<Staff> staffIPage = staffMapper.selectPage(page, qw);

        List<StaffDTO> staffDTOList = new ArrayList<>();

        for (Staff record : staffIPage.getRecords()) {
            StaffDTO staffDTO1 = new StaffDTO();
            BeanUtils.copyProperties(record, staffDTO1);
            staffDTOList.add(staffDTO1);
        }
        // 封装结果集
        PageVO<StaffDTO> staffDTOS = new PageVO<>(staffDTOList, staffIPage.getTotal(), pageNo, pageSize);

        if (staffDTOS.getCounts() == 0) {
            return staffDTOS;
        }
        //添加员工所属门店信息
        for (StaffDTO staffs : staffDTOS) {
            StoreDTO storeDTO = queryStoreById(staffs.getStoreId());
            if (storeDTO != null) {
                staffs.setStoreName(storeDTO.getStoreName());
            }
        }
        return staffDTOS;

    }

    @Override
    public StoreDTO queryStoreById(Long storeId) {
        Store store = storeMapper.selectById(storeId);
        StoreDTO storeDTO = new StoreDTO();
        BeanUtils.copyProperties(store, storeDTO);
        if (storeDTO != null) {
            List<StaffDTO> staffs = queryStoreAdmin(storeId);
            storeDTO.setStaffs(staffs);
        }
        return storeDTO;
    }

    /**
     * 商户新增员工和账号
     *
     * @param staffDTO
     * @param roleCodes
     * @throws BusinessException
     */
    @Override
    public void createStaffAndAccount(StaffDTO staffDTO, String[] roleCodes) throws BusinessException {
        //1.新增员工
        createStaff(staffDTO);
        //获取商户的租户ID
        Long tenantId = queryMerchantById(staffDTO.getMerchantId()).getTenantId();
        CreateAccountRequestDTO accountRequest = new CreateAccountRequestDTO();
        accountRequest.setMobile(staffDTO.getMobile());
        accountRequest.setUsername(staffDTO.getUsername());
        accountRequest.setPassword(staffDTO.getPassword());
        //在租户内创建账号并绑定角色，包含校验账号是否存在及角色是否绑定
        tenantService.checkCreateStaffAccountRole(tenantId, accountRequest, roleCodes);
    }

    @Reference
    private AuthorizationService authService;
    /**
     * 查询员工详情
     * @param id
     * @param tenantId
     *
     *  */
    @Override
    public StaffDTO queryStaffDetail(Long id, Long tenantId) {
        StaffDTO staffDTO = queryStaffById(id);
        //根据用户名和租户ID查询角色信息
        List<RoleDTO> roles = authService.queryRolesByUsername(staffDTO.getUsername(), tenantId);
        List<StaffRoleDTO> staffRoles = new ArrayList<>();
        if (!roles.isEmpty()) {
            roles.forEach(roleDTO -> {
                StaffRoleDTO staffRoleDTO = new StaffRoleDTO();
                BeanUtils.copyProperties(roleDTO, staffRoleDTO);
                staffRoles.add(staffRoleDTO);
            });
        }
        staffDTO.setRoles(staffRoles);
        return staffDTO;
    }

    /**
     * 删除员工
     *
     * @param id
     * @throws BusinessException
     */
    @Override
    public void removeStaff(Long id) throws BusinessException {
        Staff staff = staffMapper.selectById(id);
        //清除员工和门店的关系
        storeStaffMapper.delete(new QueryWrapper<StoreStaff>().lambda().eq(StoreStaff::getStaffId, staff.getId()));
        //删除员工对应的账号,账号-角色之间的关系
        //获取商户的租户ID
        Long tenantId = queryMerchantById(staff.getMerchantId()).getTenantId();
        tenantService.unbindTenant(tenantId, staff.getUsername());//将某账号从租户内移除，租户管理员不可移除
        //删除员工
        staffMapper.deleteById(staff);
    }

    /**
     * 修改员工信息
     *
     * @param staffDTO
     * @param roleCodes
     * * @throws BusinessException
     */
    @Override
    public void modifyStaff(StaffDTO staffDTO, String[] roleCodes) throws BusinessException {
        Staff staff = staffMapper.selectById(staffDTO.getId());
        if (staff == null) {
            throw new BusinessException(CommonErrorCode.E_200013);
        }
        //更新员工的信息
        staff.setFullName(staffDTO.getFullName());
        staff.setPosition(staffDTO.getPosition());
        staff.setStoreId(staffDTO.getStoreId());
        staffMapper.updateById(staff);
        //处理员工的角色是否有修改
        Long tenantId = queryMerchantById(staff.getMerchantId()).getTenantId();
        tenantService.getAccountRoleBind(staff.getUsername(), tenantId, roleCodes);

    }

    /**
     * 获取员工详情
     *  @param id
     *   @return
     *   */
    public StaffDTO queryStaffById(Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            throw new BusinessException(CommonErrorCode.E_200013);
        }
        StaffDTO staffDTO = new StaffDTO();
        BeanUtils.copyProperties(staff,staffDTO);
        //根据员工归属门店id，获取门店信息
        StoreDTO store = queryStoreById(staff.getStoreId());
        staffDTO.setStoreName(store.getStoreName());
        return staffDTO;
    }

    /**
     * 删除某门店
     *
     * @param id
     */
    @Override
    public void removeStore(Long id) throws BusinessException {
        Store store = storeMapper.selectById(id);
        if (store.getParentId() == null) {
            throw new BusinessException("根门店id为空");
        }

        //清除门店和员工关系
        storeStaffMapper.delete(new LambdaQueryWrapper<StoreStaff>().eq(StoreStaff::getStoreId,id));
        //清除员工所属的门店
        staffMapper.update(null, new LambdaUpdateWrapper<Staff>().eq(Staff::getStoreId,id));
        //删除门店
        storeMapper.deleteById(id);
    }

    /**
     * 商户修改门店
     *
     * @param storeDTO
     * @param longList
     * @throws BusinessException
     */
    @Override
    public void modifyStore(StoreDTO storeDTO, List<Long> longList) throws BusinessException {

        //更新门店的信息
        Store store = new Store();
        BeanUtils.copyProperties(storeDTO,store);
        storeMapper.updateById(store);
        if (longList != null) {
            //清除门店绑定的管理员
            storeStaffMapper.delete(new LambdaQueryWrapper<StoreStaff>().eq(StoreStaff::getId,storeDTO.getId()));

            //重新绑定关系
            longList.forEach( id -> {
                bindStaffToStore(store.getId(),id);
            });

        }
    }

    /**
     * 获取门店管理员列表
     *
     * @param storeId
     * @return
     */
    List<StaffDTO> queryStoreAdmin(Long storeId) {

        //根据门店获取管理员ID
        List<StoreStaff> storeStaffs = storeStaffMapper.selectList(new LambdaQueryWrapper<StoreStaff>().eq(StoreStaff::getStoreId, storeId));
        List<Staff> staff = null;
        if (storeStaffs.isEmpty()) {
            List<Long> staffIds = new ArrayList<>();
            //查询结果不为空，则遍历获取管理员id，可能有多个
            storeStaffs.forEach(ss -> {
                Long staffId = ss.getStaffId();
                staffIds.add(staffId);
            });
            //根据id获取管理员信息
            staff = staffMapper.selectBatchIds(staffIds);
        }

        List<StaffDTO> staffList = new ArrayList<>();
        for (Staff staff1 : staff) {
            StaffDTO staffDTO = new StaffDTO();
            BeanUtils.copyProperties(staff, staffDTO);
            staffList.add(staffDTO);
        }
        return staffList;

    }

    private Boolean isExitStaffByUsername(String usernamr, Long merchantId) {
        Integer integer = staffMapper.selectCount(new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, usernamr)
                .eq(Staff::getMerchantId, merchantId));
        return integer > 0;
    }

    /**
     * 数据账号判断员工是否已在指定商户
     *
     * @param mobile
     * @param merchantId
     * @return
     */
    private Boolean isExitStaffByMobile(String mobile, Long merchantId) throws BusinessException {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId);
        Integer count = staffMapper.selectCount(lambdaQueryWrapper);
        return count > 0;
    }

}
