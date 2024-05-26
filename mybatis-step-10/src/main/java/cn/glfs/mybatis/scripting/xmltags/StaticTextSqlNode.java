package cn.glfs.mybatis.scripting.xmltags;

/**
 * 静态文本sql节点
 */
public class StaticTextSqlNode implements SqlNode {
    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }
    @Override
    public boolean apply(DynamicContext context) {
        //将文本加入context
        context.appendSql(text);
        return true;
    }
}
