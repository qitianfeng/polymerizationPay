package com.polymerization.merchant.convert;

import com.polymerization.merchant.api.dto.AppDTO;
import com.polymerization.merchant.entity.App;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AppCovert {
    AppCovert INSTANCE = Mappers.getMapper(AppCovert.class);

    AppDTO entity2DTO(App app);

    App dto2App(AppDTO appDTO);

    List<AppDTO> listEntity2DTO(List<App> appList);
}
