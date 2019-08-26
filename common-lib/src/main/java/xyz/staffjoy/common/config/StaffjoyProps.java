package xyz.staffjoy.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix="staffjoy.common")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffjoyProps {

    /**
     * sentry 第三方开源日志收集平台
     */
    @NotBlank
    private String sentryDsn;

    // DeployEnvVar is set by Kubernetes during a new deployment so we can identify the code version
    @NotBlank
    private String deployEnv;
}

