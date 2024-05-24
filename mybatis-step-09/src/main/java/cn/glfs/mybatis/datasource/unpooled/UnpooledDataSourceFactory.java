package cn.glfs.mybatis.datasource.unpooled;


import cn.glfs.mybatis.datasource.DataSourceFactory;
import cn.glfs.mybatis.reflection.MetaObject;
import cn.glfs.mybatis.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 无池化数据源工厂
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {
    protected DataSource dataSource;
    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }
    @Override
    public DataSource getDataSource() {
        // 返回dataSource对象
        return dataSource;
    }
    @Override
    public void setProperties(Properties props) {
        // 通过dataSource对象获取其对应的MetaObject元对象，也就是BeanMapper，进而获取MetaClass，再使用反射器去得到该数据源的反射填充
        MetaObject metaObject = SystemMetaObject.forObject(dataSource);
        // 遍历配置属性集合
        for (Object key : props.keySet()) {
            // 获取属性名
            String propertyName = (String) key;
            // 检查是否存在对应的setter方法
            if (metaObject.hasSetter(propertyName)) {
                // 获取属性值
                String value = (String) props.get(propertyName);
                // 将属性值转换为对应的类型
                Object convertedValue = convertValue(metaObject, propertyName, value);
                // 设置属性值到dataSource对象中
                metaObject.setValue(propertyName, convertedValue);
            }
        }
    }



    /**
     * 根据setter的类型,将配置文件中的值强转成相应的类型
     */
    private Object convertValue(MetaObject metaObject, String propertyName, String value) {
        // 初始转换后的值为字符串值
        Object convertedValue = value;
        // 获取setter方法参数的类型
        Class<?> targetType = metaObject.getSetterType(propertyName);
        // 根据setter的类型进行相应的类型转换
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        // 返回转换后的值
        return convertedValue;
    }
}
