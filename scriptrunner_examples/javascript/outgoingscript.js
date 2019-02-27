function mapFromDittoProtocolMsg(
    namespace,
    id,
    group,
    channel,
    criterion,
    action,
    path,
    dittoHeaders,
    value
) {

    // ###
    // Insert your mapping logic here:
    // ###

    headers = dittoHeaders;
    textPayload = value;
    bytePayload =  null;
    contentType = "application/json";


    return  Ditto.buildExternalMsg(
        headers,
        textPayload,
        bytePayload,
        contentType
    );
}