/*
 * Copyright 2016 Bystrobank.
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
package ru.ilb.common.jpa.search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import org.apache.cxf.jaxrs.ext.search.jpa.JPATypedQueryVisitor;

/**
 *
 * @author slavb
 * @param <T>
 */
public class JPAOrderedQueryVisitor<T> extends JPATypedQueryVisitor<T> {

    private final EntityManager em;
    private final Class<T> tClass;

    public JPAOrderedQueryVisitor(EntityManager em, Class<T> tClass) {
        super(em, tClass);
        this.em = em;
        this.tClass = tClass;
    }

    public CriteriaQuery<T> getOrderedCriteriaQuery(JPATypedQueryVisitor<T> visitor, String orderList) {
        String[] orderParts = orderList.split(",");
        List<Order> orders = new ArrayList<>();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        EntityType entity = em.getMetamodel().entity(tClass);
        for (String orderPart : orderParts) {
            boolean asc = true;
            String[] sort = orderPart.split(" ");
            if (sort.length > 1) {
                asc = !sort[1].equals("desc");
            }

            String[] subSort = sort[0].split("\\.");
            if (subSort.length > 1) {
                for (Join<T, ?> join : visitor.getRoot().getJoins()) {
                    if (join.getAttribute().getName().equals(subSort[0])) {
                        Order order = asc ? cb.asc(join.get(subSort[1])) : cb.desc(join.get(subSort[1]));
                        orders.add(order);
                    }
                }
            } else {
                List<SingularAttribute<T, ?>> selections = new LinkedList<>();
                selections.add(entity.getSingularAttribute(sort[0]));
                for (SingularAttribute<T, ?> attribute : selections) {
                    Path<?> selection = visitor.getRoot().get(attribute);
                    Order order = asc ? cb.asc(selection) : cb.desc(selection);
                    orders.add(order);
                }
            }
        }
        return visitor.getCriteriaQuery().orderBy(orders);
    }

}
