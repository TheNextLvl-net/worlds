package test;

public class Test extends OtherTest {

    public static void main(String[] args) throws NoSuchMethodException {
        System.out.println(Test.class.getMethod("test").getDeclaringClass());
    }
}
