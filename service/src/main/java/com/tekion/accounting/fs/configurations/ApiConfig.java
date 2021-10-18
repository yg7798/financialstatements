package com.tekion.accounting.fs.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
//@ComponentScan(basePackageClasses = {LookupController.class,ExcelReportApi.class, ExcelReportApiV2.class, AccountingCacheApi.class}, basePackages="com.tekion.as.api")
@EnableAspectJAutoProxy
public class ApiConfig implements WebMvcConfigurer {

}
