/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.common.base;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractEncryptJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static EncryptDataSource encryptDataSource;
    
    private static final List<String> ENCRYPT_DB_NAMES = Collections.singletonList("encrypt");
    
    @BeforeClass
    public static void initEncryptDataSource() {
        if (null != encryptDataSource) {
            return;
        }
        Map<String, DataSource> dataSources = getDataSources();
        Properties props = new Properties();
        props.put(ShardingPropertiesConstant.SQL_SHOW.getKey(),true);
        encryptDataSource = new EncryptDataSource(dataSources.values().iterator().next(), createEncryptRuleConfiguration(), props);
    }
    
    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), new Predicate<String>() {
            
            @Override
            public boolean apply(final String input) {
                return ENCRYPT_DB_NAMES.contains(input);
            }
        });
    }
    
    private static EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("test", "t_encrypt.pwd", new Properties());
        EncryptorRuleConfiguration encryptorQueryConfig = new EncryptorRuleConfiguration("assistedTest", "t_query_encrypt.pwd", "t_query_encrypt.assist_pwd", new Properties());
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getEncryptorRuleConfigs().put("test", encryptorConfig);
        result.getEncryptorRuleConfigs().put("assistedTest", encryptorQueryConfig);
        return result;
    }
    
    @Before
    public void initTable() {
        try {
            EncryptConnection conn = encryptDataSource.getConnection();
            RunScript.execute(conn, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("encrypt_data.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected final EncryptDataSource getEncryptDataSource() {
        return encryptDataSource;
    }
    
    @AfterClass
    public static void close() {
        if (encryptDataSource == null) {
            return;
        }
        encryptDataSource.close();
        encryptDataSource = null;
    }
}
