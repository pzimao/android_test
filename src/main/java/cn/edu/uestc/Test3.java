package cn.edu.uestc;

public class Test3 {

    public void func() throws Exception {
        Exception exception = null;
        try {
            throw new Exception("try异常！！！");
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception != null) {
                exception.addSuppressed(new Exception("finally异常！！！"));
            } else {
                exception = new Exception("finally异常！！！");
            }
            throw exception;
        }
    }

    public static void main(String[] args) {
        try {
            new Test3().func();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
