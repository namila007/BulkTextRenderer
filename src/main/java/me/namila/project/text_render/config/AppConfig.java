package me.namila.project.text_render.config;

import me.namila.project.text_render.cli.RenderCommand;
import me.namila.project.text_render.service.CsvReaderService;
import me.namila.project.text_render.service.FontService;
import me.namila.project.text_render.service.JpegRendererService;
import me.namila.project.text_render.service.ParallelExecutorService;
import me.namila.project.text_render.service.PdfRendererService;
import me.namila.project.text_render.service.PngRendererService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for BulkTextRenderer application.
 */
@Configuration(proxyBeanMethods = false)
public class AppConfig {

    @Bean
    public CsvReaderService csvReaderService() {
        return new CsvReaderService();
    }

    @Bean
    public PdfRendererService pdfRendererService(FontService fontService) {
        return new PdfRendererService(fontService);
    }

    @Bean
    public PngRendererService pngRendererService() {
        return new PngRendererService();
    }

    @Bean
    public JpegRendererService jpegRendererService() {
        return new JpegRendererService();
    }

    @Bean
    public ParallelExecutorService parallelExecutorService() {
        return new ParallelExecutorService();
    }

    @Bean
    public FontService fontService() {
        return new FontService();
    }

    @Bean
    public RenderCommand renderCommand(CsvReaderService csvReaderService,
                                       PdfRendererService pdfRendererService,
                                       PngRendererService pngRendererService,
                                       JpegRendererService jpegRendererService,
                                       ParallelExecutorService parallelExecutorService,
                                       FontService fontService) {
        return new RenderCommand(csvReaderService, pdfRendererService, 
                                pngRendererService, jpegRendererService, 
                                parallelExecutorService, fontService);
    }
}
