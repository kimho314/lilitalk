package com.luna.groupchat

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = GroupChatApplication)
class GroupChatApplicationSpec extends Specification {
    void contextLoad() {
        expect:
        true
    }
}
