package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.AppService;
import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.common.utils.SecurityUtil;
import com.polymerization.merchant.service.SmsService;
import com.polymerization.merchant.common.intercept.utils.FastDFSClient;
import com.polymerization.merchant.vo.FastDFSFile;
import com.polymerization.merchant.vo.MerchantDetailVO;
import com.polymerization.merchant.vo.MerchantRegisterVO;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController

@Api(value = "商户平台", tags = "商户平台", description = "商户平台")
public class MerchantController {

    @Reference
    MerchantService merchantService;

    @Autowired
    SmsService smsService;

    @Reference
    AppService appService;


    @ApiOperation("根据ID查询用户")
    @ApiImplicitParam(name = "id", value = "ID号", required = true, dataType = "String")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String", paramType = "query")
    @GetMapping("/sms")
    public String getSMSCode(@RequestParam String phone) {
        return smsService.sendMsg(phone);
    }


    @ApiOperation("注册商户")
   /* @ApiImplicitParam(name = "merchantRegister", value = "注册信息", required = true,
            dataType = "MerchantRegisterVO", paramType = "body")*/
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO) {
        if (merchantRegisterVO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //手机号非空校验
        if (StringUtils.isBlank(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        //校验手机的合法性
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        //联系人非空校验‘
        if (StringUtils.isBlank(merchantRegisterVO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtils.isBlank(merchantRegisterVO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //验证码非空校验
        if (StringUtils.isBlank(merchantRegisterVO.getVerifyCode()) || StringUtils.isBlank(merchantRegisterVO.getVerifyKey())) {

            throw new BusinessException(CommonErrorCode.E_100103);
        }
        //校验验证码
        smsService.checkVerifyCode(merchantRegisterVO.getVerifyKey(), merchantRegisterVO.getVerifyCode());
        //注册商户

        MerchantDTO merchantDTO = new MerchantDTO();

        BeanUtils.copyProperties(merchantRegisterVO, merchantDTO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    @ApiOperation("证件上传")
    @ApiImplicitParam(value = "上传的文件", name = "file", required = true, dataType = "MultipartFile", paramType = "query")
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            FastDFSFile fastDFSFile = new FastDFSFile(
                    file.getOriginalFilename(),//原来文件名
                    file.getBytes(),//文件的字节数组
                    org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename()));
            String[] upload = FastDFSClient.upload(fastDFSFile);
            //  upload[0] group1
            //  upload[1] M00/00/00/wKjThF1aW9CAOUJGAAClQrJOYvs424.jpg
            //3. 拼接图片的全路径返回
            return FastDFSClient.getTrackerUrl() + "/" + upload[0] + "/" + upload[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @ApiOperation("资质申请")
    @ApiImplicitParam(name = "merchantDetailVO", value = "认证资料",
            dataType = "MerchantDetailVO", required = true, paramType = "body")
    @PostMapping("/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantDetailVO) {

        //解析token中得到的Id
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = new MerchantDTO();
        BeanUtils.copyProperties(merchantDetailVO, merchantDTO);


        //资质申请
        merchantService.applyMerchant(merchantId, merchantDTO);
    }

    @ApiOperation("获取登录用户的商户信息")
    @GetMapping("/my/merchants")
    public MerchantDTO getMyMerchantInfo(){
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        return merchantDTO;
    }

}
