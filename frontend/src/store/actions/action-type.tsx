import {
    addNewAttribute,
    editAttribute,
    errorAttributeByUsage,
    receiveAttributeByUsage,
    requestAttributeByUsage,
} from 'src/store/actions/attributes.actions';
import {
    addChat,
    addChatUser,
    addHistoryLogs,
    deleteChat,
    deleteChatUser,
    editChat,
    errorChats,
    errorHistoryLogs,
    receiveChats,
    receiveHistoryLogs,
    requestChats,
    requestHistoryLogs,
} from 'src/store/actions/chats.actions';
import {
    errorFormByType,
    receiveFormByType,
    requestFormByType,
} from 'src/store/actions/default-forms.actions';
import {
    errorFullSearch,
    errorSearch,
    receiveFullSearch,
    receiveSearch,
    requestFullSearch,
    requestSearch,
    selectSearchType,
    setFilter,
} from 'src/store/actions/search.actions';
import {
    addArchiveForm,
    errorArchiveForm,
    errorUserArchiveForms,
    removeArchiveForm,
    requestUserArchiveForms,
    successUserArchiveForms,
} from 'src/store/actions/user-archive-forms.actions';
import {
    addDraftFormAttributeProperties,
    addDraftFormAttributes,
    addNewUserForm,
    changeUserFormMode,
    deleteDraftFormAttribute,
    deleteNewUserForm,
    editUserForm,
    errorFormStats,
    errorUserForm,
    errorUserForms,
    requestFormStats,
    requestUserForms,
    setDraftFormButtonName,
    setDraftFormDescription,
    setDraftFormTitle,
    successFormStats,
    successUserForms,
} from 'src/store/actions/user-forms.actions';
import {logout, setUserData} from 'src/store/actions/user.actions';

export type Actions =
    | ReturnType<typeof receiveAttributeByUsage>
    | ReturnType<typeof requestAttributeByUsage>
    | ReturnType<typeof errorAttributeByUsage>
    | ReturnType<typeof addNewAttribute>
    | ReturnType<typeof editAttribute>
    | ReturnType<typeof receiveFormByType>
    | ReturnType<typeof requestFormByType>
    | ReturnType<typeof errorFormByType>
    | ReturnType<typeof receiveSearch>
    | ReturnType<typeof requestSearch>
    | ReturnType<typeof errorSearch>
    | ReturnType<typeof receiveFullSearch>
    | ReturnType<typeof requestFullSearch>
    | ReturnType<typeof errorFullSearch>
    | ReturnType<typeof selectSearchType>
    | ReturnType<typeof setFilter>
    | ReturnType<typeof setUserData>
    | ReturnType<typeof logout>
    | ReturnType<typeof requestUserArchiveForms>
    | ReturnType<typeof errorUserArchiveForms>
    | ReturnType<typeof successUserArchiveForms>
    | ReturnType<typeof addArchiveForm>
    | ReturnType<typeof removeArchiveForm>
    | ReturnType<typeof errorArchiveForm>
    | ReturnType<typeof requestUserForms>
    | ReturnType<typeof errorUserForms>
    | ReturnType<typeof successUserForms>
    | ReturnType<typeof addNewUserForm>
    | ReturnType<typeof deleteNewUserForm>
    | ReturnType<typeof editUserForm>
    | ReturnType<typeof errorUserForm>
    | ReturnType<typeof changeUserFormMode>
    | ReturnType<typeof setDraftFormTitle>
    | ReturnType<typeof setDraftFormDescription>
    | ReturnType<typeof setDraftFormButtonName>
    | ReturnType<typeof addDraftFormAttributes>
    | ReturnType<typeof deleteDraftFormAttribute>
    | ReturnType<typeof addDraftFormAttributeProperties>
    | ReturnType<typeof receiveChats>
    | ReturnType<typeof errorChats>
    | ReturnType<typeof requestChats>
    | ReturnType<typeof receiveHistoryLogs>
    | ReturnType<typeof errorHistoryLogs>
    | ReturnType<typeof requestHistoryLogs>
    | ReturnType<typeof addChat>
    | ReturnType<typeof deleteChat>
    | ReturnType<typeof editChat>
    | ReturnType<typeof addChatUser>
    | ReturnType<typeof deleteChatUser>
    | ReturnType<typeof addHistoryLogs>
    | ReturnType<typeof requestFormStats>
    | ReturnType<typeof errorFormStats>
    | ReturnType<typeof successFormStats>;
