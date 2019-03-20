# ScriptRunner

## Summary

The `ScriptRunner` Class provides helper functions to test the javascript mapping feature of Eclipse Ditto.

## Getting started

As described in the [Eclipse Ditto Documentation](https://www.eclipse.org/ditto/connectivity-mapping.html) you need a
 payload mapping script which you want to apply to your connection. The `ScriptRunner` Class offers a helper function
  to load this function from a file.

Please see [ScriptRunner Examples](../scriptrunner_examples) for further guidance.

A simple example:

Directory structure
```tree
|-- javascript
    |-- payloadMappingScript.js
|-- src
    |-- payloadMappingTest.java
```

payloadMappingTest.java
```java
@Test
public void testPayladMapping() {
    ScriptRunner runner = new ScriptRunner.ScriptRunnerBuilder()
                            .withContentType("application/json")
                            .withIncomingScriptOnly(ScriptRunner.readFromFile("javascript/payloadMappingScript.js"))
                            .build();
    
    ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(DittoHeaders.empty())
                                .withText("hello")
                                .build();
    
    Adaptable mappedExternalMessage = runner.mapExternalMessage(message);
}
```