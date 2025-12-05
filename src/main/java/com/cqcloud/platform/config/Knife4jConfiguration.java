package com.cqcloud.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * 配置接口文档Knife4j
 * 
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕2025年3月31日🐬🐇 💓💕
 */
@EnableKnife4j
@Configuration
public class Knife4jConfiguration {

	// 定义常量TOKEN_HEADER，用于存储认证令牌的HTTP头名称
	private static final String TOKEN_HEADER = "Authorization";

	/**
	 * 配置并返回OpenAPI对象，用于描述接口信息
	 * 此方法通过Spring的@Bean注解标记，表示该方法的返回值将被Spring容器管理，可被其他组件注入使用
	 *
	 * @return OpenAPI 实例，包含API的描述信息、安全方案、许可证等
	 */
	@Bean
	public OpenAPI apiInfo() {
	    // 创建并配置OpenAPI实例，定义安全方案和API基本信息
	    return new OpenAPI()
	            .components(new Components()
	                    // 添加安全方案，定义如何进行API认证，这里使用API密钥类型，通过HTTP头传递，采用JWT格式
	                    .addSecuritySchemes(TOKEN_HEADER,
	                            new SecurityScheme().type(SecurityScheme.Type.APIKEY).scheme("bearer")
	                                    .bearerFormat("JWT"))
	                    // 添加参数模板，定义通用的请求头参数
	                    .addParameters(TOKEN_HEADER,
	                            new Parameter().in("header").schema(new StringSchema()).name(TOKEN_HEADER)))
	            // 添加API的基本信息，如标题、版本、描述和许可证信息
	            .info(new Info().title("Pension Swagger API").version("1.0.0").description("Spring Boot 接口文档")
	                    .license(new License().name("Powered By 服务管理").url("http://127.0.0.1:8000/doc.html")))
	            // 添加外部文档链接，提供更多信息
	            .externalDocs(new ExternalDocumentation().description("Spring Boot Kubernetes 微服务开发脚手架").url(""));
	}
}