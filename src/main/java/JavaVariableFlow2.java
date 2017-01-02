import java.util.List;

import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
//import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
//import spoon.reflect.code.CtVariableWrite;
//import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

/**
 * @author sarnobat 2016-12
 */
public class JavaVariableFlow2 {

	static class MyVisitor extends CtScanner {
		// private int expected;
		// private int actual;
		public boolean equals = false;
		private CtExecutableReference<?> executable;
		private CtMethod<?> method;

		public MyVisitor(int expected) {
			// this.expected = expected;
		}
		public <T> void visitCtInvocation(final CtInvocation<T> invocation) {
			enter(invocation);
			scan(invocation.getAnnotations());
			scan(invocation.getTypeCasts());
//			System.err.println("CtScanner.visitCtInvocation()\tinvocation target\t" + invocation.getTarget());
			scan(invocation.getTarget());
			scan(invocation.getExecutable());
			this.executable = invocation.getExecutable();
//			System.err.println("CtScanner.visitCtInvocation()\tinvocation executable\t" + invocation.getExecutable());
			scan(invocation.getArguments());
//			System.err.println("CtScanner.visitCtInvocation()\tinvocation arguments\t" + invocation.getArguments());
//			System.err.println("CtScanner.visitCtInvocation()\tinvocation arguments\t" + invocation.getArguments().size());
			int i = 0;
			for (CtExpression<?> o : invocation.getArguments()) {
//				System.err.println("CtScanner.visitCtInvocation()\tinvocation arguments\t"
//						+ o.getClass() + "\t" + o.toString());
				System.out.println("\"" + invocation.getExecutable() + "::" + o.getShortRepresentation() + "\",\"" + invocation.getExecutable()
						+ "::" + i + "\"");	
				++i;
			}
			scan(invocation.getComments());
			exit(invocation);
		}

		
		@Override
		public <T> void visitCtMethod(CtMethod<T> m) {
			super.visitCtMethod(m);
			this.method = m;
			System.err.println("VisitorTest.MyVisitor.enclosing_method(): " + m.getSignature());
			System.err.println("VisitorTest.MyVisitor.enclosing_method(): " + m.getShortRepresentation());
			List<CtParameter<?>> parameters = m.getParameters();
			int i = 1;
			for(CtParameter p : parameters) {
				System.err.println("VisitorTest.MyVisitor.enclosing_method(): " + p.getSimpleName());
				System.out.println("\"" + m.getSignature() + "::" + i + "\",\"" + m.getSignature()
						+ "::" + p.getSimpleName() + "\"");
				++i;
			}
		}

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
			// System.out.println("CtScanner.visitCtAssignment()" +
			// assignement);
			enter(assignement);
			scan(assignement.getAnnotations());
			scan(assignement.getType());
			scan(assignement.getTypeCasts());
			scan(assignement.getAssigned());
			List<CtVariableRead> elements = assignement.getAssignment().getElements(
					new spoon.reflect.visitor.filter.TypeFilter<CtVariableRead>(
							CtVariableRead.class));
			System.err.println("CtScanner.visitCtAssignment()\tassigned\t"
					+ assignement.getAssigned().toString());
			// System.err.println("CtScanner.visitCtAssignment()\tassignment\t"
			// + assignement.getAssignment().toString());
			System.err.println("CtScanner.visitCtAssignment()\tassignment elements\t" + elements);
			System.err.println("JavaVariableFlow2.MyVisitor.visitCtAssignment() " + method);
//			System.err.println("JavaVariableFlow2.MyVisitor.visitCtAssignment() " + this.enclosingMethod);
//			String methodSig = assignement.getParent(CtMethod.class).getSignature();
			String methodSig = assignement.getParent(CtMethod.class).getShortRepresentation();
			System.err.println("JavaVariableFlow2.MyVisitor.visitCtAssignment() " + methodSig);
			for (CtVariableRead rhsVariableRead : elements) {
				System.out.println("\"" + methodSig + "::"+ rhsVariableRead + "\",\"" +  methodSig + "::"+ assignement.getAssigned()
						+ "\"");
			}
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
