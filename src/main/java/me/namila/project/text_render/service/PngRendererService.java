package me.namila.project.text_render.service;

import org.springframework.stereotype.Service;

/**
 * PNG renderer service that inserts text onto PNG image templates.
 * Extends AbstractImageRendererService to share common image rendering logic.
 */
@Service
public class PngRendererService extends AbstractImageRendererService {

    @Override
    protected String getFormatName() {
        return "PNG";
    }

    @Override
    protected String getImageFormat() {
        return "PNG";
    }
}
