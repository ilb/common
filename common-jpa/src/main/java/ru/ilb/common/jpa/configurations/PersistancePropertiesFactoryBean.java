/*
 * Copyright 2017 muratov_tr.
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
package ru.ilb.common.jpa.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Класс устанавливает JpaPropertyMap в LocalContainerEntityManagerFactoryBean.
 * <p>
 * В bean.xml нужно добавить properties в описание данного бина:
 * <p>
 * 1. {@code DataSource}
 * <pre>{@code
 *     <property name="dataSource" ref="dataSource"/>
 * }</pre>
 * <p>
 * 2. Путь к файлам <i>model.jpa</i>, <i>data.sql</i> (если в проекте файл не
 * используется, то его не прописываем)
 * <pre>{@code
 *      <property name="model" value="META-INF/model.jpa"/>
 *      <property name="data" value="META-INF/sql/data.sql"/>
 * }</pre>
 * <p>
 * После этого данный <tt>bean</tt> прописать как <tt>property</tt> в
 * LocalContainerEntityManagerFactoryBean. Должно получится примерно так:
 * <pre>{@code
 *      <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean" >
 *          <property name="persistenceXmlLocation" value="classpath*:META-INF/persistence.xml"></property>
 *          <property name="jpaPropertyMap" ref="jpaPropertyMap"/>
 *      </bean>
 *      <jee:jndi-lookup id="dataSource" jndi-name="jdbc/*наименование вашей БД*"/>
 *      <bean id="jpaPropertyMap" class="ru.ilb.common.jpa.configurations.PersistancePropertiesFactoryBean">
 *          <property name="dataSource" ref="dataSource"/>
 *          <property name="model" value="META-INF/model.jpa"/>
 *          <property name="data" value="META-INF/sql/data.sql"/>
 *      </bean>
 * }</pre>
 * <p>
 * По данным файлам будут устанавливаться соответствующие <tt>JpaProperty</tt>,
 * но только если в этих файлах что-то было изменено. То, что файл был изменен,
 * определяем через чек сумму файла(md5).
 * <p>
 * При этом соответствующие <tt>JpaProperty</tt> должны быть отключены для
 * данной БД. Например, в <i>persistence.xml</i> закомментировать данные строки:
 * <p>
 * Для model:
 * <pre>{@code <property name="javax.persistence.schema-generation.database.action" value="create-or-extend-tables"/>}</pre>
 * <p>
 * Для data:
 * <pre>{@code <property name="javax.persistence.sql-load-script-source" value="META-INF/sql/data.sql"/>}</pre>
 * <p>
 * Это сделано для того чтобы уменьшить время деплоя проекта.
 *
 * @author muratov_tr
 */
public class PersistancePropertiesFactoryBean implements FactoryBean<Map> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PersistancePropertiesFactoryBean.class);

    private String model;
    private String data;
    private DataSource dataSource;

    private Map<String, byte[]> checksumMap;
    private Map<String, Object> propertyMap;

    public void setModel(String model) {
        this.model = model;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Получение контрольной суммы файла
     *
     * @param file - путь до файла, относительно classpath
     * @return
     */
    private byte[] getCheckSum(String file) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(file);
                    DigestInputStream dis = new DigestInputStream(is, md)) {
                byte[] buffer = new byte[1024];
                //CHECKSTYLE:OFF
                while (dis.read(buffer) != -1) {
                }
                //CHECKSTYLE:ON
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
        return md.digest();
    }

    /**
     * Получение чек сумм, сохраненных в БД
     *
     * @return
     */
    private Map fillChecksumMapFromDB() {
        Map<String, byte[]> result = new HashMap<>();
        String createDBQuery = "CREATE TABLE IF NOT EXISTS CHECKSUM ("
                + "FILE VARCHAR(50) NOT NULL, "
                + "CHECKSUM BINARY(16) "
                + ")";
        String selectCheckSum = "SELECT * FROM CHECKSUM";
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createDBQuery); //создаем таблицу если не существует
                ResultSet rs = stmt.executeQuery(selectCheckSum);
                while (rs.next()) {
                    String file = rs.getString("FILE");
                    byte[] checkSum = rs.getBytes("CHECKSUM");
                    result.put(file, checkSum);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    /**
     * Обновление чек сумм в БД
     *
     * @param file
     * @param checkSum
     */
    private void updateCheckSumInDB(String file, byte[] checkSum) {
        String updateQuery = "UPDATE CHECKSUM SET CHECKSUM = ? WHERE FILE = ?";
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setBytes(1, checkSum);
                stmt.setString(2, file);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Вставка новой записи о чек сумме, для нового файла
     *
     * @param file
     * @param checkSum
     */
    private void insertCheckSumInDB(String file, byte[] checkSum) {
        String insertQuery = "INSERT INTO CHECKSUM\n"
                + "(FILE, CHECKSUM) value (?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, file);
                stmt.setBytes(2, checkSum);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Установка проперти для вставки в LocalContainerEntityManagerFactoryBean.
     *
     * @param file
     */
    private void setProperty(String file) {
        if (model != null && model.equals(file)) {
            propertyMap.put("javax.persistence.schema-generation.database.action", "create-or-extend-tables");
        } else if (data != null && data.equals(file)) {
            propertyMap.put("javax.persistence.sql-load-script-source", file);
        }
    }

    @Override
    public Map getObject() throws Exception {
        LOG.info(">>>START set persistence properties");
        if (propertyMap == null) {
            propertyMap = new HashMap<>();
        }
        Map<String, byte[]> checkSumFromDB = fillChecksumMapFromDB();
        Map<String, byte[]> currentCheckSums = new HashMap<>();
        if (model != null) {
            currentCheckSums.put(model, getCheckSum(model));
        }
        if (data != null) {
            currentCheckSums.put(data, getCheckSum(data));
        }
        for (Map.Entry<String, byte[]> pair : currentCheckSums.entrySet()) {
            if (checkSumFromDB.containsKey(pair.getKey())) {
                byte[] oldCheckSum = checkSumFromDB.get(pair.getKey());
                if (!Arrays.equals(pair.getValue(), oldCheckSum)) {
                    setProperty(pair.getKey());
                    updateCheckSumInDB(pair.getKey(), pair.getValue());
                    LOG.info(">>> У файла {} checkSum изменилась, устанавливаем соответствующее property", pair.getKey());
                } else {
                    LOG.info(">>> У файла {} checkSum не изменилась", pair.getKey());
                }
            } else {
                setProperty(pair.getKey());
                insertCheckSumInDB(pair.getKey(), pair.getValue());
            }
        }
        LOG.info(">>>END set persistence properties");
        return this.propertyMap;
    }

    @Override
    public Class<? extends Map> getObjectType() {
        return checksumMap != null ? checksumMap.getClass() : Map.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
