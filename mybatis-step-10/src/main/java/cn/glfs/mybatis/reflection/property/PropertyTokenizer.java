package cn.glfs.mybatis.reflection.property;

import java.util.Iterator;

/**
 * 分词器：解析属性表达式的工具类
 * 实现这俩接口可以提供对其属性的迭代和遍历功能，使得代码能够更加紧凑和易读。
 */
public class PropertyTokenizer implements Iterable<PropertyTokenizer>, Iterator<PropertyTokenizer> {

    // 例子：班级[0].学生.成绩

    // 班级
    private String name;
    // 班级[0]
    private String indexedName;
    // 0
    private String index;
    // 学生.成绩
    private String children;
    public PropertyTokenizer(String fullname){
        // 班级[0].学生.成绩
        // 找这个点 .
        int delim = fullname.indexOf('.');
        if(delim > -1){
            name = fullname.substring(0,delim);
            children = fullname.substring(delim+1);
        }else {
            //找不到.的话,取全部出来
            name = fullname;
            children = null;
        }
        indexedName = name;
        // 把中括号中的数字给解析出来
        delim = name.indexOf('[');
        if (delim > -1) {
            index = name.substring(delim+1,name.length() - 1);
            name = name.substring(0,delim);
        }
    }
    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getIndexedName() {
        return indexedName;
    }

    public String getChildren() {
        return children;
    }


    @Override
    public Iterator<PropertyTokenizer> iterator() {
        return this;
    }

    /**
     * 查看是否有children
     * @return
     */
    @Override
    public boolean hasNext() {
        return children != null;
    }
    // 取得下一个,非常简单，直接再通过儿子来new另外一个实例
    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }

}
