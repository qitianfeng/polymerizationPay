package com.polymerization.merchant.controller;

import com.polymerization.merchant.api.MerchantService;
import com.polymerization.merchant.api.dto.StoreDTO;
import com.polymerization.merchant.common.utils.SecurityUtil;
import com.shanjupay.common.domain.PageVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class StoreController {


    @Reference
    MerchantService merchantService;


    @ApiOperation("分页条件查询商户下门店")
    @PostMapping("/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        Long merchantId = SecurityUtil.getMerchantId();
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchantId);

        PageVO<StoreDTO> storeDTOS = merchantService.queryStoreByPage(storeDTO, pageNo, pageSize);
        return storeDTOS;
    }
}
