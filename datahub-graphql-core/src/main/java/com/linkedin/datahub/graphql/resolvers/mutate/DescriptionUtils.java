package com.linkedin.datahub.graphql.resolvers.mutate;

import com.google.common.collect.ImmutableList;

import com.linkedin.common.urn.Urn;
import com.linkedin.container.EditableContainerProperties;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.authorization.AuthorizationUtils;
import com.linkedin.datahub.graphql.authorization.ConjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.authorization.DisjunctivePrivilegeGroup;
import com.linkedin.datahub.graphql.generated.SubResourceType;
import com.linkedin.domain.DomainProperties;
import com.linkedin.metadata.Constants;
import com.linkedin.metadata.authorization.PoliciesConfig;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.schema.EditableSchemaFieldInfo;
import com.linkedin.schema.EditableSchemaMetadata;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import static com.linkedin.datahub.graphql.resolvers.mutate.MutationUtils.*;


@Slf4j
public class DescriptionUtils {
  private static final ConjunctivePrivilegeGroup ALL_PRIVILEGES_GROUP = new ConjunctivePrivilegeGroup(ImmutableList.of(
      PoliciesConfig.EDIT_ENTITY_PRIVILEGE.getType()
  ));

  private DescriptionUtils() { }

  public static final String EDITABLE_SCHEMA_METADATA = "editableSchemaMetadata";

  public static void updateFieldDescription(
      String newDescription,
      Urn resourceUrn,
      String fieldPath,
      Urn actor,
      EntityService entityService
  ) {
      EditableSchemaMetadata editableSchemaMetadata =
          (EditableSchemaMetadata) getAspectFromEntity(
              resourceUrn.toString(), EDITABLE_SCHEMA_METADATA, entityService, new EditableSchemaMetadata());
      EditableSchemaFieldInfo editableFieldInfo = getFieldInfoFromSchema(editableSchemaMetadata, fieldPath);

      editableFieldInfo.setDescription(newDescription);

      persistAspect(resourceUrn, EDITABLE_SCHEMA_METADATA, editableSchemaMetadata, actor, entityService);
  }

  public static void updateContainerDescription(
      String newDescription,
      Urn resourceUrn,
      Urn actor,
      EntityService entityService
  ) {
    EditableContainerProperties containerProperties =
        (EditableContainerProperties) getAspectFromEntity(
            resourceUrn.toString(), Constants.CONTAINER_EDITABLE_PROPERTIES_ASPECT_NAME, entityService, new EditableContainerProperties());
    containerProperties.setDescription(newDescription);
    persistAspect(resourceUrn, Constants.CONTAINER_EDITABLE_PROPERTIES_ASPECT_NAME, containerProperties, actor, entityService);
  }

  public static void updateDomainDescription(
      String newDescription,
      Urn resourceUrn,
      Urn actor,
      EntityService entityService
  ) {
    DomainProperties domainProperties =
        (DomainProperties) getAspectFromEntity(
            resourceUrn.toString(), Constants.DOMAIN_PROPERTIES_ASPECT_NAME, entityService, new DomainProperties());
    domainProperties.setDescription(newDescription);
    persistAspect(resourceUrn, Constants.DOMAIN_PROPERTIES_ASPECT_NAME, domainProperties, actor, entityService);
  }

  public static Boolean validateFieldDescriptionInput(
      Urn resourceUrn,
      String subResource,
      SubResourceType subResourceType,
      EntityService entityService
  ) {
    if (!entityService.exists(resourceUrn)) {
      throw new IllegalArgumentException(String.format("Failed to update %s. %s does not exist.", resourceUrn, resourceUrn));
    }

    validateSubresourceExists(resourceUrn, subResource, subResourceType, entityService);

    return true;
  }

  public static Boolean validateDomainInput(
      Urn resourceUrn,
      EntityService entityService
  ) {
    if (!entityService.exists(resourceUrn)) {
      throw new IllegalArgumentException(String.format("Failed to update %s. %s does not exist.", resourceUrn, resourceUrn));
    }

    return true;
  }

  public static Boolean validateContainerInput(
      Urn resourceUrn,
      EntityService entityService
  ) {
    if (!entityService.exists(resourceUrn)) {
      throw new IllegalArgumentException(String.format("Failed to update %s. %s does not exist.", resourceUrn, resourceUrn));
    }

    return true;
  }

  public static boolean isAuthorizedToUpdateFieldDescription(@Nonnull QueryContext context, Urn targetUrn) {
    final DisjunctivePrivilegeGroup orPrivilegeGroups = new DisjunctivePrivilegeGroup(ImmutableList.of(
        ALL_PRIVILEGES_GROUP,
        new ConjunctivePrivilegeGroup(ImmutableList.of(PoliciesConfig.EDIT_DATASET_COL_DESCRIPTION_PRIVILEGE.getType()))
    ));

    return AuthorizationUtils.isAuthorized(
        context.getAuthorizer(),
        context.getActorUrn(),
        targetUrn.getEntityType(),
        targetUrn.toString(),
        orPrivilegeGroups);
  }

  public static boolean isAuthorizedToUpdateDomainDescription(@Nonnull QueryContext context, Urn targetUrn) {
    final DisjunctivePrivilegeGroup orPrivilegeGroups = new DisjunctivePrivilegeGroup(ImmutableList.of(
        ALL_PRIVILEGES_GROUP,
        new ConjunctivePrivilegeGroup(ImmutableList.of(PoliciesConfig.EDIT_ENTITY_DOCS_PRIVILEGE.getType()))
    ));

    return AuthorizationUtils.isAuthorized(
        context.getAuthorizer(),
        context.getActorUrn(),
        targetUrn.getEntityType(),
        targetUrn.toString(),
        orPrivilegeGroups);
  }

  public static boolean isAuthorizedToUpdateContainerDescription(@Nonnull QueryContext context, Urn targetUrn) {
      final DisjunctivePrivilegeGroup orPrivilegeGroups = new DisjunctivePrivilegeGroup(ImmutableList.of(
          ALL_PRIVILEGES_GROUP,
          new ConjunctivePrivilegeGroup(ImmutableList.of(PoliciesConfig.EDIT_ENTITY_DOCS_PRIVILEGE.getType()))
      ));

      return AuthorizationUtils.isAuthorized(
          context.getAuthorizer(),
          context.getActorUrn(),
          targetUrn.getEntityType(),
          targetUrn.toString(),
          orPrivilegeGroups);
    }
}
