package org.votingsystem.web.controlcenter.ejb;

import javax.ejb.Stateful;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Stateful
public class MessagesBean {

    private static final Logger log = Logger.getLogger(MessagesBean.class.getSimpleName());

    private ResourceBundle bundle;

    public MessagesBean() { }

    public void setLocale(Locale locale) {
        this.bundle = ResourceBundle.getBundle("org.votingsystem.web.accesscontrol.messages", locale);
    }

    public String get(String key, Object... arguments) {
        String pattern = bundle.getString(key);
            /*if(arguments.length > 0) return new String(MessageFormat.format(pattern, arguments).getBytes(ISO_8859_1), UTF_8);
            else return new String(pattern.getBytes(ISO_8859_1), UTF_8);*/
        if(arguments.length > 0) return MessageFormat.format(pattern, arguments);
        else return pattern;
    }

}