package com.tekion.accounting.fs.configurations.Launcher;

import com.tekion.accounting.fs.configurations.ApiConfig;
import com.tekion.core.service.TekionServiceApplication;
import com.tekion.core.service.api.DefaultServiceApiWebConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@ComponentScan(basePackages = {"com.tekion"}, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.tekion\\..*launcher\\..*")
})
@EnableAutoConfiguration(exclude = {
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		DispatcherServletAutoConfiguration.class,
		WebMvcAutoConfiguration.class
}
)

@Configuration
@Slf4j
@Import({DefaultServiceApiWebConfig.class} )
@EnableAsync
@EnableCaching
public class Launcher extends SpringBootServletInitializer implements TekionServiceApplication {
	public static final String APP_ROOT = "/financial-statements";
	private final String config_host = System.getenv("config_host");

	public static void main(String[] args) {
		SpringApplication.run(Launcher.class, args);
	}

	@Bean
	@Override
	public String getAppRoot() {
		return APP_ROOT;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Launcher.class);
	}

	@Bean
	@Override
	public ServletRegistrationBean healthApi() {
		return TekionServiceApplication.createDefaultHealthApi(getAppRoot());
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet();
	}

	@Bean
	public DispatcherServletRegistrationBean accountingApi() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(ApiConfig.class);
		dispatcherServlet.setApplicationContext(applicationContext);
		dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
		DispatcherServletRegistrationBean servletRegistrationBean = new DispatcherServletRegistrationBean(dispatcherServlet,
				APP_ROOT + "/u/*");
		servletRegistrationBean.setName("fsApi");
		servletRegistrationBean.setLoadOnStartup(1);
		return servletRegistrationBean;
	}
}
