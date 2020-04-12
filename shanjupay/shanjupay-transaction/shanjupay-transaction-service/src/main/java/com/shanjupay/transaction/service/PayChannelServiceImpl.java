package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.api.dto.com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    PayChannelMapper payChannelMapper;

    @Autowired
    PlatformChannelMapper platformChannelMapper;

    @Autowired
    AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    PayChannelParamMapper payChannelParamMapper;

    @Resource
    private Cache cache;


    /**
     * 获取平台服务类型
     *
     * @return
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() {
        List<PlatformChannelDTO> platformChannelDTOList = new ArrayList<>();
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        for (PlatformChannel platformChannel : platformChannels) {
            PlatformChannelDTO platformChannelDTO = new PlatformChannelDTO();
            BeanUtils.copyProperties(platformChannel, platformChannelDTO);
            platformChannelDTOList.add(platformChannelDTO);
        }

        return platformChannelDTOList;
    }

    /**
     * 为APP绑定平台服务类型
     *
     * @param appId
     * @param platformChannelCodes
     * @throws BusinessException
     */
    @Override
    @Transactional //开启事务
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {

        if (StringUtils.isBlank(appId) || StringUtils.isBlank(platformChannelCodes)) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //根据APPID和平台服务类型code查询app_platform_channel
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        //如果没有绑定
        if (appPlatformChannel == null) {
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    /**
     * 应用是否绑定某个服务类型
     *
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    @Override
    public Integer queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        Integer count = appPlatformChannelMapper.selectCount(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getPlatformChannel, platformChannel)
                .eq(AppPlatformChannel::getAppId, appId));
        //已绑定
        if (count > 0) {
            return 1;
        }
        return 0;
    }

    /**
     * 根据平台服务类型获取支付渠道列表
     *
     * @param platformChannelCode
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {

        List<PayChannelDTO> payChannelDTOS = platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
        System.out.println(JSON.toJSONString(payChannelDTOS));
        return payChannelDTOS;
    }

    /**
     * 保存支付渠道参数
     *
     * @param payChannelParamDTO
     * @throws BusinessException
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException {
        if (payChannelParamDTO == null || StringUtil.isBlank(payChannelParamDTO.getAppId()) || StringUtils.isBlank(payChannelParamDTO.getPlatformChannelCode())
                || StringUtils.isBlank(payChannelParamDTO.getPayChannel())) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据APPID和服务类型查询应用与服务类型绑定id
        Long aLong = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
        if (aLong == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, aLong)
                .eq(PayChannelParam::getPayChannel, payChannelParamDTO.getPayChannel()));
        //更新已有配置
        if (payChannelParam != null) {
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            payChannelParamMapper.updateById(payChannelParam);
        } else {
            //添加新配置
            PayChannelParam payChannelParam1 = new PayChannelParam();
            BeanUtils.copyProperties(payChannelParamDTO, payChannelParam1);
            payChannelParam1.setId(null);
            payChannelParam1.setAppPlatformChannelId(aLong);
            payChannelParamMapper.insert(payChannelParam1);
        }
//保存到redis
        updateCache(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
    }

    /**
     * 获取指定应用服务类型下所包含的原始支付渠道参数列表
     *
     * @param appId
     * @param platformChannel 服务类型
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppIdAndPlatform(String appId, String platformChannel) throws BusinessException {

        //从缓存查询
        String keyBuilder = RedisUtil.keyBuilder(appId, platformChannel);
        Boolean exists = cache.exists(keyBuilder);
        if (exists) {
            String value = cache.get(keyBuilder);
            List<PayChannelParamDTO> payChannelParamDTO = JSONObject.parseObject(value, (Type) PayChannelParamDTO.class);
            return payChannelParamDTO;
        }


        Long aLong = selectIdByAppPlatformChannel(appId, platformChannel);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, aLong));

        List<PayChannelParamDTO> payChannelParamList = new ArrayList<>();
        for (PayChannelParam payChannelParam : payChannelParams) {
            PayChannelParamDTO payChannelParam1 = new PayChannelParamDTO();
            BeanUtils.copyProperties(payChannelParam, payChannelParam1);
            payChannelParamList.add(payChannelParam1);
        }
        //存入缓存
        updateCache(appId, platformChannel);
        return payChannelParamList;
    }

    /**
     * 获取指定应用服务类型下所包含的原始支付参数
     *
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppIdAndPlatform(appId, platformChannel);
        for (PayChannelParamDTO payChannelParamDTO : payChannelParamDTOS) {
            if (payChannelParamDTO.getPayChannel().equals(payChannel)) {
                return payChannelParamDTO;
            }
        }
        return null;
    }

    /**
     * 根据APPID和服务类型查询应用服务类型绑定id
     *
     * @param appId
     * @param platformChannelCode
     * @return
     */

    private Long selectIdByAppPlatformChannel(String appId, String platformChannelCode) {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel != null) {
            return appPlatformChannel.getId();
        }
        return null;
    }

    private void updateCache(String appId, String platformChannel) {
        //处理Redis缓存
        String keyBuilder = RedisUtil.keyBuilder(appId, platformChannel);
        //查询Redis，检查key
        Boolean exists = cache.exists(keyBuilder);
        if (exists) {
            cache.del(keyBuilder);
        }
        Long aLong = selectIdByAppPlatformChannel(appId, platformChannel);
        if (aLong != null){
            List<PayChannelParamDTO> payChannelParamDTOS = new ArrayList<>();
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId,aLong ));

            for (PayChannelParam payChannelParam : payChannelParams) {
                PayChannelParamDTO payChannelParam1 = new PayChannelParamDTO();
                BeanUtils.copyProperties(payChannelParam,payChannelParam1);
                payChannelParamDTOS.add(payChannelParam1);
            }
            cache.set(keyBuilder,JSON.toJSONString(payChannelParamDTOS));
        }
    }
}
