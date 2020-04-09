package com.polymerization.merchant.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnProperty(prefix = "swagger", value = {"enable"}, havingValue = "true")
@EnableSwagger2
public class SwaggerConfiguration {

    /***
     *
     * @return
     */
    @Bean
    public Docket buildDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInfo()).select()
                //设置扫描的API(Controller)包
                .apis(RequestHandlerSelectors.basePackage("com.polymerization.merchant.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /***
     * @Titile 构建API信息
     * @return
     */
    private ApiInfo buildApiInfo() {
        Contact contact = new Contact("开发者", "", "");
        return new ApiInfoBuilder()
                .title("聚合支付-API文档")
                .description("")
                .contact(contact)
                .version("1.0.0").build();
    }

}
