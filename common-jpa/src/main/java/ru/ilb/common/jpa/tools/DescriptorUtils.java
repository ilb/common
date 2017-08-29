/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.tools;

import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sessions.Session;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 *
 * @author slavb
 */
public class DescriptorUtils {

    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    /**
     * Method fixes all one-to-many and one-to-one fields with mapped-by property
     * by filling owning side reference
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

}
