package ru.ilb.common.springboot;

import java.util.Arrays;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.provider.XSLTJaxbProvider;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.validation.BeanValidationFeature;
import org.apache.cxf.validation.BeanValidationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import ru.ilb.common.jaxrs.async.AsyncTaskManager;
import ru.ilb.common.jaxrs.jaxb.JaxbContextResolver;
import ru.ilb.common.jaxrs.xml.transform.ServletContextURIResolver;
import ru.ilb.common.jpa.tools.DescriptorUtils;
import ru.ilb.common.jpa.tools.RepositoryPopulator;

/**
 *
 * @author slavb
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ILBProperties.class)
@ConditionalOnClass({SpringBus.class, CXFServlet.class})
@AutoConfigureAfter(name = {
    "org.apache.cxf.spring.boot.autoconfigure.CxfAutoConfiguration"
})
public class ILBAutoConfiguration {

    @Autowired
    ILBProperties properties;

    @Autowired
    Environment env;

    @Bean
    @ConditionalOnMissingBean
    public ru.ilb.common.jaxrs.json.MOXyJsonProvider jsonProvider() {
        // lacks @Provider annotation
        // return new org.eclipse.persistence.jaxb.rs.MOXyJsonProvider();
        return new ru.ilb.common.jaxrs.json.MOXyJsonProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public JaxbContextResolver jaxbContextResolver() {
        return new JaxbContextResolver();
    }

//    @Bean
//    @ConditionalOnMissingBean
//    @ConditionalOnClass(value = org.hibernate.validator.HibernateValidator.class)
//    BeanValidationFeature beanValidationFeature() {
//        BeanValidationFeature beanValidationFeature = new BeanValidationFeature();
//        beanValidationFeature.setProvider(new BeanValidationProvider());
//        return beanValidationFeature;
//    }

    @Bean
    @ConditionalOnMissingBean
    AsyncTaskManager asyncTaskManager() {
        return new AsyncTaskManager();
    }

    @Bean
    @ConditionalOnMissingBean
    RepositoryPopulator repositoryPopulator() {
        return new RepositoryPopulator();
    }

    @Bean
    @ConditionalOnMissingBean
    DescriptorUtils descriptorUtils() {
        return new DescriptorUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public XSLTJaxbProvider xsltJaxbProvider() {
        XSLTJaxbProvider xsltJaxbProvider = new XSLTJaxbProvider();
        xsltJaxbProvider.setResolver(new ServletContextURIResolver());
        xsltJaxbProvider.setProduceMediaTypes(Arrays.asList(properties.getXslt().getProduces()));
        // development profile: reload  xslt files on each transformation
        //xsltJaxbProvider.setRefreshTemplates(env.acceptsProfiles(Profiles.of(ILBProfile.DEVELOPMENT.value())));
        return xsltJaxbProvider;
    }
}
