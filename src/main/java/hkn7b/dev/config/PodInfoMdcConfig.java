package hkn7b.dev.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PodInfoMdcConfig {

    @Value("${POD_NAME:unknown}")
    private String podName;

    @Value("${POD_NAMESPACE:unknown}")
    private String podNamespace;

    @Value("${POD_IP:unknown}")
    private String podIp;

    @Value("${NODE_NAME:unknown}")
    private String nodeName;

    @PostConstruct
    public void initMdc() {
        MDC.put("pod.name", podName);
        MDC.put("pod.namespace", podNamespace);
        MDC.put("pod.ip", podIp);
        MDC.put("node.name", nodeName);
    }
}
