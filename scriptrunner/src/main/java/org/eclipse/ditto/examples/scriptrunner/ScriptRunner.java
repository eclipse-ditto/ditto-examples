package org.eclipse.ditto.examples.scriptrunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

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
        private final String DEFAULT_MAPPING_CONFIG = "javascript {\n" +
                "        maxScriptSizeBytes = 50000 # 50kB\n" +
                "        maxScriptExecutionTime = 500ms\n" +
                "        maxScriptStackDepth = 10\n" +
                "        }";

        public ScriptRunnerBuilder() {}

        public ScriptRunnerBuilder withConfig(String mappingConfig) {
            this.mappingConfig = ConfigFactory.parseString(mappingConfig);
            return this;
        }

        public ScriptRunner build() {
            ScriptRunner runner = new ScriptRunner();
            if (this.mappingConfig != null) runner.mappingConfig = this.mappingConfig;
            else runner.mappingConfig = ConfigFactory.parseString(DEFAULT_MAPPING_CONFIG);
            return runner;
        }
    }

    private ScriptRunner() {}

    private Config mappingConfig;

    public String readFromFile(String scriptPath) {
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

    public Adaptable adaptableMappedFromExternalMessage(ExternalMessage message) {
        DittoMessageMapper mapper = new DittoMessageMapper();
        Adaptable adaptable = mapper.map(message).get();
        return adaptable;
    }

    public Adaptable handleDittoProtocolMessageFromJson(String pathToJSON, DittoHeaders headers) {
        JsonObject obj = JsonObject.of(readFromFile(pathToJSON));
        return ProtocolFactory.jsonifiableAdaptableFromJson(obj).setDittoHeaders(headers);
    }

    public Adaptable handleDittoProtocolMessageFromString(String dittoProtocolMessage) {
        JsonObject obj = JsonObject.of(dittoProtocolMessage);
        return ProtocolFactory.jsonifiableAdaptableFromJson(obj);
    }

    public Adaptable handleExternalMessageWithMappingFromFile(ExternalMessage message, String scriptPath,
            String contentType) {
        MessageMapper rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
        rhinoMapper.configure(this.mappingConfig,
                JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap())
                        .contentType(contentType)
                        .incomingScript(readFromFile(scriptPath))
                        .build());

        Optional<Adaptable> adaptableOpt = rhinoMapper.map(message);
        return adaptableOpt.isPresent() ? adaptableOpt.get() : null;
    }

    public Adaptable handleExternalMessageWithMappingFromString(ExternalMessage message, String javascript,
            String contentType) {
        MessageMapper rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
        rhinoMapper.configure(this.mappingConfig,
                JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap())
                        .contentType(contentType)
                        .incomingScript(javascript)
                        .build());
        Optional<Adaptable> adaptableOpt = rhinoMapper.map(message);
        return adaptableOpt.isPresent() ? adaptableOpt.get() : null;
    }

    public ExternalMessage messageFromAdaptable(Adaptable adaptable) {
        DittoMessageMapper mapper = new DittoMessageMapper();
        Optional<ExternalMessage> message = mapper.map(adaptable);
        return message.isPresent() ? message.get() : null;
    }

    public ExternalMessage messageFromAdaptableMappedFromFile(String scriptPath, Adaptable adaptable,
            String contentType) {
        MessageMapper rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
        rhinoMapper.configure(this.mappingConfig,
                JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap())
                        .contentType(contentType)
                        .outgoingScript(readFromFile(scriptPath))
                        .build());

        Optional<ExternalMessage> externalMessage = rhinoMapper.map(adaptable);
        return externalMessage.isPresent() ? externalMessage.get() : null;
    }

    public ExternalMessage messageFromAdaptableMappedFromString(String javascript, Adaptable adaptable,
            String contentType) {
        MessageMapper rhinoMapper = MessageMappers.createJavaScriptMessageMapper();
        rhinoMapper.configure(this.mappingConfig,
                JavaScriptMessageMapperFactory.createJavaScriptMessageMapperConfigurationBuilder(Collections.emptyMap())
                        .contentType(contentType)
                        .outgoingScript(javascript)
                        .build());
        Optional<ExternalMessage> externalMessage = rhinoMapper.map(adaptable);
        return externalMessage.isPresent() ? externalMessage.get() : null;
    }

}