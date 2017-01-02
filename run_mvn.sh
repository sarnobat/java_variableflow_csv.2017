mvn --quiet clean compile -Dcompiler.debug=true

mvn --quiet exec:java -Dexec.mainClass="JavaVariableFlow" | sort | uniq

# cat out.csv  | sh ~/github/java_callgraph_csv/csv2d3.sh