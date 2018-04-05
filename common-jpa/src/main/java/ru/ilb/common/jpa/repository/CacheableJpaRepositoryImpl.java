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
package ru.ilb.common.jpa.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class CacheableJpaRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements CacheableJpaRepository<T, ID> {

    private List<T> cache;

    public CacheableJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public List<T> findAllCache() {
        if (cache == null) {
            cache = findAll();
        }
        return cache;
    }

    @Override
    public void fillCache(List<T> items) {
        cache = new ArrayList<>();
        cache.addAll(items);
    }

}
