/*
 * Copyright 2017 slavb.
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
package ru.ilb.common.jpa.tools;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import ru.ilb.common.jpa.annotations.AutoPopulableRepository;

/**
 * Automate JPA-repository population by public static object instances
 * Usage: 
 * 1. Register &lt;bean class="ru.ilb.common.jpa.tools.RepositoryPopulator"/>
 * 2. Add @AutoPopulableRepository annotation to JPA Repository
 * 3. Add static entity instances to JPA Repository, e.g.
 * public static PointType OFFICE = new PointType(1L, PointTypeCode.OFFICE, "Office");
 * 
 * Entity design: Specify unique constraint, equals and hashCode on unique identifier field,
 * use @Enumerated(EnumType.STRING) for enum field.
 * Create constructor for all fields or use fluent api.
 * @author slavb
 */
@Component
public class RepositoryPopulator {

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void populateAll() {
        applicationContext.getBeansWithAnnotation(AutoPopulableRepository.class).values()
                .stream().forEach(repository -> populateRepository((JpaRepository) repository));

    }
    
    public static <T> List<T> getEntities(Class repositoryInterface, Class<T> clazz) {
        return getEntities(repositoryInterface);
    }
    public static List getEntities(Class repositoryInterface) {
        ParameterizedType baseInterface = ((ParameterizedType) repositoryInterface.getGenericInterfaces()[0]);
        Type objectType = baseInterface.getActualTypeArguments()[0];

        //JpaRepository repository = applicationContext.getBean(repositoryClass);
        List objects = Arrays.stream(repositoryInterface.getDeclaredFields())
                .filter(field -> java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getGenericType().equals(objectType))
                .map(field -> getFieldValue(field))
                .collect(Collectors.toList());
        return objects;
    }

    public void populateRepository(JpaRepository repository) {
        repository.save(getEntities((Class) repository.getClass().getGenericInterfaces()[0]));
    }

    private static Object getFieldValue(Field field) {
        try {
            return field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return null;
        }
    }

}
