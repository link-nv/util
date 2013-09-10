Introduction
============

The purpose of this framework is to provides a clean and type-safe way
for you to access arbitrary configuration data. It is highly recommended
that you extract any modifiable parameters into configuration
properties. This makes it trivial to alter the way your application
works without the need for going back to the code and recompiling a
specific build of it.

While the internal operation of the framework is complex, simply using
the configuration framework is really easy. Essentially, all you need to
do is create an interface that describes your configuration properties
as annotated methods, activate your interface and use that interface
when you need to look up values for those properties.

Your configuration hierarchy
============================

A configuration is a hierarchical structure of configuration groups and
properties. The structure is defined by means of Java interface classes.
Each interface defines a configuration group and the methods in the
interface define properties or subgroups within that group. For
configuration properties, the method's name is the configuration
property's base name and the method's return value is the type of the
property's value. Internally, all values are stored as character
strings, but when accessed, they are converted to the appropriate type
by the configuration framework before they are handed over to your
application. The work of finding, loading, caching and converting values
is taken care of by the configuration framework. Defining and accessing
configuration values is clean, trivial and type-safe.

The configuration can be used in two ways. Either you're writing an
application that needs its very own configuration framework or you're
writing an application that will use another application's configuration
framework.

Imagine a situation where there is a web application which allows
customers to create their own company portal site. This hypothetical web
application supports modules of portal blocks that can be externally
developed. The portal application would provide external module
developers with an API for attaching their modules into the portal. In
this case, the portal web application should provide the main
configuration implementation and the external module developer would use
the configuration framework that was set up by the portal application.
The external developer can add their own configuration parameters
through the configuration framework's extension point.

The main application
--------------------

When you're writing the main application, you'll want to define your
application's configuration properties in an interface that extends the
`RootConfig` interface provided by the configuration framework. The
`RootConfig` interface is the root of the configuration hierarchy. As
the main configuration provider, you'll want to extend this interface
and add your own configuration hierarchy to it by adding configuration
groups and properties as methods.

Let's go back to the web portal application example. To allow a client
to customize the portal with their own branding, we may want to add a
group of branding related configuration properties. The portal may have
support for certain web services, which the client may need to
customize. We'll group those together as well. Finally, we'll provide a
group of properties that relate to the API used by portal modules.
Here's what such a `RootConfig` interface might look like:

~~~~ {.java}
    public interface PortalConfig extends RootConfig {

        BrandingConfig branding();
        ServicesConfig services();
        ModuleConfig module();
    }
                
~~~~

There we are, we've defined the groups of configuration properties that
our portal application needs at the root level. Defining groups and
properties is always purely declarative. This keeps your work minimal,
clean, transparent and easily maintainable. It's highly recommended that
you document the methods adequately with JavaDoc.

Application extensions
----------------------

If the application you're writing extends another application and you
need to use that other application's configuration hierarchy, you can
still use the configuration framework to define your own specific
configuration hierarchy. However, instead of extending the `RootConfig`
(which has already been done by the main application), you'll be
obtaining your configuration extension via the existing root config's
extension point: `RootConfig#app`.

Going back to the web portal application example, suppose you're writing
a module that allows clients to send out notifications of certain
events. We may want to add a group of configuration properties that
configure how email notifications would get sent. We'll also add a group
of properties related to SMS message notifications, and one for
properties related to styling of our module's user interface. Here's
what such a configuration interface might look like:

~~~~ {.java}
    @Group(prefix = "notifications")
    public interface NotificationsModuleConfig extends AppConfig {

        EMailConfig email();
        SMSConfig sms();
        StyleConfig style();
    }
                
~~~~

A few key differences between this configuration group and the one we
created for the root config: Unlike `RootConfig` in the example above,
this interface extends `AppConfig`. That's because this interface
defines an application extension to an existing configuration root, not
a root of itself. Moreover, our extension interface also carries a
`@Group` annotation. The annotation is necessary because the extension
interface defines a group within the configuration hierarchy. We need
the annotation's `prefix` to determine under what namespace the
extension interface's properties should be defined. You'll read more
about what `prefix` is and how it works in the next section.

Configuration Groups
--------------------

So far, we've only defined the top-level groups. Your top level group
was either the root of the configuration hierarchy and extends
`RootConfig`, or it was the root of your application extension and it
extends `AppConfig`. Let's go a level deeper and look at what the
`BrandingConfig` interface we referred to earlier could look like.
Whether the group is referenced by a `RootConfig`, an `AppConfig` or
another group doesn't matter. At this point, configuration groups all
follow the same syntax. Here we can also see how to define configuration
properties:

~~~~ {.java}
    @Group(prefix = "branding")
    public interface BrandingConfig {

        @Property(required = true, unset = "Your Company")
        String companyName();

        @Property(required = false)
        byte[] companyLogo();

        @Property(required = false)
        URL themeCSS();
    }
                
~~~~

First, let's note a few important differences between this interface and
the top-level group we defined before. Since this interface does not
define the root of the configuration hierarchy or an extension point, it
does not extend `RootConfig` or `AppConfig`. Also, since this group
exists somewhere in the hierarchy, it needs a `@Group` annotation that
specifies the group's prefix within the hierarchy. The prefix tells the
configuration framework what the namespace of the properties defined
within this group will be called. It should be a simple ASCII string
that is also valid as a method name. You'll notice that the prefix here
is `branding`, the same name as the one we used for the method that
returns this configuration interface. It's required for the prefix of
the group to match the name of the method that yields it.

Also different from the earlier interface is the fact that we have
non-group methods here. Non-group methods define configuration
properties and should always be annotated with `@Property`. The
`@Property` annotation serves to tell the configuration framework all
the details about how to interpret this property. The property can be
required or optional. Required properties will never yield `null` values
(if a value cannot be found, the framework will throw an exception
instead). You can also provide an unset value, which is the value that
will be returned when no other value can be found for this property.

The method name is used to name the property whose value it returns.
When the internal name of this property is determined, the prefixes of
all the groups accessed to reach this property from the root interface
are all added together, separated by a dot, and the property name is
added to the end. This internal name is used to load a value for the
property from different locations such as a property file, the servlet
context, or a custom location. In the case of the `companyName`
property, the internal name would be: `branding.companyName`.

Accessing properties
====================

Once your configuration interfaces have been defined, all that remains
is activating the configuration framework and making calls to the config
property methods you defined earlier to access their values.

Activating the configuration
----------------------------

When your program accesses configuration properties, it does so via
calls to static methods. In the back-end, these static methods look up
the active configuration implementation from a thread-local. It is
therefore vital that the configuration framework is first activated on
the thread before any attempt of accessing it is made.

If your application does not run within a servlet container and you want
to create a single global config holder that is always active, you can
activate a config holder globally with one of
`ConfigHolder#setGlobalConfigHolderType` or
`ConfigHolder#setGlobalConfigHolder`. Globally activated config holders
will be used when no local config holder has been activated.

If your application works in a request-response type of manner, it's
better to activate a config holder only for the duration of the request
handling, on the current thread, and deactivate it when the request
handling ends. To do this, you'll need to call
`ConfigHolder.setLocalConfigHolder` when the request handling begins and
`ConfigHolder.unsetLocalConfigHolder` when it ends, preferably in a
`finally` block.

If your application runs within a servlet container, a convenience
servlet filter exists, called `ConfigFilter`, which does the activation
of a local config holder for you when a servlet request is initiated.
The config is unbound from the thread when the request handling ends.

There are two ways of using the `ConfigFilter`. Both involve adding the
filter to the servlet context. Generally, applications configure their
servlet context from `web.xml` in the `WEB-INF` directory of their web
application archive.

The first method requires you to create a custom implementation of
`ConfigFilter`. You'll then activate that custom filter from `web.xml`.
Your custom implementation can then pass all necessary information to
the configuration framework. The second method requires no Java code.
Instead, you'll be putting all the necessary information in `web.xml` as
servlet context init parameters.

Either way, you'll need to put a config filter in `web.xml`. Here's what
the relevant part of `web.xml` might look like if you choose to
reference a custom config filter:

~~~~ {.xml}
    <filter>
        <filter-name>ConfigFilter</filter-name>
        <filter-class>my.company.MyRootConfigFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>ConfigFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
                
~~~~

Make sure the mapping comes early enough: The `ConfigFilter` must have
been activated before any application code that might use it gets
invoked.

If you choose to use the standard `ConfigFilter` and specify the
necessary information via servlet context init parameters, change the
class name in the above example to `net.link.util.config.ConfigFilter`,
and add the relevant servlet context init parameters to your servlet
context. The following table shows all available init parameters and
explains their meaning:

configHolder
:   Use this parameter if you're creating a custom `ConfigHolder`
    implementation to bootstrap the config framework. The value is the
    class name of your custom `ConfigHolder` implementation. Your custom
    implementation must have a default constructor.

configFactory
:   Use this parameter if you're not creating a custom `ConfigHolder`
    implementation, but would like to use the standard one. This
    optional parameter can be used to specify a custom
    `DefaultConfigFactory` implementation that should be used to wrap
    your config implementation or create default config implementations.
    The value is the class name of your custom `DefaultConfigFactory`
    implementation. Your custom implementation must have a default
    constructor.

configResource
:   Use this parameter if you're not creating a custom `ConfigHolder`
    implementation and you're also not creating a custom
    `DefaultConfigFactory` implementation. This optional parameter can
    be used to tell the standard `DefaultConfigFactory` where to read
    values for configuration properties from. The value is a resource
    name that references a property file within the servlet context's
    classloader.

    If this parameter is not set and you're using a standard
    `DefaultConfigFactory`, the property file that will be read should
    be named `config.xml` or `config.properties`.

configClass
:   Use this parameter if you're not creating a custom `ConfigHolder`
    implementation. This parameter is required, and is used to point the
    standard `ConfigHolder` implementation to your root config. The root
    config class can be an interface such as we've seen in examples
    above, or it can be an implementation of that interface. If you
    refer to an implementation of that interface, the implementation
    will be instantiated with its default constructor (and wrapped by
    the default config factory). Your implementation will be used
    whenever the root configuration is accessed. The value is the class
    name of your `RootConfig` interface or an implementation of it.

If you need access to the configuration framework from any servlet
context listeners, those listeners will need to extend
`ConfigContextListener` (or a custom implementation of it). The same
rules as for `ConfigFilter` apply.

For the sake of strong typing and extensibility, the first method
(creating a custom `ConfigFilter` implementation) is highly recommended.
Creating a custom implementation of these classes is really easy. Here's
an example of a custom `ConfigFilter` implementation that references a
custom `ConfigHolder` (you'll see why you may want a custom
`ConfigHolder` in the next section):

~~~~ {.java}
    public class MyRootConfigFilter extends ConfigFilter {

        public MyRootConfigFilter() {

            super( new MyRootConfigHolder() );
        }
    }
                
~~~~

Accessing the configuration
---------------------------

To access the values of your configuration properties, you will call the
`config` method of the `ConfigHolder` class. This method will give you
an implementation of the interfaces you defined earlier. From there on,
it's easy to access any property by chaining your configuration method
calls: `config().branding().companyName()`

To make the `config` method return an object of the correct type (the
type of your root configuration interface), rather than just
`RootConfig`, there are two approaches:

`config( MyRootConfig.class ).configProperty()`
:   You'll need a static import of `ConfigHolder.config`.

    This syntax is rather verbose and thus a little inconvenient for
    general use.

`config().configProperty()`
:   You'll need a static import of a custom subclass of `ConfigHolder`.

    Your custom subclass provides a new `config()` method, which takes
    care of the typing:

    ~~~~ {.java}
        public class MyRootConfigHolder extends ConfigHolder<MyRootConfig> {

            public MyRootConfigHolder() {

                super( new DefaultConfigFactory(), MyRootConfig.class, null );
            }

            public static MyRootConfig config() {

                return (MyRootConfig) ConfigHolder.config( MyRootConfig.class );
            }

            public static DefaultConfigFactory factory() {

                return (DefaultConfigFactory) ConfigHolder.factory( DefaultConfigFactory.class );
            }
        }
                                
    ~~~~

    Now you can statically import the `config()` method of this class
    and gain direct access to a correctly-typed version of your config
    implementation.

    Making your own `ConfigHolder` implementation also allows you to
    easily provide a custom implementation or extension of the
    `DefaultConfigFactory`. The use of this will be explained later on.

To access properties of an extension group, use the `app` method
declared in `RootConfig`, passing the class of your extension interface
as argument:
`config().app( NotificationsModuleConfig.class ).email().fromAddress()`

Operation
=========

Even for just using the configuration framework, it pays to know how
everything works internally.

The components
--------------

Let's have a look at all of the separate components that make up the
configuration framework, and what each component's job in the whole is:

`RootConfig`
:   The interface that extends this interface marks the root of the
    configuration hierarchy. Most other components of the framework are
    only concerned with this root class.

    To use the framework, you need a custom interface that extends this
    class, and you need to somehow activate either a custom or a default
    implementation of this interface so that the properties can be
    accessed by calling the methods defined in this interface and other
    group interfaces referenced by it.

`AppConfig`
:   Interfaces that extend this interface can be used with the
    configuration framework's extension point. The extension point is:
    `config().app( [SomeAppConfig].class )`.

    It's much the same as an ordinary config group, with the benefit
    that they are not referenced by and thus don't need to be compiled
    along with your `RootConfig` interface. You can deploy a new web
    application with a new `AppConfig` implementation at any time, and
    this new web application will be able to use the config framework
    for its own configuration properties without the need for adding
    something to the root config.

`DefaultConfigFactory`
:   This class is used to create implementations for configuration
    interfaces. It can create an instance for any configuration
    interface.

    Such an instance is called a default implementation of that
    interface (as opposed to a custom implementation of the interface,
    which would be a class that you write, one which `implements` your
    configuration interface). Default implementations of configuration
    interfaces read configuration values from a property file or the
    servlet context.

    The standard `DefaultConfigFactory` will search for a property file
    called `config` (the extension should be `.properties` if the file
    is a plain Java properties file or it should be `.xml` if the file
    is an XML Java properties file. The latter is highly recommended,
    since it is structured, validatable and the text's encoding can be
    specified). The search will happen with the context classloader and
    the framework will look in the root of the classloader, in `../conf`
    and in `../etc`.

    This factory is also used to wrap invocations to a custom
    implementation of a configuration interface. That way, when you
    implement your configuration interface yourself but would like to
    return the value in the property file, or the method's default value
    from its annotation, all you need to do is return `null` in your
    method, and this factory, having wrapped the call to your custom
    implementation, will activate and search for a good value to return
    instead of your `null`.

    As a developer, you may want to extend this class to create your own
    custom `DefaultConfigFactory`. Doing so will allow you to change the
    name of the property file that is searched for, and it will allow
    you to add extra string-to-type conversion strategies. You'll also
    be able to override the `generateValueExtension` method, which will
    allow you to generate configuration values at runtime for properties
    that are configured to auto-generate their value. This method is a
    way of doing runtime value generation for specific properties when
    no other value is set for that property. Only properties with an
    `unset` value set to `Property.AUTO` will trigger this method call
    when accessed and no other value can be resolved for the property.

    Finally, very commonly, applications may want to store the values of
    their configuration properties in a different place. Usually,
    applications that do this will store values in the database and
    provide a user interface for operators to modify the configuration
    values. To achieve this, the application developer should override
    `getStringValueFor` and look up the value for a property from its
    database. The application can create the necessary database entries
    for the configuration properties in and referenced by the
    application's `RootConfig` interface when the custom
    `DefaultConfigFactory` class is being constructed. However, this
    will not trigger creation of database entries for properties in
    application extension interfaces. To support this, the developer
    should hook the `getAppImplementation` call, create database entries
    for the application extension interface, and delegate back to
    `super`.

`ConfigHolder`
:   The config holder is used to hold the active configuration
    implementations in memory. It also provides your application access
    to them.

    Local holders are always activated on the current thread. When the
    operation that activated the holder completes, the holder is
    deactivated again, and detached from the current thread. Global
    holders are always active on all threads, from the moment they've
    been installed. When a local and a global holder are active at the
    same time, the local holder is used. As long as the holder is
    active, it can provide the application access to the implementations
    contained within.

    This class is normally extended by the application that provides the
    `RootConfig`, so that it can provide cleaner access to a well-typed
    version of the `config` method. The application normally provides
    its own `config()` method that has its own extension of the
    `RootConfig` as return value, and delegates to
    `ConfigHolder.config( MyRootConfig.class )`.

`ConfigFilter` and `ConfigContextListener`
:   These classes are used to activate a `ConfigHolder` when the
    application runs within a a servlet container.

    Whenever a servlet call triggers the application, the filter (which
    should come first in the list of filter mappings) determines which
    holder to activate (either you have a custom implementation of this
    filter, or the filter looks through the servlet context's init
    parameters to determine how to operate). When the filtered call
    completes (whether it was successfully handled or resulted in
    failure or an exception is irrelevant), the holder that was
    previously activated is deactivated again.

    The context listener works in much the same way as the filter, but
    triggers when the application is being initialized or destroyed, and
    only for the duration of its very own `doContextInitialized` and
    `doContextDestroyed` calls. It is therefore vital that any context
    listeners that need access to the configuration framework extend
    this abstract class.

    This class is normally extended by the application that provides the
    `RootConfig`, so that it can provide cleaner access to a well-typed
    version of the `config` method. The application normally provides
    its own `config()` method that has its own extension of the
    `RootConfig` as return value, and delegates to
    `ConfigHolder.config( MyRootConfig.class )`.

Loading property values
-----------------------

The implementation of your configuration interface can either be a class
created by you or a default implementation created by
`DefaultConfigFactory`.

In the first case, when a property method is invoked by the application,
your class' code for that method is invoked. Your class' method can
return a value for the property, or it can return `null` in which case
`DefaultConfigFactory`'s wrapper will intervene and a value will be
searched for the property in the same manner as if you did not have a
custom implementation of the configuration interface.

The `DefaultConfigFactory` always uses a property's internal name to
search for a value of the property. The internal name of a property is a
composition of the method names used to navigate the configuration
hierarchy from the root to the property, each element delimited from the
next with a period. As such, a property that you'd access in code by
using `config().authentication().identity().name()` will be loaded with
an internal name `authentication.identity.name`.

The `DefaultConfigFactory` loads a value from one of three locations.
The search locations have a well-defined order. If no value is given for
a property in one location, the next location is tried.

Servlet Context
:   If a servlet context is active (eg. you're using the
    `ConfigFilter`), its init parameters will be searched for a
    parameter named by the property's internal name. These init
    parameters are generally provided by use of `context-param` elements
    in the application's `web.xml`.

Property File
:   The classpath is searched for a property file named `config.xml`
    (for XML-encoded properties) or `config.properties` (for plain
    properties). If an XML property file is found, it is used and the
    framework doesn't continue looking for a plain property file. Since
    XML-encoded property files are validatable and specify their own
    text encoding, they are highly recommended over plain property
    files.

Unset Value
:   When a property had no value defined in any other location, the
    framework will look at the property's annotation to search for a
    value to return. The `unset` parameter of the annotation is used if
    set (to something other than `Property.NONE`).

If the configuration framework fails to resolve a value for a property
call through any means, the result depends on whether the property is
required. Required properties are those that have the `required`
parameter of their annotation set to `true`. If the property is required
and no value for the property can be found, the framework will throw a
runtime exception. If the property is optional, the property method call
will yield `null`.

Here's an example of what an XML property file might look like:

~~~~ {.xml}
    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">

    <properties>

        <entry key="branding.companyName">My Company</entry>
        <entry key="branding.companyLogo">
            aG93ZHktaG8tSS1hbS1hLWZha2UtY29tcGFueS1sb2dvLXN0cmluZwo=
        </entry>
        <entry key="branding.themeCSS">bliss.css</entry>

        <entry key="authentication.identity.name">mycompany</entry>
        <entry key="authentication.identity.keyProvider">
            classpath://myapp:mycompany-pass:myapp-pass@mycompany.jks
        </entry>

    </properties>
                
~~~~

As you can see, the contents of the `properties` tag is a sequence of
`entry` tags which specify a `key` attribute and provide a string value
as content. The value of the `key` attribute specifies the internal name
of the property for which the value is given.

Configuration values can also be defined in the servlet context. For
example, an application developer might use the following `web.xml` to
deploy his application:

~~~~ {.xml}
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
            "http://java.sun.com/dtd/web-app_2_3.dtd">

    <web-app>

        <display-name>My Application</display-name>

        <!-- SDK Configuration -->
        <context-param>
            <param-name>branding.companyName</param-name>
            <param-value>My Company</param-value>
        </context-param>
        <context-param>
            <param-name>branding.companyLogo</param-name>
            <param-value>aG93ZHktaG8tSS1hbS1hLWZha2UtY29tcGFueS1sb2dvLXN0cmluZwo=</param-value>
        </context-param>
        <context-param>
            <param-name>branding.themeCSS</param-name>
            <param-value>bliss.css</param-value>
        </context-param>

        <context-param>
            <param-name>authentication.identity.name</param-name>
            <param-value>mycompany</param-value>
        </context-param>
        <context-param>
            <param-name>authentication.identity.keyProvider</param-name>
            <param-value>classpath://myapp:mycompany-pass:myapp-pass@mycompany.jks</param-value>
        </context-param>

        <!-- SDK Configuration Filter -->
        <filter>
            <filter-name>ConfigFilter</filter-name>
            <filter-class>my.company.MyRootConfigFilter</filter-class>
        </filter>

        <!-- Application's Web Framework -->
        <filter>
            <filter-name>WicketFilter</filter-name>
            <filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>

            <init-param>
                <param-name>applicationClassName</param-name>
                <param-value>my.company.myapp.webapp.MyApplication</param-value>
            </init-param>
        </filter>

        <!-- Map filters to URLs -->
        <filter-mapping>
            <filter-name>ConfigFilter</filter-name>
            <url-pattern>/*</url-pattern>
        </filter-mapping>
        <filter-mapping>
            <filter-name>WicketFilter</filter-name>
            <url-pattern>/*</url-pattern>
        </filter-mapping>

        <!-- Initialization -->
        <listener>
            <listener-class>my.company.myapp.webapp.listener.MyApplicationContextListener</listener-class>
        </listener>

    </web-app>
                
~~~~

In this case, property values are defined within `context-param` tags.
The `param-name` child specifies the internal name of the property while
the `param-value` specifies the property's value.

It's important to note the main advantage of not putting the property
values in your `web.xml`: If they are defined in a property file that's
in your classpath, you can put the file somewhere in your servlet
container's configuration or lib directory, making it easily accessible
while the server is running. That means it's easy to update these values
without the need for rebuilding or repackaging the application. With
this in mind, also note that it's possible to divide parameters between
both sources. You may choose to define most parameters in a property
file and leave the static ones or those that are specific to the scope
of one of your web applications defined in your application's `web.xml`.
Note that values in the servlet's context override any values specified
by your property file. This allows you to scope certain properties to a
servlet context while leaving a more generic default for those servlet
contexts that don't specify their own scoped value.

Value filtering
---------------

After a value for a property has been loaded by the
`DefaultConfigFactory`, the string value first undergoes filtering.
Filtering is a process that is applied to a loaded value to modify or
interpret it in a runtime-specific manner. The standard
`DefaultConfigFactory` performs the following filtering operations, in
order:

Config property expansion
:   The string value is scanned for words of the syntax:
    `%{[internal-name]}`. If such a word is found in the value, it is
    removed and replaced by the value of the configuration property that
    is referenced.

    Note that this can only expand string values, so it doesn't work
    with custom config implementations. If you have one active and
    reference a property that has a custom implementation, that custom
    implementation will simply be skipped.

    Also note that in order for this to work with properties defined by
    or in groups referenced from application extension interfaces, those
    application extension interfaces must be returned by the active
    `ConfigHolder`'s `getRootTypes`.

System property expansion
:   The string value is scanned for words of the syntax:
    `${[system-property]}`. The name of the system property cannot
    contain back-brace (`}`) characters. If such a word is found in the
    value, it is removed and replaced by the value of the system
    property that is referenced.

Resource loading
:   If the string value begins with the five characters: `load:`, then
    the string after this keyword is taken as a resource name and the
    current thread's context classloader is searched for a resource by
    that name. If such a resource is found, the resource's bytes are
    read in and converted to a character string using `UTF-8` as
    encoding. The resulting characters are then used instead of the
    original value. If the resource cannot be found, a `null` value
    results.

You can add your own filter operations by using a custom implementation
of the `DefaultConfigFactory` class and overriding the `filter` method.
Make sure to invoke the `super` implementation of the method before
performing your own filter operations on the result of it.

Note that filtering is only performed on string values and in no way
affects objects returned by your custom config implementations.

Type conversion
---------------

Internally, property values are always stored as character data or
`String` objects. The configuration framework has strategies in place to
convert these values (after filtering as described in the previous
chapter) into fully typed objects. By looking at property your method
declaration's return value, it determines what strategy to use to
convert the value into the type that you desire. By default it supports
the following conversions:

`String` or `Object` typed properties or `null` property values
:   No conversion is performed, the value is returned as a `String`
    object, or as `null`.

Byte Arrays
:   Properties typed to accept `byte` or `Byte` arrays will make the
    framework decode the property value's characters with the Base 64
    algorithm into a stream of bytes.

Joda-Time
:   Properties typed to accept `DateTime` objects will make the
    framework parse the property value as a date (with optional time)
    string (in UTC) using the standard ISO format.

    Properties typed to accept `Duration` objects will make the
    framework parse the property value as a long integer and use this
    number as an amount of milliseconds until the duration has elapsed.

Key Stores
:   To make handling keys and certificates easy, the framework supports
    key stores. Set your property's return type to `KeyStore` and the
    property value will be converted to a `KeyStore` instance that
    provides access to the keys and certificates within.

    The syntax of the string value for this property is:
    `resource[:password[:format]]`. The value begins with a named
    `resource`. The resource name should not contain any colon (`:`)
    characters. It is the name of a keystore resource that will be
    loaded by the thread's context classloader. The second value is a
    password. The password should also not contain any colon characters.
    If the password is omitted, the framework attempts to load the
    keystore without unlocking it. The password will be used to unlock
    the key store once it is loaded by the classloader from the named
    resource. Finally, the keystore's type can be passed. This type
    identifies the format that is used by the keystore resource to
    encode its key information. By default, Java supports `JKS` and
    `PKCS12` key stores. The type defaults to `JKS`.

Key Providers
:   Key providers are a handy API for accessing identity and trust
    information. The key and certificates are obtained from
    implementation-specific locations (most implementations load the
    information from a key store).

    The syntax for key provider property values is explained in detail
    by the next section.

Enumerations
:   Properties typed with an `enum` type will make the framework search
    the given enumeration type for a constant with the same name as your
    property's value.

Reflection
:   To support a wide range of custom types, failing the above
    strategies, the framework will check your property's type for a
    constructor that takes a single `String` parameter. If such a
    constructor is found, it is used to construct a typed object by
    passing in the property's value.

    This strategy is an excellent fallback and immediately provides
    support for a great many standard Java types such as numbers, URLs,
    etc.

    It also makes it really easy for you to use custom objects with the
    configuration framework. All you need to do is provide a
    `toString()` implementation that encodes your custom object's data
    as a property value and a `String` constructor that parses a
    property value back into a new fully restored custom object.

Collections
:   Properties with a type that's a collection will cause the framework
    to split the property value into chunks. Your collection type (or a
    concrete subtype of it) is instantiated and the chunks are added to
    it.

    Property values are split using commas as delimitors. Leading and
    trailing whitespace is stripped from each chunk. For example, the
    following property value: `apple, pear, banana` will yield a
    collection of three elements.

    For the moment, only collections of `String`s are supported.

By extending `DefaultConfigFactory` with your custom implementation, you
can add support for additional conversion strategies. To do so, override
the `toTypeExtension` method.

Key providers
-------------

Key providers are objects that obtain keys and certificates from a
certain location and make them available to your application. The
framework supports a number of different key provider implementations.
Each implementation obtains the keys and certificates from a specific
source. Thanks to key providers, accessing identity and trust
information is decoupled from where the information is stored and how it
is loaded. This allows operators to keep the information in the most
convenient location, and to easily update it or move it to a different
location.

The syntax of a key provider property value is:
`type://[alias[:key-store-pass[:key-entry-pass]]@]path`. Note that you
cannot use `:` or `@` symbols in passwords or aliases. The optional
alias is used to load the identity entry from the key store. The
optional key store password is used to unlock the key store. The
optional key entry password is used to unlock the key entry referenced
by the identity alias. If the alias is omitted, it defaults to
`identity`. If the key entry is not sealed you can omit the key entry
password. If the key store is also not sealed, you can omit the key
store password as well. Note that in this event, you should have other
protections in place to guarantee the security of your identity keys and
trusted certificates. The following key provider implementations are
provided by the configuration framework:

Classpath (`classpath://...`)
:   The current thread's context classloader is used to look up the
    resource named by the `path`. The resource is loaded as a `JKS`
    `KeyStore`.

Remote (`url://...`)
:   A stream is opened to the URL given in `path` and the response is
    loaded in as a `JKS` `KeyStore`.

File (`file://...`)
:   The file at the given `path` is opened, read in and loaded as a
    `JKS` `KeyStore`.

A KeyProvider class (`class://...`)
:   The current thread's context classloader is used to load the class
    named by the given `path`. This should be the fully-qualified name
    of the class within the classloader.

    Your class must extend your property method's return type. The class
    will be instantiated by searching for the first supported
    constructor that exists. The framework will first search for a
    constructor that takes three `String`s, and pass in the
    `key-store-pass`, `key-entry-alias` and `key-entry-pass` as
    parameters. If such a constructor does not exist, it will look for a
    constructor that takes two `String`s, and pass in the
    `key-entry-alias` and `key-entry-pass` as parameters. Failing that,
    it will search for a constructor that takes one `String` and pass in
    the `key-entry-alias`. In a last attempt, it will try to construct
    the object with the default constructor.

Custom Config Implementation
----------------------------

The configuration framework was written with maximum customizability and
freedom to application developers in mind. There are situation where the
default implementation is inadequate: You may prefer to provide your
configuration values in a more type-safe way, your application may
require you to dynamically provide different configuration values for
certain properties depending on some external state or your application
may not be running inside a servlet container at all.

To provide your own implementation of the SDK config, all you need to do
is implement the `RootConfig` interface and the interfaces it
references. Any property values you wish to have resolved in the default
manner, you can return `null` for in your implementation. Similarly, any
groups you wish to create a default implementation for, you can return
`null` for.

To activate your custom implementation instead of a default
implementation, pass the custom implementation to the `ConfigHolder`
you're creating. To pass your implementation to a default `ConfigHolder`
from a custom `ConfigFilter`, instantiate your implementation and pass
it as an argument when you create the `ConfigHolder` for your custom
`ConfigFilter`. To pass your implementation to a default `ConfigHolder`
from a default `ConfigFilter` by means of a servlet context init
parameter, set the init parameter `configClass` to the class name of
your custom config implementation, and make sure it has a default
constructor that can be used to instantiate the class.
