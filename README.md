# MethodProfiler
Sample code to instrument/profile given classes and methods using javassist. MethodProfile requires a class and optionally a method name to insert profiling statements. If only a class name is provided, all methods of the class will be profiled. The agent will not do anything if no classname is provided. 

To compile:
   mvn package

To use:
   Download jassist.jar from http://jboss-javassist.github.io/javassist/ and add to the classpath
run:
   java -cp ".:*.jar" -javaagent:MethodProfiler-1.0.jar="class<:method>" Application 


