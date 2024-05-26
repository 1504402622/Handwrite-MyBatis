package cn.glfs.mybatis.scripting.xmltags;

import java.util.List;

/**
 * 混合SQL节点，这里保有疑点：为什么每次注入都是一个静态sql还要整一个混合sql节点呢？？
 */
public class MixedSqlNode implements SqlNode {

    // 组合模式，拥有一个SqlNode的List
    private List<SqlNode> contents;
    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }
    // 将静态sql都注入到上下文中
    @Override
    public boolean apply(DynamicContext context) {
        // 将节点列表中的所有context注入到动态上下文中
        contents.forEach(node -> node.apply(context));
        return true;
    }
}
