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
package ru.ilb.common.jpa.history;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicHelper;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.dynamic.JPADynamicTypeBuilder;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.sequencing.NativeSequence;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.tools.schemaframework.DefaultTableGenerator;
import org.eclipse.persistence.tools.schemaframework.DynamicSchemaManager;
import org.eclipse.persistence.tools.schemaframework.IndexDefinition;
import org.eclipse.persistence.tools.schemaframework.TableCreator;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Automate history dynamic entities creation
 *
 * @author slavb
 */
public class AutoHistoryEntityUtil {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AutoHistoryEntityUtil.class);

    private static final String SEQ_GEN_HIST_IDENTITY = "SEQ_GEN_HIST_IDENTITY";

    Session session;

//    public void setEntityManager(EntityManager entityManager) {
//        session = entityManager.unwrap(Session.class);
//    }
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        session = JpaHelper.getServerSession(entityManagerFactory);
    }

    @PostConstruct
    @Transactional
    void initialize() {
        //Session session = entityManager.unwrap(Session.class);
        List<ClassDescriptor> descriptors = session.getDescriptors().values().stream()
                .filter(d -> AnnotationUtils.getAnnotation(d.getJavaClass(), AutoHistory.class) != null)
                .collect(Collectors.toList());

        createHistoryEntities((DatabaseSession) session, descriptors);
    }

    /**
     * Create dynamic history entities
     *
     * @param session
     * @param descriptors
     */
    void createHistoryEntities(DatabaseSession session, Collection<ClassDescriptor> descriptors) {
        DynamicClassLoader dcl = new DynamicClassLoader(DatabaseSession.class.getClassLoader());
        List<DynamicType> types = descriptors.stream()
                .map(descriptor -> createHistoryType(descriptor, dcl))
                .collect(Collectors.toList());
        if (!types.isEmpty()) {
            boolean createMissingTables = false;
            boolean generateFKConstraints = false;
            NativeSequence sequence = new NativeSequence(SEQ_GEN_HIST_IDENTITY, true);
            session.getDatasourcePlatform().addSequence(sequence);

            addTypes(session, createMissingTables, generateFKConstraints, types.toArray(new DynamicType[0]));
            createTables(session, generateFKConstraints);
        }

    }

    /**
     * Adds dynamic types to database session
     *
     * @param session
     * @param createMissingTables
     * @param generateFKConstraints
     * @param types
     */
    private void addTypes(DatabaseSession session, boolean createMissingTables, boolean generateFKConstraints, DynamicType... types) {
        DynamicHelper helper = new DynamicHelper(session); // JPADynamicHelper
        helper.addTypes(createMissingTables, generateFKConstraints, types);
        // From JPADynamicHelper
        for (DynamicType type : types) {
            type.getDescriptor().getQueryManager().checkDatabaseForDoesExist();
        }

    }

    /**
     * create tables with create-or-extend mode
     *
     * @param session
     * @param generateFKConstraints
     */
    private void createTables(DatabaseSession session, boolean generateFKConstraints) {
        if (!session.isConnected()) {
            session.login();
        }

        DynamicSchemaManager dsm = new DynamicSchemaManager(session);

        TableCreator creator = new DefaultTableGenerator(session.getProject(), generateFKConstraints).generateFilteredDefaultTableCreator((AbstractSession) session);
        creator.setIgnoreDatabaseException(true);
        creator.extendTables((DatabaseSession) session, dsm);

    }

    /**
     * Create history dynamic type based on ClassDescriptor
     * Adds HISTID, ROWSTART, ROWEND columns, indexes, and copies all direct and *ToOne mappings from base entity
     *
     * @param descriptor
     * @param dcl
     * @return
     */
    private DynamicType createHistoryType(ClassDescriptor descriptor, DynamicClassLoader dcl) {
        //String packagePrefix = packageName.endsWith(".") ? packageName : packageName + ".";

        //descriptor.get
        Class<?> employeeClass = dcl.createDynamicClass(descriptor.getJavaClassName() + "Hist");

        //Vector<DatabaseField> f = descriptor.getAllFields();
        String tableName = descriptor.getTableName() + "HIST";
        JPADynamicTypeBuilder builder = new JPADynamicTypeBuilder(employeeClass, null, tableName);

        builder.setPrimaryKeyFields("HISTID");

        builder.addDirectMapping("histId", Long.class, "HISTID");

        IndexDefinition idxId = new IndexDefinition();
        idxId.setTargetTable(tableName);
        idxId.setName("INDEX_" + tableName + "_ID");
        idxId.addField("ID");
        builder.getType().getDescriptor().getTables().get(0).getIndexes().add(idxId);

        builder.configureSequencing(SEQ_GEN_HIST_IDENTITY, "HISTID");
        //builder.getType().getDescriptor().setSequence(sequence);
        builder.addDirectMapping("rowStart", java.sql.Timestamp.class, "ROWSTART"); // TODO LocalDateTime.class
        builder.addDirectMapping("rowEnd", java.sql.Timestamp.class, "ROWEND");
        IndexDefinition idxRowStart = new IndexDefinition();
        idxRowStart.setTargetTable(tableName);
        idxRowStart.setName("INDEX_" + tableName + "_ROWSTART");
        idxRowStart.addField("ROWSTART");
        builder.getType().getDescriptor().getTables().get(0).getIndexes().add(idxRowStart);
        IndexDefinition idxRowEnd = new IndexDefinition();
        idxRowEnd.setTargetTable(tableName);
        idxRowEnd.setName("INDEX_" + tableName + "_ROWEND");

        idxRowEnd.addField("ROWEND");
        builder.getType().getDescriptor().getTables().get(0).getIndexes().add(idxRowEnd);
        //добавляем поле DTYPE, для существующих и будущих потомков
        List<DatabaseField> fields = descriptor.getFields();
        Set<String> fieldNames = new HashSet();
        fields.stream().forEach(f -> fieldNames.add(f.getName()));
        List<DatabaseMapping> mappings = descriptor.getMappings().stream()
                .map(m -> convertMapping(m))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        mappings.forEach(m -> {
            if (DirectToFieldMapping.class.equals(m.getClass())) {
                LOG.debug("Table " + tableName + " adding DirectToFieldMapping " + m.getAttributeName());
                //builder.addDirectMapping(m.getAttributeName(), m.getAttributeClassification(), m.getField().getName());
                builder.addMapping(m);
                fieldNames.remove(m.getField().getName());
            } else {
                if (OneToOneMapping.class.equals(m.getClass())) {
                    OneToOneMapping oom = (OneToOneMapping) m;
                    oom.getSourceToTargetKeyFields().entrySet().stream().filter(entr -> fieldNames.contains(entr.getKey().getName())).forEach(entr -> fieldNames.remove(entr.getKey().getName()));
                }
                LOG.debug("Table " + tableName + " adding OneToOneMapping " + m.getAttributeName());
                builder.addMapping(m);
            }
        });

        if (!fieldNames.isEmpty()) {
            fields.stream().filter(df -> fieldNames.contains(df.getName())).forEachOrdered(df -> {
                //каждому DTYPE устанавливаем его тип
                builder.addDirectMapping(df.getName().toLowerCase(), df.getType(), df.getName());
            });
        }
        return builder.getType();
    }

    /**
     * Converts DatabaseMapping
     *
     * @param src
     * @return
     */
    private DatabaseMapping convertMapping(DatabaseMapping src) {
        if (src instanceof DirectToFieldMapping) {
            return convertMapping((DirectToFieldMapping) src);
        } else if (src instanceof OneToOneMapping) {
            return convertMapping((OneToOneMapping) src);
        } else {
            return null;
        }

    }

    /**
     * Converts DirectToFieldMapping
     *
     * @param src
     * @return
     */
    private DatabaseMapping convertMapping(DirectToFieldMapping src) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();
        if (java.time.LocalDate.class.equals(src.getAttributeClassification())) {
            mapping.setAttributeClassification(java.sql.Date.class);
        } else if (java.time.LocalDateTime.class.equals(src.getAttributeClassification())) {
            mapping.setAttributeClassification(java.sql.Timestamp.class);
        } else if (java.util.UUID.class.equals(src.getAttributeClassification())) {
            mapping.setAttributeClassification(java.sql.Blob.class);
        } else {
            mapping.setAttributeClassification(src.getAttributeClassification());
        }
        mapping.setAttributeName(src.getAttributeName());
        mapping.setFieldName(src.getField().getName());
        return mapping;

    }

    /**
     * Converts *ToOne mapping
     *
     * @param src
     * @return
     */
    private DatabaseMapping convertMapping(OneToOneMapping src) {
        OneToOneMapping mapping = new OneToOneMapping();
        mapping.setAttributeName(src.getAttributeName());
        mapping.setReferenceClass(src.getReferenceClass());

        src.getSourceToTargetKeyFields().entrySet().stream().forEach(e
                -> mapping.addForeignKeyFieldName(e.getKey().getName(), e.getValue().getName())
        );
        return mapping;
    }

}
