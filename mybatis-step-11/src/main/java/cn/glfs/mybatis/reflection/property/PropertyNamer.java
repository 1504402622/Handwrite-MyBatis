package cn.glfs.mybatis.reflection.property;

import java.util.Locale;

/**
 * 属性命名器
 */
public class PropertyNamer {
    private PropertyNamer(){

    }

    /**
     * 将getter、setter转换为属性名。
     * 如果属性名长度为1，则将其转换为小写；如果长度大于1，则将第二个字符转换为小写。
     * 在某些情况下，属性名可能是一个布尔类型的属性，例如"boolean isActive()"，这时命名约定可能就会变成以"is"开头。
     */
    public static String methodToProperty(String name){
        // 获取前缀后的方法名
        if(name.startsWith("is")){
            name = name.substring(2);
        }else if (name.startsWith("get")||name.startsWith("set")){
            name = name.substring(3);
        }else {
            throw new RuntimeException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
        }

        // 如果属性名只有一个字符，或者属性名的第二个字符不是大写字母，则将属性名的第一个字符转换为小写
        if(name.length() == 1||(name.length()>1)&&!Character.isUpperCase(name.charAt(1))){
            name = name.substring(0,1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }
        return name;
    }

    /**
     * 开头判断get/set/is
     */
    public static boolean isProperty(String name) {
        return name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
    }

    /**
     * 是否为 getter
     */
    public static boolean isGetter(String name) {
        return name.startsWith("get") || name.startsWith("is");
    }

    /**
     * 是否为 setter
     */
    public static boolean isSetter(String name) {
        return name.startsWith("set");
    }
}
