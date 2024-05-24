package cn.glfs.mybatis.executor.resultset;


import cn.glfs.mybatis.executor.Executor;
import cn.glfs.mybatis.mapping.BoundSql;
import cn.glfs.mybatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认Map结果处理器
 */
public class DefaultResultSetHandler implements ResultSetHandler {

    private final BoundSql boundSql;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement,BoundSql boundSql){
        this.boundSql = boundSql;
    }

    /**
     * 结果处理调用
     */
    @Override
    public <E> List<E> handleResultSets(Statement statement) throws SQLException {
        ResultSet resultSet = statement.getResultSet();
        try {
            return resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    private <T> List<T> resultSet2Obj(ResultSet resultSet,Class<?> clazz){
        List<T> list = new ArrayList<>();
        try {
            // 获取结果集的元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            // 获取结果集的列数
            int columnCount = metaData.getColumnCount();
            // 每次遍历行值
            while (resultSet.next()){
                T obj = (T) clazz.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set"+ columnName.substring(0,1).toUpperCase()+ columnName.substring(1);
                    Method method;
                    if(value instanceof Timestamp){
                        method = clazz.getMethod(setMethod,Date.class);
                    }else {
                        method = clazz.getMethod(setMethod,value.getClass());
                    }
                    // 调用 setter 方法，将值设置到对象中
                    method.invoke(obj,value);
                }
                list.add(obj);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
