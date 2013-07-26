package com.bumptech.glide.loader.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.bumptech.glide.loader.stream.LocalUriLoader;
import com.bumptech.glide.loader.stream.StreamLoader;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A model loader for trying to load Uris. Capable of handling 'http', 'https', 'android.resource', 'content', and
 * 'file' schemes. Unsupported schemes will throw an exception in {@link #buildStreamLoader(android.net.Uri, int, int)}.
 */
public class UriLoader extends BaseModelLoader<Uri> {
    private final Context context;
    private final ModelLoader<URL> urlLoader;

    public UriLoader(Context context, ModelLoader<URL> urlLoader) {
        this.context = context;
        this.urlLoader = urlLoader;
    }

    @Override
    protected StreamLoader buildStreamLoader(Uri model, int width, int height) {
        final String scheme = model.getScheme();

        StreamLoader result = null;
        if (isLocalUri(scheme)) {
            result = new LocalUriLoader(context, model);
        } else if ("http".equals(scheme) || "https".equals(scheme)) {
            try {
                result = urlLoader.getStreamLoader(new URL(model.toString()), width, height);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("No stream loader for uri=" + model);
        }

        return result;
    }

    @Override
    public String getId(Uri model) {
        return model.toString();
    }

    @Override
    public void clear() {
        super.clear();
        urlLoader.clear();
    }

    private boolean isLocalUri(String scheme) {
        return ContentResolver.SCHEME_FILE.equals(scheme)
                || ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme);
    }
}