mvn --quiet clean compile -Dcompiler.debug=true

mvn --quiet exec:java -Dexec.mainClass="JavaVariableFlow" -Dexec.args="Foo.java" | sort | uniq
#mvn --quiet exec:java -Dexec.mainClass="JavaVariableFlow" -Dexec.args="/Users/ssarnobat/work/src/saas/services/plancycle/src/main/java/com/itsoninc/saas/services/partner/plancycle/PlanCycleServiceImpl.java" | sort | uniq

# cat out.csv  | sh ~/github/java_callgraph_csv/csv2d3.sh