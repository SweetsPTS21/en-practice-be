package com.swpts.enpracticebe.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "stt.google")
public class GoogleSttProperties {

    private boolean enabled;
    private String projectId;
    private String location = "global";
    private String recognizer = "_";
    private List<String> languageCodes = List.of("en-US");
    private String model = "latest_long";
    private boolean interimResults = true;
    private String endpoint = "speech.googleapis.com:443";
    private String credentialsBase64;


    public String recognizerPath() {
        return String.format(
                "projects/%s/locations/%s/recognizers/%s",
                projectId, location, recognizer
        );
    }
}
