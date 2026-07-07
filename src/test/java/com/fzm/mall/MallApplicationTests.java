package com.fzm.mall;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class MallApplicationTests {

    @Test
    void contextLoads() {

    }

}
