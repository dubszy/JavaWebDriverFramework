package com.mwaltman.test.sandbox;

import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class SandboxTest {

    private static final Logger log = Logger.getLogger(SandboxTest.class);

    @Test
    public void sandboxTest() {
        assertThat("True should not be false", true, not(is(false)));

        log.info("\n\033[32mIt Works!\033[m\n");
    }
}
