package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.RoleReadModel;
import pl.wolniarskim.project_management.models.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);
    RoleReadModel fromRole(Role role);
}
