package com.polymerization.user.mapper;

import com.polymerization.user.entity.Bundle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 *
 * @since 2019-08-13
 */
@Repository
public interface BundleMapper extends BaseMapper<Bundle> {

    //@Select("select * from bundle where CODE=#{bundleCode}")
    //Bundle selectBundleByCode(@Param("bundleCode") String bundleCode);

}
