# Javacc Maven Plugin

This is the [javacc-maven-plugin](http://www.mojohaus.org/javacc-maven-plugin/).

 
[![Maven Central](https://img.shields.io/maven-central/v/org.javacc.plugin/javacc-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.javacc.plugin/javacc-maven-plugin)

This differs from the  [codehaus javacc-maven-plugin](http://www.mojohaus.org/javacc-maven-plugin/), in that this is for javacc 8.x and later.

## Quickstart

```xml

<properties>
   <javacc.version>8.0.1</javacc.version>
</properties>
...

<build>
<plugins>
    <plugin> 
        <groupId>org.javacc.plugin</groupId>
        <artifactId>javacc-maven-plugin</artifactId>
        <version>3.0.3</version>
        <executions>
            <execution>
                <goals>
                    <goal>....</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            
        </configuration>
        <dependencies>
            <dependency>
                <groupId>org.javacc.generator</groupId>
                <artifactId>java</artifactId>
                <version>${javacc.version}</version>
            </dependency>
            <dependency>
                <groupId>org.javacc</groupId>
                <artifactId>core</artifactId>
                <version>${javacc.version}</version>
            </dependency>
        </dependencies>
    </plugin>
</plugins>
</build>
```
 
