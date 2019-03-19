# ScriptRunner

Create new instance with ScriptRunnerBuilder:
```java
ScriptRunner runner = new ScriptRunner.ScriptRunnerBuilder()
                        .withContentType("application/json")
                        .withIncomingScriptOnly(<String> javascriptFunction)
                        .build();

// At least one of the following methods is required:
// .withIncomingScriptOnly()
// .withOutgoingScriptOnly()
// .withInAndOutgoingScript()
// The method .withContentType() is required as well.
// The method .withConfig() is optional. 
```

ScriptRunner methods:
```java
/**
* Returns a String out of a given file
*/
public static String readFromFile(String scriptPath);
```