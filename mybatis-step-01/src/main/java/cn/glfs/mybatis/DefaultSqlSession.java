package cn.glfs.mybatis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 默认会话实现类
 */
public class DefaultSqlSession implements SqlSession{

    private Connection connection;
    private Map<String, XNode> mapperElement;

    public DefaultSqlSession(Connection connection,Map<String,XNode> mapperElement){
        this.connection = connection;
        this.mapperElement = mapperElement;
    }

    @Override
    public <T> T selectOne(String statement) {
        try {
            XNode xNode = mapperElement.get(statement);
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
            return objects.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        XNode xNode = mapperElement.get(statement);
        Map<Integer, String> parameterMap = xNode.getParameter();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            buildParameter(preparedStatement, parameter, parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
            return objects.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public <T> List<T> selectList(String statement) {
        XNode xNode = mapperElement.get(statement);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        XNode xNode = mapperElement.get(statement);
        Map<Integer, String> parameterMap = xNode.getParameter();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            buildParameter(preparedStatement, parameter, parameterMap);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用于构建参数并为预编译的SQL语句赋予参数值。
     *
     * @param preparedStatement 预编译的SQL语句
     * @param parameter 参数值
     * @param parameterMap 参数映射关系，将预编译语句的参数位置与对象字段的对应关系存储在Map中
     * @throws SQLException 如果SQL操作发生错误
     * @throws IllegalAccessException 如果无法访问字段或方法
     */
    private void buildParameter(PreparedStatement preparedStatement, Object parameter, Map<Integer, String> parameterMap) throws SQLException, IllegalAccessException {
        //1.如果传入是一般类型进行字段映射放到执行语句中
        int size = parameterMap.size();
        // 如果参数是 Long 类型
        if (parameter instanceof Long) {
            // 循环设置预编译语句的参数值
            for (int i = 1; i <= size; i++) {
                preparedStatement.setLong(i, Long.parseLong(parameter.toString()));
            }
            return;
        }
        // 如果参数是 Integer 类型
        if (parameter instanceof Integer) {
            // 循环设置预编译语句的参数值
            for (int i = 1; i <= size; i++) {
                preparedStatement.setInt(i, Integer.parseInt(parameter.toString()));
            }
            return;
        }
        // 如果参数是 String 类型
        if (parameter instanceof String) {
            // 循环设置预编译语句的参数值
            for (int i = 1; i <= size; i++) {
                preparedStatement.setString(i, parameter.toString());
            }
            return;
        }


        //2.如果传入是对象进行字段映射放到执行语句中
        // 创建一个用于存储对象字段的映射关系的Map
        Map<String, Object> fieldMap = new HashMap<>();
        // 获取参数对象的所有字段
        Field[] declaredFields = parameter.getClass().getDeclaredFields();
        // 遍历所有字段
        for (Field field : declaredFields) {
            // 获取字段名
            String name = field.getName();
            // 设置字段可以访问
            field.setAccessible(true);
            // 获取字段值
            Object obj = field.get(parameter);
            // 恢复字段访问权限
            field.setAccessible(false);
            // 将字段名和字段值存储到映射关系中
            fieldMap.put(name, obj);
        }
        for (int i = 1; i <= size; i++) {
            // 获取参数映射关系中指定位置的字段名
            String parameterDefine = parameterMap.get(i);
            // 根据字段名获取对象字段的值
            Object obj = fieldMap.get(parameterDefine);
            // 如果字段值是 Short 类型
            if (obj instanceof Short) {
                // 设置预编译语句的参数值
                preparedStatement.setShort(i, Short.parseShort(obj.toString()));
                continue;
            }
            // 如果字段值是 Integer 类型
            if (obj instanceof Integer) {
                // 设置预编译语句的参数值
                preparedStatement.setInt(i, Integer.parseInt(obj.toString()));
                continue;
            }
            // 如果字段值是 Long 类型
            if (obj instanceof Long) {
                // 设置预编译语句的参数值
                preparedStatement.setLong(i, Long.parseLong(obj.toString()));
                continue;
            }
            // 如果字段值是 String 类型
            if (obj instanceof String) {
                // 设置预编译语句的参数值
                preparedStatement.setString(i, obj.toString());
                continue;
            }
            // 如果字段值是 Date 类型
            if (obj instanceof Date) {
                // 设置预编译语句的参数值
                preparedStatement.setDate(i, (java.sql.Date) obj);
            }
        }
    }


        /**
         * 将ResultSet转换为Java对象的List集合（ResultSet对象提供了一系列的方法来获取查询结果的数据，如获取某一列的值、移动游标到下一行、判断是否还有下一行等。）
         * @param resultSet 数据库查询结果的ResultSet对象
         * @param clazz 要转换的Java对象的Class对象
         * @param <T> 泛型类型，代表要转换的Java对象类型
         * @return 转换后的包含Java对象的List集合
         */
        private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
            List<T> list = new ArrayList<>(); // 用于存储转换后的Java对象
            try {
                ResultSetMetaData metaData = resultSet.getMetaData(); // 获取结果集的元数据
                int columnCount = metaData.getColumnCount(); // 获取结果集的列数
                // 每次遍历行值
                while (resultSet.next()) {
                    T obj = (T) clazz.newInstance(); // 实例化要转换的Java对象
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = resultSet.getObject(i); // 获取当前列的值
                        String columnName = metaData.getColumnName(i); // 获取当前列的名称
                        String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1); // 构造setter方法名
                        Method method;
                        // 根据值的类型获取相应的setter方法
                        if (value instanceof Timestamp) {
                            method = clazz.getMethod(setMethod, Date.class); // 如果值是Timestamp类型，则获取对应的Date类型setter方法
                        } else {
                            method = clazz.getMethod(setMethod, value.getClass()); // 否则获取对应类型的setter方法
                        }
                        // 使用反射调用setter方法，设置属性值
                        method.invoke(obj, value);
                    }
                    list.add(obj); // 将填充完数据的Java对象添加到List集合中
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list; // 返回填充好数据的List集合
        }


    @Override
    public void close() {
        if (null == connection) return;
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
