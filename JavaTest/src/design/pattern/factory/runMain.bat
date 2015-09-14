:: mvn archetype:create -DgroupId=com.chenjo.common -DartifactId=Factory-Pattern
:: mvn compile
mvn exec:java -Dexec.mainClass="Main"
