package com.li.gamenetty.reactive.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.io.File;

/**
 * @author li-yuanwen
 */
@Configuration
@Getter
public class SslConfig {

    /** ssl 协议 **/
    @Value("${netty.ssl.protocol:}")
    private String protocol;

    /** ssl ca证书Path **/
    @Value("${netty.openssl.caPath:}")
    private String caPath;

    /** ssl crtPath **/
    @Value("${netty.openssl.server.crtPath:}")
    private String serverCrtPath;

    /** ssl 秘钥pkcs#8编码 **/
    @Value("${netty.openssl.server.pkcs8key.path}")
    private String serverPkcs8KeyPath;


    /** ssl pkPath **/
    @Value("${netty.openssl.client.crtPath:}")
    private String clientCrtPath;

    /** ssl 秘钥pkcs#8编码 **/
    @Value("${netty.openssl.client.pkcs8key.path}")
    private String clientPkcs8KeyPath;


    @Bean("serverSslContext")
    @ConditionalOnExpression("${netty.openssl.enable:false}")
    public SslContext createServerSslContext() throws SSLException {
        // 服务端所需证书
        File certChainFile = new File(serverCrtPath);
        File keyFile = new File(serverPkcs8KeyPath);
        File rootFile = new File(caPath);

        return SslContextBuilder.forServer(certChainFile, keyFile)
                .trustManager(rootFile)
                .build();
    }

    @Bean("clientSslContext")
    @ConditionalOnExpression("${netty.openssl.enable:false}")
    public SslContext createClientSslContext() throws SSLException {
        // 服务端所需证书
        File certChainFile = new File(clientCrtPath);
        File keyFile = new File(clientPkcs8KeyPath);
        File rootFile = new File(caPath);
        return SslContextBuilder.forClient()
                .keyManager(certChainFile, keyFile)
                .trustManager(rootFile)
                .build();
    }

}
