import java.util.List;

import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.visitor.CtScanner;

/**
 * @author sarnobat 2016-12
 */
public class JavaVariableFlow2 {

	static class MyVisitor extends CtScanner {
		public boolean equals = false;

		public <T> void visitCtInvocation(final CtInvocation<T> invocation) {
			enter(invocation);
			scan(invocation.getAnnotations());
			scan(invocation.getTypeCasts());
			scan(invocation.getTarget());
			scan(invocation.getExecutable());
			scan(invocation.getArguments());
			int i = 1;
			for (CtExpression<?> argument : invocation.getArguments()) {
				System.err.print("[correct] INVOCATION\t");
				System.out.println("\"" + invocation.getExecutable() + "::" + argument.getShortRepresentation() + "\",\"" + invocation.getExecutable()
						+ "::" + i + "\"");	
				++i;
			}
			scan(invocation.getComments());
			exit(invocation);
		}

		
		@Override
		public <T> void visitCtMethod(CtMethod<T> m) {
			super.visitCtMethod(m);
			List<CtParameter<?>> parameters = m.getParameters();
			int i = 1;
			String signature = m.getSignature();
			for(CtParameter<?> param : parameters) {
				System.err.print("[fix pkg] METHOD\t");
				System.out.println("\"" + signature + "::" + i + "\",\"" + signature
						+ "::" + param.getSimpleName() + "\"");
				++i;
			}
		}

		public <T, A extends T> void visitCtAssignment(final CtAssignment<T, A> assignement) {
			enter(assignement);
			scan(assignement.getAnnotations());
			scan(assignement.getType());
			scan(assignement.getTypeCasts());
			scan(assignement.getAssigned());
			List<CtVariableRead<?>> elements = assignement.getAssignment().getElements(
					new spoon.reflect.visitor.filter.TypeFilter<CtVariableRead<?>>(
							CtVariableRead.class));
			
			List<CtExecutable<?>> elements2 = assignement.getParent(CtExecutable.class).getElements(
					new spoon.reflect.visitor.filter.TypeFilter<CtExecutable<?>>(
							CtExecutable.class));
			if (elements2.size() != 1) {
				throw new RuntimeException("Unhandled");
			}
			String methodSig = elements2.get(0).getReference().getShortRepresentation();
			for (CtVariableRead<?> rhsVariableRead : elements) {
				System.err.print("[correct] ASSIGNMENT\t");
				System.out.println("\"" + methodSig + "::"+ rhsVariableRead + "\",\"" +  methodSig + "::"+ assignement.getAssigned()
						+ "\"");
			}
			scan(assignement.getAssignment());
			scan(assignement.getComments());
			exit(assignement);
		}
	}

	public static void main(String[] args) {

		Launcher launcher = new Launcher();
		launcher.addInputResource("./Foo.java");
		launcher.buildModel();

		final MyVisitor visitor = new MyVisitor();
		visitor.scan(launcher.getFactory().Package().getRootPackage());
	}

}
