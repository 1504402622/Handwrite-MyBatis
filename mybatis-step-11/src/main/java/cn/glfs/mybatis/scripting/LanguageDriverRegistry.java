package cn.glfs.mybatis.scripting;


import java.util.HashMap;
import java.util.Map;

/**
 * 脚本语言注册器
 */
public class LanguageDriverRegistry {

    // map,类对象:实例对象
    private final Map<Class<?>,LanguageDriver> LANGUAGE_DRIVER_MAP =  new HashMap<Class<?>, LanguageDriver>();

    private Class<?> defaultDriverClass = null;

    public void register(Class<?> cls){
        if(cls == null){
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        // cls是不是这个接口的实现类或者类的子类
        if(!LanguageDriver.class.isAssignableFrom(cls)){
            throw new RuntimeException(cls.getName() + " does not implements " + LanguageDriver.class.getName());
        }
        // 如果没注册过，再去注册
        LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(cls);
        if (driver == null) {
            try {
                driver = (LanguageDriver) cls.newInstance();
                LANGUAGE_DRIVER_MAP.put(cls, driver);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public LanguageDriver getDriver(Class<?> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public Class<?> getDefaultDriverClass() {
        return defaultDriverClass;
    }


    //Configuration()有调用，默认的为XMLLanguageDriver
    public void setDefaultDriverClass(Class<?> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }
}
