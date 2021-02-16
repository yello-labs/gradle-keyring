import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class FunctionalTest {
  private static final Logger logger = LoggerFactory.getLogger(FunctionalTest.class);
  static File projectDir = new File("build/functionalTestDir");
  static String settingsTemplate = "plugins {%s} %s";
  static String buildTemplate = "plugins {%s}  %s";

  @BeforeTest
  public void setUp() throws IOException {
    projectDir.mkdirs();

    Assert.assertTrue(projectDir.canRead());
    Assert.assertTrue(projectDir.canWrite());
    // Everything needs a settings.gradle file.
    writeFile(new File(projectDir, "settings.gradle"), String.format(settingsTemplate, "", ""));
  }

  @Test
  public void templateTest() {
    String addition = "notinthetemplate";
    String test = String.format(settingsTemplate, addition, "");
    Assert.assertTrue(test.contains(addition), test);
  }

  @Test(dependsOnMethods = {"templateTest"})
  public void functionalTestTest() throws IOException {
    writeFile(new File(projectDir, "build.gradle"), String.format(buildTemplate, "", ""));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test(dependsOnMethods = {"functionalTestTest"})
  public void applyToProject() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "KeyringPlugin.setSecret('test', 'test', 'test')"));
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withDebug(true)
            .withArguments("build", "--stacktrace", "-i")
            .build();
  }

  @Test(dependsOnMethods = {"applyToProject"})
  public void applyToSettings() throws IOException {
    writeFile(
        new File(projectDir, "settings.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "KeyringPlugin.getSecret('test', 'test')"));
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test(dependsOnMethods = {"applyToProject"})
  public void setSecret() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "def added = KeyringPlugin.setSecret('domain', 'username', 'P@sSw0Rd')"));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
    Assert.assertTrue(true);
  }

  @Test()
  public void sourceFromEnv() throws IOException {
    String password = "secret";
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "def pass = KeyringPlugin.getSecret('httpsgoogleorg', 'SomethingPlausible')\n"
                + "println(pass)"));
    File dotenv = new File(projectDir, ".env");

    // TODO: Get input, none of this is ideal
    FileOutputStream a = new FileOutputStream(dotenv);
    a.write(("httpsgoogleorg_SomethingPlausible=" + password).getBytes());
    a.close();

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withDebug(true)
            .withProjectDir(projectDir)
            .withArguments("build", "-Porg.yello.labs.env")
            .build();
    Assert.assertTrue(result.getOutput().contains(password));
  }

  @Test(dependsOnMethods = {"setSecret"})
  public void getSecret() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "final String pass = KeyringPlugin"
                + ".getSecret('domain', 'username') \n"
                + "println(pass)")); // Dont ever do this though, please
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
    Assert.assertTrue(result.getOutput().contains("P@sSw0Rd"), result.getOutput());
  }

  @Test(
      dependsOnMethods = {"applyToProject"},
      enabled = false)
  public void stringInterpolation() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "KeyringPlugin.getSecret('test', 'test')"));
    // TODO: Add setting, then getting and comapring with a string known to give java issues

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test(
      dependsOnMethods = {"applyToProject"},
      enabled = false)
  public void specialCharactersCheck() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "KeyringPlugin.getSecret('test', 'test')"));
    // TODO: Add setting, then getting and comapring with a string known to give java issues

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test(dependsOnMethods = {"applyToProject"})
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
                + "        KeyringPlugin.getSecret('test','test')\n"
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

  @Test(dependsOnMethods = {"applyToProject"})
  public void methodAvailableEverywhere() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs.gradle-keyring'",
            "  \n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "KeyringPlugin.getSecret('test','test')\n"
                + "task(\"test\") {\n"
                + "    doLast {\n"
                + "        KeyringPlugin.getSecret('test','test')\n"
                + "    }\n"
                + "}\n"
                + "gradle.afterProject {\n"
                + "    KeyringPlugin.getSecret('test','test')\n"
                + "}\n"
                + "\n"
                + "gradle.beforeProject {\n"
                + "    KeyringPlugin.getSecret('test','test')\n"
                + "}"));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .build();
  }

  @Test(enabled = false)
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

  @AfterTest
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
