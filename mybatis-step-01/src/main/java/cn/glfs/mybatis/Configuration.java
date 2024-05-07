package cn.glfs.mybatis;

import java.sql.Connection;
import java.util.Map;

/**
 * 配置项
 */
public class Configuration {
    /** 连接对象 */
    protected Connection connection;
    /** 数据源，这里猜测有多个数据源 */
    protected Map<String,String> dataSource;
    /** KEY:SQL语句命名空间+id，VALUE:SQL属性 */
    protected Map<String,XNode> mapperElement;
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }
    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }
}
