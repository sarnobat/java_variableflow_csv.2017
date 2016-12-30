mvn --quiet clean compile -Dcompiler.debug=true

mvn --quiet exec:java -Dexec.mainClass="JavaVariableFlow2"
