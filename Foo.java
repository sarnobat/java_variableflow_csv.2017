package spoon.test.visitor;

public class Foo {

    public static int factorial(final int iSridharParam) {
    	int sridharLocalVariable = 1;
    	sridharLocalVariable = Math.max(sridharLocalVariable, iSridharParam);
        if (iSridharParam < 0) {
        	int ret = iSridharParam * factorial(iSridharParam - Math.min(sridharLocalVariable, 2));
            return ret;
        }
        return 1;
    }
}
