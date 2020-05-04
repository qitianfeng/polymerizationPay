package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.MerchantDTO;
import com.polymerization.merchant.api.dto.StoreDTO;
import com.polymerization.merchant.common.utils.SecurityUtil;
import com.polymerization.common.domain.PageVO;
import com.polymerization.common.util.QRCodeUtil;
import com.polymerization.transaction.api.dto.QRCodeDto;
import com.polymerization.transaction.api.TransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Api(value = "商户平台-门店管理", tags = "商户平台-门店管理", description = "商户平台-门店的增删改查")
@RestController
@Slf4j
public class StoreController {

    //"%s商品"
    @Value("${shanjupay.c2b.subject}")
    String subject;
    //"向%s付款"
    @Value("${shanjupay.c2b.body}")
    String body;


    @Reference
    MerchantService merchantService;

    @Reference
    TransactionService transactionService;

    /*@ApiOperation("门店列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name="pageNo",value = "页码",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name="pageSize",value = "每页记录数",required = true,dataType = "int",paramType = "query")
    })
    @PostMapping("/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(Integer pageNo,Integer pageSize){
        //商户id
        Long merchantId = SecurityUtil.getMerchantId();
        //查询条件
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchantId);//商户id
        //调用service查询列表
        PageVO<StoreDTO> storeDTOS = merchantService.queryStoreByPage(storeDTO, pageNo, pageSize);
        return storeDTOS;
    }*/

    @ApiOperation("生成商户应用门店的二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "商户应用id", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "storeId", value = "商户门店id", required = true, dataType = "String", paramType = "path"),
    })
    @GetMapping(value = "/my/apps/{appId}/stores/{storeId}/app-store-qrcode")
    public String createCScanBStoreQRCode(@PathVariable("storeId") Long storeId, @PathVariable("appId")String appId) throws IOException {

        //获取商户id
        Long merchantId = SecurityUtil.getMerchantId();
        //商户信息
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);

        QRCodeDto qrCodeDto = new QRCodeDto();
        qrCodeDto.setMerchantId(merchantId);
        qrCodeDto.setStoreId(storeId);
        qrCodeDto.setAppId(appId);
        //标题.用商户名称替换 %s
        String subjectFormat = String.format(subject, merchantDTO.getMerchantName());
        qrCodeDto.setSubject(subjectFormat);
        //内容
        String bodyFormat = String.format(body, merchantDTO.getMerchantName());
        qrCodeDto.setBody(bodyFormat);

        //获取二维码的URL
        String storeQRCodeURL = transactionService.createStoreQRCode(qrCodeDto);

        //调用工具类生成二维码图片
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        //二维码图片base64编码
        String qrCode = qrCodeUtil.createQRCode(storeQRCodeURL, 200, 200);
        return qrCode;

    }
    @ApiOperation("在某商户下新增门店，并设置管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store", value = "门店信息", required = true, dataType = "StoreDTO", paramType = "body"),
            @ApiImplicitParam(name = "staffIds", value = "管理员id集合", required = true, allowMultiple = true, dataType = "Long", paramType = "query")})
    @PostMapping(value = "/my/stores")
    public void createStore(@RequestBody StoreDTO store, @RequestParam List<Long> staffIds) {
        store.setMerchantId(SecurityUtil.getMerchantId());
        store.setStoreStatus(true);
        merchantService.createStore(store, staffIds);
    }
    @ApiOperation("门店列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeDTO", value = "门店dto", dataType = "StoreDTO", paramType = "body"),
            @ApiImplicitParam(name="pageNo",value = "页码",required = true,dataType = "int",paramType = "query"),
            @ApiImplicitParam(name="pageSize",value = "每页记录数",required = true,dataType = "int",paramType = "query")
    })
    @PostMapping("/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(@RequestBody StoreDTO storeDTO, Integer pageNo,Integer pageSize){
        //商户id
        Long merchantId = SecurityUtil.getMerchantId();
        //查询条件
        storeDTO.setMerchantId(merchantId);//商户id
        //调用service查询列表
        PageVO<StoreDTO> storeDTOS = merchantService.queryStoreByPage(storeDTO, pageNo, pageSize);
        return storeDTOS;
    }
    @ApiOperation("查询某个门店的信息")
    @GetMapping("/my/stores/{id}")
    public StoreDTO queryStaff( @PathVariable Long id){
        return merchantService.queryStoreById(id);
    }

    @ApiOperation("修改某门店信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "store", value = "门店信息", required = true, dataType = "StoreDTO", paramType = "body"),
            @ApiImplicitParam(name = "staffIds", value = "管理员id集合", required = true, allowMultiple = true, dataType = "Long", paramType = "query")
    })
    @PutMapping("/my/stores")
    public void modifyStaff(@RequestBody StoreDTO store, @RequestParam List<Long> staffIds) {
        merchantService.modifyStore(store, staffIds);
    }

    @ApiOperation("删除某门店")
    @ApiImplicitParam(name = "id", value = "门店id", required = true, dataType = "Long", paramType = "path")
    @DeleteMapping("/my/stores/{id}")
    public void removeStore(@PathVariable Long id) {
        merchantService.removeStore(id);
    }


}

