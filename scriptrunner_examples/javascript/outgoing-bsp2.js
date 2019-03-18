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

    let buf = new ArrayBuffer(value.length);
    let bufView = new Uint8Array(buf);
    for (let i=0, strLen=value.length; i<strLen; i++) {
        bufView[i] = value.charCodeAt(i);
    }

    headers = dittoHeaders;
    textPayload = null;
    bytePayload =  buf;
    contentType = "application/octet-stream";


    return  Ditto.buildExternalMsg(
        headers,
        textPayload,
        bytePayload,
        contentType
    );
}