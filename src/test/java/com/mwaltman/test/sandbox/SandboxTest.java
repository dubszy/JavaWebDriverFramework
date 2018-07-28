package com.mwaltman.test.sandbox;

import com.mwaltman.podriverframework.common.session.Session;
import com.mwaltman.test.common.BaseTest;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class SandboxTest extends BaseTest {

    @Test
    public void sandboxTest() {

        try (Session session = Session.defaultSession("https://www.google.com")) {

            session.getDriverEnvironment().goToUrl("https://www.google.com");

            // Simple Hamcrest assertion (true should not be false)
            assertThat("True should not be false", true, not(is(false)));

            // We won't reach this line if the above assertion fails
            log.info("\n\033[32mIt Works!\033[m\n");
        }
    }
}
