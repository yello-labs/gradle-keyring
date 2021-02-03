import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionalTest {
  private static final Logger logger = LoggerFactory.getLogger(FunctionalTest.class);
  static File projectDir = new File("build/functionalTestDir");
  static String settingsTemplate = "plugins {%s} %s";
  static String buildTemplate = "plugins {%s}  %s";

  @Before
  public void setUp() throws IOException {
    projectDir.mkdirs();
    Assume.assumeTrue(projectDir.canRead());
    Assume.assumeTrue(projectDir.canWrite());
    // Everything needs a settings.gradle file.
    writeFile(new File(projectDir, "settings.gradle"), String.format(settingsTemplate, "", ""));
  }

  @Test
  public void templateTest() {
    String addition = "notinthetemplate";
    String test = String.format(settingsTemplate, addition, "");
    Assert.assertTrue(test, test.contains(addition));
  }

  @Test
  public void testTest() throws IOException {
    writeFile(new File(projectDir, "build.gradle"), String.format(buildTemplate, "", ""));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  public void applyToProject() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n" + "import org.yello.labs.KeyringPlugin;\n" + "KeyringPlugin.getSecret('', '')"));
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withDebug(true)
            .withArguments("build", "--stacktrace", "-i")
            .build();
  }

  @Test
  public void applyToSettings() throws IOException {
    writeFile(
        new File(projectDir, "settings.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n" + "import org.yello.labs.KeyringPlugin;\n" + "KeyringPlugin.getSecret('', '')"));
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  public void setSecret() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "def added = KeyringPlugin.setSecret('', "
                + "'', '')"));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
    Assert.assertTrue(true);
  }

  @Test
  public void getSecret() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "final String pass = KeyringPlugin"
                + ".getSecret('', '')"));
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  public void stringInterpolation() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n" + "import org.yello.labs.KeyringPlugin;\n" + "KeyringPlugin.getSecret('', '')"));
    // TODO: Add setting, then getting and comapring with a string known to give java issues

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  public void specialCharactersCheck() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n" + "import org.yello.labs.KeyringPlugin;\n" + "KeyringPlugin.getSecret('', '')"));
    // TODO: Add setting, then getting and comapring with a string known to give java issues

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  public void runtimeMethodCheck() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "  \n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "\n"
                + "task(\"test\") {\n"
                + "    "
                + "doLast {\n"
                + "        KeyringPlugin.getSecret('','')\n"
                + "    }\n"
                + "}"));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  public void methodAvailableEverywhere() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs.gradle-keyring'",
            "  \n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "KeyringPlugin.getSecret('','')\n"
                + "task(\"test\") {\n"
                + "    doLast {\n"
                + "        KeyringPlugin.getSecret('','')\n"
                + "    }\n"
                + "}\n"
                + "gradle.afterProject {\n"
                + "    KeyringPlugin.getSecret('','')\n"
                + "}\n"
                + "\n"
                + "gradle.beforeProject {\n"
                + "    KeyringPlugin.getSecret('','')\n"
                + "}"));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test
  @Ignore
  public void applyConfigurations() throws IOException {
    writeFile(new File(projectDir, "build.gradle"), String.format(buildTemplate, "", ""));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @After
  public void tearDown() {
    projectDir.delete();
  }

  private void writeFile(File file, String content) throws IOException {
    if (file.exists()) {
      file.delete();
    }
    file.getParentFile().mkdirs();
    file.createNewFile();

    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write(content.getBytes());
    }
  }
}
