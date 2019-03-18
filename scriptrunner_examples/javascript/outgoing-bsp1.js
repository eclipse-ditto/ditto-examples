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

    let finalValue = value + "appendix";

    headers = dittoHeaders;
    textPayload = finalValue;
    bytePayload =  null;
    contentType = "TEXT";


    return  Ditto.buildExternalMsg(
        headers,
        textPayload,
        bytePayload,
        contentType
    );
}