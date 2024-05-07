package cn.glfs.mybatis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;



/**
 * 通过类加载器获得resource的辅助类
 */
public class Resources {

    /**
     * 使用 InputStreamReader 将输入流转换为 Reader 对象。将字节流转换为字符流,方便以字符形式读取资源内容
     * @param resource
     * @return
     * @throws IOException
     */
    public static Reader getResourceAsReader(String resource) throws IOException {
        //调用了另一个静态方法 getResourceAsStream 来获取资源的输入流,资源路径可以是文件地址，也可以是类路径下的相对路径
        return new InputStreamReader(getResourceAsStream(resource));
    }

    /**
     * 从不同的类加载器中获取指定资源的输入流
     * @param resource
     * @return
     * @throws IOException
     */
    private static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader[] classLoaders = getClassLoaders();
        for (ClassLoader classLoader : classLoaders) {
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if (null != inputStream) {
                return inputStream;
            }
        }
        throw new IOException("Could not find resource " + resource);
    }


    /**
     * 返回系统类加载器和上下文类加载器
     * 系统类加载器：Java运行时环境默认的类加载器，负责加载Java类路径（classpath）中指定的类文件，用于加载应用程序自身的类以及依赖库中的类
     * 上下文类加载器：每个线程都有一个上下文类加载器，常用于在某些特殊情况下由线程代码加载类和资源。
     * @return
     */
    private static ClassLoader[] getClassLoaders() {
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()};
    }
}
