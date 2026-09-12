package com.luna.directchat

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = DirectChatApplication)
class DirectChatApplicationSpec extends Specification {

    void contextLoad() {
        expect:
        true
    }
}
