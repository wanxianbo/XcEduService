package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJWT {

    @Test
    public void testCreateJwtToken() {
        //定义证书文件
        String keyLocation = "xc.keystore";
        //密钥库密码
        String keystorePassword = "xuechengkeystore";
        //证书文件路径
        ClassPathResource resource = new ClassPathResource(keyLocation);
        //密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, keystorePassword.toCharArray());
        //密钥别名
        String alias = "xckey";
        //密钥密码，要与别名匹配
        String keypassword = "xuecheng";
        //密钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypassword.toCharArray());
        //得到私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //定义内容payload
        Map<String, String> map = new HashMap<>();
        map.put("name", "itcast");
        String json = JSON.toJSONString(map);
        Jwt jwt = JwtHelper.encode(json, new RsaSigner(aPrivate));
        String token = jwt.getEncoded();
        System.out.println(token);
        //eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaXRjYXN0In0.lQOqL1s4DpDHROUAibkz6EMf6hcM7HmTPgmg-SlkacVoQAV7y3XQ7LXxiua6SJlN_uNX_EFjzIshEg_kyy972DtymtRMc2NIO5HzIF5I4oQCxNPsJdhu6qQni6sTas3q0JbAarMZSajDX7HhzVSYWPQJCussA4e1r9oFxDcoAo6TEAXOW8gRHzNIygQz1yCj6mdf4UOHI070kRy7f3BdhmrUJdOuDIMoRBYS4WsEOibAU1UCNPaJAXpZC0ihrtdY7SCg1N43fimeFOHrfpLb6OmRF7v7uvGMgrhg9JIYDbJ6nbode5OJkNceRx8QUICre2yKAe0ctlvXO0REf6OpRA
    }

    @Test
    public void testVerify() {
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1OTc4NzgwNTUsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsImNvdXJzZV9maW5kX3BpYyIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6ImM4OTlmMjVlLTVkN2UtNDNkZC1hMDBkLTdkYWNkNTY5Njg4YyIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.g2ozphljLF0dpQMsoiZ4My4EN3KOpj-0IZJNwpVLt1X3KOWKGg-Ycnqzn46oYZM3M1US-YDCnXBKO891uZxMv_i_C3uTOON9u5cKLrnoZJgbHfn_8sggYE5asfcHrpOcTmph1Pw6_b11xHkES0a2-n9xqo8luU9oqqx-uu_3svbkGtr_yJmqLk4tKsfNIO5wxEuhM3mE-HkrEktw2auJKNBDYx9t036c4AU0-12bPFO7g8R3MzzBe_1L72zhJcK9HUlX8u-iNflkuiTCG7_FiAP_vUQnRn2I2T8by1X6F7oG-_gDfyagnooLhe8Xa0u49QZCLukTU3enmugs_iG8Ag";
        //公钥
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //验证令牌
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        //获取内容
        String claims = jwt.getClaims();
        System.out.println(claims);
    }

    @Test
    public void getUUID() {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid.toString());
    }
}
