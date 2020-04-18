package com.polymerization.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.polymerization.transaction.api.dto.PayChannelDTO;
import com.polymerization.transaction.entity.PlatformChannel;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 */
@Repository
public interface PlatformChannelMapper extends BaseMapper<PlatformChannel> {
    List<PayChannelDTO> selectPayChannelByPlatformChannel(String platformChannelCode);


}
