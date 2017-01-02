import java.util.List;

import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
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
			
			// TODO link the local variables to the args
			
			int i = 1;
			for (CtExpression<?> argument : invocation.getArguments()) {
				System.err.print("[correct] INVOCATION\t");
				String variablePassedToInvocation = invocation.getExecutable() + "::" + argument.getShortRepresentation();
				System.out.println("\"" + variablePassedToInvocation + "\",\"" + invocation.getExecutable() + "::" + i + "\"");
				

				List<CtVariableRead<?>> elements = argument.getElements(
						new spoon.reflect.visitor.filter.TypeFilter<CtVariableRead<?>>(
								CtVariableRead.class));
				System.out.println("  JavaVariableFlow2.MyVisitor.visitCtInvocation() elements = " + elements);
				if (elements.size() != 1) {
					throw new RuntimeException("Unhandled: " + elements);
				}
				for (CtVariableRead<?> v : elements) {
					System.err.println("JavaVariableFlow2.MyVisitor.visitCtInvocation() ");
					

					CtMethod<?> parent =  getContainingMethod(invocation);
					String s = this.fixSignature(parent);
					System.out.println("\""+s+"::"+v.getShortRepresentation()+"\",\""+variablePassedToInvocation+"\"");
				}
				++i;
			}
			scan(invocation.getComments());
			exit(invocation);
		}
		
		private static CtMethod<?> getContainingMethod(CtElement e) {
			if (e instanceof CtMethod) {
				return (CtMethod<?>) e;
			} else {
				return getContainingMethod(e.getParent());
			}
		}

		// Note: the regex replacement will have issues in groovy. You'll need to do it programmatically.
		@Deprecated //Until I find out how to do this properly
		private static String fixSignature(CtMethod<?> method) {
			String classNameQualified = method.getParent().getShortRepresentation().replaceAll("^class\\s+", "");
			return method.getSignature().replaceAll("^(int)\\s+", classNameQualified + "#");
		}

		@Override
		public <T> void visitCtMethod(CtMethod<T> method) {
			super.visitCtMethod(method);
			List<CtParameter<?>> parameters = method.getParameters();
			int i = 1;
			String signature = fixSignature(method);
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
