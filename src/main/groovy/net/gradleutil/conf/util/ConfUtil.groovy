package net.gradleutil.conf.util

import com.typesafe.config.Config
import com.typesafe.config.ConfigRenderOptions

class ConfUtil {

    static String configToJson(Config configObject, String path = '') {
        def jsonString
        if (!configObject) {
            throw new Exception("No config loaded")
        }
        if (path) {
            if (!configObject.hasPath(path)) {
                throw new Exception("Config does not have path ${path}")
            }
            jsonString = configObject.getValue(path).render(ConfigRenderOptions.concise().setFormatted(true))
        } else {
            jsonString = configObject.root().render(ConfigRenderOptions.concise().setFormatted(true))
        }
        return jsonString
    }

}
