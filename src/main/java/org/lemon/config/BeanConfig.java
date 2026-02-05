package org.lemon.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.lemon.service.PropertiesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2024/10/26 12:20:55
 */
@Slf4j
@Configuration
public class BeanConfig {

    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory(PropertiesService props) {
        // 1. 创建 HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // 2. 配置代理
        if (StrUtil.isNotBlank(props.getProxyUrl())) {
            try {
                URL url = new URL(props.getProxyUrl());
                int port = url.getPort();
                if (port == -1) {
                    // 自动获取默认端口
                    port = url.getDefaultPort();
                }
                // 3.创建代理主机对象
                HttpHost proxyHost = new HttpHost(url.getProtocol(), url.getHost(), port);
                // 4.添加代理认证信息
                if (StrUtil.isNotBlank(props.getProxyUserName()) && StrUtil.isNotBlank(props.getProxyPassword())) {
                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(
                            new AuthScope(proxyHost),
                            new UsernamePasswordCredentials(props.getProxyUserName(), props.getProxyPassword().toCharArray())
                    );
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                // 5.创建连接池
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                connectionManager.setMaxTotal(50);
                connectionManager.setDefaultMaxPerRoute(20);
                // 设置代理+连接池
                httpClientBuilder.setProxy(proxyHost).setConnectionManager(connectionManager);
            } catch (Exception e) {
                log.error("代理配置错误：", e);
            }
        }

        // 6. 配置超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                // 超时 10 秒
                .setResponseTimeout(Timeout.ofSeconds(10))
                // 从连接池获取连接超时 2 秒
                .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                .build();
        // 7.创建 HttpClient
        CloseableHttpClient httpClient = httpClientBuilder
                .setDefaultRequestConfig(requestConfig)
                // 空闲连接回收
                .evictIdleConnections(Timeout.ofSeconds(5))
                .build();

        // 8. 创建请求工厂
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    public RestTemplate restTemplate(HttpComponentsClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean("commonExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(processors);
        executor.setCorePoolSize(processors * 2 + 1);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("MyBill-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
