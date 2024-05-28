package cn.glfs.mybatis.io;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 找到xml资源文件并返回其字符输入流的辅助类
 */
public class Resources {
    /**
     * 将字节输入流转换为字符输入流
     * @param resource
     * @return
     * @throws IOException
     */
    public static Reader getResourceAsReader(String resource) throws IOException{
        return new InputStreamReader(getResourceAsStream(resource));
    }

    /**
     * 通过多个类加载器找到该资源文件输入流，如果能找到就返回，不能就抛异常
     * “可通过输入路径查找xml”
     * @param resource
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(String resource) throws IOException{
        ClassLoader[] classLoaders = getClassLocader();
        for (ClassLoader classLoader : classLoaders) {
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if(null != inputStream){
                return inputStream;
            }
        }
        throw new IOException("Could not find resource " + resource);
    }

    /**
     * 返回一个包含了系统类加载器和当前线程上下文类加载器的数组
     * @return
     */
    private static ClassLoader[] getClassLocader(){
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()
        };
    }

    /**
     * 接收一个类名字符串，返回对应的 Class 对象。它直接调用了 Java 提供的 Class 类的 forName 方法来实现类的动态加载。
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException{
        return Class.forName(className);
    }
}
