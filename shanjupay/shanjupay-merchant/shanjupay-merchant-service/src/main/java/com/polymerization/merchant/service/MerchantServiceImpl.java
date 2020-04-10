package com.polymerization.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.entity.Merchant;
import com.polymerization.merchant.mapper.MerchantMapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional  //添加事务性
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
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
        Merchant merchant = new Merchant();
        //设置审核状态0-未申请 1-已申请 2-审核通过 3- 审核拒绝
        merchant.setAuditStatus("0");
        //设置手机号
        BeanUtils.copyProperties(merchantDTO, merchant);
        //保存商户
        merchantMapper.insert(merchant);
        //将新增商户id返回
        merchantDTO.setId(merchant.getId());
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
        BeanUtils.copyProperties(merchantDTO,entity);
        //将dto转成entity
        //将必要的参数设置到entity
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());//因为资质申请的时候手机号不让改，还使用数据库中原来的手机号
        entity.setAuditStatus("1");//审核状态1-已申请待审核
        entity.setTenantId(merchant.getTenantId());
        //调用mapper更新商户表
        merchantMapper.updateById(entity);

    }

}
