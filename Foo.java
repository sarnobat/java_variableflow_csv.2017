package spoon.test.visitor;

public class Foo {

    public static int factorial(final int iSridharParam) {
    	int sridharLocalVariable = 1;
    	sridharLocalVariable = Math.max(sridharLocalVariable, iSridharParam);
        if (iSridharParam < 0) {
            throw new IllegalArgumentException("iSridharParam < 0");
        } else if (iSridharParam == 0) {
            return 1;
        } else {
            return iSridharParam * factorial(iSridharParam-1);
        }
    }
}
