package net.gradleutil.conf.util;

import net.gradleutil.conf.bean.Manytyped;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class BLTest {

    File configFile;
    @BeforeEach

    public void setUp() {
        configFile = new File("src/test/resources/conf/manytyped.conf");
    }

    @Test
    public void test() throws IOException {
        Manytyped manytyped;
        manytyped = new Manytyped();
        ConfUtil.setBeanFromConf(manytyped, Arrays.toString(Files.readAllBytes(configFile.toPath())));
        assert(manytyped.get_1funkyProperty().equals("funky1"));
        assert (manytyped.getTasks().size() == 1);
    }
}
