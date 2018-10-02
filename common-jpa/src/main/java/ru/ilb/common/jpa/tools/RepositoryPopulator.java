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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.ilb.common.jpa.annotations.AutoPopulableRepository;
import ru.ilb.common.jpa.repository.CacheableJpaRepository;

/**
 * Automate JPA-repository population by public static object instances Usage:
 * 1. Register &lt;bean class="ru.ilb.common.jpa.tools.RepositoryPopulator"/> 2.
 * Add @AutoPopulableRepository annotation to JPA Repository 3. Add static
 * entity instances to JPA Repository, e.g. public static PointType OFFICE = new
 * PointType(1L, PointTypeCode.OFFICE, "Office");
 *
 * Entity design: Specify unique constraint, equals and hashCode on unique
 * identifier field, use @Enumerated(EnumType.STRING) for enum field. Create
 * constructor for all fields or use fluent api.
 *
 * @author slavb
 */
@Component
public class RepositoryPopulator {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private DescriptorUtils descriptorUtils;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager platformTransactionManager;

    @PostConstruct
    public void populateAll() {
        applicationContext.getBeansWithAnnotation(AutoPopulableRepository.class).values()
                .stream().forEach(repository -> populateRepositoryInTransaction((JpaRepository) repository));

    }

    public static <T> List<T> getEntities(Class repositoryInterface, Class<T> clazz) {
        return getEntities(repositoryInterface);
    }

    public static List getEntities(Class repositoryInterface) {
        ParameterizedType baseInterface = ((ParameterizedType) repositoryInterface.getGenericInterfaces()[0]);
        Type objectType = baseInterface.getActualTypeArguments()[0];

        //List of static fields with entity instances
        List objects = Arrays.stream(repositoryInterface.getDeclaredFields())
                .filter(field -> java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getGenericType().equals(objectType))
                .map(field -> getFieldValue(field))
                .collect(Collectors.toList());
        return objects;
    }

    public void populateRepositoryInTransaction(JpaRepository repository) {
        // https://stackoverflow.com/a/26608403
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus ts) {
                populateRepository(repository);
            }
        });

    }

    public void populateRepository(JpaRepository repository) {
        AutoPopulableRepository annotation = AnnotationUtils.findAnnotation(repository.getClass(), AutoPopulableRepository.class);
        List entities = getEntities((Class) repository.getClass().getGenericInterfaces()[0]);
        switch (annotation.mode()) {
            case SIMPLE:
                populateRepositorySimple(repository, annotation, entities);
                break;
            case FINDALL:
                populateRepositoryFindAll(repository, annotation, entities);
                break;
        }
        // populate cache
        if (repository instanceof CacheableJpaRepository) {
            ((CacheableJpaRepository) repository).fillCache(entities);
        }
    }

    private List populateRepositorySimple(JpaRepository repository, AutoPopulableRepository annotation, List entities) {

        return repository.save(entities);
    }

    private List populateRepositoryFindAll(JpaRepository repository, AutoPopulableRepository annotation, List entities) {
        if (descriptorUtils == null) {
            throw new IllegalArgumentException("DescriptorUtils bean required for FINDALL mode");
        }

        List base = repository.findAll();
        List result = new ArrayList<>();

        Map<Object, Object> dstMap = descriptorUtils.mapById(base);
        Iterator<Object> it = entities.iterator();
        while (it.hasNext()) {
            Object src = it.next();
            Object pk = descriptorUtils.getPrimaryKeyValue(src);
            // update existing entity
            if (dstMap.containsKey(pk)) {
                Object dst = dstMap.get(pk);
                descriptorUtils.copyProperties(src, dst, annotation.mappingTypes());
                result.add(dst);
            } else {
                result.add(repository.save(src));
            }
        }
        return result;
    }

    private static Object getFieldValue(Field field) {
        try {
            return field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return null;
        }
    }

}
