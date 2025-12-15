package me.namila.project.text_render.service;

import me.namila.project.text_render.model.RenderJob;

public interface RendererService {
    void render(RenderJob job) throws Exception;
}
