In intellij-

File -> Project Structure -> Modules (+) -> Find pom.xml of package you want to add

Then, add reference to it in pom.xml

example:
```xml
<dependency>
    <groupId>com.charliebaird</groupId>
    <artifactId>TeensyBottingLib</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>```