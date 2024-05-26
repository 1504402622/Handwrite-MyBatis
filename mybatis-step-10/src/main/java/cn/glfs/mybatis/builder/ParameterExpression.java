package cn.glfs.mybatis.builder;

import java.util.HashMap;

/**
 * 参数表达式 #{}里面可能是表达式，也可能是属性需要处理
 * 处理表达式： #{substring(name, 1, 3)}
 * 处理属性：#{property,javaType=int,jdbcType=NUMERIC}或#{property:VARCHAR}
 *
 * 属性处理例子：
 * ParameterExpression parameterExpression = new ParameterExpression();
 * String expression = "#{username, jdbcType=VARCHAR}";
 * ParameterExpression.ParameterMapping mapping = parameterExpression.parse(expression);
 *
 * String property = mapping.getProperty(); // 获取属性名，这里是 "username"
 * String jdbcType = mapping.getJdbcType(); // 获取 JDBC 类型，这里是 "VARCHAR"
 */
public class ParameterExpression extends HashMap<String, String> {

    private static final long serialVersionUID = -2417552199605158680L;

    public ParameterExpression(String expression) {
        parse(expression);
    }

    /**
     * 解析参数表达式
     * @param expression 参数表达式字符串
     */
    private void parse(String expression) {
        // #{property,javaType=int,jdbcType=NUMERIC}
        // 首先去除空白,返回的p是第一个不是空白的字符位置
        int p = skipWS(expression, 0);
        if (expression.charAt(p) == '(') {
            //处理表达式
            expression(expression, p + 1);
        } else {
            //处理属性
            property(expression, p);
        }
    }

    /**
     * 处理参数表达式中的子表达式
     * @param expression 参数表达式字符串
     * @param left 子表达式的起始位置
     */
    private void expression(String expression, int left) {
        int match = 1;
        int right = left + 1;
        while (match > 0) {
            if (expression.charAt(right) == ')') {
                match--;
            } else if (expression.charAt(right) == '(') {
                match++;
            }
            right++;
        }
        put("expression", expression.substring(left, right - 1));
        jdbcTypeOpt(expression, right);
    }

    /**
     * 处理属性
     * @param expression 参数表达式字符串
     * @param left 属性的起始位置
     */
    private void property(String expression, int left) {
        // #{property,javaType=int,jdbcType=NUMERIC}
        // property:VARCHAR
        if (left < expression.length()) {
            //首先，得到逗号或者冒号之前的字符串，加入到property，如果找不到就全加入到property
            int right = skipUntil(expression, left, ",:");
            put("property", trimmedStr(expression, left, right));
            // 第二，处理javaType，jdbcType
            jdbcTypeOpt(expression, right);
        }
    }

    /**
     * 查找并返回第一个非空白字符位置
     * @param expression 要查找的字符串
     * @param p 起始位置
     * @return 第一个非空白字符的位置
     */
    private int skipWS(String expression, int p) {
        for (int i = p; i < expression.length(); i++) {
            // 获取字符串中索引为 i 的字符，并将其与十六进制值 0x20（相当于十进制的32，即空格字符的ASCII码值）进行比较。如果当前字符大于空格字符（即非空白字符），则返回当前位置 i，表示找到了第一个非空白字符的位置。
            if (expression.charAt(i) > 0x20) {
                return i;
            }
        }
        return expression.length();
    }


    /**
     * 查找并返回第一个在 endChars 字符串中出现的字符的位置
     * @param expression 要查找的字符串
     * @param p 起始位置
     * @param endChars 结束字符集合
     * @return 第一个在 endChars 字符串中出现的字符的位置
     */
    private int skipUntil(String expression, int p, final String endChars) {
        for (int i = p; i < expression.length(); i++) {
            char c = expression.charAt(i);
            // 字符是否在enchars出现，出现返回索引值，不出现返回-1
            if (endChars.indexOf(c) > -1) {
                return i;
            }
        }
        return expression.length();
    }

    /**
     * 处理参数表达式中的 JdbcType 选项
     * @param expression 参数表达式字符串
     * @param p JdbcType 选项的起始位置
     */
    private void jdbcTypeOpt(String expression, int p) {
        // #{property,javaType=int,jdbcType=NUMERIC}
        // property:VARCHAR
        // 首先去除空白,返回的p是第一个不是空白的字符位置
        p = skipWS(expression, p);
        if (p < expression.length()) {
            //第一个property解析完有两种情况，逗号和冒号
            if (expression.charAt(p) == ':') {
                jdbcType(expression, p + 1);
            } else if (expression.charAt(p) == ',') {
                option(expression, p + 1);
            } else {
                throw new RuntimeException("Parsing error in {" + new String(expression) + "} in position " + p);
            }
        }
    }


    /**
     * 处理 JdbcType
     * @param expression 参数表达式字符串
     * @param p JdbcType 的起始位置
     */
    private void jdbcType(String expression, int p) {
        // property:VARCHAR
        int left = skipWS(expression, p);
        int right = skipUntil(expression, left, ",");
        if (right > left) {
            put("jdbcType", trimmedStr(expression, left, right));
        } else {
            throw new RuntimeException("Parsing error in {" + new String(expression) + "} in position " + p);
        }
        option(expression, right + 1);
    }

    /**
     * 处理属性中的选项
     * @param expression 参数表达式字符串
     * @param p 选项的起始位置
     */
    private void option(String expression, int p) {
        // #{property,javaType=int,jdbcType=NUMERIC}
        int left = skipWS(expression, p);
        if (left < expression.length()) {
            int right = skipUntil(expression, left, "=");
            String name = trimmedStr(expression, left, right);
            left = right + 1;
            right = skipUntil(expression, left, ",");
            String value = trimmedStr(expression, left, right);
            put(name, value);
            // 递归调用option，进行逗号后面一个属性的解析
            option(expression, right + 1);
        }
    }

    /**
     * 去除字符串的前导和尾随空白字符
     * @param str 要处理的字符串
     * @param start 起始位置
     * @param end 结束位置
     * @return 去除前导和尾随空白字符后的结果字符串
     */
    private String trimmedStr(String str, int start, int end) {

        while (str.charAt(start) <= 0x20) {
            start++;
        }
        while (str.charAt(end - 1) <= 0x20) {
            end--;
        }
        return start >= end ? "" : str.substring(start, end);
    }

}
