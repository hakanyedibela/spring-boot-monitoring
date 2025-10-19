package hkn7b.dev.config;

import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class PodInfoMdcFilter implements Filter {

    @Value("${POD_NAME:unknown}")
    private String podName;

    @Value("${POD_NAMESPACE:unknown}")
    private String podNamespace;

    @Value("${POD_IP:unknown}")
    private String podIp;

    @Value("${NODE_NAME:unknown}")
    private String nodeName;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            MDC.put("pod.name", podName);
            MDC.put("pod.namespace", podNamespace);
            MDC.put("pod.ip", podIp);
            MDC.put("node.name", nodeName);

            chain.doFilter(request, response);
        } finally {
            MDC.remove("pod.name");
            MDC.remove("pod.namespace");
            MDC.remove("pod.ip");
            MDC.remove("node.name");
        }
    }
}
