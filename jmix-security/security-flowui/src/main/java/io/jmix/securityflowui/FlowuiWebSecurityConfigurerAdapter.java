package io.jmix.securityflowui;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.spring.VaadinConfigurationProperties;
import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;
import io.jmix.core.security.PostAuthenticationChecks;
import io.jmix.core.security.PreAuthenticationChecks;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.impl.SubstitutedUserAuthenticationProvider;
import io.jmix.core.security.impl.SystemAuthenticationProvider;
import io.jmix.flowui.FlowuiProperties;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.ScreenRegistry;
import io.jmix.securityflowui.access.FlowuiScreenAccessChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.DelegatingAccessDeniedHandler;
import org.springframework.security.web.access.RequestMatcherDelegatingAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class FlowuiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    private static final Logger log = LoggerFactory.getLogger(FlowuiWebSecurityConfigurerAdapter.class);

    public static final String LOGOUT_SUCCESS_URL = "/";


    protected VaadinDefaultRequestCache vaadinDefaultRequestCache;
    protected VaadinConfigurationProperties configurationProperties;
    protected FlowuiScreenAccessChecker viewAccessChecker;
    protected RequestUtil requestUtil;
    protected UserRepository userRepository;
    protected PasswordEncoder passwordEncoder;
    protected PreAuthenticationChecks preAuthenticationChecks;
    protected PostAuthenticationChecks postAuthenticationChecks;
    protected FlowuiProperties flowuiProperties;
    protected ScreenRegistry screenRegistry;

    @Autowired
    public void setVaadinDefaultRequestCache(VaadinDefaultRequestCache vaadinDefaultRequestCache) {
        this.vaadinDefaultRequestCache = vaadinDefaultRequestCache;
    }

    @Autowired
    public void setConfigurationProperties(VaadinConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    @Autowired
    public void setViewAccessChecker(FlowuiScreenAccessChecker viewAccessChecker) {
        this.viewAccessChecker = viewAccessChecker;
    }

    @Autowired
    public void setRequestUtil(RequestUtil requestUtil) {
        this.requestUtil = requestUtil;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setPreAuthenticationChecks(PreAuthenticationChecks preAuthenticationChecks) {
        this.preAuthenticationChecks = preAuthenticationChecks;
    }

    @Autowired
    public void setPostAuthenticationChecks(PostAuthenticationChecks postAuthenticationChecks) {
        this.postAuthenticationChecks = postAuthenticationChecks;
    }

    @Autowired
    public void setFlowuiProperties(FlowuiProperties flowuiProperties) {
        this.flowuiProperties = flowuiProperties;
    }

    @Autowired
    public void setScreenRegistry(ScreenRegistry screenRegistry) {
        this.screenRegistry = screenRegistry;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new SystemAuthenticationProvider(userRepository));
        auth.authenticationProvider(new SubstitutedUserAuthenticationProvider(userRepository));

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userRepository);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setPreAuthenticationChecks(preAuthenticationChecks);
        daoAuthenticationProvider.setPostAuthenticationChecks(postAuthenticationChecks);

        auth.authenticationProvider(daoAuthenticationProvider);
    }

    /**
     * The paths listed as "ignoring" in this method are handled without any
     * Spring Security involvement. They have no access to any security context
     * etc.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(getDefaultWebSecurityIgnoreMatcher(getUrlMapping()));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Use a security context holder that can find the context from Vaadin
        // specific classes
        SecurityContextHolder.setStrategyName(
                VaadinAwareSecurityContextHolderStrategy.class.getName());

        // Respond with 401 Unauthorized HTTP status code for unauthorized
        // requests for protected Fusion endpoints, so that the response could
        // be handled on the client side using e.g. `InvalidSessionMiddleware`.
        http.exceptionHandling()
                .accessDeniedHandler(createAccessDeniedHandler())
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        requestUtil::isEndpointRequest);

        // Vaadin has its own CSRF protection.
        // Spring CSRF is not compatible with Vaadin internal requests
        http.csrf().ignoringRequestMatchers(
                requestUtil::isFrameworkInternalRequest);

        // Ensure automated requests to e.g. closing push channels, service
        // workers,
        // endpoints are not counted as valid targets to redirect user to on
        // login
        http.requestCache().requestCache(vaadinDefaultRequestCache);

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry = http
                .authorizeRequests();

        // Vaadin internal requests must always be allowed to allow public Flow
        // pages
        // and/or login page implemented using Flow.
        urlRegistry.requestMatchers(requestUtil::isFrameworkInternalRequest)
                .permitAll();

        // Public routes are OK to access
//        urlRegistry.requestMatchers(requestSupport::isAnonymousRoute).permitAll();
        urlRegistry.requestMatchers(getDefaultHttpSecurityPermitMatcher(getUrlMapping())).permitAll();

        // all other requests require authentication
        urlRegistry.anyRequest().authenticated();

        // Enable view access control
        viewAccessChecker.enable();

        initLoginScreen(http);
    }

    protected void initLoginScreen(HttpSecurity http) throws Exception {
        String loginScreenId = flowuiProperties.getLoginScreenId();
        if (Strings.isNullOrEmpty(loginScreenId)) {
            log.debug("Login screen Id is not defined");
            return;
        }

        // TODO: gg, reimplement with Id
        Class<? extends Screen> controllerClass = screenRegistry.getScreenInfo(loginScreenId).getControllerClass();
        setLoginView(http, controllerClass, LOGOUT_SUCCESS_URL);
    }

    /**
     * Matcher for framework internal requests, with Vaadin servlet mapped on
     * the given path.
     *
     * @param urlMapping url mapping for the Vaadin servlet.
     * @return default {@link HttpSecurity} bypass matcher
     */
    public static RequestMatcher getDefaultHttpSecurityPermitMatcher(String urlMapping) {
        Objects.requireNonNull(urlMapping,
                "Vaadin servlet url mapping is required");
        Stream.Builder<String> paths = Stream.builder();
        Stream.of(HandlerHelper.getPublicResourcesRequiringSecurityContext())
                .map(path -> applyUrlMapping(urlMapping, path))
                .forEach(paths::add);

        String mappedRoot = applyUrlMapping(urlMapping, "");
        if ("/".equals(mappedRoot)) {
            // Permit should be needed only on /vaadinServlet/, not on sub paths
            // The '**' suffix is left for backward compatibility.
            // Should we remove it?
            paths.add("/vaadinServlet/**");
        } else {
            // We need only to permit root of the mapping because other Vaadin
            // public urls and resources are already permitted
            paths.add(mappedRoot);
        }
        return new OrRequestMatcher(paths.build()
                .map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }

    /**
     * Matcher for Vaadin static (public) resources, with Vaadin servlet mapped
     * on the given path.
     * <p>
     * Assumes Vaadin servlet to be mapped on root path ({@literal /*}).
     *
     * @param urlMapping the url mapping for the Vaadin servlet
     * @return default {@link WebSecurity} ignore matcher
     */
    public static RequestMatcher getDefaultWebSecurityIgnoreMatcher(String urlMapping) {
        Objects.requireNonNull(urlMapping,
                "Vaadin servlet url mapping is required");
        return new OrRequestMatcher(Stream
                .of(HandlerHelper.getPublicResources())
                .map(path -> applyUrlMapping(urlMapping, path))
                .map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }

    /**
     * Sets up login for the application using form login with the given path
     * for the login view.
     * <p>
     * This is used when your application uses a Fusion based login view
     * available at the given path.
     *
     * @param http                the http security from {@link #configure(HttpSecurity)}
     * @param fusionLoginViewPath the path to the login view
     * @throws Exception if something goes wrong
     */
    // TODO: gg, rename to screen Id and handle ID
    protected void setLoginView(HttpSecurity http, String fusionLoginViewPath) throws Exception {
        setLoginView(http, fusionLoginViewPath, LOGOUT_SUCCESS_URL);
    }

    /**
     * Sets up login for the application using form login with the given path
     * for the login view.
     * <p>
     * This is used when your application uses a Fusion based login view
     * available at the given path.
     *
     * @param http                the http security from {@link #configure(HttpSecurity)}
     * @param fusionLoginViewPath the path to the login view
     * @param logoutUrl           the URL to redirect the user to after logging out
     * @throws Exception if something goes wrong
     */
    // TODO: gg, rename to screen Id and handle ID
    protected void setLoginView(HttpSecurity http, String fusionLoginViewPath,
                                String logoutUrl) throws Exception {
        fusionLoginViewPath = applyUrlMapping(fusionLoginViewPath);
        FormLoginConfigurer<HttpSecurity> formLogin = http.formLogin();
        formLogin.loginPage(fusionLoginViewPath).permitAll();
        formLogin.successHandler(
                getVaadinSavedRequestAwareAuthenticationSuccessHandler(http));
        http.logout().logoutSuccessUrl(logoutUrl);
        http.exceptionHandling().defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(fusionLoginViewPath),
                AnyRequestMatcher.INSTANCE);
        viewAccessChecker.setLoginView(fusionLoginViewPath);
    }

    /**
     * Sets up login for the application using the given Flow login view.
     *
     * @param http          the http security from {@link #configure(HttpSecurity)}
     * @param flowLoginView the login view to use
     * @throws Exception if something goes wrong
     */
    protected void setLoginView(HttpSecurity http,
                                Class<? extends Component> flowLoginView) throws Exception {
        setLoginView(http, flowLoginView, LOGOUT_SUCCESS_URL);
    }

    /**
     * Sets up login for the application using the given Flow login view.
     *
     * @param http          the http security from {@link #configure(HttpSecurity)}
     * @param flowLoginView the login view to use
     * @param logoutUrl     the URL to redirect the user to after logging out
     * @throws Exception if something goes wrong
     */
    protected void setLoginView(HttpSecurity http,
                                Class<? extends Component> flowLoginView, String logoutUrl) throws Exception {
        Optional<Route> route = AnnotationReader.getAnnotationFor(flowLoginView, Route.class);

        if (route.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unable find a @Route annotation on the login view "
                            + flowLoginView.getName());
        }

        String loginPath = RouteUtil.getRoutePath(flowLoginView, route.get());
        if (!loginPath.startsWith("/")) {
            loginPath = "/" + loginPath;
        }
        loginPath = applyUrlMapping(loginPath);

        // Actually set it up
        FormLoginConfigurer<HttpSecurity> formLogin = http.formLogin();
        formLogin.loginPage(loginPath).permitAll();
        formLogin.successHandler(
                getVaadinSavedRequestAwareAuthenticationSuccessHandler(http));
        http.csrf().ignoringAntMatchers(loginPath);
        http.logout().logoutSuccessUrl(logoutUrl);
        http.exceptionHandling().defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint(loginPath),
                AnyRequestMatcher.INSTANCE);
        viewAccessChecker.setLoginView(flowLoginView);
    }

    protected VaadinSavedRequestAwareAuthenticationSuccessHandler getVaadinSavedRequestAwareAuthenticationSuccessHandler(
            HttpSecurity http) {
        VaadinSavedRequestAwareAuthenticationSuccessHandler vaadinSavedRequestAwareAuthenticationSuccessHandler = new VaadinSavedRequestAwareAuthenticationSuccessHandler();
        vaadinSavedRequestAwareAuthenticationSuccessHandler
                .setDefaultTargetUrl(applyUrlMapping(""));
        RequestCache requestCache = http.getSharedObject(RequestCache.class);
        if (requestCache != null) {
            vaadinSavedRequestAwareAuthenticationSuccessHandler
                    .setRequestCache(requestCache);
        }
        return vaadinSavedRequestAwareAuthenticationSuccessHandler;
    }

    protected AccessDeniedHandler createAccessDeniedHandler() {
        final AccessDeniedHandler defaultHandler = new AccessDeniedHandlerImpl();

        final AccessDeniedHandler http401UnauthorizedHandler = new Http401UnauthorizedAccessDeniedHandler();

        final LinkedHashMap<Class<? extends AccessDeniedException>, AccessDeniedHandler> exceptionHandlers =
                new LinkedHashMap<>();
        exceptionHandlers.put(CsrfException.class, http401UnauthorizedHandler);

        final LinkedHashMap<RequestMatcher, AccessDeniedHandler> matcherHandlers = new LinkedHashMap<>();
        matcherHandlers.put(requestUtil::isEndpointRequest,
                new DelegatingAccessDeniedHandler(exceptionHandlers,
                        new AccessDeniedHandlerImpl()));

        return new RequestMatcherDelegatingAccessDeniedHandler(matcherHandlers,
                defaultHandler);
    }

    protected static class Http401UnauthorizedAccessDeniedHandler
            implements AccessDeniedHandler {
        @Override
        public void handle(HttpServletRequest request,
                           HttpServletResponse response,
                           AccessDeniedException accessDeniedException)
                throws IOException, ServletException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    public String getUrlMapping() {
        return configurationProperties.getUrlMapping();
    }

    /**
     * Prepends to the given {@code path} with the configured url mapping.
     * <p>
     * A {@code null} path is treated as empty string; the same applies for
     * url mapping.
     *
     * @return the path with prepended url mapping.
     * @see VaadinConfigurationProperties#getUrlMapping()
     */
    public String applyUrlMapping(String path) {
        return applyUrlMapping(configurationProperties.getUrlMapping(), path);
    }

    /**
     * Prepends to the given {@code path} with the servlet path prefix from
     * input url mapping.
     * <p>
     * A {@code null} path is treated as empty string; the same applies for
     * url mapping.
     *
     * @return the path with prepended url mapping.
     * @see VaadinConfigurationProperties#getUrlMapping()
     */
    public static String applyUrlMapping(String urlMapping, String path) {
        if (urlMapping == null) {
            urlMapping = "";
        } else {
            // remove potential / or /* at the end of the mapping
            urlMapping = urlMapping.replaceFirst("/\\*?$", "");
        }
        if (path == null) {
            path = "";
        } else if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return urlMapping + "/" + path;
    }
}
