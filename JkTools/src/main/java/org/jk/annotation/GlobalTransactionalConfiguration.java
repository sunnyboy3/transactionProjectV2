package org.jk.annotation;

import brave.handler.FinishedSpanHandler;
import org.jk.interceptor.BaseFilter;
import org.jk.interceptor.HttpAutoInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.sleuth.autoconfig.SleuthProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.cloud.sleuth.propagation.SleuthTagPropagationProperties;
import org.springframework.cloud.sleuth.propagation.TagPropagationFinishedSpanHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvc
@AutoConfigureBefore(TraceAutoConfiguration.class)
public class GlobalTransactionalConfiguration  implements WebMvcConfigurer {
    /**
     * 目的注入自定义BaseFilter
     * @return
     */
    @Bean
    public FilterRegistrationBean<Filter> baseFilter(){
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new BaseFilter());
        List<String> patters = new ArrayList<>();
        patters.add("/*");
        filterFilterRegistrationBean.setUrlPatterns(patters);
        filterFilterRegistrationBean.setOrder(1);
        return filterFilterRegistrationBean;
    }

    /**
     * 目的自定义transaction-trace-id
     */
    @Configuration
    @EnableConfigurationProperties(SleuthTagPropagationProperties.class)
    protected static class TagPropagationConfiguration{
        @Bean
        public FinishedSpanHandler finishedSpanHandler(SleuthProperties sleuthProperties,SleuthTagPropagationProperties tagPropagationProperties){
            return new TagPropagationFinishedSpanHandler(sleuthProperties,tagPropagationProperties);
        }
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager (DataSource dataSource){
        CustomizeTransactionManager transactionManager = new CustomizeTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    @Bean(name = "transactionTemplate")
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager){
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        return transactionTemplate;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        configurer.setPathMatcher(matcher);
    }

    /**
     *注册拦截器
     */
    @Bean
    HttpAutoInterceptor httpAutoInterceptor() {
        return new HttpAutoInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpAutoInterceptor());
    }
}
