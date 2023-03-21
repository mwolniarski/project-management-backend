package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Qualifier;
import pl.wolniarskim.project_management.models.DTO.UserReadModel;
import pl.wolniarskim.project_management.models.Project;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;

@Qualifier
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ProjectUsersMapper {
}
