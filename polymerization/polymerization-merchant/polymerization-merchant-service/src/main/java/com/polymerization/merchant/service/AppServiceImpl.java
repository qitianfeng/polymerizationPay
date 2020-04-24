package com.polymerization.merchant.service;

import com.polymerization.merchant.entity.App;
import com.polymerization.merchant.entity.Merchant;
import com.polymerization.merchant.mapper.AppMapper;
import com.polymerization.merchant.mapper.MerchantMapper;

import java.util.List;

@Service
public class AppServiceImpl implements AppService {
    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 商户下创建应用
     *
     * @param merchantId
     * @param appDTO
     * @return
     */
    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {
        if (merchantId == null || appDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        if(isExistAppName(appDTO.getAppName())) {
            throw new BusinessException(CommonErrorCode.E_200004);
        }

        //保存应用信息
        appDTO.setAppId(RandomStringUtil.getRandomString(32));
        appDTO.setMerchantId(merchantId);
        App entity = new App();// AppCovert.INSTANCE.dto2App(appDTO);
        BeanUtils.copyProperties(appDTO,entity);
        appMapper.insert(entity);
        AppDTO appDTO1 = new AppDTO();
        BeanUtils.copyProperties(entity,appDTO1);
        return appDTO1;
    }

    /**
     * 根据业务id查询应用
     *
     * @return
     * @throws BusinessException
     */
    @Override
    public AppDTO getAppById(String id) throws BusinessException {
        App app = appMapper.selectById(id);
        AppDTO appDTO = new AppDTO();//AppCovert.INSTANCE.entity2DTO(app);
        BeanUtils.copyProperties(app,appDTO);
        return appDTO;
    }

    /**
     * 查询商户的应用列表
     *
     * @param merchantId
     * @return
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) {
        List<App> appList = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        List<AppDTO> appDTOS = new ArrayList<>();
        for (App app : appList) {
            AppDTO appDTO = new AppDTO();
            BeanUtils.copyProperties(app,appDTO);
            appDTOS.add(appDTO);
        }
        return appDTOS;
    }

    /**
     * 查询应用是否属于某个商户
     *
     * @param appId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) throws BusinessException {

        Integer integer = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppId, appId)
                .eq(App::getMerchantId, merchantId));
        return integer > 0;
    }

    /**
     * 校验应用名是否被使用
     * @param appName
     * @return
     */
    private boolean isExistAppName(String appName) {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, appName));
        return count > 0;
    }
}
