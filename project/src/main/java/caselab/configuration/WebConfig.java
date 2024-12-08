package caselab.configuration;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final int PAGINATION_MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setOneIndexedParameters(true); // Нумерация страниц начинается с 1
        pageableResolver.setMaxPageSize(PAGINATION_MAX_PAGE_SIZE); // Максимальный размер страницы
        pageableResolver.setFallbackPageable(PageRequest.of(
            DEFAULT_PAGE_NUMBER,
            DEFAULT_PAGE_SIZE
        )); // Параметры по умолчанию
        resolvers.add(pageableResolver);
    }
}
