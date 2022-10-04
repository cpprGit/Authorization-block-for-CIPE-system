import {parseForm} from 'src/api/api-parser/form-parser';
import {parsePosts} from 'src/api/api-parser/post-parser';
import {
    ApiResponse,
    Attribute,
    FormMode,
    ProfileStatus,
    ProfileType,
    ProjectRequestStatus,
    UserRole,
} from 'src/types';
import {StageMode} from 'src/ui/blocks/stage-view/stage-view';

export const parseUserRole: (role: string) => UserRole = (role) => {
    if (Object.values(UserRole).includes(role as UserRole)) {
        return role as UserRole;
    }
    throw new Error('Ошибка парсинга роли пользователя');
};

export const parseProfile = (
    result: ApiResponse,
    profileType: ProfileType,
    userRole: UserRole
):
    | {
          fields: Attribute[];
          info: UserRole | ProjectRequestStatus | string;
          firstList: any[];
          hasWarning: boolean;
          status: ProfileStatus;
          schemaContentId: string;
          modifyAllowed: boolean;
          blocked: boolean;
      }
    | undefined => {
    const content = result.schemaContent ? JSON.parse(result.schemaContent) : {};
    const parsedForm = parseForm(result, FormMode.View);
    if (!parsedForm) {
        return;
    }

    let hasWarning = false;
    const fields = parsedForm.attributes.map((item) => {
        if (item.realName && item.realName in result) {
            return {...item, defaultValue: result[item.realName]};
        }
        if (item.name && item.name in content) {
            return {...item, defaultValue: content[item.name]};
        }
        if (
            result.modifyAllowed &&
            !hasWarning &&
            (profileType === ProfileType.User || profileType === ProfileType.Organisation)
        ) {
            hasWarning = true;
        }
        return {...item};
    });
    const info =
        profileType === ProfileType.User
            ? result.role
            : profileType === ProfileType.ProjectRequest
            ? result.status
            : '';

    const stages =
        [ProfileType.Project, ProfileType.Activity].includes(profileType) && result.stages
            ? result.stages.map((stage: ApiResponse) => ({
                  ...stage,
                  stageMode:
                      userRole === UserRole.Student && result.uploadAllowed
                          ? StageMode.Student
                          : StageMode.All,
              }))
            : undefined;
    const posts =
        [ProfileType.User, ProfileType.Organisation, ProfileType.ProjectRequest].includes(
            profileType
        ) && result.posts
            ? parsePosts(result.posts)
            : undefined;
    const firstList = stages || posts || [];
    return {
        fields,
        info,
        firstList,
        hasWarning,
        status: result.status,
        schemaContentId: result.schemaContentId,
        modifyAllowed: result.modifyAllowed,
        blocked: result.blocked || false,
    };
};
