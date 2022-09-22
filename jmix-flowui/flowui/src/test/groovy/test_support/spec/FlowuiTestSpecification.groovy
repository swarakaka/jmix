package test_support.spec

import com.google.common.base.Strings
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.server.InitParameters
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.SpringServlet
import com.vaadin.flow.spring.VaadinServletContextInitializer
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory
import io.jmix.core.security.SystemAuthenticator
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.view.ViewRegistry
import io.jmix.flowui.view.View
import io.jmix.flowui.sys.ViewControllersConfiguration
import org.apache.commons.lang3.ArrayUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockServletConfig
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import spock.lang.Specification
import test_support.*

import javax.servlet.ServletException

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField
import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE

@ContextConfiguration(classes = [FlowuiTestConfiguration])
class FlowuiTestSpecification extends Specification {

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    ViewNavigators screenNavigators

    // saving session and UI to avoid it be GC'ed
    protected VaadinSession vaadinSession
    protected UI ui

    void setup() {
        setupAuthentication()
        setupVaadinUi()
        registerScreenBasePackages("test_support.view")
    }

    void cleanup() {
        removeAuthentication()
    }

    protected void setupAuthentication() {
        SystemAuthenticator systemAuthenticator = applicationContext.getBean(SystemAuthenticator.class)
        systemAuthenticator.begin()
    }

    protected void removeAuthentication() {
        SystemAuthenticator systemAuthenticator = applicationContext.getBean(SystemAuthenticator.class)
        systemAuthenticator.end()
    }

    protected void setupVaadinUi() {
        SpringServlet springServlet = new TestSpringServlet(applicationContext, true)

        VaadinServletContextInitializer contextInitializer =
                applicationContext.getBean(VaadinServletContextInitializer.class, applicationContext)
        try {
            TestServletContext servletContext = new TestServletContext()
            servletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext)
            servletContext.setInitParameter(InitParameters.SERVLET_PARAMETER_ENABLE_DEV_SERVER, "false")

            MockServletConfig mockServletConfig = new MockServletConfig(servletContext)

            // fill servlet context with listeners from VaadinServletContextInitializer
            contextInitializer.onStartup(mockServletConfig.getServletContext())

            servletContext.fireServletContextInitialized()

            springServlet.init(mockServletConfig)
        } catch (ServletException e) {
            throw new IllegalStateException(String.format("Cannot init %s", TestSpringServlet.class.getName()), e)
        }

        VaadinService.setCurrent(springServlet.getService())

        vaadinSession = new TestVaadinSession(springServlet.getService())
        VaadinSession.setCurrent(vaadinSession)

        def request = new TestVaadinRequest(springServlet.getService())

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))

        ui = new UI()
        ui.getInternals().setSession(vaadinSession)
        ui.doInit(request, 1)
        UI.setCurrent(ui)
    }

    protected void registerScreenBasePackages(String[] screenBasePackages) {
        if (ArrayUtils.isEmpty(screenBasePackages)) {
            return
        }

        def metadataReaderFactory = applicationContext.getBean(AnnotationScanMetadataReaderFactory.class)
        ViewRegistry windowConfig = applicationContext.getBean(ViewRegistry.class)

        def configuration = new ViewControllersConfiguration(applicationContext, metadataReaderFactory)

        def injector = applicationContext.getAutowireCapableBeanFactory()
        injector.autowireBean(configuration)

        configuration.setBasePackages(Arrays.asList(screenBasePackages))

        try {
            def configurationsField = getDeclaredField(ViewRegistry.class,
                    "configurations", true)
            //noinspection unchecked
            def configurations = (Collection<ViewControllersConfiguration>) configurationsField.get(windowConfig)

            def modifiedConfigurations = new ArrayList<>(configurations)
            modifiedConfigurations.add(configuration)

            configurationsField.set(windowConfig, modifiedConfigurations)

            getDeclaredField(ViewRegistry.class, "initialized", true)
                    .set(windowConfig, false)
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot register screen packages", e)
        }

        registerScreenRoutes(screenBasePackages)
    }

    protected void registerScreenRoutes(String[] screenBasePackages) {
        if (ArrayUtils.isEmpty(screenBasePackages)) {
            return
        }

        def screenRegistry = applicationContext.getBean(ViewRegistry.class)
        def screens = screenRegistry.getViewInfos()
        screens.forEach({
            Class<? extends View> controllerClass = it.getControllerClass()
            Route route = controllerClass.getAnnotation(Route.class)
            if (route == null) {
                return
            }

            RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope()
            if (Strings.isNullOrEmpty(route.value())
                    || routeConfiguration.isPathAvailable(route.value())) {
                return
            }

            if (route.layout() == UI.class) {
                routeConfiguration.setRoute(route.value(), controllerClass)
            } else {
                routeConfiguration.setRoute(route.value(), controllerClass, route.layout())
            }
        })
    }

    protected <T extends View> T openScreen(Class<T> screen) {
        def activeRouterTargetsChain = getRouterChain(screen)

        activeRouterTargetsChain.get(0) as T
    }

    protected List<HasElement> getRouterChain(Class<View> screenClass) {
        screenNavigators.view(screenClass)
                .navigate()

        UI.getCurrent().getInternals().getActiveRouterTargetsChain()
    }
}
