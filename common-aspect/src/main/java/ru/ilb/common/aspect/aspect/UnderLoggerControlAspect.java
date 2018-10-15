/*
 * Copyright 2018 shadrin_nv.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ilb.common.aspect.aspect;

import javax.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ilb.common.aspect.annotation.UnderLoggerControl;
import ru.ilb.common.aspect.statelogger.BaseStateLogger;
import ru.ilb.common.aspect.statelogger.StateLoggerFactory;

/**
 *
 * @author shadrin_nv
 */
@Aspect
@Component
public class UnderLoggerControlAspect {
    protected static final Logger LOG = LoggerFactory.getLogger(UnderLoggerControlAspect.class);

    @Autowired
    StateLoggerFactory stateLoggerFactory;

    @Around(value = "@annotation(underLoggerControl)")
    public Object underLoggerControlAdvice(final ProceedingJoinPoint proceedingJoinPoint, final UnderLoggerControl underLoggerControl) throws Throwable {
        BaseStateLogger stateLogger = stateLoggerFactory.getStateLogger(underLoggerControl.controller(), underLoggerControl.loggerClass());
        if (stateLogger != null) {
            stateLogger.start();
            stateLogger.working(0L, null);
        }
		Object value = null;
		try {
			value = proceedingJoinPoint.proceed();
		} catch (Throwable e) {
            if (stateLogger != null) {
                stateLogger.working(-1L, e.getMessage());
            }
            throw e;
		}
        if (stateLogger != null) {
            stateLogger.end();
        }

        return value;
    }

    @PostConstruct
    public void init() {
        LOG.info("Initialazing UnderLoggerControlAspect");
    }

}
