package caselab.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setOneIndexedParameters(true); // Нумерация страниц начинается с 1
        pageableResolver.setMaxPageSize(50); // Максимальный размер страницы
        pageableResolver.setFallbackPageable(PageRequest.of(0, 10)); // Параметры по умолчанию
        resolvers.add(pageableResolver);
    }
}
