package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.DTO.UserReadModel;
import pl.wolniarskim.project_management.models.User;

@Mapper
public interface UserMapper {

    //todo do zmiany na komponent
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserReadModel toReadModel(User user);
}
