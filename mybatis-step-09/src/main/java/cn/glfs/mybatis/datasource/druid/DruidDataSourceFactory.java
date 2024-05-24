package cn.glfs.mybatis.datasource.druid;


import cn.glfs.mybatis.datasource.DataSourceFactory;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 数据源工厂
 */
public class DruidDataSourceFactory implements DataSourceFactory {
    private Properties props;
    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public DataSource getDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(props.getProperty("driver"));
        druidDataSource.setUrl(props.getProperty("url"));
        druidDataSource.setUsername(props.getProperty("username"));
        druidDataSource.setPassword(props.getProperty("password"));
        return druidDataSource;
    }
}
