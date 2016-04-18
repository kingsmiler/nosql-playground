package org.xman.nosql.ssl;


import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NoneKeystoreTest {

    public static void main(String[] args) {
        final SSLConnectionSocketFactory sslsf;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();

        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(100);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .build();

    }

    @Test
    public void testSelfSignedFailure() throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("https://localhost:8443/hello");

            assertThatThrownBy(() -> {
                try (CloseableHttpResponse response = client.execute(get)) {
                    assertThat(response.getStatusLine().getStatusCode())
                            .isEqualTo(HttpStatus.SC_OK);
                    assertThat(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8))
                            .contains("apache2");
                }
            })
                    .isInstanceOf(SSLHandshakeException.class)
                    .hasMessageContaining("sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target");
        }
    }

    @Test
    public void testSelfSignedSuccess() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy trustStrategy = new TrustSelfSignedStrategy();

        SSLContext sslContext =
                SSLContexts
                        .custom()
                        .loadTrustMaterial(trustStrategy)
                        .build();

        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        try (CloseableHttpClient client =
                     HttpClients
                             .custom()
                             .setSSLContext(sslContext)
                             .setSSLHostnameVerifier(hostnameVerifier)
                             .build()) {
            HttpGet get = new HttpGet("https://localhost:8443/hello");

            try (CloseableHttpResponse response = client.execute(get)) {
                assertThat(response.getStatusLine().getStatusCode())
                        .isEqualTo(HttpStatus.SC_OK);
                assertThat(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8))
                        .contains("apache2");
            }
        }
    }
}
