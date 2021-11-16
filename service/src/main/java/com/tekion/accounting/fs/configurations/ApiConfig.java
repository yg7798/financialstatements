package com.tekion.accounting.fs.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.tekion.clients.excelGeneration.client.helpers.ExcelReportApi;
import com.tekion.clients.excelGeneration.client.helpers.v2.ExcelReportApiV2;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses ={ExcelReportApi.class, ExcelReportApiV2.class}, basePackages="com.tekion.accounting.fs.api")
@EnableAspectJAutoProxy
public class ApiConfig implements WebMvcConfigurer {

}
