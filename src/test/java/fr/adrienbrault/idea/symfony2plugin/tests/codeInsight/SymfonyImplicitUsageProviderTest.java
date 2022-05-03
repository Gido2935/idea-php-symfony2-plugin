package fr.adrienbrault.idea.symfony2plugin.tests.codeInsight;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import fr.adrienbrault.idea.symfony2plugin.codeInsight.SymfonyImplicitUsageProvider;
import fr.adrienbrault.idea.symfony2plugin.tests.SymfonyLightCodeInsightFixtureTestCase;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 * @see SymfonyImplicitUsageProvider
 */
public class SymfonyImplicitUsageProviderTest extends SymfonyLightCodeInsightFixtureTestCase {
    public void setUp() throws Exception {
        super.setUp();

        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("routes.yml"));
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("services.yml"));
    }

    public String getTestDataPath() {
        return "src/test/java/fr/adrienbrault/idea/symfony2plugin/tests/codeInsight/fixtures";
    }

    public void testControllerClassIsUsedWhenAMethodHasRoute() {
        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent("" +
            "#[Route()]\n" +
            "public function foo2() {}"
        )));

        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent("" +
            "/**\n" +
            "* @Route()\n" +
            "*/\n" +
            "public function foo() {}\n"
        )));
    }

    public void testControllerClassIsUnusedIfRoutesArePrivate() {
        assertFalse(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent("" +
            "/**\n" +
            "* @Route()\n" +
            "*/\n" +
            "private function foo() {}\n" +
            "#[Route()]\n" +
            "private function foo2() {}"
        )));
    }

    public void testControllerMethodIsUsedWhenAMethodIsHasRouteDefinition() {
        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerMethodWithRouteContent("" +
            "/**\n" +
            "* @Route()\n" +
            "*/\n" +
            "public function foobar() {}"
        )));

        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerMethodWithRouteContent("" +
            "#[Route()]\n" +
            "public function foobar() {}"
        )));
    }

    public void testControllerMethodIsUntouchedForPrivateMethods() {
        assertFalse(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerMethodWithRouteContent("" +
            "/**\n" +
            "* @Route()\n" +
            "*/\n" +
            "private function foobar() {}"
        )));

        assertFalse(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerMethodWithRouteContent("" +
            "#[Route()]\n" +
            "private function foobar() {}"
        )));
    }

    public void testControllerForDefinitionInsideYaml() {
        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent("" +
            "public function foobarYaml() {}"
        )));

        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent(
            "\\App\\Controller\\FooControllerInvoke",
            "public function __invoke() {}"
        )));
    }

    public void testControllerForDefinitionInsideYamlWithAction() {
        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent("" +
            "public function foobarYamlAction() {}"
        )));
    }

    public void testControllerForDefinitionInsideYamlAsService() {
        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent(
            "\\App\\Controller\\FooControllerService",
            "public function foo() {}"
        )));

        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent(
            "\\App\\Controller\\FooControllerService",
            "public function foo() {}"
        )));

        assertTrue(new SymfonyImplicitUsageProvider().isImplicitUsage(createPhpControllerClassWithRouteContent(
            "\\App\\Controller\\FooControllerServiceInvoke",
            "public function __invoke() {}"
        )));
    }

    private PhpClass createPhpControllerClassWithRouteContent(@NotNull String content) {
        return createPhpControllerClassWithRouteContent("\\App\\Controller\\FooController", content);
    }

    private PhpClass createPhpControllerClassWithRouteContent(@NotNull String className, @NotNull String content) {
        String[] split = StringUtils.stripStart(className, "\\").split("\\\\");

        PsiFile psiFile = myFixture.configureByText(PhpFileType.INSTANCE, "<?php" +
            "<?php\n" +
            "namespace " + StringUtils.join(Arrays.copyOf(split, split.length - 1), "\\") + ";\n" +
            "\n" +
            "use Symfony\\Component\\Routing\\Annotation\\Route;\n" +
            "\n" +
            "class " + split[split.length - 1] + "\n" +
            "{\n" +
            "" + content + "\n" +
            "}"
        );

        return PhpElementsUtil.getFirstClassFromFile((PhpFile) psiFile.getContainingFile());
    }

    @NotNull
    private Method createPhpControllerMethodWithRouteContent(@NotNull String content) {
        PhpClass phpClass = createPhpControllerClassWithRouteContent("\\App\\Controller\\FooController", content);
        return phpClass.getMethods().iterator().next();
    }
}
