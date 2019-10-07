package br.com.alura.forum.config.swagger;

import br.com.alura.forum.model.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;

@Configuration
public class SwaggerConfigurations {
    @Bean
    public Docket forumApi(){
        return new Docket(DocumentationType.SWAGGER_2) //The documentation Type
                .select() //
                .apis(RequestHandlerSelectors.basePackage("br.com.alura.forum")) //What is the first package to start the documentation
                .paths(PathSelectors.ant("/**")) //What endpoints Swagger needs to analyze
                .build()
                .ignoredParameterTypes(Usuario.class) //Ignore all URLs that works with Usuario class
                .globalOperationParameters(Arrays.asList(new ParameterBuilder() //Configures parameters that Swagger must show globaly
                                                            .name("Authorization")
                                                            .description("Header para token JWT")
                                                            .modelRef(new ModelRef("string"))
                                                            .parameterType("header")
                                                            .required(false)
                                                            .build()));
    }
}
