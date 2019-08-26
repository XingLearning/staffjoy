package xyz.staffjoy.common.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service is an app on Staffjoy that runs on a subdomain
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    /**
     * Public, Authenticated, or Admin
     */
    private int security;
    /**
     * If true, service is suppressed in stage and prod
     */
    private boolean restrictDev;
    /**
     * Backend service to query
     * 后端服务查询
     */
    private String backendDomain;
    /**
     * If true, injects a header for HTML responses telling the browser not to cache HTML
     * 如果为true，则为HTML响应注入一个标头，告知浏览器不要缓存HTML
     */
    private boolean noCacheHtml;
}
