package xyz.staffjoy.faraday.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 追踪配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TracingProperties {
    /**
     * Flag for enabling and disabling tracing HTTP requests proxying processes.
     */
    private boolean enabled;
}
