package com.qcloud.scf.spring.boot.autoconfig;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@ConditionalOnClass(DruidDataSource.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class DruidDataSourceAutoConfigure implements BeanFactoryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DruidDataSourceAutoConfigure.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        try {
            registerDataSources(configurableListableBeanFactory);
        } catch (SQLException e) {
            LOGGER.error("Cannot register data source", e);
        }
    }

    private void registerDataSources(ConfigurableListableBeanFactory configurableListableBeanFactory) throws SQLException {
        String dbList = System.getenv("DB_LIST");
        if (dbList == null) {
            return;
        }

        String[] databases = dbList.split(",");
        for (String database : databases) {
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setUrl(System.getenv("DB_" + database + "_URL"));
            druidDataSource.setUsername(System.getenv("DB_" + database + "_USER"));
            druidDataSource.setPassword(System.getenv("DB_" + database + "_PASSWORD"));
            druidDataSource.setMaxActive(1);
            druidDataSource.init();
            configurableListableBeanFactory.registerSingleton(database, druidDataSource);
        }
    }
}
