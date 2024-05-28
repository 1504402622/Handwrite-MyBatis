package cn.glfs.mybatis.scripting.xmltags;


public interface SqlNode {
    boolean apply(DynamicContext context);
}