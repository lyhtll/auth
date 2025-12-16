package com.example.auth.global.security.jwt.config

import com.example.auth.global.config.properties.CorsProperties
import com.example.auth.global.config.properties.JwtKeyProperties
import com.example.auth.global.security.jwt.properties.CookieProperties
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Configuration
@EnableConfigurationProperties(JwtKeyProperties::class, CookieProperties::class, CorsProperties::class)
class JwtConfig(
    private val jwtKeyProperties: JwtKeyProperties
) {

    @Bean
    fun rsaPublicKey(): RSAPublicKey {
        val publicKeyPEM = jwtKeyProperties.publicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(publicKeyPEM)
        val spec = X509EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec) as RSAPublicKey
    }

    @Bean
    fun rsaPrivateKey(): RSAPrivateKey {
        val privateKeyPEM = jwtKeyProperties.privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decoded = Base64.getDecoder().decode(privateKeyPEM)
        val spec = PKCS8EncodedKeySpec(decoded)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePrivate(spec) as RSAPrivateKey
    }

    @Bean
    fun jwtDecoder(rsaPublicKey: RSAPublicKey): JwtDecoder {
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build()
    }

    @Bean
    fun jwtEncoder(rsaPublicKey: RSAPublicKey, rsaPrivateKey: RSAPrivateKey): JwtEncoder {
        val jwk = RSAKey.Builder(rsaPublicKey)
            .privateKey(rsaPrivateKey)
            .build()
        val jwks = ImmutableJWKSet<SecurityContext>(JWKSet(jwk))
        return NimbusJwtEncoder(jwks)
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("role")
            setAuthorityPrefix("ROLE_")
        }

        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        }
    }
}
