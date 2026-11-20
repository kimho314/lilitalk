package com.luna.demokafka;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ShortUuidTest {

    private static final Logger log = LoggerFactory.getLogger(ShortUuidTest.class);

    private String randomBase64UrlUuid() {
        return toBase64Url(UUID.randomUUID());
    }

    private String toBase64Url(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(buffer.array());
    }
    @Test
    void createShortUuid(){
        String uuid = randomBase64UrlUuid();
        log.info("uuid:{}", uuid);
    }

}
