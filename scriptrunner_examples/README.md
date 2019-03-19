# ScriptRunner examples

## Summary

This example is about how to test javascript mapping scripts. Due to the fact, that Bosch IoT Things does not support
 on side debugging of your scripts so far, this is the proper way to check if the mapping script is valid. The 
 example makes use of the `ScriptRunner` class which wraps the mapping functionality into a class and makes it easy 
 to use. With this class it is possible to write unit tests for your scripts.
 
 ## Basic concept
 ![basic message flow](images/message-flow.png)
 
 As you can see, messages from a device will be received by the gateway and afterwards handled as an ExternalMessage.
  The `ExternalMessage` Object wraps the payload of the message either in `bytePayload` or in `textPayload`. 
  Depending on the content-type, your mapping can decide whether to map or to drop the message.
  
  If you don't apply any javascript mapping to your connection, the messages have to be valid Ditto Protocol messages
  . Hence you can assume, that your message, which you have to apply payload mapping on, has to be exact the same 
  `Adaptable` after mapping as a valid Ditto Protocol Message with the same intention. Therefore you need to think 
  about how the valid Ditto Protocol Message looks like, then you can compare your mapped `Adaptable` with the 
  `Adaptable` generated out of a Ditto Protocol Message.
  
  ![compare message flow](images/message-flow-compare.png)
  
  Vice versa you can apply the same logic on outgoing payload mapping comparison - but instead of using the `Adaptable`
   for comparison, you can use the `ExternalMessage`. The `ExternalMessage` contains either the `byte` or 
   `textPayload` which you expect to be received by your device or any other consumer.
   
   