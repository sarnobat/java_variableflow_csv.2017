import java.util.List;

import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.visitor.CtScanner;

/**
 * @author sarnobat 2016-12
 */
public class JavaVariableFlow {

	private static class MyVisitor extends CtScanner {

		public <T> void visitCtInvocation(final CtInvocation<T> invocation) {
			super.visitCtInvocation(invocation);
			
			int i = 1;
			for (CtExpression<?> argument : invocation.getArguments()) {
				
				if (argument instanceof CtLiteral<?>) {
					continue;
				}
				if (argument instanceof CtInvocation<?>) {
					// I'm not sure why this doesn't happen automatically.
					this.visitCtInvocation((CtInvocation<?>)argument);
					continue;
				}
				if (argument instanceof CtBinaryOperator<?>) {
					this.visitCtBinaryOperator((CtBinaryOperator<?>) argument);
					continue;
				}
				if (argument instanceof CtThisAccess<?>) {
					// TODO: What do we do here?
					continue;
				}
				String variablePassedToInvocation = invocation.getExecutable() + "::" + argument.getShortRepresentation().replaceAll("(<.*>\\s+)?(int|void|[a-zA-Z\\.]+)\\s+","");
				System.out.println("\"" + variablePassedToInvocation + "\",\"" + invocation.getExecutable() + "::" + i + "\"");

				List<CtVariableRead<?>> elements = argument.getElements(
						new spoon.reflect.visitor.filter.TypeFilter<CtVariableRead<?>>(
								CtVariableRead.class));
				if (elements.size() > 1) {
					throw new RuntimeException("Unhandled 1: " +argument.getClass()+ "\t"+ elements);
				}
				if (elements.size() < 1) {
					throw new RuntimeException("Unhandled 2: " +argument.getClass()+ "\t"+  invocation);
				}
				for (CtVariableRead<?> v : elements) {

					CtMethod<?> containingMethod =  getContainingMethod(invocation);
					String containingMethodSignature = this.fixSignature(containingMethod);
					System.out.println("\""+containingMethodSignature+"::"+v.getShortRepresentation().replaceAll("(<.*>\\s+)?(void|int|[a-zA-Z\\.]+)\\s+","")+"\",\""+variablePassedToInvocation+"\"");
				}
				++i;
			}
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
			String replaceAll = method.getSignature().replaceAll("^(<.*>\\s+)?(int|void|[a-zA-Z\\.]+)\\s+", classNameQualified + "#");
			if (replaceAll.contains("<")) {
				throw new RuntimeException("Unhandled: fixing signature with generic: " + replaceAll);
			}
			return replaceAll;
		}

		@Override
		public <T> void visitCtMethod(CtMethod<T> method) {
			super.visitCtMethod(method);
			List<CtParameter<?>> parameters = method.getParameters();
			int i = 1;
			String signature = fixSignature(method);
			for(CtParameter<?> param : parameters) {
				System.out.println("\"" + signature + "::" + i + "\",\"" + signature
						+ "::" + param.getSimpleName() + "\"");
				++i;
			}
		}

		public <T, A extends T> void visitCtAssignment(final CtAssignment<T, A> assignment) {
			super.visitCtAssignment(assignment);
			// TODO: call supermethod instead
			List<CtVariableRead<?>> elements = assignment.getAssignment().getElements(
					new spoon.reflect.visitor.filter.TypeFilter<CtVariableRead<?>>(
							CtVariableRead.class));
			
			List<CtExecutable<?>> elements2 = assignment.getParent(CtExecutable.class).getElements(
					new spoon.reflect.visitor.filter.TypeFilter<CtExecutable<?>>(
							CtExecutable.class));
			if (elements2.size() != 1) {
				throw new RuntimeException("Unhandled");
			}
			String methodSig = elements2.get(0).getReference().getShortRepresentation();
			for (CtVariableRead<?> rhsVariableRead : elements) {
				System.out.println("\"" + methodSig + "::"+ rhsVariableRead + "\",\"" +  methodSig + "::"+ assignment.getAssigned()
						+ "\"");
			}
		}
	}

	public static void main(String[] args) {

		Launcher launcher = new Launcher();
		//launcher.addInputResource("./Foo.java");
		launcher.addInputResource("./CtScanner.java");
		launcher.buildModel();

		new MyVisitor().scan(launcher.getFactory().Package().getRootPackage());
	}

}
