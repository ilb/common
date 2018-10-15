package ru.ilb.common.aspect;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"ru.ilb.common.aspect", "ru.ilb.common.aspect.aspect", "ru.ilb.common.aspect.statelogger"})
public class SpringApplicationConfig {

}
