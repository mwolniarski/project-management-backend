package pl.wolniarskim.project_management.mappers;

import org.mapstruct.Mapper;
import pl.wolniarskim.project_management.models.DTO.OrganizationReadModel;
import pl.wolniarskim.project_management.models.DTO.OrganizationWriteModel;
import pl.wolniarskim.project_management.models.Organization;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    Organization fromOrganizationWriteModel(OrganizationWriteModel organizationWriteModel);
    OrganizationReadModel toOrganizationReadModel(Organization organization);
}
