package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import pl.wolniarskim.project_management.models.Comment;
import pl.wolniarskim.project_management.models.DTO.CommentReadModel;

@Mapper(uses = UserMapper.class)
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(target = "createdBy", expression = "java(UserMapper.INSTANCE.toReadModel(comment.getCreatedBy()))")
    CommentReadModel toReadModel(Comment comment);
}
