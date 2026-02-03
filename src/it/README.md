# Integration tests for JavaCC Maven plugin, from javacc org

### errors

Different `.jj` grammars to be processed by the `javacc` goal, producing errors catched by
 the plugin with appropriate parameters so that no build failure should occur:  
- non existing or not directory sourceDirectory
- invalid value for an option read by the plugin
- invalid or not absolute outputDirectory path
- wrong (unknown) encoding
- 2 inconsistent options
- processor (generation) error (missing production)
- skipped execution

### failures

Different `.jj` grammars to be processed by the `javacc` goal, producing errors catched by
 the plugin with appropriate parameters so that build failure should occur:  
- processor (generation) error (missing production) (ignore, first, last)
- read error (empty grammar or missing parser name) (ignore, first, last)
- wrong (unknown) code generator value
- invalid or not absolute outputDirectory path
- non existing or not directory sourceDirectory

### javacc-goal

Different `.jj` grammars `BasicParserN.jj` to be processed by the `javacc` goal; combinations of:
- Java & C#
- Maven standard and non standard (canonical and non canonical) source and output directories
- with/without package for Java or with/without sub directory for C#
- ASCII and non ASCII identifiers (parser names, packages, namespaces) and tokens string literals

### jjtree-javacc-goal

Different `.jjt` grammars `Simple0N.jjt` to be processed by the `jjtree-javacc` goal; combinations of:
- Java & C#
- Maven standard and non standard (canonical and non canonical) source and output directories
- with/without package for Java or with/without sub directory for C#
- ASCII and non ASCII identifiers (parser names, packages, namespaces) and tokens string literals
- non escaped and unicode escaped characters

### jjtree-then-javacc-goals

Different `.jjt` grammars `SimpleN.jjt` to be processed by the `jjtree` goal in a first execution, then by the `javacc` goal in a second one; combinations of:
- Java & C#
- Maven standard and non standard (canonical and non canonical) source and output directories
- with/without package for Java or with/without sub directory for C#
- ASCII and non ASCII identifiers (parser names, packages, namespaces) and tokens string literals
- non escaped and unicode escaped characters

### jtb-javacc-goal

Different `.jtb` grammars `Eg0N.jtb` to be processed by the `jtb-javacc` goal; combinations of:
- Java
- Maven standard and non standard (canonical and non canonical) source and output directories
- with/without package
- ASCII and non ASCII identifiers (parser names, packages, namespaces) and tokens string literals
- non escaped and unicode escaped characters

### jtb-then-javacc-goals

Different `.jtb` grammars `EgN.jtb` to be processed by the `jtb` goal in a first execution, then by the `javacc` goal in a second one; combinations of:
- Java
- Maven standard and non standard (canonical and non canonical) source and output directories
- with/without package
- ASCII and non ASCII identifiers (parser names, packages, namespaces) and tokens string literals
- non escaped and unicode escaped characters

### jjdoc-goal

Different `.jj` grammars `BasicParserN.jj` to be processed by the `jjdoc` goal; combinations of:
- Java & C#
- Maven standard and non standard (canonical and non canonical) source and output directories
- with/without package for Java or with/without sub directory for C#
- ASCII and non ASCII identifiers (parser names, packages, namespaces) and tokens string literals
