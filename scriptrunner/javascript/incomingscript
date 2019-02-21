function mapToDittoProtocolMsg( 
    headers, 
    textPayload, 
    bytePayload, 
    contentType 
) { 
 
    // ### 
    // Insert your mapping logic here 
    let namespace = "org.eclipse.ditto";
    let id = "fancy-car-11";
    let group = "things"; 
    let channel = "twin"; 
    let criterion = "commands"; 
    let action = "modify"; 
    let path = "/attributes/foo";
    let dittoHeaders = {}; 
    dittoHeaders["correlation-id"] = headers["correlation-id"]; 
    let value = textPayload; 
    // ### 
 
    return Ditto.buildDittoProtocolMsg( 
        namespace, 
        id, 
        group, 
        channel, 
        criterion, 
        action, 
        path, 
        dittoHeaders, 
        value 
    ); 
};