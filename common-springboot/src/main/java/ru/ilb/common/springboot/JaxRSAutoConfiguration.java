package ru.ilb.common.springboot;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.validation.BeanValidationFeature;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ilb.common.jaxrs.async.AsyncTaskManager;
import ru.ilb.common.jaxrs.jaxb.JaxbContextResolver;
import ru.ilb.common.jpa.tools.DescriptorUtils;
import ru.ilb.common.jpa.tools.RepositoryPopulator;


/**
 *
 * @author slavb
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({ SpringBus.class, CXFServlet.class })
//@EnableConfigurationProperties(CxfProperties.class)
@AutoConfigureAfter(name = {
        "org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration"
})
public class JaxRSAutoConfiguration {
    @Bean
    public ru.ilb.common.jaxrs.json.MOXyJsonProvider jsonProvider() {
        // lacks @Provider annotation
        // return new org.eclipse.persistence.jaxb.rs.MOXyJsonProvider();
        return new ru.ilb.common.jaxrs.json.MOXyJsonProvider();
    }
    @Bean
    public JaxbContextResolver jaxbContextResolver() {
        return new JaxbContextResolver();
    }

    BeanValidationFeature beanValidationFeature() {
        return new BeanValidationFeature();
    }

    @Bean
    AsyncTaskManager asyncTaskManager() {
        return new AsyncTaskManager();
    }

    @Bean
    RepositoryPopulator repositoryPopulator() {
        return new RepositoryPopulator();
    }

    @Bean
    DescriptorUtils descriptorUtils() {
        return new DescriptorUtils();
    }
}
