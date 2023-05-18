package com.ec0lint;

import com.ec0lint.settings.Settings;
import com.intellij.openapi.project.Project;
import ec0lint.Ec0lintInspection;

public class Ec0lintTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return TestUtils.getTestDataPath();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    protected void doTest(final String file) {
        Project project = myFixture.getProject();
        Settings settings = Settings.getInstance(project);
        settings.ec0lintExecutable = Ec0lintRunnerTest.EC0LINT_BIN;
        settings.ec0lintRcFile = getTestDataPath() + "/.ec0lintrc";
        settings.nodeInterpreter = Ec0lintRunnerTest.NODE_INTERPRETER;
        settings.rulesPath = "";
        settings.pluginEnabled = true;
        myFixture.configureByFile(file);
        myFixture.enableInspections(new Ec0lintInspection());
        myFixture.checkHighlighting(true, false, true);
    }

    protected void doTest() {
        String name = getTestName(true).replaceAll("_", "-");
        doTest("/inspections/" + name + ".js");
    }

    public void testEqeqeq() {
        doTest();
    }

    public void testNo_negated_in_lhs() {
        doTest();
    }

    public void testValid_typeof() {
        doTest();
    }

    public void testNo_lonely_if() {
        doTest();
    }

    public void testNo_new_object() {
        doTest();
    }

    public void testNo_array_constructor() {
        doTest();
    }
}
