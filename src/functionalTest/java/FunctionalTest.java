import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FunctionalTest {
  private static final Logger logger = LoggerFactory.getLogger(FunctionalTest.class);
  static File projectDir = new File("build/functionalTestDir");
  static String settingsTemplate = "plugins {%s} %s";
  static String buildTemplate = "plugins {%s}  %s";

  @BeforeMethod
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
            .withDebug(true)
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
            .withDebug(true)
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
                + "def added = KeyringPlugin.setSecret('https://realistic.domain', 'username', 'P@sSw0Rd')"));

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .withDebug(true)
            .build();
    Assert.assertTrue(true);
  }

  @Test()
  public void sourceFromEnv() throws IOException {
    // Value in env is fine for any character it seems
    String password = "R@ac:;:;;:\\/fda";
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "def pass = KeyringPlugin.getSecret('https://realistic.domain', 'Something.Plausible')\n"
                + "println(\"Password Found: \" + pass)"));
    File dotenv = new File(projectDir, ".env");

    // TODO: Make this my problem, not yours.
    FileOutputStream a = new FileOutputStream(dotenv);
    a.write(("aHR0cHM6Ly9yZWFsaXN0aWMuZG9tYWlu_Something.Plausible=" + password).getBytes());
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

  @Test()
  public void simpleSourceFromEnv() throws IOException {
    // Value in env is fine for any character it seems
    String password = "R@ac:;:;;:\\/fda";
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "def pass = KeyringPlugin.getSecret('localhost', 'Something.Plausible')\n"
                + "println(\"Password Found: \" + pass)"));
    File dotenv = new File(projectDir, ".env");

    // TODO: Make this my problem, not yours.
    FileOutputStream a = new FileOutputStream(dotenv);
    a.write(("localhost_Something.Plausible=" + password).getBytes());
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

  @Test()
  public void testErrorHandlingDotenv() throws IOException {
    // Value in env is fine for any character it seems
    String password = "R@ac:;:;;:\\/fda";
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "def pass = KeyringPlugin.getSecret('https://google.com', 'Something.Plausible')\n"
                + "println(\"Password Found: \" + pass)"));
    File dotenv = new File(projectDir, ".env");

    // TODO: Make this my problem, not yours.
    FileOutputStream a = new FileOutputStream(dotenv);
    a.write(("https://google.com.Plausible=" + password).getBytes());
    a.close();

    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withDebug(true)
            .withProjectDir(projectDir)
            .withArguments("build", "-Porg.yello.labs.env")
            .buildAndFail();
    Assert.assertTrue(result.getOutput().contains("base64"));
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
                + ".getSecret('https://realistic.domain', 'username') \n"
                + "println(pass)")); // Dont ever do this though, please
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("build")
            .withDebug(true)
            .build();
    Assert.assertTrue(result.getOutput().contains("P@sSw0Rd"), result.getOutput());
  }

  @Test(dependsOnMethods = {"setSecret"})
  public void getSecretFromEnv() throws IOException {
    writeFile(
        new File(projectDir, "build.gradle"),
        String.format(
            buildTemplate,
            "id 'org.yello-labs" + ".gradle" + "-keyring'",
            "\n"
                + "import org.yello.labs.KeyringPlugin;\n"
                + "final String pass = KeyringPlugin"
                + ".getSecret('https://realistic.domain', 'Something.Plausible') \n"
                + "println(\"Password Found: \" + pass)")); // Dont ever do this though, please

    new File(projectDir, ".env").delete();
    String password = "R@ac:;:;;:\\/fda";

    Map<String, String> environment = new HashMap<>();
    environment.put("ORG_YELLO_LABS_ENV", "true");
    environment.put("aHR0cHM6Ly9yZWFsaXN0aWMuZG9tYWlu_Something.Plausible", password);
  
    //    TODO: Find a way to make this work in modern shells and I'll owe you one
    //    environment.put("https://realistic.domain_Something.Plausible", password);
    
    
    BuildResult result =
        GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withEnvironment(environment)
            .withArguments("build", "-Porg.yello.labs.env=true")
            .build();

    Assert.assertTrue(result.getOutput().contains(password), result.getOutput());
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
            .withDebug(true)
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
            .withDebug(true)
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
            .withDebug(true)
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
            .withDebug(true)
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
            .withDebug(true)
            .build();
  }

  @AfterMethod
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
