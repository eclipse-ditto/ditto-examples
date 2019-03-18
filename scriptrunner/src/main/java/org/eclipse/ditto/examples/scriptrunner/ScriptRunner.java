package org.eclipse.ditto.examples.scriptrunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.ProtocolFactory;
import org.eclipse.ditto.services.connectivity.mapping.DittoMessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMappers;
import org.eclipse.ditto.services.connectivity.mapping.javascript.JavaScriptMessageMapperFactory;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ScriptRunner {

    public static class ScriptRunnerBuilder {

        private Config mappingConfig;
        private String contentType;
        private MessageMapper rhinoMapper;
        private final String DEFAULT_MAPPING_CONFIG = "javascript {\n" +
                "        maxScriptSizeBytes = 50000 # 50kB\n" +
                "        maxScriptExecutionTime = 500ms\n" +
                "        maxScriptStackDepth = 10\n" +
                "        }";
        private final String DEFAULT_CONTENT_TYPE = "application/json";

        public ScriptRunnerBuilder() {
            this.mappingConfig = ConfigFactory.parseString(DEFAULT_MAPPING_CONFIG);
            this.contentType = DEFAULT_CONTENT_TYPE;
        }

        public ScriptRunnerBuilder withConfig(String mappingConfig) {
            this.mappingConfig = ConfigFactory.parseString(mappingConfig);
            return this;
        }

        public ScriptRunnerBuilder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ScriptRunnerBuilder withIncomingScriptOnly(String mappingFunction) {
            this.rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
            this.rhinoMapper.configure(this.mappingConfig,
                    JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(
                            Collections.emptyMap())
                            .contentType(this.contentType)
                            .incomingScript(mappingFunction)
                            .build());
            return this;
        }

        public ScriptRunnerBuilder withOutgoingScriptOnly(String mappingFunction) {
            this.rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
            this.rhinoMapper.configure(this.mappingConfig,
                    JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(
                            Collections.emptyMap())
                            .contentType(this.contentType)
                            .outgoingScript(mappingFunction)
                            .build());
            return this;
        }

        public ScriptRunnerBuilder withInAndOutgoingScript(String incomingFunction, String outgoingFunction) {
            this.rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
            this.rhinoMapper.configure(this.mappingConfig,
                    JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(
                            Collections.emptyMap())
                    .contentType(this.contentType)
                    .incomingScript(ScriptRunner.readFromFile(incomingFunction))
                    .outgoingScript(ScriptRunner.readFromFile(outgoingFunction))
                    .build());
            return this;
        }

        public ScriptRunner build() {
            ScriptRunner runner = new ScriptRunner();

            runner.mappingConfig = this.mappingConfig;
            runner.contentType = this.contentType;
            runner.rhinoMapper = this.rhinoMapper;

            return runner;
        }
    }

    private ScriptRunner() {}

    private Config mappingConfig;
    private String contentType;
    private MessageMapper rhinoMapper;

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setRhinoMapper(MessageMapper mapper) {
        this.rhinoMapper = mapper;
    }

    public static String readFromFile(String scriptPath) {
        FileReader input;
        BufferedReader bufRead;
        String myLine;
        String fromFile = null;
        try {
            input = new FileReader(scriptPath);
            bufRead = new BufferedReader(input);
            while ((myLine = bufRead.readLine()) != null) {
                if (fromFile == null) {
                    fromFile = myLine + "\n";
                } else {
                    fromFile = fromFile + myLine + "\n";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fromFile;
    }

    public Adaptable adaptableFromExternalMessage(ExternalMessage message) {
        DittoMessageMapper mapper = new DittoMessageMapper();
        Adaptable adaptable = mapper.map(message).get();
        return adaptable;
    }

    public Adaptable adaptableFromJson(String JSON, DittoHeaders headers) {
        JsonObject obj = JsonFactory.newObject(JSON);
        return ProtocolFactory.jsonifiableAdaptableFromJson(obj).setDittoHeaders(headers);
    }

    public Adaptable adaptableFromString(String dittoProtocolMessage) {
        JsonObject obj = JsonFactory.newObject(dittoProtocolMessage);
        return ProtocolFactory.jsonifiableAdaptableFromJson(obj);
    }

    public Adaptable mapExternalMessage(ExternalMessage message) {
        Optional<Adaptable> adaptableOpt = rhinoMapper.map(message);
        return adaptableOpt.isPresent() ? adaptableOpt.get() : null;
    }

    public ExternalMessage externalMessageFromAdaptable(Adaptable adaptable) {
        DittoMessageMapper mapper = new DittoMessageMapper();
        Optional<ExternalMessage> message = mapper.map(adaptable);
        return message.isPresent() ? message.get() : null;
    }

    public ExternalMessage mapAdaptable(Adaptable adaptable) {
        Optional<ExternalMessage> externalMessage = rhinoMapper.map(adaptable);
        return externalMessage.isPresent() ? externalMessage.get() : null;
    }

}