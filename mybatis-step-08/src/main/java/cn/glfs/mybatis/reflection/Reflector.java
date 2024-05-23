package cn.glfs.mybatis.reflection;


import cn.glfs.mybatis.reflection.invoker.GetFieldInvoker;
import cn.glfs.mybatis.reflection.invoker.Invoker;
import cn.glfs.mybatis.reflection.invoker.MethodInvoker;
import cn.glfs.mybatis.reflection.invoker.SetFieldInvoker;
import cn.glfs.mybatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射器，获取类的信息，字段Field，getterMethod，setterMethod
 * 主要负责对目标类进行反射解析，包括获取类的属性、方法以及注解等信息，并且它会对这些解析出来的信息进行缓存，以提高性能。
 */
public class Reflector {

    private static boolean classCacheEnabled = true;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    // 线程安全的缓存
    private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();

    // 目标类的类型
    private Class<?> type;
    // 可读属性的名称数组，就是有get方法对应属性名
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
    // 可写属性的名称数组，就是有set方法对应的属性名
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
    // 父类及实现接口方法以字段名称为键，对应的Setter方法的Invoker对象为值 和 本类以字段名为键，字段类型为值 的Map
    private Map<String, Invoker> setMethods = new HashMap<>();
    // 父类及实现接口方法以字段名称为键，对应的Getter方法的Invoker对象为值 和 本类以字段名为键，如果该字段没有setter方法可通过反射直接修改属性的Invoker对象为值 的Map
    private Map<String, Invoker> getMethods = new HashMap<>();
    // 父类及实现接口方法以字段名称为键，对应的Setter方法的参数类型为值 和 本类以字段名为键，如果该字段没有getter方法可通过反射直接获取属性的Invoker对象为值 的Map
    private Map<String, Class<?>> setTypes = new HashMap<>();
    // 父类及实现接口方法以字段名称为键，对应的Getter方法的返回类型为值 和 本类以字段名为键，字段类型为值 的Map
    private Map<String, Class<?>> getTypes = new HashMap<>();
    // 默认空参构造函数
    private Constructor<?> defaultConstructor;
    // 忽略属性名称大小写的属性名称映射表，也就是大写-》小写
    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz) {
        this.type = clazz;
        // 设置默认空参构造器构
        addDefaultConstructor(clazz);
        // 加入 getter
        addGetMethods(clazz);
        // 加入 setter
        addSetMethods(clazz);
        // 加入字段
        addFields(clazz);
        readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
        writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
        for (String propName : readablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for (String propName : writeablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }


    // 添加默认构造函数
    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] consts = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : consts) {
            if (constructor.getParameterTypes().length == 0) {
                if (canAccessPrivateMethods()) {
                    try {
                        constructor.setAccessible(true);
                    } catch (Exception ignore) {
                        // Ignored. This is only a final precaution, nothing we can do
                    }
                }
                if (constructor.isAccessible()) {
                    this.defaultConstructor = constructor;
                }
            }
        }
    }


    // 添加Getter方法
    private void addGetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        // 这里 本身，父类，乃至实现接口的方法，其唯一签名为此 返回值类型#方法名:参数1、参数2、参数3、
        // 冲突问题1：
        // getName和getname违法JavaBeans规范（好像默认不会出现）
        // 冲突问题2：
        // getName() 返回 String 类型的姓名。
        // getName() 返回 Object 类型的姓名，两种getter方法产生冲突，需要Object类型
         Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3) {
                if (method.getParameterTypes().length == 0) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            } else if (name.startsWith("is") && name.length() > 2) {
                if (method.getParameterTypes().length == 0) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
        }
        resolveGetterConflicts(conflictingGetters);
    }

    // 解决Getter方法冲突
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (String propName : conflictingGetters.keySet()) {
            List<Method> getters = conflictingGetters.get(propName);
            Iterator<Method> iterator = getters.iterator();
            Method firstMethod = iterator.next();
            if (getters.size() == 1) {
                addGetMethod(propName, firstMethod);
            } else {
                Method getter = firstMethod;
                Class<?> getterType = firstMethod.getReturnType();
                while (iterator.hasNext()) {
                    Method method = iterator.next();
                    Class<?> methodType = method.getReturnType();
                    // 解决冲突1
                    if (methodType.equals(getterType)) {
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                        // 查看getterType是不是继承或者实现methodType
                    } else if (methodType.isAssignableFrom(getterType)) {
                        // OK getter type is descendant
                    // 解决冲突2
                    } else if (getterType.isAssignableFrom(methodType)) {
                        getter = method;
                        getterType = methodType;
                    } else {
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    }
                }
                addGetMethod(propName, getter);
            }
        }
    }

    // 属性名对应的get方法的调用者，属性名对应get方法的返回值类型，填充getMethods、getTypes
    private void addGetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            getMethods.put(name, new MethodInvoker(method));
            getTypes.put(name, method.getReturnType());
        }
    }


    // 添加Setter方法
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        // 这里 本身，父类，乃至实现接口的方法，其唯一签名为此 返回值类型#方法名:参数1、参数2、参数3、
        // 冲突问题1：
        // setName和setname违法JavaBeans规范（好像默认不会出现）
        // 冲突问题2：
        // getName(String Name) 传入 String 类型的姓名。
        // getName(Object Name) 传入 Object 类型的姓名，两种getter方法产生冲突需要从gettypes也就是get的返回值类型得到相应的传参类型
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3) {
                if (method.getParameterTypes().length == 1) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingSetters, name, method);
                }
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    // 解决Setter方法冲突
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (String propName : conflictingSetters.keySet()) {
            List<Method> setters = conflictingSetters.get(propName);
            Method firstMethod = setters.get(0);
            if (setters.size() == 1) {
                addSetMethod(propName, firstMethod);
            } else {
                // 通过get得到的返回值类型判断name需要的set类型
                Class<?> expectedType = getTypes.get(propName);
                if (expectedType == null) {
                    throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                            + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                            "specification and can cause unpredicatble results.");
                } else {
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    // 找到需要相同类型的值
                    while (methods.hasNext()) {
                        Method method = methods.next();
                        if (method.getParameterTypes().length == 1
                                && expectedType.equals(method.getParameterTypes()[0])) {
                            setter = method;
                            break;
                        }
                    }
                    if (setter == null) {
                        throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                                "specification and can cause unpredicatble results.");
                    }
                    addSetMethod(propName, setter);
                }
            }
        }
    }

    // 属性名对应的get方法的调用者，属性名对应get方法的返回值类型，填充setMethods、setTypes
    private void addSetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, new MethodInvoker(method));
            setTypes.put(name, method.getParameterTypes()[0]);
        }
    }

    // 添加字段
    private void addFields(Class<?> clazz) {
        // 如果是本类中的字段而不是父类的话不通过setter、getter方法修改，而是通过反射的方式进行修改
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 如果是私有方法，将它设置成可访问
            if (canAccessPrivateMethods()) {
                try {
                    field.setAccessible(true);
                } catch (Exception e) {
                    // Ignored. This is only a final precaution, nothing we can do.
                }
            }

            if (field.isAccessible()) {
                // 如果没有setter中不能设置该属性的方法并且该属性的不被final和static修饰，设置成通过反射方式设置该属性
                if (!setMethods.containsKey(field.getName())) {
                    // issue #379 - removed the check for final because JDK 1.5 allows
                    // modification of final fields through reflection (JSR-133). (JGB)
                    // pr #16 - final static can only be set by the classloader
                    int modifiers = field.getModifiers();//  方法返回一个整数，代表了字段的修饰符的整数位表示。要检查特定修饰符是否存在，通常使用 Modifier 类中的静态方法。在这段代码中，修饰符被用于检查字段是否是 final 和 static，从而确定是否应该将该字段添加到相应的设置方法集合中。
                    if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                        addSetField(field);
                    }
                }
                // 如果没有getter中不能设置该属性的方法，设置成通过反射方式设置该属性
                if (!getMethods.containsKey(field.getName())) {
                    addGetField(field);
                }
            }
        }
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    // 添加字段的Setter方法
    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }
    }

    // 添加字段的Getter方法
    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMethods.put(field.getName(), new GetFieldInvoker(field));
            getTypes.put(field.getName(), field.getType());
        }
    }




    // 检查给定的字段名是否是一个有效的属性名
    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
        // 如果name在conflictingMethods中不存在，则创建一个新的ArrayList作为其值；如果name已经存在，那么就返回与之相关联的ArrayList
        List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(method);
    }

    private Method[] getClassMethods(Class<?> cls) {
        Map<String, Method> uniqueMethods = new HashMap<String, Method>();
        // 从给定类开始，沿着类层次结构向上追溯
        Class<?> currentClass = cls;
        while (currentClass != null) {
            // currentClass.getDeclaredMethods返回一个包含当前类或接口声明的所有方法的数组
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
            // 我们还需要查找接口方法，因为类可能是抽象的，抽象类可以选择性实现接口方法
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }
            currentClass = currentClass.getSuperclass();
        }
        // 将唯一方法Map中的值（唯一方法）转换为Method数组并返回
        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[methods.size()]);
    }

    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            // 排除桥接方法，因为它们是编译器生成的，不是用户代码
            if (!currentMethod.isBridge()) {
                //获取方法的签名
                String signature = getSignature(currentMethod);
                if (!uniqueMethods.containsKey(signature)) {
                    // 如果可以访问私有方法，则尝试设置方法可访问性为true
                    if (canAccessPrivateMethods()) {
                        try {
                            currentMethod.setAccessible(true);
                        } catch (Exception e) {
                            // 忽略异常。这只是最后的预防措施，我们无法做任何事情。
                        }
                    }
                    // 将方法添加到唯一方法Map中
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    // 获取方法的签名  返回值类型#方法名:参数1、参数2、参数3、
    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append('#');
        }
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    // 检查是否可以访问私有方法
    //尝试获取系统安全管理器（SecurityManager），然后如果存在安全管理器，则会检查是否具有 suppressAccessChecks 权限。如果没有安全管理器或无法获得该权限，则返回 false；否则返回 true。
    private static boolean canAccessPrivateMethods() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }


// ---------------------------------------------------  对外开放接口
    // 获取本类类型
    public Class<?> getType() {
        return type;
    }

    // 获取无参构造器
    public Constructor<?> getDefaultConstructor() {
        if (defaultConstructor != null) {
            return defaultConstructor;
        } else {
            throw new RuntimeException("There is no default constructor for " + type);
        }
    }

    // 查看是否有无参构造器
    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }

    // 获取字段使用set设置的类型
    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    // 获取字段的set-Invoke对象用来获取对象值
    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    // 获取字段的get-Invoke对象用来修改对象值
    public Invoker getSetInvoker(String propertyName) {
        Invoker method = setMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    // 获取字段使用get获取值的类型
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    // 获取可读字段名
    public String[] getGetablePropertyNames() {
        return readablePropertyNames;
    }

    // 获取可写字段名
    public String[] getSetablePropertyNames() {
        return writeablePropertyNames;
    }

    // 在set中查看有无该字段，也就是查看能不能进行参数修改
    public boolean hasSetter(String propertyName) {
        return setMethods.keySet().contains(propertyName);
    }

    // 在get中查看有无该字段，也就是查看能不能进行参数获取
    public boolean hasGetter(String propertyName) {
        return getMethods.keySet().contains(propertyName);
    }

    /*
     * 获取该字段大写对应的原本形式
     */
    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    /*
     * Gets an instance of ClassInfo for the specified class.
     * 得到某个类的反射器，是静态方法，而且要缓存，又要多线程，所以REFLECTOR_MAP是一个ConcurrentHashMap
     * 如果设置了缓存先去缓存看看也没有，没有再创建
     * @param clazz The class for which to lookup the method cache.
     * @return The method cache for the class
     */
    public static Reflector forClass(Class<?> clazz) {
        if (classCacheEnabled) {
            // synchronized (clazz) removed see issue #461
            // 对于每个类来说，我们假设它是不会变的，这样可以考虑将这个类的信息(构造函数，getter,setter,字段)加入缓存，以提高速度
            Reflector cached = REFLECTOR_MAP.get(clazz);
            if (cached == null) {
                cached = new Reflector(clazz);
                REFLECTOR_MAP.put(clazz, cached);
            }
            return cached;
        } else {
            return new Reflector(clazz);
        }
    }

    // 设置是缓存是否可用
    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.classCacheEnabled = classCacheEnabled;
    }

    // 查看缓存是否可用
    public static boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

}
