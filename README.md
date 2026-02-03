# JavaCC Maven plugin, from javacc.org

This is the **JavaCC Maven plugin** Git repository, for **JavaCC / JJTree version 8+**.  

The other JavaCC plugins in the wild do not support the full JavaCC 8 features (architecture and options).  

Since version 3.8.0+, the plugin is no more compatible (i.e. configuration is different) with its previous versions (3.0.x).  
In fact, this plugin started as a fork of the MojoHaus JavaCC Maven Plugin, then it derived from it (to support JavaCC 8 artifacts), and neither never converged to the other one.  
Now it has been completely rewritten, to make it much simpler and transparent to JavaCC and related processors evolutions.  
It has an integration tests suite driven by (JaCoCo) instrumented code coverage.  

Note that one can still use the previous versions with JavaCC 8 if he does not need to pass
 options not managed by these versions. But users are encouraged to migrate.  

Note also that one can use the MojoHaus Exec Maven plugin to run JavaCC, JJTree, JJDoc or JTB
 as standalone executable, within appropriate phases and directories configurations (but those
 are out of the scope of this documentation).  

### Features

- supports the different JavaCC 8 generators, through dependencies declaration
- supports **javacc**, **jjtree**, **jtb** & **jjdoc** simple goals (executing the corresponding processor)
- supports the combined **jjtree-javacc**, **jtb-javacc** goals (chaining execution of the preprocessor and javacc on a grammar file)
- plugin agnostic of the processors options (except a very few of them) (options strings are passed transparently to the processors; no need to update the plugin when a processor changes some of its options)
- supports detection of stale / not stale generated files with respect to the grammar file and the processors artifacts (the jars and customization resources): the plugin will launch the processors:
    * if the grammar file has been touched after the last generation
    * if the artifacts are newer than the generated files (this is handy when working on processors snapshots)
- supports *ignoring* errors and failing on *last* error besides usual failing on *first* error, for plugin configuration, grammar reading and grammar processing errors (this is handy for integration tests, and may be also of interest for some projects)

### Usage

##### Help

Type  

`mvn org.javacc.plugin:javacc-maven-plugin:help -Ddetail=true -DlineLength=100`

or for a specific version  

`mvn org.javacc.plugin:javacc-maven-plugin:<version>:help -Ddetail=true -DlineLength=100`  

to get the up-to-date generated documentation of the plugin's goals and parameters.

##### Pom.xml

In project's pom.xml or in parent's pom.xml:

```
<?xml version="1.0" encoding="UTF-8"?>
...
  <properties>
    <!-- adapt versions -->
    <!-- run mvn versions:display-property-updates to display available updates -->
    <javacc-maven-plugin.version>3.8.0</javacc-maven-plugin.version>
    <javacc.core.version>8.1.0</javacc.core.version>
    <javacc.cpp.version>8.1.0</javacc.cpp.version>
    <javacc.csharp.version>8.1.0</javacc.csharp.version>
    <javacc.java.version>8.1.0</javacc.java.version>
    <jtb.version>1.5.3</jtb.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
...
    <pluginManagement>
      <plugins>
...
        <plugin>
          <groupId>org.javacc.plugin</groupId>
          <artifactId>javacc-maven-plugin</artifactId>
          <version>${javacc-maven-plugin.version}</version>
          <dependencies>
            <!-- declare all used processors artifacts -->
            <dependency>
              <groupId>org.javacc</groupId>
              <artifactId>core</artifactId>
              <version>${javacc.core.version}</version>
            </dependency>
            <dependency>
              <groupId>org.javacc.generator</groupId>
              <artifactId>java</artifactId>
              <version>${javacc.java.version}</version>
            </dependency>
            <dependency>
              <groupId>org.javacc.generator</groupId>
              <artifactId>csharp</artifactId>
              <version>${javacc.csharp.version}</version>
            </dependency>
            ...
          </dependencies>
        </plugin>
...
      </plugins>
    </pluginManagement>
...
    <plugins>
...
      <plugin>
        <groupId>org.javacc.plugin</groupId>
        <artifactId>javacc-maven-plugin</artifactId>
        <!-- default values for common arguments for different executions  -->
        <configuration>
          <javaccCmdLineArgs>
            <enc>-GRAMMAR_ENCODING="UTF-8"</enc>
            <jjod>-OUTPUT_DIRECTORY="${project.build.directory}/generated-sources/javacc"</jjod>
          </javaccCmdLineArgs>
          <jjtreeCmdLineArgs>
            <jjtod>-JJTREE_OUTPUT_DIRECTORY="${project.build.directory}/generated-sources/jjtree"</jjtod>
          </jjtreeCmdLineArgs>
        </configuration>
      </plugin>
...
    </plugins>
...
```

In project's pom.xml:

```
...
    <plugins>
...
      <plugin>
        <groupId>org.javacc.plugin</groupId>
        <artifactId>javacc-maven-plugin</artifactId>
        <executions>
          <!-- as many executions as different sets of grammars similar configurations -->
          <!-- here we use a non standard layout: 
                ("my cc" and "gen-src" instead of "src" and "generated-sources") -->
          <execution>
            <id>jjtree-javacc</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>jjtree-javacc</goal>
            </goals>
            <configuration>
              <includes>
                <include>...</include> <!-- default is *.jjt for JJTree -->
              </includes>
              <excludes>
                <exclude>...</exclude> <!-- default is nothing -->
              </excludes>
              <keepIntermediateDirectory>false</keepIntermediateDirectory> <!-- the default -->
              <skip>false</skip> <!-- the default -->
              <sourceDirectory>my cc</sourceDirectory> <!-- default is ${project.source.directory}/src/jjtree -->
              <jjtreeCmdLineArgs>
                <!-- overrides parent's one -->
                <jjtod>-JJTREE_OUTPUT_DIRECTORY="${project.build.directory}/gen-src/jjtree"</jjtod>
                <!-- additional arguments not in parent -->
                <arg>-CODE_GENERATOR="Java"</arg>
                <arg>-MULTI=true</arg>
              </jjtreeCmdLineArgs>
              <javaccCmdLineArgs>
                <!-- overrides parent's one -->
                <jjod>-OUTPUT_DIRECTORY="${project.build.directory}/gen-src/javacc"</jjod>
                <!-- additional arguments not in parent -->
                <arg>-CODE_GENERATOR="Java"</arg>
                <arg>-STATIC:false</arg>
              </javaccCmdLineArgs>
            </configuration>
          </execution>
        </executions>
      </plugin>
...
    </plugins>
...
```
See the different pom.xml in the integration tests under `/src/it` for more complex examples.  

##### Debug

You can enable the plugin's debug mode by passing the SLF4J managed environment variable 
  `org.slf4j.simpleLogger.log.org.javacc.mojo` set to `debug`:

```
mvn generate-sources -Dorg.slf4j.simpleLogger.log.org.javacc.mojo=debug
```

##### Plugin parameters

The plugin has a few parameters:  

- `failOnPluginError`: handling of errors encountered on general configuration (i.e. parameters not related to grammars):  
    * if set to `false` (the default), the error message is displayed, the execution terminates but the plugin does not report an error for the current execution (i.e. it continues with the next execution)
    * if set to `true`, the error message is displayed, the execution terminates and the plugin reports an error for the build

- `failOnGrammarError`: handling of errors encountered while trying to read a grammar file to retrieve the parser name (and the parser package for languages that use it) (and in an execution the plugin may process zero, one or many grammars):  
    * if set to `first` (the default), the error message is displayed, the plugin stops processing other grammars and reports an error for the build
    * if set to `last`, the error message is displayed, the plugin continues processing other grammars and at the end it reports an error for the build
    * if set to `ignore`, the error message(s) is(are) displayed but the plugin does not report an error for the current execution (i.e. it continues with the next execution)

- `failOnProcessorError`: handling of errors returned by the processor invocations and the plugin post-processor copy operations (in an execution the plugin may process zero, one or many grammars and invoke one or more processors for each):  
    * if set to `first` (the default), the error message is displayed, the plugin stops processing other grammars and reports an error for the build
    * if set to `last`, the error message is displayed, the plugin continues processing other grammars and at the end it reports an error for the build
    * if set to `ignore`, the error message(s) is(are) displayed but the plugin does not report an error for the current execution (i.e. it continues with the next execution)

- `sourceDirectory`: (*not for jjdoc*): the directory where the plugin will (recursively) search for grammar files; if one wants a pom to process more than one source directory, he must configure multiple executions with different source directories; default value, adequate for a user written grammar, but not for a generated (by a preprocessor) grammar, is:  
    * `${basedir}/src/main/javacc` for JavaCC and JJDoc
    * `${basedir}/src/main/jjtree` for JJTree
    * `${basedir}/src/main/jtb` for JTB  
  if not an absolute pathname, it will be taken as relative to the project's base directory; can be a non-canonical pathname

- `sourceDirectories`: (*jjdoc only*): the list of directories where the plugin will (recursively) search for grammar files; as above

- `excludes`: a list of exclusion filters on searching the source directory (to omit grammars to process); default is none `[]`

- `includes`: a list of inclusion filters on searching the source directory (to select grammars to process); default is:
    * `[**/*.jj]` for JavaCC
    * `[**/*.jjt]` for JJTree
    * `[**/*.jtb]` for JTB
    * `[**/*.jj, **/*.jjt, **/*.jtb]` for JJDoc

- `keepIntermediateDirectory`: (*not for jjdoc*): `true` to not delete the intermediate directory (see later), `false` otherwise (the default)

- `skip`: `true` to skip the execution, `false` otherwise (the default)

- `timestampDeltaMs`: (*not for jjdoc*): the difference in milliseconds of the last modification timestamps for testing whether a grammar file needs regeneration (often named `staleMillis` in other plugins); default value is `0`; if set to a negative value, no comparison will be performed and grammars will always be regenerated;  otherwise a grammar file will be passed to the processor if the sum of the main generated file timestamp plus this delta is lower than the grammar file timestamp or than the more recent of the dependent jars timestamps

- `jjdocReportsDirectory` (*jjdoc only*): default value is `${project.build.directory}/generated-jjdoc`; the output directory where Maven / Doxia generates the HTML files summarizing the JJDoc reports generation; only relevant if the goal is run from the command line or from the default build lifecycle; if the goal is run indirectly as part of a site generation, the output directory configured in the Maven Site Plugin is used instead (it will usually be "${project.build.directory}/site"); this is **not** the directory where JJDoc itself generates its reports, which must be set by the JJDoc `OUTPUT_DIRECTORY` option

- `jjdocReportsDirectory` (*jjdoc only*): default value is `/jjdoc-reports.html`; the HTML page name (with extension), relative to the output directory, where Maven / Doxia generates table of the JJDoc reports for the current execution

- the processor parameters (see next)

##### Processor parameters

The plugin can pass options to the processors through their command line arguments inside dedicated parameters of lists of strings of any format accepted by the processors.  

```
    <jjtreeCmdLineArgs>
      <arg>-NODE_EXTENDS="MyNode"</arg>
      <arg>-MuLtI=true</arg>
      <arg>-code_generator:"Java"</arg>
      <arg>-Grammar_Encoding="UTF-8"</arg>
      <arg>-JJTREE_OUTPUT_DIRECTORY="${project.build.directory}/gen-src/jjt"</arg>
    </jjtreeCmdLineArgs>
    
    <javaccCmdLineArgs>
      <arg>-StAtIc=false</arg>
      <arg>-IGNORE_CASE:true</arg>
      <arg>-LookAhead=2</arg>
      <arg>-CODE_GENERATOR:"C++"</arg>
      <arg>-GRAMMAR_ENCODING="UTF-8"</arg>
      <arg>-OUTPUT_DIRECTORY="${project.build.directory}/gen-src/jj"</arg>
    </javaccCmdLineArgs>
    
    <jjdocCmdLineArgs>
      <arg>-BNF=true</arg>
      <arg>-CSS="src/main/resources/my_css2"</arg>
      <arg>-CODE_GENERATOR:"C++"</arg>
      <arg>-GRAMMAR_ENCODING="UTF-8"</arg>
      <arg>-OUTPUT_DIRECTORY="${project.build.directory}/gen-jjdoc"</arg>
    </jjdocCmdLineArgs>
    
    <jtbCmdLineArgs>
      <arg>-OUTPUT_DIRECTORY="${project.build.directory}/generated-sources/javacc"</arg>
      <arg>-JTB_O="${project.build.directory}/generated-sources/jtb/Eg01.jj"</arg>
    </jtbCmdLineArgs>
```
As a standard Maven feature, the lists tags names (`jjtreeCmdLineArgs`, `javaccCmdLineArgs`...) are imposed but the sub-lists tags names (here `arg`) are not; however there are subtle side effects of using different tags names in a parent and a child pom.xml, so it is wise to keep the same tags names between the parent and the children; and the following is worth to know (cf. [maven plugins advanced configuration inheritance](https://maven.apache.org/pom.html#Plugins)):
- by default (i.e. with no `combine....="..."` attribute), a child configuration merges with the parent one: in that case any tag name appearing one or more times in the parent
    * will not be taken in account after the merge if it appears in the child (all the occurrences in the child will be taken in account after the merge),
    * will be taken in account after the merge if it does not appear in the child (all the occurrences in the child will be taken in account after the merge)
- the full parameter string, that is the option name plus the option value (and case matters) is not considered by maven, only the tag name is considered;
- so the most simple strategy is to use
    * in the parent specific different tag names (like `css`, `outdir`), and
    * in the children
        + another common tag name (like `arg`) for other arguments and
        + the same tags for the arguments defined in the parent if overriding them is needed

See for example the `jjdoc` integration tests under `/src/it/jjdoc-goal` for the effects of default merge an `combine.children="append"` and `combine.self="override"` attributes in different combinations.  

The plugin will try to read within the arguments the following options (*except for jjdoc*) and use them:

- `-GRAMMAR_ENCODING`: if given, the plugin will use this encoding to read the grammar file if it needs to (at the present time to find the package name inside a grammar for Java); if none is defined, it will use the `file.encoding` system property (which usually is not recommended as, unless it has been explicitly set, its default makes the build platform dependent)  

- `-CODE_GENERATOR`: if given, it must match a valid code generator string (as defined by the generators: `Java`, `C++`, `C#`, `JS`); if not given, the plugin will consider a default value of `Java`; the plugin will use this information to find the corresponding generator artifact and customizable templates, the use of package names or not  

- `-OUTPUT_DIRECTORY`: the directory where JavaCC will output its generated parser files (default value is `.`) (for JavaCC related goals) (see next)  
- `-JJTREE_OUTPUT_DIRECTORY`: the directory where JJTree will output its generated node files (default value is `.`) (for JJTree related goals) (see next)
- `-d` or `-JTB_D`: the directory where JTB will output its generated node and other files (default value is `.`) (for JTB related goals) (see next)  

  if these directories are not absolute pathnames, they will be taken as relative to the project's base directory; if they are non-canonical pathnames, they will be transformed to canonical ones  

For *jjdoc* the plugin will not manipulate command line arguments.  

#### Output directories and intermediate output directory

The processors standard behavior (inherited for the old ages) is to generate their files in directories but to not overwrite files that already exist there (usually because the user wants custom hand modified versions of them). This does not fit well with more modern build principles where one separates user files from generated ones.

As many of the other JavaCC Maven plugins, this one passes to the processor an intermediate output empty directory (*instead of the configured output directory* - it is the only user option that is transformed by the plugin before passing it to the processor), where the processor will generate all its files, then the plugin will copy these files to the (configured or default) output directory, except those that already reside in a compile source root (i.e. those for which the user wants his own custom version).  

This intermediate directory is deleted at the end of the execution; however, for debugging purposes, the parameter flag `keepIntermediateDirectory` allows to keep the intermediate directory.

### Maven plugin like web site pages

Web site pages in the Maven Plugin layout is generated under TODO.

### License

This plugin has been completely rewritten in version 3.8 and is published since as the other **javacc.org** softwares under the [BSD-3-Clause](LICENSE).

