import java.util.List;

import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
//import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtVariableRead;
//import spoon.reflect.code.CtVariableWrite;
//import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

/**
 * @author sarnobat
 * 2016-12
 */
public class JavaVariableFlow2 {

	static class MyVisitor extends CtScanner {
		// private int expected;
		// private int actual;
		public boolean equals = false;

		public MyVisitor(int expected) {
			// this.expected = expected;
		}

		// @Override
		// public <T> void visitCtMethod(CtMethod<T> m) {
		// actual = 0;
		// super.visitCtMethod(m);
		// equals = expected == actual;
		// // System.out.println("VisitorTest.MyVisitor.enclosing_method()" +
		// // m);
		// }

		// @Override
		// public <T> void visitCtVariableWrite(final CtVariableWrite<T>
		// variableWrite) {
		// System.out.println("VisitorTest.MyVisitor.visitCtVariableWrite() "
		// + variableWrite.toString());
		// }

		// @Override
		// public void visitCtIf(CtIf ifElement) {
		// actual++;
		// super.visitCtIf(ifElement);
		// // System.out.println("VisitorTest.MyVisitor.enclosing_method()" +
		// // ifElement.getShortRepresentation());
		// }
		// I believe this is what we need for our CSV
		public <T, A extends T> void visitCtAssignment(final CtAssignment<T, A> assignement) {
			System.out.println("CtScanner.visitCtAssignment()" + assignement);
			enter(assignement);
			scan(assignement.getAnnotations());
			scan(assignement.getType());
			scan(assignement.getTypeCasts());
			scan(assignement.getAssigned());
			List<CtVariableRead> elements = assignement.getAssignment().getElements(
					new spoon.reflect.visitor.filter.TypeFilter<>(
							spoon.reflect.code.CtVariableRead.class));
			System.err.println("CtScanner.visitCtAssignment()\tassigned\t"
					+ assignement.getAssigned().toString());
			// System.err.println("CtScanner.visitCtAssignment()\tassignment\t"
			// + assignement.getAssignment().toString());
			System.err.println("CtScanner.visitCtAssignment()\tassignment elements\t" + elements);
			scan(assignement.getAssignment());
			scan(assignement.getComments());
			exit(assignement);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Launcher launcher = new Launcher();
		launcher.addInputResource("./Foo.java");
		launcher.buildModel();

		final MyVisitor visitor = new MyVisitor(2);
		visitor.scan(launcher.getFactory().Package().getRootPackage());
	}

}
