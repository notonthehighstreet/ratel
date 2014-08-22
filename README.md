##Ratel Library##

###Responsibilities###
The Ratel library is responsible for communicating the details of exceptions to
the [Honeybadger](https://honeybadger.io) service. It is also possible to
include other pieces of information with the exception such as the user agent
of the browser making the request.

###Component initialisation instructions###
To initialise the library, construct a new instance of the `Honeybadger` class
with the relevant configuration, `Executor` and
[Jackson](http://wiki.fasterxml.com/JacksonHome) `ObjectMapper`. This instance
can be kept for the lifetime of the application.

###State characteristics (i.e. stateless)###
The Ratel library does not store any state and is thread safe.

###Failure modes of component###
The only part that might fail is the actual call to Honeybadger. To prevent any
failure affecting the rest of the system, communication to Honeybadger only
occurs on another thread.

###Developer Highlights (i.e. Classes of interest)###
The two classes of interest are `Honeybadger` and `HoneybadgerConfiguration`

####Honeybadger####
This class is responsible for communicating exception details to Honeybadger.
This will typically be called from a `Filter` or `Interceptor`.

####HoneybadgerConfiguration####
There are a number of pieces of information that is required to communicate
with Honeybadger and this interface is used to retrieve this information. To
use this library, you must implement this interface and retrieve the
configuration from the relevant location.

###Example###
This is an example of using this library with Spring Boot:

Implement the configuration class:
````java
@Component
public class ErrorNotificationConfiguration
    implements HoneybadgerConfiguration {

    @Override
    public String getKey() {...}

    @Override
    public URL getUrl() {...}

    @Override
    public String getName() {...}

    @Override
    @CheckForNull
    public String getVersion() {...}

    @Override
    public String getEnvironment() {...}

    @Override
    public Collection<String> getExcludeExceptions() {...}

}
````
Add the `Honeybadger` class to the Spring context
````java
@Configuration
@ComponentScan
public class Application {
    ...
    @Bean
    public Honeybadger honeybadger(final ErrorNotificationConfiguration config,
          final ObjectMapper mapper, final Executor executor) {
        return new Honeybadger(config, mapper, executor);
    }
    ...
}
````
Wire up the `Honeybadger` class so that it gets called when an exception occurs
````java
@Component
public class ErrorNotifyingHandlerInterceptor
    extends HandlerInterceptorAdapter {

    private final Honeybadger honeybadger;

    @Autowired
    public ErrorNotifyingHandlerInterceptor(final Honeybadger honeybadger) {
        this.honeybadger = honeybadger;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request,
            final HttpServletResponse response, final Object handler,
            final Exception ex) {
        if (ex == null) {
            return;
        }

        final String controller;
        final String action;

        if (handler instanceof HandlerMethod) {
            final HandlerMethod method = (HandlerMethod) handler;
            controller = method.getBeanType().getSimpleName();
            action = method.getMethod().getName();
        } else {
            controller = null;
            action = null;
        }

        final String requestMethod = request.getMethod();
        final String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        final String remoteAddress = request.getRemoteAddr();

        honeybadger.notify(request.getRequestURL().toString(), controller,
            action, requestMethod, userAgent, remoteAddress,
            request.getParameterMap(), ex);
    }
}
````
Honeybadger will then receive notifications everytime an exception occurs. You may wish to look into the `@ConditionalOnProperty` annotation to only enable Honeybadger integration when an application property has been set.
