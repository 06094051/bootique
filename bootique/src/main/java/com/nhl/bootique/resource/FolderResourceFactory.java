package com.nhl.bootique.resource;

import java.net.URL;
import java.util.Objects;

/**
 * A {@link ResourceFactory} that corresponds to a "folder". Cleans up the
 * resource ID to correct missing trailing slashes.
 *
 * @since 0.15
 */
public class FolderResourceFactory extends ResourceFactory {

    static String normalizeResourceId(String resourceId) {

        // folder resources must end with a slash. Otherwise relative URLs won't
        // resolve properly

        if (resourceId.length() == 0) {
            return normalizeResourceId(System.getProperty("user.dir"));
        }

        if (resourceId.startsWith(ResourceFactory.CLASSPATH_URL_PREFIX)) {

            String classpath = resourceId.substring(ResourceFactory.CLASSPATH_URL_PREFIX.length());
            if (classpath.length() == 0) {
                return ResourceFactory.CLASSPATH_URL_PREFIX;
            }

            // classpath:/ is invalid
            if (classpath.equals("/")) {
                return ResourceFactory.CLASSPATH_URL_PREFIX;
            }
        }

        return resourceId.endsWith("/")
                ? resourceId : resourceId + "/";
    }


    public FolderResourceFactory(String resourceId) {
        super(normalizeResourceId(Objects.requireNonNull(resourceId)));
    }

    /**
     * Returns a URL of a resource based on a path relative to this folder.
     *
     * @param subResourcePath a path relative to this folder that points to a resource.
     * @return a URL of the specified resource located within the folder.
     * @since 0.17
     */
    public URL getUrl(String subResourcePath) {

        if (subResourcePath.startsWith("/")) {
            subResourcePath = subResourcePath.substring(1);
        }

        return resolveUrl(this.resourceId + subResourcePath);
    }

    @Override
    public String toString() {
        return "FolderResourceFactory:" + resourceId;
    }
}
