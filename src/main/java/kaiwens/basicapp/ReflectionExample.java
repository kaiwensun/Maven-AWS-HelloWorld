package kaiwens.basicapp;

import java.lang.reflect.Method;

public class ReflectionExample {
    public static void main() {
        try {
            Class<?> classType = Class.forName("kaiwens.basicapp.ReflectionExample");
            Method[] methods = classType.getMethods();
            for (Method method : methods) {
                System.out.println(method);
            }

            ReflectionExample reflection = new ReflectionExample();
            Method method = classType.getMethod("sayHi", new Class<?>[] {});
            method.invoke(reflection);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sayHi() {
        System.out.println("I am saying Hi!");
    }
}
