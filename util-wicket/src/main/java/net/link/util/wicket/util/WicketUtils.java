/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.util.wicket.util;

import net.link.util.j2ee.NamingStrategy;
import net.link.util.wicket.behaviour.FocusOnReady;
import net.link.util.wicket.component.WicketPage;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.*;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.wicketstuff.javaee.injection.AnnotJavaEEInjector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <h2>{@link WicketUtils}</h2>
 * <p/>
 * <p>
 * [description / usage].
 * </p>
 * <p/>
 * <p>
 * <i>Sep 17, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class WicketUtils {

    static final Log LOG = LogFactory.getLog(WicketUtils.class);
    static ConfigurableInjector eeInjector;

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static Pattern fsPattern = Pattern.compile(formatSpecifier);

    /**
     * @return A formatter according to the given locale in short form.
     */
    public static DateFormat getDateFormat(Locale locale) {

        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    }

    /**
     * @return A string that is the formatted representation of the given date according to the given locale in short form.
     */
    public static String format(Locale locale, Date date) {

        return getDateFormat(locale).format(date);
    }

    /**
     * @return A formatter according to the given locale's currency.
     */
    public static NumberFormat getCurrencyFormat(Locale locale) {

        return NumberFormat.getCurrencyInstance(locale);
    }

    /**
     * @return A string that is the formatted representation of the given amount of currency according to the given locale.
     */
    public static String format(Locale locale, Number number) {

        return getCurrencyFormat(locale).format(number);
    }

    /**
     * Add an injector to the given Wicket web application that will resolve fields according the specified {@link NamingStrategy}.
     *
     * @see NamingStrategy
     */
    public static void addInjector(WebApplication application, final NamingStrategy namingStrategy) {

        application.addComponentInstantiationListener(new ComponentInjector() {

            @Override
            public void onInstantiation(Component component) {

                inject(component, namingStrategy);
            }
        });
    }

    /**
     * Perform Java EE injections on the given objects.
     */
    public static void inject(Object injectee, NamingStrategy namingStrategy) {

        if (eeInjector == null)
            eeInjector = new AnnotJavaEEInjector(namingStrategy);

        eeInjector.inject(injectee);
    }

    /**
     * Get the {@link HttpServletRequest} contained in the active Wicket {@link Request}.
     */
    public static HttpServletRequest getServletRequest() {

        return ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest();
    }

    /**
     * Get the {@link HttpServletResponse} contained in the active Wicket {@link Response}.
     */
    public static HttpServletResponse getServletResponse() {

        return ((WebResponse) RequestCycle.get().getResponse()).getHttpServletResponse();
    }

    /**
     * Get the {@link HttpSession} contained in the active Wicket {@link Request}.
     */
    public static HttpSession getHttpSession() {

        return getServletRequest().getSession();
    }

    /**
     * Uses the application's localizer and the active session's locale.
     * <p/>
     * Note: You can use this method with a single argument, too. This will cause the first argument (format) to be evaluated as a
     * localization key.
     *
     * @param format The format specification for the arguments. See {@link String#format(java.util.Locale, String, Object...)}. To that
     *               list,
     *               add the 'l' conversion parameter. This parameter first looks the arg data up as a localization key, then processes the
     *               result as though it was given with the 's' conversion parameter.
     * @param args   The arguments that contain the data to fill into the format specifications.
     */
    public static String localize(Component component, String format, Object... args) {

        return localize(Application.get().getResourceSettings().getLocalizer(), component, Session.get().getLocale(), format, args);
    }

    /**
     * Note: You can use this method with a single argument, too. This will cause the first argument (format) to be evaluated as a
     * localization key.
     *
     * @param localizer The localization provider.
     * @param component The component in whose context to resolve localization keys.
     * @param locale    The locale for which to resolve localization keys.
     * @param format    The format specification for the arguments. See {@link String#format(java.util.Locale, String, Object...)}. To that
     *                  list,
     *                  add the 'l' conversion parameter. This parameter first looks the arg data up as a localization key, then processes the
     *                  result as though it was given with the 's' conversion parameter.
     * @param args      The arguments that contain the data to fill into the format specifications.
     */
    public static String localize(Localizer localizer, Component component, Locale locale, String format, Object... args) {

        if (args.length == 0)
            // Single argument invocation: format is localization key.
            return localizer.getString(format, component);

        List<Object> localizationData = new ArrayList<Object>(args.length);
        StringBuffer newFormat = new StringBuffer(format);
        Matcher specifiers = fsPattern.matcher(format);

        int pos = 0, num = 0;
        while (specifiers.find(pos)) {
            if ("l".equalsIgnoreCase(specifiers.group(6))) {
                if ("L".equals(specifiers.group(6)))
                    newFormat.setCharAt(specifiers.end(6) - 1, 'S');
                else
                    newFormat.setCharAt(specifiers.end(6) - 1, 's');

                if (args[num] == null)
                    throw new NullPointerException(String.format("Key for localization must be String, got %s (arg: %d)", "null", num));
                if (!(args[num] instanceof String))
                    throw new IllegalArgumentException(String.format("Key for localization must be String, got %s (arg: %d)",
                            args[num].getClass(), num));

                localizationData.add(localizer.getString((String) args[num], component));
            } else
                localizationData.add(args[num]);

            ++num;
            pos = specifiers.end();
        }

        return String.format(locale, newFormat.toString(), localizationData.toArray());
    }

    @SuppressWarnings("unchecked")
    public static List<Locale> getLocales() {

        List<Locale> uniqueLocales = new LinkedList<Locale>();
        for (final Locale locale : (List<Locale>) LocaleUtils.availableLocaleList())
            if (CollectionUtils.find(uniqueLocales, new Predicate() {

                public boolean evaluate(Object object) {

                    Locale uniqueLocale = (Locale) object;
                    return uniqueLocale.getDisplayLanguage().equals(locale.getDisplayLanguage());
                }
            }) == null)
                uniqueLocales.add(locale);
        Collections.sort(uniqueLocales, new Comparator<Locale>() {

            public int compare(Locale l1, Locale l2) {

                return l1.getDisplayLanguage().compareTo(l2.getDisplayLanguage());
            }
        });
        return uniqueLocales;
    }

    /**
     * Cause a component to be focussed referenced by the path given by element ids separated by colons.
     *
     * @param componentPage The page to add the focusing behaviour to.
     */
    public static FocusOnReady focus(Component component, Page componentPage) {

        if (component == null)
            return null;

        if (componentPage == null) {
            LOG.warn("Tried to focus " + component.getId() + " but don't know the page it's on.");
            return null;
        }

        if (componentPage instanceof WicketPage)
            return ((WicketPage) componentPage).focus(component);

        // Not a WicketPage, add the behaviour manually; won't be managed now.
        FocusOnReady focusOnReady = new FocusOnReady(component);
        componentPage.add(focusOnReady);
        return focusOnReady;
    }
}
