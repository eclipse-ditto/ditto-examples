
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.ditto.services.connectivity.mapping.MessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapperConfiguration;
import org.eclipse.ditto.services.connectivity.mapping.MessageMappers;
import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperFactory;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.eclipse.ditto.services.models.connectivity.ExternalMessageFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ScriptRunner {

    public static class ScriptRunnerBuilder {

        private String pathToJavascript;
        private String pathToConfig;
        private String contentType;

        public ScriptRunnerBuilder withJavascriptPath(String path) {
            this.pathToJavascript = path;
            return this;
        }

        public ScriptRunnerBuilder withConfigPath(String path) {
            this.pathToConfig = path;
            return this;
        }

        public ScriptRunnerBuilder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ScriptRunner build() {
            ScriptRunner runner = new ScriptRunner();
            runner.pathToJavascript = pathToJavascript;
            runner.pathToConfig = pathToConfig;
            runner.contentType = contentType;

            runner.loadJavascript();
            return runner;
        }

    }

    private ScriptRunner() {}

    private String pathToJavascript;
    private String pathToConfig;
    private String contentType;
    private String javascriptMappingFunction;

    public void setPathToJavascript(String path) {
        pathToJavascript = path;
    }

    public String getPathToJavascript() {
        return pathToJavascript;
    }

    public void setPathToConfig(String path) {
        pathToConfig = path;
    }

    public String getPathToConfig() {
        return pathToConfig;
    }

    public void setContentType(String _contentType) {
        contentType = _contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public MessageMapper javaScriptMapper;

    private static final String CONTENT_TYPE_PLAIN = "text/plain";
    private static final String CONTENT_TYPE_BINARY = "application/octet-stream";

    private final static Config MAPPING_CONFIG = ConfigFactory.parseString("javascript {\n" +
            "        maxScriptSizeBytes = 50000 # 50kB\n" +
            "        maxScriptExecutionTime = 500ms\n" +
            "        maxScriptStackDepth = 10\n" +
            "      }");


    private void loadJavascript() {
        FileReader input;
        BufferedReader bufRead;
        String myLine;
        try {
            input = new FileReader(pathToJavascript);
            bufRead = new BufferedReader(input);
            while ((myLine = bufRead.readLine()) != null) {
                if (javascriptMappingFunction == null) {
                    javascriptMappingFunction = myLine + "\n";
                } else {
                    javascriptMappingFunction = javascriptMappingFunction + myLine + "\n";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MessageMapper javascriptRhinoMapper;

    private void testSomething() {
        // TODO: Mapping config configurable -> load from pathToConfig
        javascriptRhinoMapper = MessageMappers.createJavaScriptMessageMapper();
        javascriptRhinoMapper.configure(MAPPING_CONFIG,
                JavaScriptMessageMapperFactory
                        .createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap())
                        .contentType(contentType)
                        .incomingScript(javascriptMappingFunction)
                        .build());

        final String correlationId = UUID.randomUUID().toString();
        final Map<String, String> headers = new HashMap<>();
        headers.put("correlation-id", correlationId);
        headers.put("content-type", "text/plain");
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withText("hello")
                .build();

        System.out.println(javascriptRhinoMapper.map(message));
    }

    private void printJavascriptMappingFunction() {
        System.out.println(javascriptMappingFunction);
    }

//    private void setup() {
//        this.javaScriptMapper = MessageMappers.createJavaScriptMessageMapper();
//        this.javaScriptMapper.configure(MAPPING_CONFIG,
//                JavaScriptMessageMapperFactory
//                        .createJavaScriptMessageMapperConfigurationBuilder(Collections.<String, String>emptyMap())
//                        .contentType(CONTENT_TYPE_BINARY)
//                        .incomingScript(MAPPING_INCOMING_DEFAULT)
//                        .outgoingScript(MAPPING_OUTGOING_DEFAULT)
//                        .build()
//        );
//    }

    public static void main(String[] args) {

        ScriptRunner runner =
                new ScriptRunnerBuilder().withJavascriptPath("scriptrunner/javascript/incomingscript")
                        .withConfigPath("")
                        .withContentType("text/plain")
                        .build();

//        runner.printJavascriptMappingFunction();

        runner.testSomething();

//        ScriptRunner runner = new ScriptRunner();
//        runner.setup();
//        runner.loadJavascript("scriptrunner/javascript/incomingscript");
//        runner.printStrings();
//        final String correlationId = UUID.randomUUID().toString();
//        final Map<String, String> headers = new HashMap<String, String>();
//        headers.put("correlation-id", correlationId);
//        headers.put(ExternalMessage.CONTENT_TYPE_HEADER, DittoConstants.DITTO_PROTOCOL_CONTENT_TYPE);
//
//        final ModifyAttribute modifyAttribute = ModifyAttribute.of("org.eclipse.ditto:fancy-car-11",
//                JsonPointer.of("foo"),
//                JsonValue.of("hello"),
//                DittoHeaders.newBuilder().correlationId(correlationId).schemaVersion(JsonSchemaVersion.V_2).build());
//
//        final Adaptable inputAdaptable = DittoProtocolAdapter.newInstance().toAdaptable(modifyAttribute);
//        final JsonifiableAdaptable jsonifiableAdaptableInputAdaptable =
//                ProtocolFactory.wrapAsJsonifiableAdaptable(inputAdaptable);
//        final ExternalMessage message =
//                ExternalMessageFactory.newExternalMessageBuilder(headers)
//                        .withText(jsonifiableAdaptableInputAdaptable.toJsonString())
//                        .build();
//
//        final Optional<Adaptable> adaptableOpt = runner.javaScriptMapper.map(message);
//        final Adaptable mappedAdaptable = adaptableOpt.get();
//        System.out.print(mappedAdaptable);
//
//        assertThat(mappedAdaptable).isEqualTo(jsonifiableAdaptableInputAdaptable);

    }
}