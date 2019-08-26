package xyz.staffjoy.faraday.core.http;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.staffjoy.faraday.config.FaradayProperties;
import xyz.staffjoy.faraday.config.MappingProperties;
import xyz.staffjoy.faraday.core.interceptor.PreForwardRequestInterceptor;
import xyz.staffjoy.faraday.core.mappings.MappingsProvider;
import xyz.staffjoy.faraday.core.trace.ProxyingTraceInterceptor;
import xyz.staffjoy.faraday.exceptions.FaradayException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * faraday 代理控制类
 */
public class ReverseProxyFilter extends OncePerRequestFilter {

    /**
     * （XFF）报头是用于通过 HTTP 代理或负载平衡器识别连接到 web 服务器的客户端的发起 IP 地址的事实上的标准报头。
     */
    protected static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    /**
     * （XFP）报头是用于识别协议（HTTP 或 HTTPS）
     */
    protected static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    /**
     * （XFH）报头是用于识别由客户机在所要求的原始主机一个事实上的标准报头Host的 HTTP 请求报头。
     */
    protected static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    protected static final String X_FORWARDED_PORT_HEADER = "X-Forwarded-Port";

    private static final ILogger log = SLoggerFactory.getLogger(ReverseProxyFilter.class);

    protected final FaradayProperties faradayProperties;
    /**
     * 请求参数抽取
     */
    protected final RequestDataExtractor extractor;
    protected final MappingsProvider mappingsProvider;
    protected final RequestForwarder requestForwarder;
    protected final ProxyingTraceInterceptor traceInterceptor;
    protected final PreForwardRequestInterceptor preForwardRequestInterceptor;

    public ReverseProxyFilter(
            FaradayProperties faradayProperties,
            RequestDataExtractor extractor,
            MappingsProvider mappingsProvider,
            RequestForwarder requestForwarder,
            ProxyingTraceInterceptor traceInterceptor,
            PreForwardRequestInterceptor requestInterceptor
    ) {
        this.faradayProperties = faradayProperties;
        this.extractor = extractor;
        this.mappingsProvider = mappingsProvider;
        this.requestForwarder = requestForwarder;
        this.traceInterceptor = traceInterceptor;
        this.preForwardRequestInterceptor = requestInterceptor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String originUri = extractor.extractUri(request);
        String originHost = extractor.extractHost(request);

        HttpHeaders headers = extractor.extractHttpHeaders(request);
        HttpMethod method = extractor.extractHttpMethod(request);

        log.debug("Incoming: %s %s %s -> request", "method", method, "host", originHost, "uri", originUri);

        String traceId = traceInterceptor.generateTraceId();
        traceInterceptor.onRequestReceived(traceId, method, originHost, originUri, headers);

        //路由解析
        MappingProperties mapping = mappingsProvider.resolveMapping(originHost, request);
        if (mapping == null) {
            traceInterceptor.onNoMappingFound(traceId, method, originHost, originUri, headers);

            log.debug(String.format("Forwarding: %s %s %s -> no mapping found", method, originHost, originUri));

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Unsupported domain");
            return;
        } else {
            log.debug(String.format("Forwarding: %s %s %s -> %s", method, originHost, originUri, mapping.getDestinations()));
        }

        byte[] body = extractor.extractBody(request);
        // 添加转发 headers
        addForwardHeaders(request, headers);

        RequestData dataToForward = new RequestData(method, originHost, originUri, headers, body, request);
        // 转发 前的截获器
        preForwardRequestInterceptor.intercept(dataToForward, mapping);
        if (dataToForward.isNeedRedirect() && !isBlank(dataToForward.getRedirectUrl())) {
            log.debug(String.format("Redirecting to -> %s", dataToForward.getRedirectUrl()));
            response.sendRedirect(dataToForward.getRedirectUrl());
            return;
        }
        // 进行请求转发
        ResponseEntity<byte[]> responseEntity =
                requestForwarder.forwardHttpRequest(dataToForward, traceId, mapping);
        // 处理转发响应
        this.processResponse(response, responseEntity);
    }

    protected void addForwardHeaders(HttpServletRequest request, HttpHeaders headers) {
        List<String> forwordedFor = headers.get(X_FORWARDED_FOR_HEADER);
        if (isEmpty(forwordedFor)) {
            forwordedFor = new ArrayList<>(1);
        }
        forwordedFor.add(request.getRemoteAddr());
        headers.put(X_FORWARDED_FOR_HEADER, forwordedFor);
        headers.set(X_FORWARDED_PROTO_HEADER, request.getScheme());
        headers.set(X_FORWARDED_HOST_HEADER, request.getServerName());
        headers.set(X_FORWARDED_PORT_HEADER, valueOf(request.getServerPort()));
    }


    protected void processResponse(HttpServletResponse response, ResponseEntity<byte[]> responseEntity) {
        response.setStatus(responseEntity.getStatusCode().value());
        responseEntity.getHeaders().forEach((name, values) ->
                values.forEach(value -> response.addHeader(name, value))
        );
        if (responseEntity.getBody() != null) {
            try {
                //得到响应body
                response.getOutputStream().write(responseEntity.getBody());
            } catch (IOException e) {
                throw new FaradayException("Error writing body of HTTP response", e);
            }
        }
    }
}