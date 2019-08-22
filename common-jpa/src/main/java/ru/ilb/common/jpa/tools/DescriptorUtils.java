/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author slavb
 */
public class DescriptorUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DescriptorUtils.class);

    @Autowired
    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Method fixes all one-to-many and one-to-one fields with mapped-by
     * property by filling owning side reference
     *
     * @param entity
     */
    public void fixInverseLinks(Object entity) {
        final BeanWrapper src = new BeanWrapperImpl(entity);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        ClassDescriptor cd = entityManager.unwrap(Session.class).getDescriptor(entity);
        if (cd == null) {
            return;
        }
        for (java.beans.PropertyDescriptor pd : pds) {
            DatabaseMapping dm = cd.getMappingForAttributeName(pd.getName());
            if (dm != null) {
                if (dm instanceof OneToManyMapping) {
                    OneToManyMapping dmOtM = (OneToManyMapping) dm;
                    if (dmOtM.getMappedBy() != null) {
                        List srcValue = (List) src.getPropertyValue(pd.getName());
                        if (srcValue != null && EclipseLinkUtils.isInitialized(srcValue)) {
                            for (Object v : srcValue) {
                                final BeanWrapper srcv = new BeanWrapperImpl(v);
                                srcv.setPropertyValue(dmOtM.getMappedBy(), entity);
                                fixInverseLinks(v);
                            }
                        }

                    }
                }
                if (dm instanceof OneToOneMapping) {
                    OneToOneMapping dmOtO = (OneToOneMapping) dm;
                    if (dmOtO.getMappedBy() != null) {
                        Object srcValue = src.getPropertyValue(pd.getName());
                        if (srcValue != null && EclipseLinkUtils.isInitialized(srcValue)) {
                            final BeanWrapper srcv = new BeanWrapperImpl(srcValue);
                            srcv.setPropertyValue(dmOtO.getMappedBy(), entity);
                            fixInverseLinks(srcValue);
                        }

                    }
                }
            }

        }
    }

    /**
     * Transform list of entities to Map PrimaryKey =&gt; Entity
     * @param srcList
     * @return
     */
    public Map<Object, Object> mapById(List<Object> srcList) {
        return srcList.stream().collect(Collectors.toMap(o -> getPrimaryKeyValue(o), o -> o));
    }

    /**
     * Get primary key value of entity
     * @param object
     * @return
     */
    public Object getPrimaryKeyValue(Object object){
        ClassDescriptor cd = entityManager.unwrap(Session.class).getDescriptor(object);
        String pk = cd.getPrimaryKeyFields().iterator().next().getName().toLowerCase(); //FIXME
        return new BeanWrapperImpl(object).getPropertyValue(pk);
    }

    /**
     * Copy src to dst filtered by DatabaseMapping types
     * @param srcObject
     * @param dstObject
     * @param copyMappingTypes
     */
    public void copyProperties(Object srcObject, Object dstObject, Class<? extends DatabaseMapping>[] copyMappingTypes) {
        if (srcObject == dstObject) {
            return;
        }
        final BeanWrapper src = new BeanWrapperImpl(srcObject);
        final BeanWrapper dst = new BeanWrapperImpl(dstObject);
        java.beans.PropertyDescriptor[] pdsSrc = src.getPropertyDescriptors();
        ClassDescriptor cd = entityManager.unwrap(Session.class).getDescriptor(srcObject);

        if (cd == null) {
            return;
        }
        for (java.beans.PropertyDescriptor pd : pdsSrc) {
            DatabaseMapping dm = cd.getMappingForAttributeName(pd.getName());
            if (dm != null) {
                if (copyMappingTypes == null || copyMappingTypes.length == 0||  Stream.of(copyMappingTypes).anyMatch(mt -> mt.isAssignableFrom(dm.getClass()))) {
                    Object srcValue = src.getPropertyValue(pd.getName());
                    LOG.trace("copy {}", pd.getName());
                    dst.setPropertyValue(pd.getName(), srcValue);;
                }
            }
        }
    }

}
